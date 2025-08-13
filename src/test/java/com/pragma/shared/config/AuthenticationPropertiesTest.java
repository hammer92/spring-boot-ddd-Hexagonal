package com.pragma.shared.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = AuthenticationProperties.class)
@EnableConfigurationProperties(AuthenticationProperties.class)
class AuthenticationPropertiesTest {

    @Autowired
    private AuthenticationProperties authenticationProperties;

    @Test
    void shouldHaveDefaultValues() {
        // Then
        assertThat(authenticationProperties.getHeaderName()).isEqualTo("Authorization");
        assertThat(authenticationProperties.getIncludePathPatterns()).containsExactly("/api/**");
        assertThat(authenticationProperties.getExcludePathPatterns()).containsExactly("/actuator/**");
    }

    @SpringBootTest(classes = AuthenticationProperties.class)
    @EnableConfigurationProperties(AuthenticationProperties.class)
    @TestPropertySource(properties = {
            "app.auth.header-name=X-User-ID",
            "app.auth.include-path-patterns=/api/**,/v1/**",
            "app.auth.exclude-path-patterns=/actuator/**,/health/**"
    })
    static class CustomPropertiesTest {

        @Autowired
        private AuthenticationProperties authenticationProperties;

        @Test
        void shouldLoadCustomProperties() {
            // Then
            assertThat(authenticationProperties.getHeaderName()).isEqualTo("X-User-ID");
            assertThat(authenticationProperties.getIncludePathPatterns()).containsExactly("/api/**", "/v1/**");
            assertThat(authenticationProperties.getExcludePathPatterns()).containsExactly("/actuator/**", "/health/**");
        }
    }
}