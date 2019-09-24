# netty-websocket-broadcast

## Monitoring 

Metrics: http://localhost:8080/actuator/prometheus

Prometheus: http://localhost:9090

Grafana: http://localhost:3030

RabbitMQ: http://localhost:15672

### Start

```
java --module-path server/target/dependency:server/target/classes --module broadcaster.server/pbouda.broadcaster.server.Server

java --module-path client/target/dependency:client/target/classes --module broadcaster.client/pbouda.broadcaster.client.Client

java --module-path pusher/target/dependency:pusher/target/classes --module broadcaster.pusher/pbouda.broadcaster.pusher.Pusher
```