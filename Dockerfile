# Stage 1: Build the application
FROM maven:3.9.2-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Final Tomcat image
FROM tomcat:9-jre8

# Clean existing apps
RUN rm -rf /usr/local/tomcat/webapps/*

# Copy WAR as ROOT.war
COPY --from=build /app/target/*.war /usr/local/tomcat/webapps/ROOT.war

EXPOSE 8080
CMD ["catalina.sh", "run"]

