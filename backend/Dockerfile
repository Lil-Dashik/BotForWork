FROM openjdk:17-jdk-slim-buster
WORKDIR /app
COPY target/backend-1.0.0-SNAPSHOT.jar /app/backend.jar
ENTRYPOINT ["java","-jar", "backend.jar"]