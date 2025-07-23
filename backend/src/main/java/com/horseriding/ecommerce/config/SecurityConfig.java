package com.horseriding.ecommerce.config;

import com.horseriding.ecommerce.auth.JwtAuthenticationEntryPoint;
import com.horseriding.ecommerce.auth.JwtAuthenticationFilter;
import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * Spring Security configuration for JWT-based authentication and authorization.
 * Configures security filters, CORS, and access control rules.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * Configures the security filter chain with JWT authentication.
     *
     * @param http the HttpSecurity configuration
     * @return the configured SecurityFilterChain
     * @throws Exception if configuration fails
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // Disable CSRF as we're using JWT tokens
                .csrf(AbstractHttpConfigurer::disable)

                // Configure CORS
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // Configure session management - stateless for JWT
                .sessionManagement(
                        session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Configure authentication entry point
                .exceptionHandling(
                        exceptions ->
                                exceptions.authenticationEntryPoint(jwtAuthenticationEntryPoint))

                // Configure authorization rules
                .authorizeHttpRequests(
                        authz ->
                                authz
                                        // Public endpoints - no authentication required
                                        .requestMatchers("/api/auth/**")
                                        .permitAll()
                                        .requestMatchers("/api/public/**")
                                        .permitAll()
                                        .requestMatchers("/actuator/health")
                                        .permitAll()
                                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**")
                                        .permitAll()
                                        .requestMatchers("/favicon.ico", "/error")
                                        .permitAll()

                                        // Public read-only endpoints for products and categories
                                        .requestMatchers(HttpMethod.GET, "/api/products/**")
                                        .permitAll()
                                        .requestMatchers(HttpMethod.GET, "/api/categories/**")
                                        .permitAll()

                                        // Admin-only endpoints
                                        .requestMatchers(HttpMethod.POST, "/api/products/**")
                                        .hasRole("ADMIN")
                                        .requestMatchers(HttpMethod.PUT, "/api/products/**")
                                        .hasRole("ADMIN")
                                        .requestMatchers(HttpMethod.DELETE, "/api/products/**")
                                        .hasRole("ADMIN")
                                        .requestMatchers(HttpMethod.POST, "/api/categories/**")
                                        .hasRole("ADMIN")
                                        .requestMatchers(HttpMethod.PUT, "/api/categories/**")
                                        .hasRole("ADMIN")
                                        .requestMatchers(HttpMethod.DELETE, "/api/categories/**")
                                        .hasRole("ADMIN")

                                        // Superadmin-only endpoints
                                        .requestMatchers("/api/admin/users/**")
                                        .hasRole("SUPERADMIN")

                                        // All other endpoints require authentication
                                        .anyRequest()
                                        .authenticated())

                // Add JWT authentication filter
                .addFilterBefore(
                        jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Configures CORS settings for frontend integration.
     *
     * @return the CORS configuration source
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Allow specific origins (configure for your frontend URL)
        configuration.setAllowedOriginPatterns(
                Arrays.asList("http://localhost:3000", "http://localhost:3001"));

        // Allow all HTTP methods
        configuration.setAllowedMethods(
                Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));

        // Allow all headers
        configuration.setAllowedHeaders(List.of("*"));

        // Allow credentials (cookies, authorization headers)
        configuration.setAllowCredentials(true);

        // Cache preflight response for 1 hour
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }

    /**
     * Password encoder bean for secure password hashing.
     *
     * @return BCrypt password encoder
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Authentication manager bean for Spring Security.
     *
     * @param config the authentication configuration
     * @return the authentication manager
     * @throws Exception if configuration fails
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
            throws Exception {
        return config.getAuthenticationManager();
    }
}
