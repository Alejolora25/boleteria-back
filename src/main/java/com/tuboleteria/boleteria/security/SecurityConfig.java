package com.tuboleteria.boleteria.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource())) // Usa la configuración recomendada
            .csrf(csrf -> csrf.disable()) // Desactiva CSRF para pruebas
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll() // Permitir acceso sin autenticación
                .requestMatchers("/api/usuarios").hasRole("ADMIN") // Solo usuarios con rol ADMIN pueden acceder
                .requestMatchers("/api/eventos/**", "/api/boletas/**").hasAnyRole("ADMIN", "USER") // Usuarios autenticados pueden acceder
                .anyRequest().authenticated() // Proteger todo lo demás
            )
            .httpBasic(httpBasic -> httpBasic.realmName("MyApp")) // Configura HTTP Basic con un nombre de realm
            .logout(logout -> logout
                .logoutUrl("/api/auth/logout") // Define el endpoint para logout
                .logoutSuccessHandler((request, response, authentication) -> {
                    response.setStatus(200); // Responder con 200 OK
                    response.getWriter().write("Logout exitoso.");
                })
            );
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.addAllowedOriginPattern("*"); // Cambiar "*" por dominios específicos en producción
        configuration.addAllowedMethod("*");
        configuration.addAllowedHeader("*");
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(); // Proveer un encriptador para contraseñas
    }
}
