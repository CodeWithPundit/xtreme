FROM openjdk:17-jdk-slim AS build

WORKDIR /app

# Copy gradle files
COPY gradlew .
COPY gradle gradle
COPY build.gradle.kts .
COPY settings.gradle.kts .
COPY gradle.properties .

# Copy source code
COPY app app
COPY core core
COPY feature feature
COPY service service
COPY build-logic build-logic

# Make gradlew executable
RUN chmod +x gradlew

# Build the app
RUN ./gradlew assembleRelease --no-daemon

# Runtime stage
FROM alpine:latest

RUN apk add --no-cache bash

COPY --from=build /app/app/build/outputs/apk/release /apk

CMD ["bash"]
