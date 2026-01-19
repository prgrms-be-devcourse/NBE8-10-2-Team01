# Use an official JDK runtime as a parent image
FROM openjdk:17-jdk-slim

# Set the working directory
WORKDIR /app

# Copy the build files
COPY build/libs/plog-0.0.1-SNAPSHOT.jar ./app.jar

# Expose the port the application runs on
EXPOSE 4040

# Command to run the application
ENTRYPOINT ["java", "-jar", "app.jar"]