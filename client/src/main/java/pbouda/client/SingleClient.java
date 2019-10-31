package pbouda.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshakerFactory;
import io.netty.handler.codec.http.websocketx.WebSocketVersion;
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketClientCompressionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;

public class SingleClient {

    private static final Logger LOG = LoggerFactory.getLogger(SingleClient.class);

    private final URI uri;
    private final EventLoopGroup group;
    private final int id;

    public SingleClient(URI uri, EventLoopGroup group, int id) {
        this.uri = uri;
        this.group = group;
        this.id = id;
    }

    public void connect() throws InterruptedException {
        var handshaker = WebSocketClientHandshakerFactory.newHandshaker(
                uri, WebSocketVersion.V13, null, true, new DefaultHttpHeaders());

        var handler = new WebSocketClientHandler(handshaker, id);

        Bootstrap b = new Bootstrap()
                .group(group)
                .channel(EpollSocketChannel.class)
                .handler(new CustomClientInitializer(handler));

        // Channel to send message to server
        ChannelFuture channelFuture = b.connect(uri.getHost(), uri.getPort()).sync();

        channelFuture.channel().closeFuture()
                .addListener(v -> LOG.info(id + " - Websocket Client died!"));
    }

    private static class CustomClientInitializer extends ChannelInitializer<SocketChannel> {

        private final ChannelHandler handler;

        private CustomClientInitializer(ChannelHandler handler) {
            this.handler = handler;
        }

        @Override
        protected void initChannel(SocketChannel channel) {
            channel.pipeline()
                    .addLast(new HttpClientCodec())
                    .addLast(new HttpObjectAggregator(8192))
                    .addLast(WebSocketClientCompressionHandler.INSTANCE)
                    .addLast(handler);
        }
    }
}
