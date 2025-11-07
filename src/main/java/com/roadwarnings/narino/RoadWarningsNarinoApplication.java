package com.roadwarnings.narino;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class RoadWarningsNarinoApplication {

    public static void main(String[] args) {
        SpringApplication.run(RoadWarningsNarinoApplication.class, args);
        System.out.println("╔═══════════════════════════════════════════════════════════╗");
        System.out.println("║          ROADWARNINGS NARIÑO - BACKEND API                ║");
        System.out.println("║  🚀 Servidor iniciado correctamente                       ║");
        System.out.println("║  📡 API: http://localhost:8080/api                        ║");
        System.out.println("║  📚 Swagger: http://localhost:8080/api/swagger-ui.html   ║");
        System.out.println("║  🗄️  H2 Console: http://localhost:8080/api/h2-console    ║");
        System.out.println("╚═══════════════════════════════════════════════════════════╝");
    }
}
