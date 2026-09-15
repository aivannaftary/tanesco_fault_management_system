package com.tanesco.faultmanagement.config;

import com.tanesco.faultmanagement.security.JwtAuthenticationFilter;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;

import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

import org.springframework.security.config.http.SessionCreationPolicy;

import org.springframework.security.core.userdetails.UserDetailsService;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final UserDetailsService userDetailsService;

    public SecurityConfig(
            JwtAuthenticationFilter jwtAuthenticationFilter,
            UserDetailsService userDetailsService
    ) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http
    ) throws Exception {

        http

                // REST API uses JWT, so CSRF is disabled.
                .csrf(csrf -> csrf.disable())

                // Allow requests from our frontend.
                .cors(cors -> cors.configurationSource(
                        corsConfigurationSource()
                ))

                // JWT authentication is stateless.
                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                .authorizeHttpRequests(auth -> auth

                        // ==============================
                        // PUBLIC ENDPOINTS
                        // ==============================
                       .requestMatchers(
                            "/",
                            "/index.html",
                            "/login.html",
                           "/register.html",

                           "/customer/**",
                           "/technician/**",
                           "/officer/**",
                           "/admin/**",

                          "/css/**",
                          "/js/**",
                          "/images/**",

                         "/api/auth/register",
                          "/api/auth/login"
                         ).permitAll()
                        // ==============================
                        // ADMIN ENDPOINTS
                        // ADMIN ONLY
                        // ==============================
                        .requestMatchers(
                                "/api/admin/**"
                        ).hasRole("ADMIN")

                        // ==============================
                        // STAFF ENDPOINTS
                        // ==============================
                        .requestMatchers(
                                "/api/staff/**"
                        ).hasAnyRole(
                                "ADMIN",
                                "OFFICER",
                                "TECHNICIAN"
                        )

                        // ==============================
                        // FAULT ENDPOINTS
                        // Any authenticated user can
                        // reach these endpoints.
                        //
                        // Service-level authorization
                        // determines which fault they
                        // are actually allowed to use.
                        // ==============================
                        .requestMatchers(
                                "/api/faults/**"
                        ).authenticated()

                        // ==============================
                        // EVERYTHING ELSE
                        // ==============================
                        .anyRequest()
                        .authenticated()
                )

                // Authentication provider used for
                // username/password authentication.
                .authenticationProvider(
                        authenticationProvider()
                )

                // JWT filter must run before Spring's
                // username/password authentication filter.
                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }

    // ==============================================
    // AUTHENTICATION PROVIDER
    // ==============================================

    @Bean
    public AuthenticationProvider authenticationProvider() {

        DaoAuthenticationProvider authenticationProvider =
                new DaoAuthenticationProvider(
                        userDetailsService
                );

        authenticationProvider.setPasswordEncoder(
                passwordEncoder()
        );

        return authenticationProvider;
    }

    // ==============================================
    // AUTHENTICATION MANAGER
    // ==============================================

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration configuration
    ) throws Exception {

        return configuration
                .getAuthenticationManager();
    }

    // ==============================================
    // PASSWORD ENCODER
    // ==============================================

    @Bean
    public PasswordEncoder passwordEncoder() {

        return new BCryptPasswordEncoder();
    }

    // ==============================================
    // CORS CONFIGURATION
    // ==============================================

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration configuration =
                new CorsConfiguration();

        /*
         * Backend / frontend development origins.
         *
         * 8080 -> Spring Boot
         * 5500 -> VS Code Live Server
         */
        configuration.setAllowedOrigins(
                List.of(
                        "http://localhost:8080",
                        "http://127.0.0.1:8080",
                        "http://localhost:5500",
                        "http://127.0.0.1:5500"
                )
        );

        configuration.setAllowedMethods(
                List.of(
                        "GET",
                        "POST",
                        "PUT",
                        "PATCH",
                        "DELETE",
                        "OPTIONS"
                )
        );

        configuration.setAllowedHeaders(
                List.of(
                        "Authorization",
                        "Content-Type"
                )
        );

        configuration.setExposedHeaders(
                List.of(
                        "Authorization"
                )
        );

        configuration.setAllowCredentials(
                true
        );

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration(
                "/**",
                configuration
        );

        return source;
    }
}