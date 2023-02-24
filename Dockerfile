FROM openjdk:17
MAINTAINER Zhao
USER root
RUN mkdir -p /opt/chat-gpt-java/conf
COPY ./target/chat-*-SNAPSHOT.jar /opt/chat-gpt-java/app.jar
COPY ./api-key.conf /opt/chat-gpt-java/api-key.conf
WORKDIR /opt/chat-gpt-java
CMD ["--server.port=8080"]
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "-Xms512m", "-Xmx512m", "/opt/chat-gpt-java/app.jar", "--run.env.active=docker"]