FROM openjdk:17-jdk-alpine

COPY build/libs/*.jar gateway.jar

ENTRYPOINT ["java","-jar","/gateway.jar"]