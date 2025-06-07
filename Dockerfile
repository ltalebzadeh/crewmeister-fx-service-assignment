FROM openjdk:11-jre-slim
COPY target/cm-coding-challenge-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]
