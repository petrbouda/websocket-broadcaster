package pbouda.pusher;


import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.management.ManagementFactory;
import java.time.Duration;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

public class Pusher {

    private static final Logger LOG = LoggerFactory.getLogger(Pusher.class);

    public static void main(String[] args) {
        Config config = ConfigFactory.load("application.conf")
                .getConfig("kafka");

        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, config.getString("bootstrap-servers"));
        props.put(ProducerConfig.CLIENT_ID_CONFIG, UUID.randomUUID().toString());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        //props.put(ProducerConfig.PARTITIONER_CLASS_CONFIG, CustomPartitioner.class.getName());
        KafkaProducer<Object, String> producer = new KafkaProducer<>(props);

        Duration interval = args.length > 0
                ? Duration.ofMillis(Long.parseLong(args[0]))
                : Duration.ofMillis(20);

        String message = "„Lidé mají rádi tajemství. My jsme za pomoci vědy přidali další kapitolu do příběhu " +
                "o lochneské záhadě,” řekl vedoucí týmu genetik Neil Gemmell. Postup vědců byl ve své podstatě " +
                "velmi prostý. Každý živočich za sebou ve vodě nechává stopu DNA. Ať už pochází ze šupiny, srsti, " +
                "peří či moči, moderní metody dokážou odhalit původ biologického materiálu.";

        LOG.info("PID {} - Pusher started", ManagementFactory.getRuntimeMXBean().getPid());

        Runtime.getRuntime().addShutdownHook(new Thread(() -> System.out.println("Pusher Killed!")));

        while (true) {
            try {
                producer.send(new ProducerRecord<>(config.getString("topic"), message)).get();
                Thread.sleep(interval.toMillis());
            } catch (ExecutionException | InterruptedException e) {
                System.out.println("Error in sending record");
                System.out.println(e);
            }
        }
    }
}
