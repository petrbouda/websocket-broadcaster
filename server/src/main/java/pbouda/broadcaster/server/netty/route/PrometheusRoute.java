package pbouda.broadcaster.server.netty.route;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import io.prometheus.client.exporter.common.TextFormat;
import pbouda.broadcaster.server.prometheus.PrometheusRegistry;

public class PrometheusRoute extends AbstractRoute {

    public PrometheusRoute() {
        this("/prometheus");
    }

    public PrometheusRoute(String uri) {
        super(uri);
    }

    @Override
    public void process(ChannelHandlerContext context, FullHttpRequest request) {
        DefaultFullHttpResponse response = new DefaultFullHttpResponse(request.protocolVersion(),
                HttpResponseStatus.OK, Unpooled.copiedBuffer(PrometheusRegistry.instance().scrape(), CharsetUtil.UTF_8));
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, TextFormat.CONTENT_TYPE_004);
        response.headers().setInt(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());

        if (HttpUtil.isKeepAlive(request)) {
            response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
            context.write(response);
        } else {
            // If keep-alive is off, close the connection once the content is fully written.
            context.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
        }
    }
}
