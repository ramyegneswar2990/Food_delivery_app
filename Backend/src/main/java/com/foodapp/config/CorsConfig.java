package com.foodapp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;
import java.util.Collections;

/**
 * CORS configuration allowing the React development server (http://localhost:3000)
 * to communicate with this backend.
 *
 * Allows all HTTP methods, all headers, and credentials (for JWT cookies/Authorization headers).
 */
@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration corsConfiguration = new CorsConfiguration();

        // Allow React dev server origin
        corsConfiguration.setAllowedOrigins(Collections.singletonList("http://localhost:3000"));

        // Allow all standard HTTP methods
        corsConfiguration.setAllowedMethods(Arrays.asList(
                "GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS", "HEAD"
        ));

        // Allow all headers (Authorization, Content-Type, etc.)
        corsConfiguration.setAllowedHeaders(Collections.singletonList("*"));

        // Allow credentials (required for Authorization header / JWT)
        corsConfiguration.setAllowCredentials(true);

        // Cache preflight response for 1 hour
        corsConfiguration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfiguration);

        return new CorsFilter(source);
    }
}
