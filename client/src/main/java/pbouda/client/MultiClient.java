package pbouda.client;

import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.EpollEventLoopGroup;

import java.net.URI;

public class MultiClient {

    public static void main(String[] args) throws Exception {
        int clients = args.length > 0 ? Integer.parseInt(args[0]) : 20_000;
        URI uri = new URI("ws://127.0.0.1:8081/ws");

        EventLoopGroup group = new EpollEventLoopGroup(0, new NamedThreadFactory("client"));
        Runtime.getRuntime().addShutdownHook(new Thread(group::shutdownGracefully));

        for (int i = 0; i < clients; i++) {
            SingleClient client = new SingleClient(uri, group, i);
            client.connect();
        }

        // Stop and don't kill the clients.
        Thread.currentThread().join();
    }
}
