package com.example.NotsHub.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${frontend.url}")
    String frontEndUrl;

    @Value("${frontend.allowed-origins:}")
    String additionalAllowedOrigins;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        String normalizedFrontendUrl = frontEndUrl == null ? "" : frontEndUrl.trim().replaceAll("/+$", "");
        List<String> allowedOrigins = new ArrayList<>();
        if (!normalizedFrontendUrl.isBlank()) {
            allowedOrigins.add(normalizedFrontendUrl);
        }
        if (additionalAllowedOrigins != null && !additionalAllowedOrigins.isBlank()) {
            allowedOrigins.addAll(Arrays.stream(additionalAllowedOrigins.split(","))
                    .map(String::trim)
                    .map(origin -> origin.replaceAll("/+$", ""))
                    .filter(origin -> !origin.isBlank())
                    .collect(Collectors.toList()));
        }

        registry.addMapping("/**")
                .allowedOrigins(allowedOrigins.toArray(new String[0]))
                .allowedMethods("GET","POST","PUT","PATCH","DELETE","OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}
