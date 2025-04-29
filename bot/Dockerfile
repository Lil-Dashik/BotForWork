FROM openjdk:17-jdk-slim-buster
WORKDIR /app
COPY target/bot-1.0.0-SNAPSHOT.jar /app/bot.jar
ENTRYPOINT ["java","-jar", "bot.jar"]