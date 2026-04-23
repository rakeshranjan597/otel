#!/bin/bash

alias docker=podman
IMAGE_NAME=java-otel-app

# Build the Docker Image
docker build --tls-verify=false -f Dockerfile -t $IMAGE_NAME .

# Run the Container
docker run -p 8081:8080 $IMAGE_NAME

# Change Spring Boot to listen on 8081
# docker run -p 8081:8081 -e SERVER_PORT=8081 $IMAGE_NAME


# docker build: Tells Docker to look at a Dockerfile and start creating an image.
# -t shield-app: The Tag flag. This gives your image a human-readable name (shield-app) so you don't have to remember a long random ID like a1b2c3d4. You can also add a version like shield-app:1.0.0.
# . (The Dot): This is the Build Context. It tells Docker to look for the Dockerfile and all the files it needs to COPY (like your JARs) in the current directory.


# docker run: This command creates a new, isolated container based on your shield-app image and starts it.
# -d: Detached mode. This runs the container in the background. Without this, your terminal will be "stuck" showing the logs, and if you close the terminal, the app will stop.
# -p 8081:8081: Port Mapping.
  # The first 8081 is your Mac's port.
  # The second 8080 is the Container's port.
  # Analogy: It’s like a phone extension. When you call localhost:8081 on your laptop, Docker "forwards" that call to the app inside the box.
# --name shield-container: Gives the running instance a specific name. This makes it easier to run commands like docker logs shield-container or docker stop shield-container.
# shield-app: The name of the image you built in the first step.