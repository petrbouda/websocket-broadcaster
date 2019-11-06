package pbouda.broadcaster.netty;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.SocketAddress;

public class SlowConsumerDisconnectHandler extends ChannelOutboundHandlerAdapter {

    private static final Logger LOG = LoggerFactory.getLogger(SlowConsumerDisconnectHandler.class);

    @Override
    public void write(ChannelHandlerContext context, Object obj, ChannelPromise promise) {
        if ((obj instanceof TextWebSocketFrame) || (obj instanceof BinaryWebSocketFrame)) {
            if (context.channel().isWritable()) {
                context.writeAndFlush(obj);
                promise.setSuccess();
            } else {
                // Implementation of Timeout for Writability and closing the connection
                // context.executor().schedule(() -> context.channel().isWritable(), 5, TimeUnit.SECONDS);

                context.close().addListener(future -> {
                    SocketAddress target = context.channel().remoteAddress();
                    if (future.isSuccess()) {
                        LOG.warn("Connection closed, became non-writable: " + target);
                    } else {
                        LOG.error("Could not close a non-writable connection: " + target, future.cause());
                    }
                });
            }
        } else if (obj instanceof FullHttpResponse) {
            context.writeAndFlush(obj);
            promise.setSuccess();
        }
    }
}
