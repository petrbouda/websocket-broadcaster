package pbouda.broadcaster.server.netty.route;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;

import java.io.IOException;

public interface Route {

    void process(ChannelHandlerContext context, FullHttpRequest request) throws IOException;

    boolean isApplicable(FullHttpRequest request);

    String uri();
}
