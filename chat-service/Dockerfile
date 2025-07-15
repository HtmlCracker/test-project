FROM openjdk:17-jdk-alpine

COPY build/libs/*.jar chat-service.jar

ENTRYPOINT ["java","-jar","/chat-service.jar"]