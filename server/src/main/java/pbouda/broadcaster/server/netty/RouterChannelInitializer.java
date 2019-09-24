package pbouda.broadcaster.server.netty;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketServerCompressionHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import pbouda.broadcaster.server.netty.route.ClientRoute;
import pbouda.broadcaster.server.netty.route.PrometheusRoute;
import pbouda.broadcaster.server.netty.route.Route;
import pbouda.broadcaster.server.netty.route.WebsocketRoute;

import java.util.List;

public class RouterChannelInitializer extends ChannelInitializer<SocketChannel> {

    private final String websocketPath;
    private final ChannelGroup channelGroup;

    RouterChannelInitializer(String websocketPath, ChannelGroup channelGroup) {
        this.websocketPath = websocketPath;
        this.channelGroup = channelGroup;
    }

    @Override
    protected void initChannel(SocketChannel channel) {
        ChannelPipeline pipeline = channel.pipeline();

        List<Route> httpRoutes = List.of(new ClientRoute(), new PrometheusRoute(), new WebsocketRoute());

        pipeline.addLast(new HttpServerCodec());
        pipeline.addLast(new ChunkedWriteHandler());
        pipeline.addLast(new HttpObjectAggregator(64 * 1024));

        // pipeline.addLast(new IdleStateHandler(60, 30, 0));
        // children: ReadTimeoutHandler & WriteTimeoutHandler

        pipeline.addLast(new HttpRequestHandler(httpRoutes));
        // Sec-WebSocket-Extensions: permessage-deflate
//        pipeline.addLast(new WebSocketServerCompressionHandler());
        pipeline.addLast(new WebSocketServerProtocolHandler(websocketPath, null, true));
        pipeline.addLast(new WebsocketEventHandler(channelGroup));
        pipeline.addLast(new TextWebsocketFrameHandler(channelGroup));
        pipeline.addLast(new SlowConsumerDisconnectHandler());
    }
}
