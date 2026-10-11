package com.ford.vehicleapi.security;

import org.springframework.context.annotation.Bean;
import org.springframework.security.config.Customizer;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.PATCH;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpMethod.PUT;

/**
 * Regras de acesso da API (autorização por URL + método HTTP).
 * Regras que dependem do dono do recurso (ex.: test drive do próprio cliente)
 * ficam na camada de serviço, usando as claims do token.
 */
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private static final String[] PUBLIC_DOCS = {"/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**"};

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http,
                                            JwtAuthenticationConverter jwtAuthenticationConverter,
                                            RestAuthenticationEntryPoint authenticationEntryPoint,
                                            RestAccessDeniedHandler accessDeniedHandler) throws Exception {
        http
                // API stateless com Bearer token: sem sessão, sem cookie, logo sem CSRF.
                .csrf(AbstractHttpConfigurer::disable)
                // Usa o CorsConfigurationSource de config/CorsConfig (preflight respondido antes da autenticação).
                .cors(Customizer.withDefaults())
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // ---- Públicos ----
                        .requestMatchers(POST, "/api/v1/auth/login", "/api/v1/auth/register").permitAll()
                        .requestMatchers(GET, "/api/v1/models", "/api/v1/models/**").permitAll()
                        .requestMatchers(GET, "/api/v1/fipe/**").permitAll()
                        .requestMatchers(PUBLIC_DOCS).permitAll()
                        .requestMatchers("/error").permitAll()
                        // ---- Catálogo: escrita ----
                        .requestMatchers(POST, "/api/v1/models", "/api/v1/models/**").hasAnyRole("ADMIN", "DEALER")
                        .requestMatchers(PUT, "/api/v1/models/**").hasAnyRole("ADMIN", "DEALER")
                        .requestMatchers(DELETE, "/api/v1/models/**").hasRole("ADMIN")
                        // ---- Test drives ----
                        .requestMatchers(POST, "/api/v1/test-drives").hasRole("CUSTOMER")
                        .requestMatchers(PATCH, "/api/v1/test-drives/*/status").hasAnyRole("ADMIN", "DEALER")
                        .requestMatchers("/api/v1/test-drives", "/api/v1/test-drives/**").authenticated()
                        // ---- Administração de usuários ----
                        .requestMatchers("/api/v1/users", "/api/v1/users/**").hasRole("ADMIN")
                        // ---- Qualquer outra rota exige token ----
                        .anyRequest().authenticated())
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter))
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler))
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler));
        return http.build();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
