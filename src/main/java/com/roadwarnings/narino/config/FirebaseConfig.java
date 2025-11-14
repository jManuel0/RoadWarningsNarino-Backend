package com.roadwarnings.narino.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import jakarta.annotation.PostConstruct;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

@Configuration
@Slf4j
public class FirebaseConfig {

    @Value("${firebase.config.file:firebase-service-account.json}")
    private String firebaseConfigPath;

    @PostConstruct
    public void initialize() {
        try {
            if (FirebaseApp.getApps().isEmpty()) {
                InputStream serviceAccount;

                try {
                    // Intenta cargar desde classpath primero
                    serviceAccount = new ClassPathResource(firebaseConfigPath).getInputStream();
                    log.info("Firebase config cargado desde classpath");
                } catch (Exception e) {
                    // Si no está en classpath, intenta cargar desde filesystem
                    try {
                        serviceAccount = new FileInputStream(firebaseConfigPath);
                        log.info("Firebase config cargado desde filesystem");
                    } catch (Exception ex) {
                        log.warn("No se encontró archivo de configuración de Firebase. Las notificaciones push no estarán disponibles.");
                        log.warn("Para habilitar notificaciones push, configura: {}", firebaseConfigPath);
                        return;
                    }
                }

                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                        .build();

                FirebaseApp.initializeApp(options);
                log.info("Firebase inicializado correctamente");
            }
        } catch (IOException e) {
            log.error("Error al inicializar Firebase: {}", e.getMessage());
            log.warn("Las notificaciones push no estarán disponibles");
        }
    }
}
