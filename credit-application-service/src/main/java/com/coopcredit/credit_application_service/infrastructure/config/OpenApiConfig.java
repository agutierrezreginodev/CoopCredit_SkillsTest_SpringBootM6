package com.coopcredit.credit_application_service.infrastructure.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.models.OpenAPI;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración de OpenAPI/Swagger
 */
@Configuration
@OpenAPIDefinition(
    info = @Info(
        title = "CoopCredit API",
        version = "1.0",
        description = "API REST para el Sistema de Solicitudes de Crédito de CoopCredit",
        contact = @Contact(
            name = "CoopCredit Development Team",
            email = "dev@coopcredit.com"
        )
    ),
    servers = {
        @Server(url = "http://localhost:8080", description = "Servidor Local"),
        @Server(url = "https://credit-application-service-qzh1.onrender.com", description = "Servidor de Producción (Render)")
    }
)
@SecurityScheme(
    name = "Bearer Authentication",
    type = SecuritySchemeType.HTTP,
    bearerFormat = "JWT",
    scheme = "bearer"
)
public class OpenApiConfig {

    @Value("${server.url:}")
    private String serverUrl;

    @Bean
    public OpenAPI customOpenAPI() {
        OpenAPI openAPI = new OpenAPI();
        
        // Si hay un server URL configurado (desde variable de entorno), lo usamos
        if (serverUrl != null && !serverUrl.isEmpty()) {
            openAPI.addServersItem(new io.swagger.v3.oas.models.servers.Server()
                .url(serverUrl)
                .description("Servidor Configurado"));
        }
        
        return openAPI;
    }
}
