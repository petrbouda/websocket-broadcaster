package pbouda.broadcaster.pusher;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.time.Duration;
import java.util.concurrent.TimeoutException;

public class Pusher {

    private static final Logger LOG = LoggerFactory.getLogger(Pusher.class);

    public static void main(String[] args) throws IOException, TimeoutException, InterruptedException {
        Duration interval = args.length > 0
                ? Duration.ofMillis(Long.parseLong(args[0]))
                : Duration.ofMillis(20);

        Config config = ConfigFactory.load("application.conf");
        Config rabbitConfig = config.getConfig("rabbitmq");

        Connection connection = createConnection(rabbitConfig);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                connection.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }));

        Channel channel = connection.createChannel();

        String message = "„Lidé mají rádi tajemství. My jsme za pomoci vědy přidali další kapitolu do příběhu " +
                "o lochneské záhadě,” řekl vedoucí týmu genetik Neil Gemmell. Postup vědců byl ve své podstatě " +
                "velmi prostý. Každý živočich za sebou ve vodě nechává stopu DNA. Ať už pochází ze šupiny, srsti, " +
                "peří či moči, moderní metody dokážou odhalit původ biologického materiálu.";

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Pusher Killed!");
        }));

        LOG.info("PID {} - Pusher started", ManagementFactory.getRuntimeMXBean().getPid());

        for (int i = 0; true; i++) {
            channel.basicPublish(rabbitConfig.getString("exchange"), "", null, message.getBytes());
            Thread.sleep(interval.toMillis());
        }
    }

    private static Connection createConnection(Config config) throws IOException, TimeoutException {
        var factory = new ConnectionFactory();
        factory.setUsername(config.getString("username"));
        factory.setPassword(config.getString("password"));
        factory.setHost(config.getString("hostname"));
        factory.setPort(config.getInt("port"));
        return factory.newConnection();
    }
}
