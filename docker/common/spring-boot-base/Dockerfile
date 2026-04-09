# syntax = docker/dockerfile:1
FROM debian:bullseye

# Install wget
RUN --mount=type=cache,id=aerius-apt-cache-debian,mode=0755,sharing=locked,target=/var/cache/apt \
    --mount=type=cache,id=aerius-apt-lib-debian,mode=0755,sharing=locked,target=/var/lib/apt \
    apt update \
    && apt install -y wget

# Get java from eclipse-temurin image (could perhaps do something with jlink later on)
ENV JAVA_HOME=/opt/java/openjdk
COPY --from=eclipse-temurin:21 $JAVA_HOME $JAVA_HOME
ENV PATH "${JAVA_HOME}/bin:${PATH}"

# Set defaults
ENV JAVA_OPTIONS="-XX:MaxRAMPercentage=75"

# Copy the actual application
COPY app.jar /

# Add the opentelemetry agent to the root of the container
RUN wget -qO /opentelemetry-javaagent.jar https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/download/v1.20.2/opentelemetry-javaagent.jar

# Java tool options won't be set by default: if needed, this can be set by setting JAVA_TOOL_OPTIONS='-javaagent:/opentelemetry-javaagent.jar'
# We might want to do this in a startup script

# Set up a non root account called aerius (debian based)
RUN groupadd -r aerius && useradd -r -g aerius aerius

USER aerius

# Set working directory to a directory a non-root user will have access to
WORKDIR /tmp
