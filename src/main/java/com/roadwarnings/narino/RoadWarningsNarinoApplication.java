package com.roadwarnings.narino;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@Slf4j
@SpringBootApplication
public class RoadWarningsNarinoApplication {

    public static void main(String[] args) {
        SpringApplication.run(RoadWarningsNarinoApplication.class, args);

        log.info("\n╔═══════════════════════════════════════════════════════════╗");
        log.info("║          ROADWARNINGS NARIÑO - BACKEND API                ║");
        log.info("║  🚀 Servidor iniciado correctamente                       ║");
        log.info("║  📡 API: http://localhost:8080/api                        ║");
        log.info("║  📚 Swagger: http://localhost:8080/swagger-ui.html         ║");
        log.info("║  🗄️  H2 Console: http://localhost:8080/h2-console          ║");
        log.info("╚═══════════════════════════════════════════════════════════╝\n");
    }
}
