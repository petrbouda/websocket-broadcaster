package pbouda.broadcaster.server.prometheus;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;
import io.netty.buffer.ByteBufAllocatorMetric;

import java.util.List;

public class ByteBufAllocatorMetrics implements MeterBinder {

    private static final String DESCRIPTION =
            "Returns the number of bytes of pool memory used by a ByteBufAllocator or -1 if unknown " +
                    "(Return the number of active bytes that are currently allocated by all arenas)";

    private final List<NettyPoolMetricPair> metricPairs;

    ByteBufAllocatorMetrics(List<NettyPoolMetricPair> metricPairs) {
        this.metricPairs = metricPairs;
    }

    @Override
    public void bindTo(MeterRegistry registry) {
        for (NettyPoolMetricPair metric : metricPairs) {
            Gauge.builder("netty_memory_pool_used_bytes", metric.allocatorMetric, ByteBufAllocatorMetric::usedHeapMemory)
                    .tags("pool", "heap", "type", metric.poolType)
                    .description(DESCRIPTION)
                    .register(registry);

            Gauge.builder("netty_memory_pool_used_bytes", metric.allocatorMetric, ByteBufAllocatorMetric::usedDirectMemory)
                    .tags("pool", "direct", "type", metric.poolType)
                    .description(DESCRIPTION)
                    .register(registry);
        }
    }

    static class NettyPoolMetricPair {

        private final String poolType;
        private final ByteBufAllocatorMetric allocatorMetric;

        NettyPoolMetricPair(String poolType, ByteBufAllocatorMetric allocatorMetric) {
            this.poolType = poolType;
            this.allocatorMetric = allocatorMetric;
        }
    }
}
