package pbouda.broadcaster.server.netty.test;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

public class WebSocketOutputTracer2 extends ChannelOutboundHandlerAdapter {

    @Override
    public void write(ChannelHandlerContext context, Object obj, ChannelPromise promise) {
        // ping and pong frames already handled

        if (obj instanceof TextWebSocketFrame) {
//            context.writeAndFlush(((ByteBufHolder) obj).retain());
            context.writeAndFlush(obj);
            promise.setSuccess();
//            String request = ((TextWebSocketFrame) frame).text();
        } else if (obj instanceof FullHttpResponse) {
            context.writeAndFlush(obj);
            promise.setSuccess();
        }
    }

    @Override
    public void flush(ChannelHandlerContext ctx) throws Exception {
        super.flush(ctx);
    }
}
