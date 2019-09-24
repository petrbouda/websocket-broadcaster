package pbouda.broadcaster.server.netty.route;

import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.DefaultFileRegion;
import io.netty.handler.codec.http.*;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.stream.ChunkedNioFile;
import pbouda.broadcaster.server.netty.HttpRequestHandler;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URISyntaxException;
import java.net.URL;

public class ClientRoute extends AbstractRoute {

    private static final File INDEX;

    static {
        URL location = HttpRequestHandler.class
                .getProtectionDomain()
                .getCodeSource()
                .getLocation();

        try {
            String path = location.toURI() + "index.html";
            path = !path.contains("file:") ? path : path.substring(5);
            INDEX = new File(path);
        } catch (URISyntaxException e) {
            throw new IllegalStateException("Unable to locate index.html", e);
        }
    }

    public ClientRoute() {
        super("/");
    }

    public ClientRoute(String uri) {
        super(uri);
    }

    @Override
    public void process(ChannelHandlerContext context, FullHttpRequest request) throws IOException {
        if (HttpUtil.is100ContinueExpected(request)) {
            send100Continue(context);
        }

        var file = new RandomAccessFile(INDEX, "r");
        var response = new DefaultHttpResponse(request.protocolVersion(), HttpResponseStatus.OK);
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/html; charset=UTF-8");

        boolean keepAlive = HttpUtil.isKeepAlive(request);
        if (keepAlive) {
            response.headers().set(HttpHeaderNames.CONTENT_LENGTH, file.length());
            response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
        }

        context.write(response);

        if (context.pipeline().get(SslHandler.class) == null) {
            context.write(new DefaultFileRegion(file.getChannel(), 0, file.length()));
        } else {
            context.write(new ChunkedNioFile(file.getChannel()));
        }

        // TODO: Try to remove keep-alive - what happens with WebSocket?
        var channelFuture = context.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);
        if (!keepAlive) {
            channelFuture.addListener(ChannelFutureListener.CLOSE);
        }
    }

    private static void send100Continue(ChannelHandlerContext context) {
        var response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.CONTINUE);
        context.writeAndFlush(response);
    }

    @Override
    public boolean isApplicable(FullHttpRequest request) {
        return uri().equalsIgnoreCase(request.uri())
                || request.uri().startsWith("/css");
    }
}
