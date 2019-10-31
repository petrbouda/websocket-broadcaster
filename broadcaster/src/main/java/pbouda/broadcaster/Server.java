package pbouda.broadcaster;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pbouda.broadcaster.kafka.KafkaFactory;
import pbouda.broadcaster.netty.Broadcaster;
import pbouda.broadcaster.prometheus.PrometheusConfigurer;

public class Server {

    private static final Logger LOG = LoggerFactory.getLogger(Server.class);

    public static void main(String[] args) {
        Config config = ConfigFactory.load("application.conf");

        var broadcaster = new Broadcaster(config.getConfig("broadcaster"));
        var kafkaFactory = new KafkaFactory(config.getConfig("kafka"));
        var kafkaStreams = kafkaFactory.provide(broadcaster.getChannelGroup());

        PrometheusConfigurer.configure(broadcaster.getChannelGroup());

        // Start consuming from Kafka when WebSocket is UP and RUNNING
        Channel serverChannel = broadcaster.start(f -> kafkaStreams.start());

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            kafkaStreams.close();
            broadcaster.close();
        }));

        // A blocking operation which joins current thread and waits until the websocket server is not consider to be
        // fully closed.
        serverChannel.closeFuture()
                .addListener(f -> LOG.info("Broadcaster closed"))
                .syncUninterruptibly();
    }
}
