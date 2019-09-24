package pbouda.broadcaster.server.netty;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TextWebsocketFrameHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {

    private static final Logger LOG = LoggerFactory.getLogger(TextWebsocketFrameHandler.class);

    private final ChannelGroup channelGroup;

    TextWebsocketFrameHandler(ChannelGroup channelGroup) {
        this.channelGroup = channelGroup;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext context, TextWebSocketFrame msg) {
        channelGroup.writeAndFlush(msg.retain());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        ctx.close().addListener(future ->
                LOG.error("Connection closed: " + ctx.channel().remoteAddress() + ", " + cause.getMessage(), cause));
    }
}
