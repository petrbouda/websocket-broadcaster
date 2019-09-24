package pbouda.broadcaster.server.netty;

import io.micrometer.core.instrument.Counter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pbouda.broadcaster.server.prometheus.PrometheusRegistry;

public class WebsocketEventHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {

    private static final Logger LOG = LoggerFactory.getLogger(WebsocketEventHandler.class);

    private static final Counter CONNECTED_USERS_TOTAL =
            Counter.builder("broadcaster_connected_users_total")
                    .description("Total number of all connected users")
                    .register(PrometheusRegistry.instance());

    private static final Counter DISCONNECTED_USERS_TOTAL_BY_CLOSE_FRAME =
            Counter.builder("broadcaster_disconnected_users_total")
                    .tag("reason", "close_frame")
                    .description("Total number of all regularly disconnected users")
                    .register(PrometheusRegistry.instance());

    private static final Counter DISCONNECTED_USERS_TOTAL_BY_EXCEPTION =
            Counter.builder("broadcaster_disconnected_users_total")
                    .tag("reason", "exception")
                    .description("Total number of all exceptionally disconnected users")
                    .register(PrometheusRegistry.instance());

    private final ChannelGroup channelGroup;

    WebsocketEventHandler(ChannelGroup channelGroup) {
        this.channelGroup = channelGroup;
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext context, Object event) throws Exception {
        if (event instanceof WebSocketServerProtocolHandler.HandshakeComplete) {
            context.pipeline().remove(HttpRequestHandler.class);
            channelGroup.add(context.channel());
            LOG.info("WS Client added: " + context.channel().remoteAddress());
            CONNECTED_USERS_TOTAL.increment();
//        } else if (event instanceof IdleStateEvent) {
//            /*
//             * Automatic PING - PONG mechanism in WebSocket?
//             */
//            IdleStateEvent e = (IdleStateEvent) event;
//            if (e.state() == IdleState.READER_IDLE) {
//                context.close();
//            } else if (e.state() == IdleState.WRITER_IDLE) {
//                context.writeAndFlush(new PingMessage());
//            }

        } else if (event == WebSocketServerProtocolHandler.ServerHandshakeStateEvent.HANDSHAKE_TIMEOUT) {
            LOG.info("WS HandshakeTimeout occurred: " + context.channel().remoteAddress());

        } else {
            super.userEventTriggered(context, event);
        }
    }

    @Override
    public void channelWritabilityChanged(ChannelHandlerContext context) throws Exception {
        if (!context.channel().isWritable()) {
            LOG.error("Channel '{}' became not writable (probably slower consumer)", context.channel().remoteAddress());
        } else {
            LOG.info("Channel '{}' became writable again", context.channel().remoteAddress());
        }

        super.channelWritabilityChanged(context);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext context, TextWebSocketFrame msg) {
        context.fireChannelRead(msg.retain());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext context, Throwable cause) {
        LOG.error("An exception occurred, closing client " + context.channel().remoteAddress(), cause);
        context.close();
        DISCONNECTED_USERS_TOTAL_BY_EXCEPTION.increment();
    }

    @Override
    public void channelInactive(ChannelHandlerContext context) {
        LOG.info("Closing WS Client: " + context.channel().remoteAddress());
        DISCONNECTED_USERS_TOTAL_BY_CLOSE_FRAME.increment();
    }
}
