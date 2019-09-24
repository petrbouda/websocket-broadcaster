package pbouda.broadcaster.server.netty.route;

import io.netty.handler.codec.http.FullHttpRequest;

public abstract class AbstractRoute implements Route {

    private final String uri;

    AbstractRoute(String uri) {
        this.uri = uri;
    }

    @Override
    public boolean isApplicable(FullHttpRequest request) {
        return uri.equalsIgnoreCase(request.uri());
    }

    @Override
    public String uri() {
        return uri;
    }
}
