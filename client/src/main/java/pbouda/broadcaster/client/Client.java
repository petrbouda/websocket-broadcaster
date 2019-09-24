package pbouda.broadcaster.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketClientCompressionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.net.URI;
import java.net.URISyntaxException;

public class Client {

    private static final Logger LOG = LoggerFactory.getLogger(Client.class);

    private static final URI URL;

    static {
        try {
            URL = new URI("ws://127.0.0.1:8080/ws");
        } catch (URISyntaxException e) {
            throw new Error("Could not parse WS URL");
        }
    }

    public static void main(String[] args) throws Exception {
        // Connect with V13 (RFC 6455 aka HyBi-17). You can change it to V08 or V00.
        // If you change it to V00, ping is not supported and remember to change
        // HttpResponseDecoder to WebSocketHttpResponseDecoder in the pipeline.
        WebSocketClientHandler handler = new WebSocketClientHandler(
                WebSocketClientHandshakerFactory.newHandshaker(
                        URL, WebSocketVersion.V13, null, true, new DefaultHttpHeaders()));

        EpollEventLoopGroup eventLoopGroup = new EpollEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap()
                    .group(eventLoopGroup)
                    .channel(EpollSocketChannel.class)
                    // Let's fill up SO_RCV_BUFFER
                    // .option(ChannelOption.SO_RCVBUF,  64 * 1024)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel channel) {
                            channel.pipeline()
                                    .addLast(new HttpClientCodec())
                                    .addLast(new HttpObjectAggregator(8192))
                                    .addLast(WebSocketClientCompressionHandler.INSTANCE)
                                    .addLast(handler);
                        }
                    });

            Channel channel = bootstrap.connect(URL.getHost(), URL.getPort()).sync().channel();
            handler.handshakeFuture().sync();

            LOG.info("PID {} - Client started, local-address '{}', remote-address '{}'",
                    ManagementFactory.getRuntimeMXBean().getPid(), channel.localAddress(), channel.remoteAddress());

            BufferedReader console = new BufferedReader(new InputStreamReader(System.in));
            while (true) {
                String msg = console.readLine();
                if (msg == null) {
                    break;
                } else if ("bye".equals(msg.toLowerCase())) {
                    channel.writeAndFlush(new CloseWebSocketFrame());
                    channel.closeFuture().sync();
                    break;
                } else if ("ping".equals(msg.toLowerCase())) {
                    WebSocketFrame frame = new PingWebSocketFrame(Unpooled.wrappedBuffer(new byte[]{8, 1, 8, 1}));
                    channel.writeAndFlush(frame);
                } else {
                    WebSocketFrame frame = new TextWebSocketFrame(msg);
                    channel.writeAndFlush(frame);
                }
            }
        } finally {
            eventLoopGroup.shutdownGracefully();
        }
    }
}

