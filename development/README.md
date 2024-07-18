# Development Environment Setup

This document provides instructions on setting up the development environment for the OpenMRS-REDCap Integration Project. This project uses Java, Maven, and Spring Boot, and it integrates OpenMRS with REDCap via a Camel routes.

## Prerequisites

Before you begin, ensure you have the following installed on your system:  
* Java JDK 17
* Maven
* Docker and Docker Compose
* IntelliJ IDEA (or your preferred IDE)

## Environment Configuration

1. **Clone the Repository:** Start by cloning the project repository to your local machine.  <pre>git clone https://github.com/CDC-HIS/eip-openmrs-redcap.git && cd eip-openmrs-redcap </pre>
2. **Configure Environment Variables:**  The project uses environment variables for configuration. Copy the `.env.example` file to a new file named `.env` in the development directory and update it with your local settings.  <pre>cp development/.env.example development/.env </pre> Edit the `development/.env` file to match your local development environment settings, such as database credentials, OpenMRS server URL, and REDCap API details.  
3. **Build the Project:**  Use Maven to build the project. This step compiles the Java code and packages it into a JAR file.  <pre>mvn clean install </pre>
4. **Start `eip-openmrs-redcap` service with Docker Compose:**  To navigate to development directory and start the service, run the following command.  <pre>cd development && docker-compose up -d </pre> This command starts the `eip-openmrs-redcap` container, mounting the generated JAR file and applying the environment variables defined in the `.env` file.  
5. **Verify the Setup:**  After starting the services, verify that the `eip-openmrs-redcap` is running correctly and able to connect to both OpenMRS and REDCap. Check the Docker container logs for any errors.  <pre>docker logs ozone-eip-openmrs-redcap </pre>

## Development Workflow

1. **Code Changes:** Make changes to the codebase using IntelliJ IDEA or your preferred IDE.
2. **Testing:** Run tests locally using Maven to ensure your changes do not break existing functionality. `mvn test`
3. **Rebuild & Restart service:** 
   After making changes, rebuild the project and restart the Docker service to apply the changes. 
   ```bash 
   mvn clean install && docker-compose restart eip-openmrs-redcap
    ```
4. **Debugging:** Use the IDE's debugging tools to troubleshoot issues and step through the code.
