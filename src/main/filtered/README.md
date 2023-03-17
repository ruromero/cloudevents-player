# CloudEvents Player

![Build](https://img.shields.io/github/actions/workflow/status/ruromero/cloudevents-player/build.yaml)
[![amd64](https://img.shields.io/badge/container-amd64-blue)](https://quay.io/repository/ruben/cloudevents-player?tab=tags)
[![arm64](https://img.shields.io/badge/container-arm64-blue)](https://quay.io/repository/ruben/cloudevents-player?tab=tags)

- [Running the container image](#running-the-container-image)
- [Running the binary](#running-the-binary)
- [Running on Kubernetes](#running-on-kubernetes)
  * [Requirements](#requirements)
  * [Create the broker](#create-the-broker)
  * [Create the Knative Service](#create-the-knative-service)
  * [Bind the service with the broker](#bind-the-service-with-the-broker)
  * [Create the Knative trigger](#create-the-knative-trigger)
  * [Manual deployment](#manual-deployment)
- [Build](#build)
  * [JVM Build](#jvm-build)
  * [Quarkus dev mode](#quarkus-dev-mode)
  * [Native build](#native-build)
  * [Skip frontend build](#skip-frontend-build)
  * [Build the container image](#build-the-container-image)
- [Configuration](#configuration)
  * [Player Mode](#player-mode)
  * [Broker URI](#broker-uri)
  * [Broker Name and Namespace](#broker-name-and-namespace)
  * [CORS](#cors)

It is an application that can send and receive [CloudEvents](https://cloudevents.io/). Its purpose is to be deployed on a
[Knative Eventing](https://knative.dev/docs/eventing/) environment so that users can monitor received events in the Activity section and
also send events of the desired type to see if it is being forwarded back to the application through
the broker.

It supports both [Structured](https://github.com/cloudevents/spec/blob/v1.0.2/cloudevents/bindings/http-protocol-binding.md#32-structured-content-mode)
and [Binary](https://github.com/cloudevents/spec/blob/v1.0.2/cloudevents/bindings/http-protocol-binding.md#31-binary-content-mode) content mode.

The application has a web interface in which you can define the events you want to send to the broker:

![create event](docs/images/create_event.png)

On the right-hand side all the emitted and received events will be listed. In the image below there are two received 
and one emitted event.

![activity](docs/images/activity.png)

And you will also be able to display the payload of the event.

![event](docs/images/event.png)

## Running the container image

You can expect to have built container images in `arm64` and `amd64` architectures in `${quarkus.container-image.registry}/${quarkus.container-image.group}/${quarkus.container-image.name}:${quarkus.container-image.tag}`

_Podman_

```bash
podman run -p 8080:8080 -e PLAYER_MODE=LOCAL --rm ${quarkus.container-image.registry}/${quarkus.container-image.group}/${quarkus.container-image.name}:${quarkus.container-image.tag}
```

_Docker_
```bash
docker run -p 8080:8080 -e PLAYER_MODE=LOCAL --rm ${quarkus.container-image.registry}/${quarkus.container-image.group}/${quarkus.container-image.name}:${quarkus.container-image.tag}
```

## Running the binary

First you will have to build the application. See the [Build](#build) section for more details. Then you can
start the application locally with the following command.

```bash
PLAYER_MODE=LOCAL ./target/${project.artifactId}-${project.version}-runner
```

## Running on Kubernetes

### Requirements

- Knative serving
- Knative eventing
- `kubectl`
- `kn`

### Create the broker

Let's use the `kn` CLI to create an InMemoryChannel-backed Broker.

```bash
$ kn broker create example-broker
Broker 'example-broker' successfully created in namespace 'eventing-demo'.
```

Let's confirm the Broker is created and ready.

```bash
$ kn broker list                 
NAME             URL                                                                                AGE   CONDITIONS   READY   REASON
example-broker   http://broker-ingress.knative-eventing.svc.cluster.local/eventing-demo/example-broker   20s   6 OK / 6     True   
```

### Create the Knative Service

If you have the `kn` cli available you can just create the broker with the CLI

```bash
$ kn service create cloudevents-player \
--image quay.io/ruben/cloudevents-player:latest

Creating service 'cloudevents-player' in namespace 'eventing-demo':

  0.224s Configuration "cloudevents-player" is waiting for a Revision to become ready.
  6.043s ...
  6.044s Ingress has not yet been reconciled.
  6.045s Waiting for load balancer to be ready
  6.248s Ready to serve.

Service 'cloudevents-player' created to latest revision 'cloudevents-player-00001' is available at URL:
https://cloudevents-player-eventing-demo.apps.example.com
```

### Bind the service with the broker

It is possible to connect the Cloudevents Player with the broker in different ways.

1. Create a SinkBinding. This is the easiest one and works for any type of broker. It injects the `K_SINK` environment variable
into the Knative Service. The service is rolled out and starts using it for sending events.

```bash
kn source binding create ce-player-binding --subject "Service:serving.knative.dev/v1:cloudevents-player" --sink broker:example-broker
```

2. Define the `BROKER_NAME` and `BROKER_NAMESPACE` environment variables to the Deployment, through the Service spec. The `BROKER_URI`
is calculated from these values as `http://broker-ingress.knative-eventing.svc.cluster.local/{broker_namespace}/{broker_name}`. This
option is only compatible with the In-Memory Broker.

```bash
## The BROKER_NAMESPACE env var is only required if the broker is in a different namespace

kn service update --env BROKER_NAME=example-broker --env BROKER_NAMESPACE=other-ns cloudevents-player
```

3. Define the `BROKER_URI`. As a last resort you can manually define the `BROKER_URI` of the Broker. This will take precedence over any
other variable.

```bash
kn service update --env BROKER_URI=http://other-ingress.other-ns.svc.cluster.local/foo/bar cloudevents-player
```

Try out the Service URL, you will be able to send events but your events will not be consumed by any
service.

```bash
$ CLOUDEVENTS_URL=$(kn service describe cloudevents-player -o url)
$ curl -vk $CLOUDEVENTS_URL \
        -H "Content-Type: application/json" \
        -H "Ce-Id: 123456789" \
        -H "Ce-Specversion: 1.0" \
        -H "Ce-Type: some-type" \
        -H "Ce-Source: command-line" \
        -d '{"msg":"Hello CloudEvents!"}'
```

### Create the Knative trigger

In order for the Cloudevents Player to subscribe to events from the broker you need to create a `trigger`

```bash
kn trigger create cloudevents-trigger --sink cloudevents-player  --broker example-broker                                             
```

### Manual deployment

As an alternative, you can deploy the application manually by using the [knative.yaml](deploy/knative.yaml) file

```shell script
$ kubectl apply -n myproject -f deploy/knative.yaml
service.serving.knative.dev/cloudevents-player created
trigger.eventing.knative.dev/cloudevents-player created
sinkbinding.sources.knative.dev/ce-player-binding created
broker.eventing.knative.dev/example-broker created
```

The following resources are created:

* InMemory Broker: The Broker that will receive and forward the events
* Knative Service: Pointing to the image and mounting the volume from the configMap
* SinkBinding: Binding the Knative service to the Broker
* Trigger: To subscribe to any message in the Broker

## Build

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
2022-06-24 18:39:07,794 INFO  [io.und.websockets] (main) UT026003: Adding annotated server endpoint class com.redhat.syseng.tools.cloudevents.resources.MessagesSocket for path /socket
2022-06-24 18:39:08,130 INFO  [io.qua.sma.ope.run.OpenApiRecorder] (main) Default CORS properties will be used, please use 'quarkus.http.cors' properties instead
2022-06-24 18:39:08,216 INFO  [io.quarkus] (main) ${project.artifactId} ${project.version} on JVM (powered by Quarkus ${quarkus.platform.version}) started in 0.879s. Listening on: http://0.0.0.0:8080
2022-06-24 18:39:08,217 INFO  [io.quarkus] (main) Profile prod activated. 
2022-06-24 18:39:08,217 INFO  [io.quarkus] (main) Installed features: [cdi, hibernate-validator, kubernetes-client, rest-client, resteasy-reactive, resteasy-reactive-jackson, smallrye-context-propagation, smallrye-openapi, vertx, websockets, websockets-client] 
```

### Quarkus dev mode

```shell script
$ mvn quarkus:dev -Dplayer.mode=LOCAL
...
Listening for transport dt_socket at address: 5005
...
[INFO] --- quarkus-maven-plugin:1.0.1.Final:dev (default-cli) @ ${project.artifactId} ---
Listening for transport dt_socket at address: 5005
2022-06-24 18:51:43,172 INFO  [io.und.websockets] (Quarkus Main Thread) UT026003: Adding annotated server endpoint class com.redhat.syseng.tools.cloudevents.resources.MessagesSocket for path /socket

2022-06-24 18:51:43,229 WARN  [org.jbo.res.res.i18n] (Quarkus Main Thread) RESTEASY002155: Provider class io.cloudevents.http.restful.ws.CloudEventsProvider is already registered.  2nd registration is being ignored.
2022-06-24 18:51:43,513 INFO  [io.quarkus] (Quarkus Main Thread) ${project.artifactId} ${project.version} on JVM (powered by Quarkus ${quarkus.platform.version}) started in 2.543s. Listening on: http://localhost:8080
2022-06-24 18:51:43,514 INFO  [io.quarkus] (Quarkus Main Thread) Profile dev activated. Live Coding activated.
2022-06-24 18:51:43,515 INFO  [io.quarkus] (Quarkus Main Thread) Installed features: [cdi, hibernate-validator, kubernetes-client, rest-client-reactive, rest-client-reactive-jackson, resteasy-reactive, resteasy-reactive-jackson, smallrye-context-propagation, smallrye-openapi, swagger-ui, vertx, websockets, websockets-client]
```

### Native build

Build

```shell script
mvn clean install -Pnative
```

Run

```shell script
$ ./target/${project.artifactId}-${project.version}-runner -Dplayer.mode=LOCAL
...
2022-06-24 18:48:11,565 INFO  [io.quarkus] (main) ${project.artifactId} ${project.version} native (powered by Quarkus ${quarkus.platform.version}) started in 0.022s. Listening on: http://0.0.0.0:8080
2022-06-24 18:48:11,565 INFO  [io.quarkus] (main) Profile prod activated. 
2022-06-24 18:48:11,565 INFO  [io.quarkus] (main) Installed features: [cdi, hibernate-validator, kubernetes-client, rest-client-reactive, rest-client-reactive-jackson, resteasy-reactive, resteasy-reactive-jackson, smallrye-context-propagation, smallrye-openapi, swagger-ui, vertx, websockets, websockets-client]
2022-06-24 18:48:17,028 INFO  [com.git.rur.clo.ser.MessageService] (ForkJoinPool.commonPool-worker-3) Player mode LOCAL - broker: http://localhost:8080/
```

### Skip frontend build

The `skipFrontend` profile will not run npm commands. Useful when you are just changing Java code.

```{bash}
mvn clean package -PskipFrontend
```

### Build the container image

```shell script
mvn clean package -Dcontainer -Dquarkus.native.container-build=true
```

## Configuration

The Cloudevents Player can be configured by:

 - System properties. Usually in lower case and separated by dots e.g. `player.mode` 
 - Environment variables. In upper case and separated by underscores e.g. `PLAYER_MODE`

These are the configuration options provided in the Cloudevents Player.

- `PLAYER_MODE`
- `BROKER_URI`
- `BROKER_NAME`
- `BROKER_NAMESPACE`

In the `KNATIVE` mode it will also look for the `K_SINK` environment variable injected after
defining a `SinkBinding`.

For more configuration options refer to the [Quarkus documentation](https://quarkus.io/guides/all-config)

### Player Mode

Cloudevents Player comes with 2 modes defined in the `PLAYER_MODE` environment variable:

- `LOCAL`: Received events are forwarded to the loopback broker. This mode is just for development and testing 
- `KNATIVE` (default): The application will get the current namespace it is running in and will use the `BROKER_NAME`
 environment variable to decide which broker to connect to (`default` is the default broker).

```bash
# Local Mode
# system property
./target/${project.artifactId}-${project.version}-runner -Dplayer.mode=LOCAL

# environment variable
PLAYER_MODE=LOCAL ./target/${project.artifactId}-${project.version}-runner

# Knative Mode
# system property
./target/${project.artifactId}-${project.version}-runner -Dplayer.mode=KNATIVE

# environment variable
PLAYER_MODE=KNATIVE ./target/${project.artifactId}-${project.version}-runner
```

### Broker URI

Sets the `BROKER_URI` where the messages will be sent to. It will always be `localhost:8080` for `LOCAL` mode.
Overrides the name and namespace properties.

```bash
# with a system property
./target/${project.artifactId}-${project.version}-runner -Dbroker.uri=http://some-broker:1234

# or using an environment variable
BROKER_URI=http://some-broker:1234 ./target/${project.artifactId}-${project.version}-runner
```

### Broker Name and Namespace

Define the broker name and namespace to guess the broker URI. The default broker name is `default` and the default
namespace will be the current namespace.

```bash
# system property
./target/${project.artifactId}-${project.version}-runner -Dbroker.name=example -Dbroker.namespace=other
...
2022-06-24 19:08:53,681 INFO  [com.git.rur.clo.ser.MessageService] (ForkJoinPool.commonPool-worker-3) Player mode KNATIVE - broker: http://broker-ingress.knative-eventing.svc.cluster.local/other/example

# environment variable
BROKER_NAME=example BROKER_NAMESPACE=other ./target/${project.artifactId}-${project.version}-runner
...
2022-06-24 19:08:53,681 INFO  [com.git.rur.clo.ser.MessageService] (ForkJoinPool.commonPool-worker-3) Player mode KNATIVE - broker: http://broker-ingress.knative-eventing.svc.cluster.local/other/example
```

### CORS

By default the cloudevents player will allow all origins but it is possible to defined the allowed origins with the following environment variable:

```
  - name: QUARKUS_HTTP_CORS_ORIGINS
    value: https://cloudevents-player-myns.apps.example.com
```

See the [Quarkus CORS documentation](https://quarkus.io/guides/http-reference#cors-filter) for more configuration parameters.