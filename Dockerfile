# Stage 1: Build the application
FROM maven:3.9.2-eclipse-temurin-17 AS build
RUN apt-get update -y
WORKDIR /app
COPY pom.xml .
COPY src .
RUN mvn clean install

# Stage 2: Create the final image with Tomcat and deploy the WAR file
FROM tomcat:9-jre8
WORKDIR /usr/local/tomcat/webapps
RUN rm -rf /usr/local/tomcat/webapps/*
ARG ARTIFACT
COPY --from=build /app/target/*.war ARTIFACT
COPY ${ARTIFACT} /usr/local/tomcat/ROOT.war
EXPOSE 8080
CMD ["catalina.sh","run"]