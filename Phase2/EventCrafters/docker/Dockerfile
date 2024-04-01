# Base image for the build container
FROM maven:3.9.6-eclipse-temurin-17 as builder

# Sets the working directory for commands to run
WORKDIR /project

# Copies the project's dependencies
ARG PROJECT_DIR
COPY ${PROJECT_DIR}/pom.xml /project/

# Downloads the project's dependencies
#RUN mvn clean verify

# Copies the project's code
COPY ${PROJECT_DIR}/src /project/src

# Compiles the project
RUN mvn clean package -DskipTests=true

#################################################
# Base image for the application container
#################################################
FROM openjdk:17.0.2

# Sets the working directory where the JAR is located
WORKDIR /usr/src/app

# Copies the JAR from the build container
COPY --from=builder /project/target/*.jar /usr/src/app/eventcrafters.jar

# Indicates the port that the container exposes
EXPOSE 8443

# Command to run on docker run
CMD [ "java", "-jar", "eventcrafters.jar" ]
