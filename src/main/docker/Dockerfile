FROM openjdk:8-jdk-alpine
VOLUME /tmp
ADD ./ESPractice-0.0.1-SNAPSHOT.jar app.jar
RUN sh -c 'touch /app.jar'
ENV JAVA_OPTS=""
ENTRYPOINT ["java","-Djava.security.egd=file:/prod/./urandom","-jar","/app.jar"]