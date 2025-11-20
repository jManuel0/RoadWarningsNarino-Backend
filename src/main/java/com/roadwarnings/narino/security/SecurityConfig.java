package com.roadwarnings.narino.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(auth -> auth
                        // Preflight CORS - DEBE IR PRIMERO
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // Endpoints abiertos (independiente del método)
                        .requestMatchers(
                                "/auth/**",
                                "/api/auth/**",
                                "/public/**",
                                "/ping",
                                "/",
                                "/v3/api-docs/**",
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/actuator/health",
                                "/h2-console/**",
                                "/api/public/**"
                        ).permitAll()

                        // Endpoints GET que aun así deben ir autenticados
                        .requestMatchers(HttpMethod.GET, "/alert/my-alerts").authenticated()
                        .requestMatchers("/api/favorites/**").authenticated()

                        // Lectura pública (solo GET) de datos
                        .requestMatchers(HttpMethod.GET,
                                // Alertas de tráfico abiertas a invitados
                                "/alert/**",
                                "/api/alert/**",

                                // Datos de rutas y gasolineras
                                "/api/routes/**",
                                "/api/gas-stations/**",

                                // Clima y tráfico
                                "/api/weather/**",
                                "/api/traffic/**",

                                // Comentarios públicos de alertas
                                "/api/comments/**"
                        ).permitAll()

                        // Todo lo demás requiere autenticación (JWT válido)
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }
}
