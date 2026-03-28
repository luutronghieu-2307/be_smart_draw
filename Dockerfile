# Base image with NVIDIA CUDA support
FROM nvidia/cuda:12.4.1-runtime-ubuntu22.04

ENV DEBIAN_FRONTEND=noninteractive

# Install Java 21 LTS, Maven and build dependencies
RUN apt-get update && apt-get install -y \
    openjdk-21-jdk \
    maven \
    wget \
    curl \
    && rm -rf /var/lib/apt/lists/*

# Install GStreamer 1.26.x and plugins
RUN apt-get update && apt-get install -y \
    libgstreamer1.0-dev \
    libgstreamer-plugins-base1.0-dev \
    gstreamer1.0-plugins-base \
    gstreamer1.0-plugins-good \
    gstreamer1.0-plugins-bad \
    gstreamer1.0-libav \
    && rm -rf /var/lib/apt/lists/*

# Install OpenCV 4.12.0 native libs
RUN apt-get update && apt-get install -y \
    libopencv-dev \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app

# Copy pom.xml first to cache dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy assets (models, classes.txt)
COPY assets ./assets

# Copy the source code
COPY src ./src

# Expose Spring Boot port
EXPOSE 8080

# Run the application
CMD ["mvn", "spring-boot:run"]