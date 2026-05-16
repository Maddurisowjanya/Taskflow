package com.taskflow;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;

/**
 * TaskFlowApplication - Main entry point for the Spring Boot application.
 *
 * @SpringBootApplication is a convenience annotation that combines:
 * - @Configuration: marks this class as a source of bean definitions
 * - @EnableAutoConfiguration: tells Spring Boot to auto-configure beans
 * - @ComponentScan: scans this package and sub-packages for components
 *
 * @OpenAPIDefinition sets up the Swagger UI documentation metadata
 */
@SpringBootApplication
@OpenAPIDefinition(
    info = @Info(
        title = "TaskFlow API",
        version = "1.0.0",
        description = "REST API documentation for the TaskFlow Task & Productivity Management System. " +
                      "Authenticate using the /api/auth/login endpoint to get a JWT token, " +
                      "then click 'Authorize' and enter: Bearer <your_token>",
        contact = @Contact(
            name = "TaskFlow Support",
            email = "support@taskflow.com"
        ),
        license = @License(
            name = "MIT License",
            url = "https://opensource.org/licenses/MIT"
        )
    )
)
public class TaskFlowApplication {

    /**
     * Main method - JVM entry point.
     * SpringApplication.run() bootstraps the Spring Boot app.
     */
    public static void main(String[] args) {
        SpringApplication.run(TaskFlowApplication.class, args);
        System.out.println("====================================================");
        System.out.println("  TaskFlow API is running!");
        System.out.println("  Swagger UI: http://localhost:8080/swagger-ui.html");
        System.out.println("  API Docs:   http://localhost:8080/api-docs");
        System.out.println("====================================================");
    }
}
