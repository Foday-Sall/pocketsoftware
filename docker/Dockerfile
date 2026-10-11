# PocketJava engine image: JRE only, the fat jar is built on the dev machine.
FROM eclipse-temurin:25-jre

WORKDIR /app
COPY target/pocketsoftware.jar /app/pocketsoftware.jar

# GOWA webhook port (POST /gowa).
EXPOSE 8085

ENTRYPOINT ["java", "-jar", "/app/pocketsoftware.jar"]
