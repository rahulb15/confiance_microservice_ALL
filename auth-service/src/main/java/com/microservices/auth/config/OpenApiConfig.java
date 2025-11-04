package com.microservices.auth.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Authentication Service API",
                version = "1.0.0",
                description = "JWT Authentication and Authorization Service",
                contact = @Contact(
                        name = "Development Team",
                        email = "dev-team@microservices.com"
                )
        ),
        servers = {
                @Server(url = "http://localhost:8081", description = "Development server"),
                @Server(url = "http://localhost:8080", description = "API Gateway")
        }
)
@SecurityScheme(
        name = "Bearer Authentication",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        scheme = "bearer"
)
public class OpenApiConfig {
}
