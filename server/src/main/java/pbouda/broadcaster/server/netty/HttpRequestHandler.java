package pbouda.broadcaster.server.netty;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;
import pbouda.broadcaster.server.netty.route.Route;

import java.util.List;

public class HttpRequestHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    private final List<Route> routers;

    HttpRequestHandler(List<Route> routers) {
        this.routers = routers;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext context, FullHttpRequest request) throws Exception {
        for (Route router : routers) {
            if (router.isApplicable(request)) {
                router.process(context, request);
                break;
            }
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext context) {
        context.flush();
    }

    @Override
    public void channelInactive(ChannelHandlerContext context) {
        // Closed connections for HTTP are not interesting
        // Very likely, it's Prometheus scrape mechanism
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext context, Throwable cause) {
        cause.printStackTrace();
        context.close();
    }
}
