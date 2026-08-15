# Build stage
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app
COPY gradlew .
COPY gradle gradle
COPY build.gradle settings.gradle ./
COPY mopl-app mopl-app
COPY mopl-batch mopl-batch
COPY mopl-chat mopl-chat
COPY mopl-common mopl-common
COPY mopl-composite mopl-composite
COPY mopl-content mopl-content
COPY mopl-notification mopl-notification
COPY mopl-playlist mopl-playlist
COPY mopl-user mopl-user

RUN chmod +x gradlew
RUN ./gradlew :mopl-app:bootJar -x test --no-daemon

# Run stage
FROM eclipse-temurin:21-jre-alpine AS runner
WORKDIR /app
COPY --from=builder /app/mopl-app/build/libs/*-SNAPSHOT.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]

