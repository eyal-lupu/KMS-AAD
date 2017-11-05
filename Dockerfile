FROM openjdk:8u141-jre-slim
MAINTAINER eyal.lupu@gmail.com

EXPOSE 8080
#COPY build/libs/aad-sample-webapp-0.1.0.jar /
ADD target/aad-sample-webapp-0.1.0.jar app.jar

ENV JAVA_OPTS=""
ENTRYPOINT exec java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar /app.jar
