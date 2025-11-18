package com.roadwarnings.narino.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    OpenAPI roadWarningsOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("RoadWarnings Nariño API")
                        .description("""
                                # API de RoadWarnings Nariño

                                Sistema colaborativo de alertas viales para Nariño, Colombia.

                                ## Características
                                - 🚨 Gestión de alertas viales en tiempo real
                                - 👥 Sistema de usuarios con reputación y badges
                                - 🔔 Notificaciones inteligentes por rutas favoritas
                                - 📊 Analytics y métricas del sistema
                                - 🌦️ Integración con clima y tráfico
                                - 🛡️ Rate limiting y seguridad RBAC

                                ## Autenticación
                                Usa JWT en header: `Authorization: Bearer {token}`

                                ## Rate Limits
                                - Anónimos: 20 req/min
                                - Autenticados: 100 req/min
                                - Alertas: 5/hora
                                - Comentarios: 10/hora
                                """)
                        .version("2.0.0")
                        .contact(new Contact()
                                .name("RoadWarnings Team")
                                .email("support@roadwarnings.co"))
                        .license(new License()
                                .name("MIT")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server()
                                .url("https://roadwarnings-narino.onrender.com/api")
                                .description("Producción"),
                        new Server()
                                .url("http://localhost:8080/api")
                                .description("Desarrollo")
                ))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("JWT token del endpoint /auth/login")))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
    }
}
