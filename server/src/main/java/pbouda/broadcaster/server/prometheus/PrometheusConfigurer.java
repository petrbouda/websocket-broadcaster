package pbouda.broadcaster.server.prometheus;

import io.github.mweirauch.micrometer.jvm.extras.ProcessMemoryMetrics;
import io.github.mweirauch.micrometer.jvm.extras.ProcessThreadMetrics;
import io.micrometer.core.instrument.binder.jvm.ClassLoaderMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmGcMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmThreadMetrics;
import io.micrometer.core.instrument.binder.logging.LogbackMetrics;
import io.micrometer.core.instrument.binder.system.FileDescriptorMetrics;
import io.micrometer.core.instrument.binder.system.ProcessorMetrics;
import io.micrometer.core.instrument.binder.system.UptimeMetrics;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.buffer.UnpooledByteBufAllocator;
import io.netty.channel.group.ChannelGroup;
import pbouda.broadcaster.server.prometheus.ByteBufAllocatorMetrics.NettyPoolMetricPair;

import java.util.List;
import java.util.UUID;

public class PrometheusConfigurer {

    public static void configure(ChannelGroup channelGroup) {
        PrometheusMeterRegistry prometheus = PrometheusRegistry.instance();

        String instance = UUID.randomUUID().toString().substring(0, 5);
        prometheus.config().commonTags("application", "broadcaster", "instance_label", instance);

        new JvmMemoryMetrics().bindTo(prometheus);
        new JvmGcMetrics().bindTo(prometheus);
        new ProcessorMetrics().bindTo(prometheus);
        new JvmThreadMetrics().bindTo(prometheus);
        new ClassLoaderMetrics().bindTo(prometheus);
        new UptimeMetrics().bindTo(prometheus);
        new LogbackMetrics().bindTo(prometheus);
        new FileDescriptorMetrics().bindTo(prometheus);

        // JVM Extras
        new ProcessMemoryMetrics().bindTo(prometheus);
        new ProcessThreadMetrics().bindTo(prometheus);

        // Netty Pool Memory Metrics
        var nettyPools = List.of(
                new NettyPoolMetricPair("pooled", PooledByteBufAllocator.DEFAULT.metric()),
                new NettyPoolMetricPair("unpooled", UnpooledByteBufAllocator.DEFAULT.metric()));
        new ByteBufAllocatorMetrics(nettyPools).bindTo(prometheus);

        new ChannelGroupMetrics(channelGroup).bindTo(prometheus);
    }
}
