# Step 1: Use a lightweight Java runtime image
FROM eclipse-temurin:21-jre

# Step 2: Create a directory for our app and agents
WORKDIR /java-otel

# Step 3: Copy ALL jars from the local folder to the container's /java-otel/ folder
COPY java-jars/*.jar ./

# Step 4: Set the OTel environment variables
ENV OTEL_JAVAAGENT_EXTENSIONS=/java-otel/otel-1.0.0.jar
ENV OTEL_PROPAGATORS="tracecontext,baggage,java-propagator"
ENV OTEL_TRACES_EXPORTER=none
ENV OTEL_METRICS_EXPORTER=none
ENV OTEL_LOGS_EXPORTER=none

# Step 5: Start the application with the agent attached
ENTRYPOINT ["java", "-javaagent:/java-otel/opentelemetry-javaagent.jar", "-jar", "test-1.0.0.jar"]
