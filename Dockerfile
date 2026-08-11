# syntax=docker/dockerfile:1

# ---- Frontend build ----
FROM node:22-alpine AS frontend-build
WORKDIR /workspace
COPY frontend/package.json frontend/package-lock.json ./frontend/
RUN cd frontend && npm ci
COPY frontend ./frontend
COPY src ./src
RUN cd frontend && npm run build

# ---- Backend build ----
FROM eclipse-temurin:21-jdk AS backend-build
WORKDIR /workspace
COPY gradlew build.gradle settings.gradle ./
COPY gradle ./gradle
RUN chmod +x gradlew
COPY src ./src
COPY --from=frontend-build /workspace/src/main/resources/static ./src/main/resources/static
RUN ./gradlew bootJar --no-daemon -x test

# ---- Runtime ----
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=backend-build /workspace/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
