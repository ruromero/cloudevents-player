# CloudEvents Player

It is an application that can send and receive CloudEvents v3. Its purpose is to be deployed on a 
KNative Eventing environment so that users can monitor received events in the Activity section and
also send events of the desired type to see if it is being forwarded back to the application through
the broker.

## Build and run the application

It is a Quarkus application with a React frontend. In order to build the application use any of the
following alternatives:

### JVM Build

Build

```shell script
mvn clean package
```

Run

```shell script
$ java -jar target/cloudevent-player-1.0-SNAPSHOT-runner.jar
...
2019-10-24 11:06:33,880 INFO  [io.quarkus] (main) cloudevent-player 1.0-SNAPSHOT (running on Quarkus 0.26.1) started in 1.875s. Listening on: http://0.0.0.0:8080
2019-10-24 11:06:33,881 INFO  [io.quarkus] (main) Profile prod activated. 
2019-10-24 11:06:33,882 INFO  [io.quarkus] (main) Installed features: [cdi, hibernate-validator, rest-client, resteasy, resteasy-jackson, servlet, undertow-websockets, vertx]
```

### Quarkus dev mode

```shell script
$ mvn clean compile quarkus:dev
...
Listening for transport dt_socket at address: 5005
...
2019-10-24 11:04:53,877 INFO  [io.quarkus] (main) Quarkus 0.26.1 started in 2.139s. Listening on: http://0.0.0.0:8080
2019-10-24 11:04:53,877 INFO  [io.quarkus] (main) Profile dev activated. Live Coding activated.
2019-10-24 11:04:53,877 INFO  [io.quarkus] (main) Installed features: [cdi, hibernate-validator, rest-client, resteasy, resteasy-jackson, servlet, undertow-websockets, vertx]
```

### Native build

Build

```shell script
$ mvn clean package -Pnative
```

Run

```shell script
$ ./target/cloudevent-player-1.0-SNAPSHOT-runner
...
2019-10-24 11:15:12,990 INFO  [io.quarkus] (main) cloudevent-player 1.0-SNAPSHOT (running on Quarkus 0.26.1) started in 0.048s. Listening on: http://0.0.0.0:8080
2019-10-24 11:15:12,990 INFO  [io.quarkus] (main) Profile prod activated. 
2019-10-24 11:15:12,990 INFO  [io.quarkus] (main) Installed features: [cdi, hibernate-validator, rest-client, resteasy, resteasy-jackson, servlet, undertow-websockets, vertx]
```

## Use the application locally

By default, the application will send events to itself to ensure that both send/receive 
work well and send valid CloudEvents.

If needed, the broker endpoint can be configured in the application.properties file.
Create a ./config/application.properties file with the custom endpoint as in the example:

```properties
brokerUrl/mp-rest/url=http://endpoint.example.com
```

You can send a message from inside the application by filling in the form and the activity will show the sent
event and the received event (from the loopback)

You can also simulate the broker with the `curl`:

```shell script
$ curl -v http://localhost:8080 \
   -H "Content-Type: application/json" \
   -H "Ce-Id: foo-1" \
  -H "Ce-Specversion: 0.3" \
  -H "Ce-Type: dev.example.events" \
  -H "Ce-Source: curl-source" \
  -d '{"msg":"Hello team!"}'

> POST / HTTP/1.1
> User-Agent: curl/7.35.0
> Host: localhost:8080
> Accept: */*
> Ce-Id: foo-1
> Ce-Specversion: 0.3
> Ce-Type: dev.example.events
> Ce-Source: curl-source
> Content-Type: application/json
> Content-Length: 21
> 
< HTTP/1.1 202 Accepted
< Content-Length: 0
< Date: Thu, 24 Oct 2019 08:27:06 GMT
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

Before creating it, make sure you update the configMap with the right brokerUrl endpoint.

```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: cloudevents-player
data:
  application.properties: |
    brokerUrl/mp-rest/url=http://default-broker.<PROJECT_NAME>.svc.cluster.local
```

Use [deploy.yaml](./src/main/knative/deploy.yaml) to create the resources

```shell script
$ kubectl apply -n myproject -f src/main/knative/deploy.yaml
configmap/cloudevents-player created
service.serving.knative.dev/cloudevents-player created
trigger.eventing.knative.dev/cloudevents-player created
```

The following resources are created:

* ConfigMap: Containing the application.properties file which will be mounted as a volume.
* KNative Service: Pointing to the image and mounting the volume from the configMap
* Trigger: To subscribe to any message in the broker

### Known issues

Currently the native version doesn't work. When sending a new event or receiving it the following error
is shown:

```shell script
$ curl -v http://localhost:8080 \
   -H "Content-Type: application/json" \
   -H "Ce-Id: foo-1" \
  -H "Ce-Specversion: 0.3" \
  -H "Ce-Type: dev.example.events" \
  -H "Ce-Source: curl-source" \
  -d '{"msg":"Hello team!"}'
*   Trying ::1:8080...
* TCP_NODELAY set
* Connected to localhost (::1) port 8080 (#0)
> POST / HTTP/1.1
> Host: localhost:8080
> User-Agent: curl/7.65.3
> Accept: */*
> Content-Type: application/json
> Ce-Id: say-hello
> Ce-Specversion: 0.3
> Ce-Type: dev.ruben.events
> Ce-Source: ruben-source
> Content-Length: 21
> 
* upload completely sent off: 21 out of 21 bytes
* Mark bundle as not supporting multiuse
< HTTP/1.1 500 Internal Server Error
< Content-Type: text/plain;charset=UTF-8
< validation-exception: true
< Content-Length: 192
< 
* Connection #0 to host localhost left intact
javax.validation.NoProviderFoundException: Unable to create a Configuration, because no Bean Validation provider could be found. Add a provider like Hibernate Validator (RI) to your classpath.%
```
