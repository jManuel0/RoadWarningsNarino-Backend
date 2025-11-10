package com.roadwarnings.narino.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;




@Configuration
public class OpenApiConfig {
 @Bean
    public OpenAPI roadWarningsOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("RoadWarnings Nariño API")
                        .description("Documentación de la API de RoadWarnings Nariño")
                        .version("v1"));
    }   
}
