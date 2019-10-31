# netty-websocket-broadcast

## Monitoring 

Metrics: http://localhost:8080/prometheus

Prometheus: http://localhost:9090

Grafana: http://localhost:3030

### Start

```
java -XX:+UnlockDiagnosticVMOptions -XX:+DebugNonSafepoints --module-path server/target/dependency:server/target/classes --module broadcaster.server/Server

java --module-path pusher/target/dependency:pusher/target/classes --module broadcaster.pusher/Pusher

java --module-path multi-client/target/dependency:multi-client/target/classes --module broadcaster.multi.client/MultiClient
```
