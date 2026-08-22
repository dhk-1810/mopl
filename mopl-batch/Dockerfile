# 1. 빌드 스테이지
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

# Gradle 래퍼 및 빌드 설정 파일 복사
COPY gradlew .
COPY gradle gradle
COPY build.gradle settings.gradle ./
COPY mopl-batch/build.gradle mopl-batch/
COPY mopl-content/build.gradle mopl-content/
COPY mopl-dm/build.gradle mopl-dm/
COPY mopl-gateway/build.gradle mopl-gateway/
COPY mopl-image/build.gradle mopl-image/
COPY mopl-notification/build.gradle mopl-notification/
COPY mopl-playlist/build.gradle mopl-playlist/
COPY mopl-user/build.gradle mopl-user/
COPY mopl-watching-session/build.gradle mopl-watching-session/

# 의존성 다운로드 캐싱
RUN chmod +x ./gradlew && ./gradlew :mopl-batch:dependencies --no-daemon

# 소스 코드 복사 및 애플리케이션 빌드
COPY mopl-batch/src mopl-batch/src
RUN ./gradlew :mopl-batch:bootJar -x test --no-daemon

# 2. 실행 스테이지
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY --from=builder /app/mopl-batch/build/libs/*.jar app.jar

EXPOSE 8088

ENTRYPOINT ["java", "-jar", "app.jar"]
