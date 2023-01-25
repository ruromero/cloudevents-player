# CloudEvents Player

It is an application that can send and receive CloudEvents v1. Its purpose is to be deployed on a
KNative Eventing environment so that users can monitor received events in the Activity section and
also send events of the desired type to see if it is being forwarded back to the application through
the broker.

The application has a web interface in which you can define the events you want to send to the broker:

![create event](docs/images/create_event.png)

In the right-hand side all the emitted and received events will be listed. In the image below there are two received 
and one emitted event.

![activity](docs/images/activity.png)

And you will also be able to display the payload of the event.

![event](docs/images/event.png)

## Build and run the application

The application can be configured to send events to itself to ensure that both send/receive
work well and send valid CloudEvents.

By default the application will start using a loopback endpoint

```{bash}
./target/cloudevent-player-1.2-SNAPSHOT-runner
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
$ java -Dplayer.mode=LOCAL -jar target/quarkus-app/quarkus-run.jar
...
2022-06-24 18:39:07,794 INFO  [io.und.websockets] (main) UT026003: Adding annotated server endpoint class com.github.ruromero.cloudeventsplayer.resources.MessagesSocket for path /socket
2022-06-24 18:39:08,130 INFO  [io.qua.sma.ope.run.OpenApiRecorder] (main) Default CORS properties will be used, please use 'quarkus.http.cors' properties instead
2022-06-24 18:39:08,216 INFO  [io.quarkus] (main) cloudevent-player 1.2-SNAPSHOT on JVM (powered by Quarkus 2.15.3.Final) started in 0.879s. Listening on: http://0.0.0.0:8080
2022-06-24 18:39:08,217 INFO  [io.quarkus] (main) Profile prod activated. 
2022-06-24 18:39:08,217 INFO  [io.quarkus] (main) Installed features: [cdi, hibernate-validator, kubernetes-client, rest-client, resteasy-reactive, resteasy-reactive-jackson, smallrye-context-propagation, smallrye-openapi, vertx, websockets, websockets-client] 
```

### Quarkus dev mode

```shell script
$ mvn quarkus:dev -Dplayer.mode=LOCAL
...
Listening for transport dt_socket at address: 5005
...
[INFO] --- quarkus-maven-plugin:1.0.1.Final:dev (default-cli) @ cloudevent-player ---
Listening for transport dt_socket at address: 5005
2022-06-24 18:51:43,172 INFO  [io.und.websockets] (Quarkus Main Thread) UT026003: Adding annotated server endpoint class com.github.ruromero.cloudeventsplayer.resources.MessagesSocket for path /socket

2022-06-24 18:51:43,229 WARN  [org.jbo.res.res.i18n] (Quarkus Main Thread) RESTEASY002155: Provider class io.cloudevents.http.restful.ws.CloudEventsProvider is already registered.  2nd registration is being ignored.
2022-06-24 18:51:43,513 INFO  [io.quarkus] (Quarkus Main Thread) cloudevent-player 1.2-SNAPSHOT on JVM (powered by Quarkus 2.15.3.Final) started in 2.543s. Listening on: http://localhost:8080
2022-06-24 18:51:43,514 INFO  [io.quarkus] (Quarkus Main Thread) Profile dev activated. Live Coding activated.
2022-06-24 18:51:43,515 INFO  [io.quarkus] (Quarkus Main Thread) Installed features: [cdi, hibernate-validator, kubernetes-client, rest-client, resteasy-reactive, resteasy-reactive-jackson, smallrye-context-propagation, smallrye-openapi, swagger-ui, vertx, websockets, websockets-client]


```

### Native build

Build

```shell script
mvn clean install -Pnative
```

Run

```shell script
$ ./target/cloudevent-player-1.2-SNAPSHOT-runner -Dplayer.mode=LOCAL
...
2022-06-24 18:48:11,565 INFO  [io.quarkus] (main) cloudevent-player 1.2-SNAPSHOT native (powered by Quarkus 2.15.3.Final) started in 0.022s. Listening on: http://0.0.0.0:8080
2022-06-24 18:48:11,565 INFO  [io.quarkus] (main) Profile prod activated. 
2022-06-24 18:48:11,565 INFO  [io.quarkus] (main) Installed features: [cdi, hibernate-validator, kubernetes-client, rest-client, resteasy-reactive, resteasy-reactive-jackson, smallrye-context-propagation, smallrye-openapi, vertx, websockets, websockets-client]
2022-06-24 18:48:17,028 INFO  [com.red.sys.too.clo.ser.MessageService] (ForkJoinPool.commonPool-worker-3) Player mode LOCAL - broker: http://localhost:8080/ 
```

### Skip frontend build

The `skipFrontend` profile will not run npm commands. Useful when you are just changing Java code.

```{bash}
mvn clean package -PskipFrontend
```

## Build the container image

```shell script
mvn package -Dcontainer
```

## Running CloudEvents Player on Kubernetes

### Requirements

* Knative serving
* Knative eventing

### Deploy the application

Use [knative.yaml](deploy/knative.yaml) to create the resources

```shell script
$ kubectl apply -n myproject -f deploy/knative.yaml
service.serving.knative.dev/cloudevents-player created
trigger.eventing.knative.dev/cloudevents-player created
```

The following resources are created:

* KNative Service: Pointing to the image and mounting the volume from the configMap
* Trigger: To subscribe to any message in the broker

## Configuration

### Mode

Cloudevents-player comes with 2 modes defined in the PLAYER_MODE environment variable:

- LOCAL: Received events are forwarded to the loopback broker. This mode is just for development and testing 
- KNATIVE (default): The application will get the current namespace it is running in and will use the `PLAYER_BROKER` 
 environment variable to decide which broker to connect to (`default` is the default broker).

```bash
# Local Mode
./target/cloudevent-player-1.2-SNAPSHOT-runner -Dplayer.mode=LOCAL

# Knative Mode
./target/cloudevent-player-1.2-SNAPSHOT-runner -Dplayer.mode=KNATIVE
```

### Broker URI

Sets the broker URI where the messages will be sent to. It will always be `localhost:8080` for `LOCAL` mode.
Overrides the name and namespace properties.

```bash
./target/cloudevent-player-1.2-SNAPSHOT-runner -Dplayer.mode=KNATIVE -Dbroker.uri=http://some-broker:1234
```

### Broker Name and Namespace

Define the broker name and namespace to guess the broker URI. The default broker name is `default` and the default
namespace will be the current namespace.

```bash
# The broker URL
./target/cloudevent-player-1.2-SNAPSHOT-runner -Dplayer.mode=KNATIVE -Dbroker.name=example -Dbroker.namespace=other
...
2022-06-24 19:08:53,681 INFO  [com.red.sys.too.clo.ser.MessageService] (ForkJoinPool.commonPool-worker-3) Player mode KNATIVE - broker: http://broker-ingress.knative-eventing.svc.cluster.local/other/example
```