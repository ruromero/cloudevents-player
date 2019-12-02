# CloudEvents Player

It is an application that can send and receive CloudEvents v1. Its purpose is to be deployed on a
KNative Eventing environment so that users can monitor received events in the Activity section and
also send events of the desired type to see if it is being forwarded back to the application through
the broker.

## Build and run the application

The application can be configured to send events to itself to ensure that both send/receive
work well and send valid CloudEvents.

The application requires the `broker.url` environment variable to be set.

```{bash}
BROKER_URL=http://localhost:8080 cloudevent-player-1.0-SNAPSHOT-runner
```

You can send a message from inside the application by filling in the form and the activity will show the sent
event and the received event (from the loopback)

You can also simulate the broker with the `curl`:

```shell script
$ curl -v http://localhost:8080 \
  -H "Content-Type: application/json" \
  -H "Ce-Id: foo-1" \
  -H "Ce-Specversion: 1.0" \
  -H "Ce-Type: dev.example.events" \
  -H "Ce-Source: curl-source" \
  -d '{"msg":"Hello team!"}'

> POST / HTTP/1.1
> User-Agent: curl/7.35.0
> Host: localhost:8080
> Accept: */*
> Ce-Id: foo-1
> Ce-Specversion: 1.0
> Ce-Type: dev.example.events
> Ce-Source: curl-source
> Content-Type: application/json
> Content-Length: 21
>
< HTTP/1.1 202 Accepted
< Content-Length: 0
< Date: Thu, 24 Oct 2019 08:27:06 GMT
```

It is a Quarkus application with a React frontend. In order to build the application use any of the
following alternatives:

### JVM Build

Build

```shell script
mvn clean package
```

Run

```shell script
$ BROKER_URL=http://localhost:8080 java -jar target/cloudevent-player-1.0-SNAPSHOT-runner.jar
...
2019-10-24 11:06:33,880 INFO  [io.quarkus] (main) cloudevent-player 1.0-SNAPSHOT (running on Quarkus 0.26.1) started in 1.875s. Listening on: http://0.0.0.0:8080
2019-10-24 11:06:33,881 INFO  [io.quarkus] (main) Profile prod activated.
2019-10-24 11:06:33,882 INFO  [io.quarkus] (main) Installed features: [cdi, hibernate-validator, rest-client, resteasy, resteasy-jackson, servlet, undertow-websockets, vertx]
```

### Quarkus dev mode

```shell script
$ BROKER_URL=http://localhost:8080 mvn clean compile quarkus:dev
...
Listening for transport dt_socket at address: 5005
...
[INFO] --- quarkus-maven-plugin:1.0.1.Final:dev (default-cli) @ cloudevent-player ---
Listening for transport dt_socket at address: 5005
2019-12-02 17:14:33,177 INFO  [io.und.web.jsr] (main) UT026003: Adding annotated server endpoint class com.redhat.syseng.tools.cloudevents.resources.MessagesSocket for path /socket
2019-12-02 17:14:33,556 INFO  [io.quarkus] (main) Quarkus 1.0.1.Final started in 2.161s. Listening on: http://0.0.0.0:8080
2019-12-02 17:14:33,556 INFO  [io.quarkus] (main) Profile dev activated. Live Coding activated.
2019-12-02 17:14:33,556 INFO  [io.quarkus] (main) Installed features: [cdi, hibernate-validator, rest-client, resteasy, resteasy-jsonb, servlet, undertow-websockets, vertx]
```

### Native build

Build

```shell script
mvn clean package -Pnative
```

Run

```shell script
$ BROKER_URL=http://localhost:8080 ./target/cloudevent-player-1.0-SNAPSHOT-runner
...
2019-12-02 17:13:52,298 INFO  [io.quarkus] (main) cloudevent-player 1.0-SNAPSHOT (running on Quarkus 1.0.1.Final) started in 0.032s. Listening on: http://0.0.0.0:8080
2019-12-02 17:13:52,299 INFO  [io.quarkus] (main) Profile prod activated.
2019-12-02 17:13:52,299 INFO  [io.quarkus] (main) Installed features: [cdi, hibernate-validator, rest-client, resteasy, resteasy-jsonb, servlet, undertow-websockets, vertx]
```

### Skip frontend build

The `skipFrontend` profile will not run npm commands. Useful when you are just changing Java code.

```{bash}
mvn clean package -PskipFrontend
```

## Build the container image

### JVM version

```shell script
docker build -t ruromero/cloudevents-player-jdk8:latest -f src/main/docker/Dockerfile.jvm .
```

### Native version

```shell script
docker build -t ruromero/cloudevents-player:latest -f src/main/docker/Dockerfile.native .
```

## Running CloudEvents Player on Kubernetes

### Requirements

* Knative serving
* Knative eventing

### Deploy the application

Use [deploy_native.yaml](./src/main/knative/deploy_native.yaml) to create the resources

```shell script
$ kubectl apply -n myproject -f src/main/knative/deploy_native.yaml
service.serving.knative.dev/cloudevents-player created
trigger.eventing.knative.dev/cloudevents-player created
```

The following resources are created:

* KNative Service: Pointing to the image and mounting the volume from the configMap
* Trigger: To subscribe to any message in the broker
