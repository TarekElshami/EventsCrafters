# Base image for the build container
FROM maven:3.9.6-eclipse-temurin-17 as builder

# Sets the working directory for commands to run
WORKDIR /project

# Copies the project's dependencies
COPY pom.xml /project/

RUN mvn clean verify --fail-never

# Copy project source from local project to compilation container directory
COPY src /project/src

# Compiles the project
RUN mvn package -DskipTests

#################################################
# Base image for the application container
#################################################
FROM openjdk:17.0.2

# Sets the working directory where the JAR is located
WORKDIR /usr/app

# Copies the JAR from the build container
COPY --from=builder /project/target/*.jar /usr/app/ec.jar

# Indicates the port that the container exposes
EXPOSE 8443

# Command to run on docker run
CMD [ "java", "-jar", "ec.jar" ]
