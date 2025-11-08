package com.roadwarnings.narino.config;

import com.roadwarnings.narino.entity.*;
import com.roadwarnings.narino.enums.*;
import com.roadwarnings.narino.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private static final String PASSWORD_HASH = "$2a$10$xXxXxXxXxXxXxXxXxXxXxO";
    private static final String PASTO_CENTRO = "Pasto Centro";
    private static final Double PASTO_LAT = 1.2136;
    private static final Double PASTO_LON = -77.2811;
    
    private final UserRepository userRepository;
    private final AlertRepository alertRepository;
    private final GasStationRepository gasStationRepository;
    private final RouteRepository routeRepository;

    @Override
    public void run(String... args) {
        if (userRepository.count() == 0) {
            log.info("🌱 Inicializando datos de prueba...");
            initializeUsers();
            initializeAlerts();
            initializeGasStations();
            initializeRoutes();
            log.info("✅ Datos de prueba creados exitosamente!");
        } else {
            log.info("ℹ️ La base de datos ya contiene datos.");
        }
    }

    private void initializeUsers() {
        userRepository.save(createUser("admin", "admin@roadwarnings.com", UserRole.ADMIN, "dark"));
        userRepository.save(createUser("moderador", "moderador@roadwarnings.com", UserRole.MODERATOR, "light"));
        userRepository.save(createUser("juan_pasto", "juan@example.com", UserRole.USER, "light"));
        log.info("✅ 3 usuarios creados");
    }

    private User createUser(String username, String email, UserRole role, String theme) {
        return User.builder()
                .username(username)
                .email(email)
                .password(PASSWORD_HASH)
                .role(role)
                .isActive(true)
                .preferredTheme(theme)
                .build();
    }

    private void initializeAlerts() {
        User user = userRepository.findByUsername("juan_pasto").orElseThrow();

        alertRepository.save(Alert.builder()
                .type(AlertType.DERRUMBE)
                .title("Derrumbe en Vía Panamericana")
                .description("Bloqueo total de la vía por derrumbe. Tráfico desviado por ruta alterna.")
                .latitude(PASTO_LAT)
                .longitude(PASTO_LON)
                .location("Pasto - Chachagüí, Km 5")
                .severity(AlertSeverity.CRITICAL)
                .status(AlertStatus.ACTIVE)
                .user(user)
                .upvotes(15)
                .downvotes(1)
                .build());

        alertRepository.save(Alert.builder()
                .type(AlertType.ACCIDENTE)
                .title("Colisión múltiple en Ipiales")
                .description("Choque entre 3 vehículos. Ambulancias en camino.")
                .latitude(0.8247)
                .longitude(-77.6425)
                .location("Ipiales, Sector Las Lajas")
                .severity(AlertSeverity.HIGH)
                .status(AlertStatus.ACTIVE)
                .user(user)
                .upvotes(8)
                .downvotes(0)
                .build());

        alertRepository.save(Alert.builder()
                .type(AlertType.NEBLINA)
                .title("Neblina densa en Túquerres")
                .description("Visibilidad reducida a menos de 20 metros. Conducir con precaución.")
                .latitude(1.0869)
                .longitude(-77.6169)
                .location("Túquerres - Guachucal")
                .severity(AlertSeverity.MEDIUM)
                .status(AlertStatus.ACTIVE)
                .user(user)
                .upvotes(12)
                .downvotes(2)
                .build());

        alertRepository.save(Alert.builder()
                .type(AlertType.TRAFICO_PESADO)
                .title("Congestión en Centro de Pasto")
                .description("Tráfico lento por hora pico. Tiempo estimado: 25 minutos adicionales.")
                .latitude(PASTO_LAT)
                .longitude(PASTO_LON)
                .location("Pasto, Av. Panamericana")
                .severity(AlertSeverity.MEDIUM)
                .status(AlertStatus.ACTIVE)
                .user(user)
                .upvotes(20)
                .downvotes(3)
                .build());

        alertRepository.save(Alert.builder()
                .type(AlertType.OBRAS_VIALES)
                .title("Mantenimiento en Puente Rumichaca")
                .description("Trabajos de reparación en puente. Paso habilitado por un solo carril.")
                .latitude(0.8152)
                .longitude(-77.6450)
                .location("Frontera Colombia-Ecuador")
                .severity(AlertSeverity.LOW)
                .status(AlertStatus.ACTIVE)
                .user(user)
                .upvotes(5)
                .downvotes(0)
                .build());

        alertRepository.save(Alert.builder()
                .type(AlertType.PROTESTA)
                .title("Manifestación en Túquerres")
                .description("Manifestación pacífica de agricultores. Vía bloqueada parcialmente.")
                .latitude(1.0869)
                .longitude(-77.6169)
                .location("Túquerres, entrada norte")
                .severity(AlertSeverity.HIGH)
                .status(AlertStatus.ACTIVE)
                .user(user)
                .upvotes(18)
                .downvotes(4)
                .build());

        alertRepository.save(Alert.builder()
                .type(AlertType.POLICIA)
                .title("Control de Tránsito")
                .description("Policía de carreteras realizando control de documentos y alcoholemia.")
                .latitude(1.1500)
                .longitude(-77.3000)
                .location("Pasto - Catambuco")
                .severity(AlertSeverity.LOW)
                .status(AlertStatus.ACTIVE)
                .user(user)
                .upvotes(10)
                .downvotes(1)
                .build());

        alertRepository.save(Alert.builder()
                .type(AlertType.VEHICULO_VARADO)
                .title("Bus averiado en carril derecho")
                .description("Bus de servicio público averiado. Carril derecho ocupado.")
                .latitude(1.1800)
                .longitude(-77.2900)
                .location("Vía Pasto - Cali, Km 12")
                .severity(AlertSeverity.MEDIUM)
                .status(AlertStatus.ACTIVE)
                .user(user)
                .upvotes(7)
                .downvotes(0)
                .build());

        log.info("✅ 8 alertas creadas");
    }

    private void initializeGasStations() {
        gasStationRepository.save(GasStation.builder()
                .name("Estación de Servicio El Rosal")
                .brand("Terpel")
                .latitude(PASTO_LAT)
                .longitude(PASTO_LON)
                .address("Av. Panamericana Km 3")
                .municipality("Pasto")
                .phoneNumber("+57 2 7331234")
                .hasGasoline(true)
                .hasDiesel(true)
                .gasolinePrice(new BigDecimal("13500"))
                .dieselPrice(new BigDecimal("11800"))
                .isOpen24Hours(true)
                .openingTime(null)
                .closingTime(null)
                .isAvailable(true)
                .build());

        gasStationRepository.save(GasStation.builder()
                .name("Gasolinera Ipiales Centro")
                .brand("Mobil")
                .latitude(0.8247)
                .longitude(-77.6425)
                .address("Calle 14 con Carrera 6")
                .municipality("Ipiales")
                .phoneNumber("+57 2 7731122")
                .hasGasoline(true)
                .hasDiesel(true)
                .gasolinePrice(new BigDecimal("13300"))
                .dieselPrice(new BigDecimal("11600"))
                .isOpen24Hours(false)
                .openingTime("06:00")
                .closingTime("22:00")
                .isAvailable(true)
                .build());

        gasStationRepository.save(GasStation.builder()
                .name("Estación Las Lajas")
                .brand("Esso")
                .latitude(0.8500)
                .longitude(-77.6200)
                .address("Vía Ipiales - Santuario Las Lajas")
                .municipality("Ipiales")
                .phoneNumber("+57 2 7739999")
                .hasGasoline(true)
                .hasDiesel(true)
                .gasolinePrice(new BigDecimal("13400"))
                .dieselPrice(new BigDecimal("11700"))
                .isOpen24Hours(true)
                .openingTime(null)
                .closingTime(null)
                .isAvailable(true)
                .build());

        log.info("✅ 3 estaciones de gasolina creadas");
    }

    private void initializeRoutes() {
        routeRepository.save(Route.builder()
                .name("Pasto - Ipiales")
                .originLatitude(PASTO_LAT)
                .originLongitude(PASTO_LON)
                .originName(PASTO_CENTRO)
                .destinationLatitude(0.8247)
                .destinationLongitude(-77.6425)
                .destinationName("Ipiales Centro")
                .distanceKm(82.5)
                .estimatedTimeMinutes(120)
                .activeAlertsCount(2)
                .isActive(true)
                .build());

        routeRepository.save(Route.builder()
                .name("Pasto - Tumaco")
                .originLatitude(PASTO_LAT)
                .originLongitude(PASTO_LON)
                .originName(PASTO_CENTRO)
                .destinationLatitude(1.8047)
                .destinationLongitude(-78.7617)
                .destinationName("Tumaco Puerto")
                .distanceKm(285.0)
                .estimatedTimeMinutes(420)
                .activeAlertsCount(3)
                .isActive(true)
                .build());

        routeRepository.save(Route.builder()
                .name("Pasto - Cali")
                .originLatitude(PASTO_LAT)
                .originLongitude(PASTO_LON)
                .originName(PASTO_CENTRO)
                .destinationLatitude(3.4516)
                .destinationLongitude(-76.5320)
                .destinationName("Cali Centro")
                .distanceKm(392.0)
                .estimatedTimeMinutes(540)
                .activeAlertsCount(1)
                .isActive(true)
                .build());

        log.info("✅ 3 rutas creadas");
    }
}