FROM openjdk:17-jdk-alpine

COPY build/libs/*.jar security-service.jar

ENTRYPOINT ["java","-jar","/security-service.jar"]