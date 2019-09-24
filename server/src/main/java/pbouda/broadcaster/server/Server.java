package pbouda.broadcaster.server;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pbouda.broadcaster.server.prometheus.PrometheusConfigurer;
import pbouda.broadcaster.server.rabbit.EventConsumer;
import pbouda.broadcaster.server.rabbit.Rabbit;
import pbouda.broadcaster.server.netty.Broadcaster;

public class Server {

    private static final Logger LOG = LoggerFactory.getLogger(Server.class);

    public static void main(String[] args) {
        Config config = ConfigFactory.load("application.conf");

        var broadcaster = new Broadcaster(config.getConfig("broadcaster"));
        var rabbit = new Rabbit(config.getConfig("rabbitmq"));
        var consumer = new EventConsumer(broadcaster.getChannelGroup());

        PrometheusConfigurer.configure(broadcaster.getChannelGroup());

        // Start consuming from Rabbit when WebSocket is UP and RUNNING
        Channel serverChannel = broadcaster.start(f -> rabbit.start(consumer));

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            rabbit.close();
            broadcaster.close();
        }));

        // A blocking operation which joins current thread and waits until the websocket server is not consider to be
        // fully closed.
        serverChannel.closeFuture()
                .addListener(f -> LOG.info("Broadcaster closed"))
                .syncUninterruptibly();
    }
}
