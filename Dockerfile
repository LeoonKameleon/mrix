FROM maven:3.9.9-eclipse-temurin-23-alpine AS builder
WORKDIR /app
COPY . .
RUN mvn clean package

FROM eclipse-temurin:23-jre-alpine
WORKDIR /app

RUN addgroup -S mrixgroup && adduser -S mrixuser -G mrixgroup
USER mrixuser

COPY --from=builder /app/target/mrix.jar /app/mrix.jar

ENTRYPOINT [ "java", "-jar", "mrix.jar" ]