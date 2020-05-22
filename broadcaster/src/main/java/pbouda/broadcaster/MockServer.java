package pbouda.broadcaster;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pbouda.broadcaster.kafka.KafkaFactory;
import pbouda.broadcaster.netty.Broadcaster;
import pbouda.broadcaster.prometheus.PrometheusConfigurer;

public class MockServer {

    private static final Logger LOG = LoggerFactory.getLogger(MockServer.class);

    public static void main(String[] args) {
        Config config = ConfigFactory.load("application.conf");

        var broadcaster = new Broadcaster(config.getConfig("broadcaster"));
        var generator = new MessageGenerator(broadcaster.getChannelGroup(), 10);

        PrometheusConfigurer.configure(broadcaster.getChannelGroup());

        // Start consuming from Kafka when WebSocket is UP and RUNNING
        Channel serverChannel = broadcaster.start(f -> generator.start());

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            generator.close();
            broadcaster.close();
        }));

        // A blocking operation which joins current thread and waits until the websocket server is not consider to be
        // fully closed.
        serverChannel.closeFuture()
                .addListener(f -> LOG.info("Broadcaster closed"))
                .syncUninterruptibly();
    }
}
