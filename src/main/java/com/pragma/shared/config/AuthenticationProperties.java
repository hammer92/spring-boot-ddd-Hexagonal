package com.pragma.shared.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Configuration properties for authentication middleware.
 * Allows customization of authentication behavior through application properties.
 */
@Data
@Component
@ConfigurationProperties(prefix = "app.auth")
public class AuthenticationProperties {

    /**
     * Name of the HTTP header that contains the Google user ID.
     * Default: "Authorization"
     */
    private String headerName = "Authorization";

    /**
     * List of path patterns where authentication should be applied.
     * Default: ["/api/**"]
     */
    private List<String> includePathPatterns = List.of("/api/**");

    /**
     * List of path patterns that should be excluded from authentication.
     * Default: ["/actuator/**"]
     */
    private List<String> excludePathPatterns = List.of("/actuator/**");
}