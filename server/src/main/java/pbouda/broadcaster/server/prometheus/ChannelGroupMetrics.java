package pbouda.broadcaster.server.prometheus;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;
import io.netty.channel.group.ChannelGroup;

import java.util.Set;

public class ChannelGroupMetrics implements MeterBinder {

    private final ChannelGroup channelGroup;

    ChannelGroupMetrics(ChannelGroup channelGroup) {
        this.channelGroup = channelGroup;
    }

    @Override
    public void bindTo(MeterRegistry registry) {
        Gauge.builder("netty_connections_total", channelGroup, Set::size)
                .description("Returns a current number of connections")
                .register(registry);
    }
}
