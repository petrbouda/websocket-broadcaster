# Websocket Broadcaster in Netty

## Infrastructure Setup

Start all components to get ready for investigation
- Prometheus `localhost:9090`
- Grafana `localhost:3000`
- Kafka `localhost:9092`

```
docker-compose up
```

Create one topic `notes` with one partitions for one consumer

```
docker exec -it kafka kafka-topics --create --bootstrap-server localhost:9092 --replication-factor 1 --partitions 1 --topic notes
```

## Start

#### Broadcaster

- It's Websocket Server that reads messages from Kafka and broadcast them using Netty to all connected users
- Options `-XX:+UnlockDiagnosticVMOptions -XX:+DebugNonSafepoints` are included to preserve inlined methods and make them visible for AsyncProfiler
- Broadcaster (as well as other projects) is a modular project `Java Platform Module System (JPMS)` but contains `requires jdk.unsupported;` to enable `Unsafe`

```
java -XX:+UnlockDiagnosticVMOptions -XX:+DebugNonSafepoints --module-path broadcaster/target/dependency:broadcaster/target/classes --module broadcaster.server/pbouda.broadcaster.Server
```

#### Clients

- Start Websocket clients and connect them to Broadcaster
- Clients just log the incoming messages to see that something flows through the network

```
java --module-path client/target/dependency:client/target/classes --module broadcaster.client/pbouda.client.MultiClient

# For more clients (5000)
java --module-path client/target/dependency:client/target/classes --module broadcaster.client/pbouda.client.MultiClient 5000
```

#### Pusher

- Starts sending messages to Kafka
- Create unique Lorem ipsum message with 1-5 paragraphs using https://github.com/mdeanda/lorem

```
# Default 20ms delay
java --module-path pusher/target/dependency:pusher/target/classes --module broadcaster.pusher/pbouda.pusher.Pusher

# Custom delay with in millis
java --module-path pusher/target/dependency:pusher/target/classes --module broadcaster.pusher/pbouda.pusher.Pusher 50
```
