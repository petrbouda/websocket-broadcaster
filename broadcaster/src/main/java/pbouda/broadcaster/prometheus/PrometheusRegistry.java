package pbouda.broadcaster.prometheus;

import io.micrometer.core.instrument.Clock;
import io.micrometer.prometheus.PrometheusConfig;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import io.prometheus.client.CollectorRegistry;

public class PrometheusRegistry {

    public static PrometheusMeterRegistry instance() {
        return Holder.PROMETHEUS;
    }

    private static final class Holder {
        private static final PrometheusMeterRegistry PROMETHEUS = new PrometheusMeterRegistry(
                PrometheusConfig.DEFAULT, CollectorRegistry.defaultRegistry, Clock.SYSTEM);
    }
}
