<p align="center">
  <h3 align="center">Custom Java OpenTelemetry</h3>
</p>
</br>

### CustomContextPropagator.java - [Propagator Logic]:
- Implements the TextMapPropagator (OTel API component) that propagates cross-cutting concerns (like trace context) as string key-value pairs across process boundaries.
- It's an interface that allows your custom data to travel between different microservices.
- TextMapPropagator uses a generic carrier object. This means the CustomContextPropagator logic can work for an HTTP call & a Kafka message without you having to rewrite your security logic for every different protocol.
  - Functionality: It acts as an interface with inject (sender) and extract (receiver) methods.
  - Carriers: Typically used with HTTP headers, gRPC metadata, or message queues, using TextMapSetter and TextMapGetter to read/write data.
  - Usage Example: Often implemented via library-specific interceptors or middleware to automatically pass trace IDs and span context between service calls.
  - Synonyms/Related Components: Often implemented through concrete classes such as CompositeTextMapPropagator, TraceContextPropagator, or B3Propagator.
- It overrides 3 methods:
  - `fields() (Metadata Phase)` ->
    - Like a checklist that tells the system exactly which "stamps" (headers) it needs to look for.
    - This tells OpenTelemetry which HTTP headers this specific propagator is allowed to touch.
  - `extract() (Inbound Phase)` ->
    - Like a customs officer reading a passport and entering the data into the local system.
    - Read the inbound request's header & save it to the OTel Context memory. Pass Baggage through.
    - When Service receives an incoming request, OTel pauses the request and runs this method.
    - Now, even if the application code does 10 minutes of database work, that token is safely stored in the background memory of that specific thread.
      - carrier: This is the incoming HTTP Request object.
      - getter: A tool that safely reads headers from the request.
      - context: This is the invisible "Thread Memory" (ThreadLocal) that OTel maintains while your application processes the request.
  - `inject() (Outbound Phase)` ->
    - Like a travel agent writing your visa details onto your ticket before you leave the country.
    - Read the token from the OTel Context memory & write it as new-auth: <token> on the outbound request.
    - When Service A finishes its work and makes a new HTTP call to Service B, OTel pauses the outgoing request and runs this method.
      - context: The memory where you saved the token during extract.
      - carrier: The new outgoing HTTP Request.
      - setter: A tool to write headers into the outgoing request.

### CustomContextPropagatorProvider.java - [SPI (Service Provider Interface) Factory Provider]:
- Implementing ConfigurablePropagatorProvider is the step that converts your custom Java class from "just a file in a JAR" into a selectable feature that the OpenTelemetry agent can actually use.
- OpenTelemetry uses Java's SPI (Service Provider Interface) to automatically discover the custom code at startup and must write a simple factory class to register the propagator
- It overrides 2 methods:
  - `getPropagator() (Outbound Phase)` ->  
    - Returns a new instance of your CustomContextPropagator.java class
  - `getName() (Outbound Phase)` ->
    - This is the magic string used in OTEL_PROPAGATORS
  
### Register the SPI:
- In src/main/resources folder, must create a folder structure exactly like this: META-INF/services/io.opentelemetry.context.propagation.spi.TextMapPropagatorProvider
- Inside that text file, write exactly one line—the fully qualified name of provider CustomContextPropagatorProvider.java
- Note: (io.opentelemetry... is the actual name of the text file itself, it has no file extension like .txt or .java)
- When the OTel Java Agent boots up, it looks inside META-INF/services/, finds the file named after the ConfigurablePropagatorProvider interface, reads the single line of text, and instantiates your CustomContextPropagatorProvider.java to register the custom Auth logic.

### Build the Extension:
- Run `mvn clean package` to generate your lightweight .jar (e.g., cs-otel-extension.jar).


### Infrastructure Setup:
- Once we compile the project using `mvn clean package`, we will get a file named something like [java-otel-1.0.0.jar](target/java-otel-1.0.0.jar).
- This phase must happen before the developers' applications boot up.
- Drops two files onto the EC2 server:
- [opentelemetry-javaagent.jar]() (The official agent)
- [java-otel-1.0.0.jar](target/java-otel-1.0.0.jar) (The custom code)

### Environment Configuration:
- Instruct the JVM & the OTel Agent on how to behave by setting global OS environment variables.
- Set the OS Environment variables for the application:
  - Link the Extension
  - Activate the Propagator
  - Attach the Agent to the JVM

  Commands:
     `Tell official agent to load your custom extension`
  - export OTEL_JAVAAGENT_EXTENSIONS=${HOME_PATH}/[java-otel-1.0.0.jar](target/java-otel-1.0.0.jar)
     `Tell OTel to use standard tracing & baggage, but custom propagator`
  - export OTEL_PROPAGATORS="tracecontext,baggage,java-propagator"
     `Java app starts on this server, it auto pulls in the OTel agent Tell JVM to start the OTel Agent before the main application starts.`
  - java -javaagent:${HOME_PATH}/[java-otel-1.0.0.jar](target/java-otel-1.0.0.jar) -jar [java-otel-test-1.0.0.jar](../java-otel-test/target/java-otel-test-1.0.0.jar)
- By doing this, the standard agent boots up, reads the extension, finds the META-INF file, loads the custom extract/inject logic, and begins silently swapping Authorization to New-Authorization on every single HTTP and SQS call. The developers never write a single line of trace code.


### Run Locally:
- Package custom otel [java-otel-1.0.0.jar](target/java-otel-1.0.0.jar)
- Package spring boot service [java-otel-test-1.0.0.jar](../java-otel-test/target/java-otel-test-1.0.0.jar)
- Download the javaagent jar [opentelemetry-javaagent.jar]()
- Drop all 3 jars in one folder and run below command
- env OTEL_JAVAAGENT_EXTENSIONS=[java-otel-1.0.0.jar](target/java-otel-1.0.0.jar) \
    OTEL_PROPAGATORS="tracecontext,baggage,java-propagator" \
    OTEL_TRACES_EXPORTER=none \
    OTEL_METRICS_EXPORTER=none \
    OTEL_LOGS_EXPORTER=none \
    java -javaagent:[opentelemetry-javaagent.jar]() -jar [java-otel-test-1.0.0.jar](../java-otel-test/target/java-otel-test-1.0.0.jar)
