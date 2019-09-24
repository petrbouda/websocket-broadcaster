package pbouda.broadcaster.server.rabbit;

import com.rabbitmq.client.*;
import com.rabbitmq.client.impl.recovery.AutorecoveringChannel;
import com.rabbitmq.client.impl.recovery.AutorecoveringConnection;
import com.typesafe.config.Config;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class Rabbit implements AutoCloseable {

    private final ConnectionFactory connectionFactory;
    private final String queueName;
    private final int parallelism;

    private Connection connection;

    public Rabbit(Config config) {
        this.queueName = config.getString("queue");
        this.connectionFactory = createConnectionFactory(config);

        if (config.hasPath("parallelism")) {
            this.parallelism = config.getInt("parallelism");
        } else {
            this.parallelism = Runtime.getRuntime().availableProcessors() * 2;
        }
    }

    private static ConnectionFactory createConnectionFactory(Config config) {
        var factory = new ConnectionFactory();
        factory.setUsername(config.getString("username"));
        factory.setPassword(config.getString("password"));
        factory.setHost(config.getString("hostname"));
        factory.setPort(config.getInt("port"));
        factory.setThreadFactory(new CustomThreadFactory("rabbitmq-consumer"));
        factory.setAutomaticRecoveryEnabled(true);
        return factory;
    }

    /**
     * Returns a {@link Connection} which is kept as a singleton in the application and all created
     * {@link Channel channels} belongs to this {@link #connection}. If the connection has not been created yet, then
     * create it first and then just return this instance.
     */
    private synchronized Connection getConnection() {
        try {
            if (this.connection == null) {
                this.connection = connectionFactory.newConnection();
                this.connection.addBlockedListener(new CustomBlockedListener());

                /*
                 * By default 5 seconds recovery interval.
                 * - Connection recovery attempts by default will continue at identical time intervals
                 *   until a new connection is successfully opened
                 *
                 * Connection recovery will not kick in when a channel is closed due to a channel-level
                 * exception. Such exceptions often indicate application-level issues. The library cannot
                 * make an informed decision about when that's the case.
                 *
                 * Closed channels won't be recovered even after connection recovery kicks in. This includes
                 * both explicitly closed channels and the channel-level exception case above.
                 */
                if (connection instanceof AutorecoveringConnection) {
                    var recoveryConnection = (AutorecoveringConnection) connection;
                    var listener = new CustomRecoveryListener(CustomRecoveryListener.RecoveryListenerType.CONNECTION);
                    recoveryConnection.addRecoveryListener(listener);
                }
            }
            return this.connection;
        } catch (IOException | TimeoutException e) {
            throw new RuntimeException("Cannot create a new connection", e);
        }
    }

    /**
     * A newly created instance of {@link Channel} for the given {@link #connection}.
     *
     * @return newly created channel for the one connection.
     */
    private synchronized Channel createNewChannel() {
        try {
            return getConnection().createChannel();
        } catch (IOException e) {
            throw new RuntimeException("Cannot create a new channel", e);
        }
    }

    /**
     * Parallelism: When using the “consumer” approach (basicConsume, ie rabbitmq invokes application code when a
     * message is available) then the maximum parallelism is: min(n_channels, thread_pool_size) where the threadpool
     * is the one associated with the ExecutorService passed to ConnectionFactory.newConnection.
     *
     * thread_pool = Executors.newFixedThreadPool(DEFAULT_NUM_THREADS, threadFactory)
     * DEFAULT_NUM_THREADS = Runtime.getRuntime().availableProcessors() * 2
     *
     * - by default, the "parallelism" (a number of channels) equals to DEFAULT_NUM_THREADS
     *
     * http://moi.vonos.net/bigdata/rabbitmq-threading/
     */
    public void start(Consumer consumer) {
        try {
            if (parallelism > 1) {
                for (int i = 0; i < parallelism; i++) {
                    Channel channel = createNewChannel();
                    channel.basicConsume(queueName, true, consumer);

                    if (channel instanceof AutorecoveringChannel) {
                        var recoveryChannel = (AutorecoveringChannel) channel;
                        var listener = new CustomRecoveryListener(CustomRecoveryListener.RecoveryListenerType.CHANNEL);
                        recoveryChannel.addRecoveryListener(listener);
                    }
                }
            } else {
                Channel channel = createNewChannel();

                // TODO: Remove it: For testing purposes
                channel.exchangeDeclare("events", BuiltinExchangeType.FANOUT);
                channel.queueDeclare("events", true, false, false, null);
                channel.queueBind("events", "events", "");

                channel.basicConsume(queueName, true, "", false,
                        true, null, consumer);
            }
        } catch (IOException e) {
            throw new RuntimeException("Cannot start RabbitMQ Consumer", e);
        }
    }

    @Override
    public void close() {
        if (this.connection != null) {
            try {
                this.connection.close();
            } catch (IOException e) {
                throw new RuntimeException("Cannot close a channel properly", e);
            }
        }
    }
}
