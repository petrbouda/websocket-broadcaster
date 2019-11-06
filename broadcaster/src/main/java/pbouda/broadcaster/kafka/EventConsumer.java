package pbouda.broadcaster.kafka;

import io.micrometer.core.instrument.Counter;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.group.ChannelGroup;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.CharsetUtil;
import org.apache.kafka.streams.kstream.ForeachAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pbouda.broadcaster.prometheus.PrometheusRegistry;

public class EventConsumer implements ForeachAction<Object, Object> {

    private static final Logger LOG = LoggerFactory.getLogger(EventConsumer.class);

    private static final Counter ROUTED_MESSAGES =
            Counter.builder("broadcaster_messages_total")
                    .description("Total number of all unique messages written to the broadcaster")
                    .register(PrometheusRegistry.instance());

    private final ChannelGroup channelGroup;

    public EventConsumer(ChannelGroup channelGroup) {
        this.channelGroup = channelGroup;
    }

    @Override
    public void apply(Object key, Object value) {
        // It's pointless to use direct buffers POOLED because the byte array has been already created and just need to
        // wrap it around String. Netty internally wraps byte array and does not create a new one.
//         ByteBuf buffer = PooledByteBufAllocator.DEFAULT.buffer();
//         buffer.writeCharSequence((String) value, CharsetUtil.UTF_8);

        channelGroup.write(new TextWebSocketFrame((String) value))
                .addListener(future -> {
                    if (future.isSuccess()) {
                        ROUTED_MESSAGES.increment();
                    } else {
                        LOG.error("Error during sending messages", future.cause());
                    }
                });
    }
}