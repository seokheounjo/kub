# --- Build Stage ---
FROM gradle:8.10.2-jdk17-alpine AS builder
WORKDIR /workspace
COPY . .
RUN gradle clean bootJar --no-daemon

# --- Run Stage ---
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
ENV APP_VERSION=0.0.2
COPY --from=builder /workspace/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
