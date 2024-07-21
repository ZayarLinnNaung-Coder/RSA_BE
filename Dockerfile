FROM openjdk:17-jdk-slim as build

#Information around who maintains the image
MAINTAINER ZAYARLINNNAUNG

# Add the application's jar to the container
COPY target/rsa-0.0.1-SNAPSHOT.jar rsa.jar

EXPOSE 8080

#execute the application
ENTRYPOINT ["java","-jar","/rsa.jar"]