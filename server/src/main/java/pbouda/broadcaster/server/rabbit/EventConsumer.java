package pbouda.broadcaster.server.rabbit;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.ShutdownSignalException;
import io.micrometer.core.instrument.Counter;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.group.ChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pbouda.broadcaster.server.prometheus.PrometheusRegistry;

public class EventConsumer implements Consumer {

    private static final Logger LOG = LoggerFactory.getLogger(EventConsumer.class);

    private static final Counter ROUTED_MESSAGES =
            Counter.builder("broadcaster_messages_total")
                    .description("Total number of all unique messages written to the broadcaster")
                    .register(PrometheusRegistry.instance());

    private final ChannelGroup channelGroup;

    public EventConsumer(ChannelGroup channelGroup) {
        this.channelGroup = channelGroup;
    }

    private void handleDeliveryInternal(byte[] body) {
        // It's pointless to use direct buffers POOLED because the byte array has been already created and just need to
        // wrap it around String. Netty internally wraps byte array and does not create a new one.
        //
         ByteBuf buffer = PooledByteBufAllocator.DEFAULT.buffer();
         buffer.writeCharSequence(new String(body), CharsetUtil.UTF_8);
         channelGroup.write(new TextWebSocketFrame(buffer))
//        channelGroup.write(new TextWebSocketFrame(new String(body)))
                .addListener(future -> {
                    if (future.isSuccess()) {
                        ROUTED_MESSAGES.increment();
                    } else {
                        LOG.error("Error during sending messages", future.cause());
                    }
                });
    }

    @Override
    public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) {
        handleDeliveryInternal(body);
    }

    @Override
    public void handleCancel(String consumerTag) {
        LOG.error("Rabbit Consumer has been unintentionally cancelled.");
    }

    @Override
    public void handleShutdownSignal(String consumerTag, ShutdownSignalException sig) {
        if (sig.isInitiatedByApplication()) {
            LOG.info("Rabbit Consumer has been shut down: initiated-by-application '{}', message: '{}'",
                    sig.isInitiatedByApplication(), sig.getMessage());
        } else {
            LOG.error("Rabbit Consumer has been shut down: hard-error '{}', initiated-by-application '{}'",
                    sig.isHardError(), sig.isInitiatedByApplication(), sig);
        }
    }

    @Override
    public void handleConsumeOk(String consumerTag) {
    }

    @Override
    public void handleCancelOk(String consumerTag) {
    }

    @Override
    public void handleRecoverOk(String consumerTag) {
    }
}
