FROM openjdk:17-oracle
MAINTAINER Ahmet
COPY tgbot_backend_docker.jar tgbackdocker.jar
ENTRYPOINT ["java", "-jar", "/tgbackdocker.jar"]
