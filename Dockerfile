FROM openjdk:9.0.1-11-jre-slim
MAINTAINER eyal.lupu@gmail.com

EXPOSE 8080
RUN adduser --disabled-login -u 1000 appuser
USER appuser

ADD target/aad-sample-webapp-0.1.0.jar app.jar

ENV JAVA_OPTS=""
ENTRYPOINT exec java $JAVA_OPTS --add-modules java.xml.bind -Djava.security.egd=file:/dev/./urandom -jar /app.jar
