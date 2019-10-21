package pbouda.broadcaster.server.kafka;

import com.typesafe.config.Config;
import io.netty.channel.group.ChannelGroup;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;

import java.time.Duration;
import java.util.Properties;
import java.util.UUID;

public class KafkaFactory {

    private final Properties configuration;
    private final String topic;

    public KafkaFactory(Config config) {
        this.topic = config.getString("topic");

        this.configuration = new Properties();
        this.configuration.put(StreamsConfig.APPLICATION_ID_CONFIG, UUID.randomUUID().toString());
        this.configuration.put(StreamsConfig.CLIENT_ID_CONFIG, "broadcaster");
        this.configuration.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, config.getString("bootstrap-servers"));
        this.configuration.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        this.configuration.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        // Records should be flushed every 10 seconds. This is less than the default in order to keep this example interactive.
        this.configuration.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, Duration.ofSeconds(10).toMillis());
    }

    public KafkaStreams provide(ChannelGroup channelGroup) {
        StreamsBuilder builder = new StreamsBuilder();
        builder.stream(topic).foreach(new EventConsumer(channelGroup));
        return new KafkaStreams(builder.build(), configuration);
    }
}
