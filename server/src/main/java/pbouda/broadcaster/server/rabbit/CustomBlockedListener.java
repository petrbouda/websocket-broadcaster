package pbouda.broadcaster.server.rabbit;

import com.rabbitmq.client.BlockedListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CustomBlockedListener implements BlockedListener {

    private static final Logger LOG = LoggerFactory.getLogger(CustomBlockedListener.class);

    @Override
    public void handleBlocked(String reason) {
        LOG.warn("Rabbit Connection is blocked because of low on resource: " + reason);
    }

    @Override
    public void handleUnblocked() {
        LOG.warn("Rabbit Connection is unblocked");
    }
}
