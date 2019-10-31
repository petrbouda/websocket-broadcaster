package pbouda.broadcaster.netty;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import pbouda.broadcaster.netty.route.PrometheusRoute;
import pbouda.broadcaster.netty.route.WebsocketRoute;

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
        pipeline.addLast(new HttpServerCodec());
        pipeline.addLast(new ChunkedWriteHandler());
        pipeline.addLast(new HttpObjectAggregator(64 * 1024));

        // pipeline.addLast(new IdleStateHandler(60, 30, 0));
        // children: ReadTimeoutHandler & WriteTimeoutHandler

        pipeline.addLast(new HttpRequestHandler(List.of(new PrometheusRoute(), new WebsocketRoute())));
        // Sec-WebSocket-Extensions: permessage-deflate
        // pipeline.addLast(new WebSocketServerCompressionHandler());
        pipeline.addLast(new WebSocketServerProtocolHandler(websocketPath, null, true));
        pipeline.addLast(new WebsocketEventHandler(channelGroup));
        pipeline.addLast(new SlowConsumerDisconnectHandler());
    }
}
