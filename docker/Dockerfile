#########################
#  ANGULAR COMPILATION  #
#########################
# Base image to build the Angular project
FROM node:18.13 AS angular

# Choose container working directory where we are going to execute commands
WORKDIR /code

# Copy project dependencies from local project to compilation container directory
COPY ProjectFrontend/package*.json ProjectFrontend/angular.json ProjectFrontend/tsconfig*.json /code/

# Execute command in compilation container to download project dependencies
RUN npm install

# Copy project source from local project to compilation container directory
COPY ProjectFrontend/src /code/src

# Compile project using already downloaded libraries configurating the base href
RUN npm run build -- --configuration production --base-href=/new/

# Base image for the build container
FROM maven:3.9.6-eclipse-temurin-17 as builder

# Sets the working directory for commands to run
WORKDIR /project

# Copies the project's dependencies
COPY Phase2/EventCrafters/pom.xml /project/

RUN mvn clean verify --fail-never

# Copy project source from local project to compilation container directory
COPY Phase2/EventCrafters/src /project/src
COPY --from=angular /code/dist/project-frontend /project/src/main/resources/static/new/

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
