package pbouda.broadcaster.netty.route;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;

public class WebsocketRoute extends AbstractRoute {

    public WebsocketRoute() {
        this("/ws");
    }

    public WebsocketRoute(String uri) {
        super(uri);
    }

    @Override
    public void process(ChannelHandlerContext context, FullHttpRequest request) {
        context.fireChannelRead(request.retain());
    }
}
