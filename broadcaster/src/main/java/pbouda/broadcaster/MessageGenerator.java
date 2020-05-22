package pbouda.broadcaster;

import com.thedeanda.lorem.Lorem;
import com.thedeanda.lorem.LoremIpsum;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.group.ChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.CharsetUtil;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MessageGenerator implements AutoCloseable {

    private static final Lorem LOREM = LoremIpsum.getInstance();

    private final ChannelGroup group;
    private final long delay;
    private final ScheduledExecutorService scheduler;

    public MessageGenerator(ChannelGroup group, long delay) {
        this.group = group;
        this.delay = delay;
        this.scheduler = Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory("message-generator"));
    }

    public void start() {
        Runnable action = () -> {
            String paragraphs = LOREM.getParagraphs(1, 5);
            ByteBuf buffer = PooledByteBufAllocator.DEFAULT.buffer();
            buffer.writeCharSequence(paragraphs, CharsetUtil.UTF_8);
            group.writeAndFlush(new TextWebSocketFrame(buffer));
        };

        this.scheduler.scheduleAtFixedRate(action, 0, delay, TimeUnit.MILLISECONDS);
    }

    @Override
    public void close() {
        scheduler.shutdown();
    }
}
