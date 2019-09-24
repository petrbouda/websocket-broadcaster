package pbouda.broadcaster.server.rabbit;

import com.rabbitmq.client.Recoverable;
import com.rabbitmq.client.RecoveryListener;

public class CustomRecoveryListener implements RecoveryListener {

    private final RecoveryListenerType type;

    public CustomRecoveryListener(RecoveryListenerType type) {
        this.type = type;
    }

    @Override
    public void handleRecovery(Recoverable recoverable) {
        // TODO: for Channel / Connection
    }

    @Override
    public void handleRecoveryStarted(Recoverable recoverable) {
        // TODO: for Channel / Connection
    }

    enum RecoveryListenerType {
        CONNECTION, CHANNEL
    }
}
