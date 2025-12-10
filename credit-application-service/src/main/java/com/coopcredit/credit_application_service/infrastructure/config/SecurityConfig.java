package com.coopcredit.credit_application_service.infrastructure.config;

import com.coopcredit.credit_application_service.infrastructure.security.filters.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Configuración de Seguridad: JWT, CORS, y control de acceso
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;

    @Value("${cors.allowed-origins}")
    private String allowedOrigins;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .authorizeHttpRequests(auth -> auth
                // Endpoints públicos
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/actuator/**").permitAll()
                .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**").permitAll()
                
                // Endpoints de afiliados
                .requestMatchers(HttpMethod.POST, "/api/affiliates").hasAnyAuthority("ROLE_ADMIN", "ROLE_ANALISTA")
                .requestMatchers(HttpMethod.PUT, "/api/affiliates/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_ANALISTA")
                .requestMatchers(HttpMethod.GET, "/api/affiliates/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_ANALISTA", "ROLE_AFILIADO")
                
                // Endpoints de solicitudes
                .requestMatchers(HttpMethod.POST, "/api/applications").hasAnyAuthority("ROLE_AFILIADO", "ROLE_ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/applications/*/evaluate").hasAnyAuthority("ROLE_ANALISTA", "ROLE_ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/applications/pending").hasAnyAuthority("ROLE_ANALISTA", "ROLE_ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/applications/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_ANALISTA", "ROLE_AFILIADO")
                
                // Todo lo demás requiere autenticación
                .anyRequest().authenticated()
            )
            .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Configurar orígenes permitidos desde application.yml
        if (allowedOrigins.equals("*")) {
            configuration.addAllowedOriginPattern("*");
            configuration.setAllowCredentials(false);
        } else {
            List<String> origins = Arrays.asList(allowedOrigins.split(","));
            configuration.setAllowedOrigins(origins);
            configuration.setAllowCredentials(true);
        }
        
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setExposedHeaders(Arrays.asList("Authorization", "Content-Type"));
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
