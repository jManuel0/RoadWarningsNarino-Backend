package com.roadwarnings.narino.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Configuración de caché con Redis
 */
@Configuration
@EnableCaching
public class CacheConfig {

    // Nombres de los cachés
    public static final String ALERTS_CACHE = "alerts";
    public static final String ALERTS_ACTIVE_CACHE = "alerts:active";
    public static final String ALERTS_NEARBY_CACHE = "alerts:nearby";
    public static final String USER_CACHE = "users";
    public static final String USER_STATS_CACHE = "user:stats";
    public static final String GAS_STATIONS_CACHE = "gasStations";
    public static final String GAS_STATIONS_NEARBY_CACHE = "gasStations:nearby";
    public static final String ROUTES_CACHE = "routes";
    public static final String LEADERBOARD_CACHE = "leaderboard";
    public static final String WEATHER_CACHE = "weather";
    public static final String WEATHER_FORECAST_CACHE = "weather-forecast";
    public static final String TRAFFIC_CACHE = "traffic";

    @Bean
    CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        // Configurar ObjectMapper para serialización JSON
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.activateDefaultTyping(
                BasicPolymorphicTypeValidator.builder()
                        .allowIfSubType(Object.class)
                        .build(),
                ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.PROPERTY
        );

        GenericJackson2JsonRedisSerializer jsonSerializer =
                new GenericJackson2JsonRedisSerializer(objectMapper);

        // Configuración por defecto del caché
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(10)) // TTL por defecto: 10 minutos
                .serializeKeysWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer())
                )
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(jsonSerializer)
                )
                .disableCachingNullValues();

        // Configuraciones específicas por caché
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();

        // Alertas activas - TTL corto (2 minutos) porque cambian frecuentemente
        cacheConfigurations.put(ALERTS_ACTIVE_CACHE,
                defaultConfig.entryTtl(Duration.ofMinutes(2)));

        // Alertas cercanas - TTL corto (3 minutos)
        cacheConfigurations.put(ALERTS_NEARBY_CACHE,
                defaultConfig.entryTtl(Duration.ofMinutes(3)));

        // Alertas individuales - TTL medio (5 minutos)
        cacheConfigurations.put(ALERTS_CACHE,
                defaultConfig.entryTtl(Duration.ofMinutes(5)));

        // Usuarios - TTL largo (30 minutos) porque no cambian frecuentemente
        cacheConfigurations.put(USER_CACHE,
                defaultConfig.entryTtl(Duration.ofMinutes(30)));

        // Estadísticas de usuario - TTL medio (15 minutos)
        cacheConfigurations.put(USER_STATS_CACHE,
                defaultConfig.entryTtl(Duration.ofMinutes(15)));

        // Estaciones de gasolina - TTL muy largo (1 hora)
        cacheConfigurations.put(GAS_STATIONS_CACHE,
                defaultConfig.entryTtl(Duration.ofHours(1)));

        // Estaciones cercanas - TTL medio (10 minutos)
        cacheConfigurations.put(GAS_STATIONS_NEARBY_CACHE,
                defaultConfig.entryTtl(Duration.ofMinutes(10)));

        // Rutas - TTL largo (30 minutos)
        cacheConfigurations.put(ROUTES_CACHE,
                defaultConfig.entryTtl(Duration.ofMinutes(30)));

        // Leaderboard - TTL corto (5 minutos)
        cacheConfigurations.put(LEADERBOARD_CACHE,
                defaultConfig.entryTtl(Duration.ofMinutes(5)));

        // Clima - TTL medio (30 minutos) - los datos meteorológicos no cambian muy rápido
        cacheConfigurations.put(WEATHER_CACHE,
                defaultConfig.entryTtl(Duration.ofMinutes(30)));

        // Pronóstico del clima - TTL largo (1 hora)
        cacheConfigurations.put(WEATHER_FORECAST_CACHE,
                defaultConfig.entryTtl(Duration.ofHours(1)));

        // Tráfico - TTL muy corto (5 minutos) porque las condiciones cambian rápido
        cacheConfigurations.put(TRAFFIC_CACHE,
                defaultConfig.entryTtl(Duration.ofMinutes(5)));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigurations)
                .build();
    }
}
