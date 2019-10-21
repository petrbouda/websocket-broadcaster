package pbouda.broadcaster.multi;

import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.EpollEventLoopGroup;

import java.net.URI;

public class MultiClient {

    private static int CLIENTS_COUNT = 5000;

    public static void main(String[] args) throws Exception {
        URI uri = new URI("ws://127.0.0.1:8080/ws");

        EventLoopGroup group = new EpollEventLoopGroup();
        try {
            for (int i = 0; i < CLIENTS_COUNT; i++) {
                SingleClient client = new SingleClient(uri, group, i);
                client.connect();
            }

            // Stop and don't kill the client.
            Thread.currentThread().join();
        } finally {
            group.shutdownGracefully();
        }
    }
}
