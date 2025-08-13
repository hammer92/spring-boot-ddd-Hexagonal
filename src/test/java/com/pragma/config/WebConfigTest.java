package com.pragma.config;

import com.pragma.shared.config.AuthenticationProperties;
import com.pragma.shared.security.GoogleAuthInterceptor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.config.annotation.InterceptorRegistration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;

import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WebConfigTest {

    @Mock
    private GoogleAuthInterceptor googleAuthInterceptor;

    @Mock
    private AuthenticationProperties authProperties;

    @Mock
    private InterceptorRegistry interceptorRegistry;

    @Mock
    private InterceptorRegistration interceptorRegistration;

    private WebConfig webConfig;

    @BeforeEach
    void setUp() {
        webConfig = new WebConfig(googleAuthInterceptor, authProperties);
        
        // Set up default behavior for auth properties
        when(authProperties.getIncludePathPatterns()).thenReturn(List.of("/api/**"));
        when(authProperties.getExcludePathPatterns()).thenReturn(List.of("/actuator/**"));
    }

    @Test
    void shouldRegisterGoogleAuthInterceptorWithCorrectPathPatterns() {
        // Given
        when(interceptorRegistry.addInterceptor(googleAuthInterceptor)).thenReturn(interceptorRegistration);
        when(interceptorRegistration.addPathPatterns(any(String[].class))).thenReturn(interceptorRegistration);

        // When
        webConfig.addInterceptors(interceptorRegistry);

        // Then
        verify(interceptorRegistry).addInterceptor(googleAuthInterceptor);
        verify(interceptorRegistration).addPathPatterns(new String[]{"/api/**"});
        verify(interceptorRegistration).excludePathPatterns(new String[]{"/actuator/**"});
    }

    @Test
    void shouldConfigureInterceptorToApplyToConfiguredPathPatterns() {
        // Given
        when(authProperties.getIncludePathPatterns()).thenReturn(List.of("/api/**", "/v1/**"));
        when(interceptorRegistry.addInterceptor(googleAuthInterceptor)).thenReturn(interceptorRegistration);
        when(interceptorRegistration.addPathPatterns(any(String[].class))).thenReturn(interceptorRegistration);

        // When
        webConfig.addInterceptors(interceptorRegistry);

        // Then
        verify(interceptorRegistration).addPathPatterns(new String[]{"/api/**", "/v1/**"});
    }

    @Test
    void shouldExcludeConfiguredPathPatternsFromInterceptor() {
        // Given
        when(authProperties.getExcludePathPatterns()).thenReturn(List.of("/actuator/**", "/health/**"));
        when(interceptorRegistry.addInterceptor(googleAuthInterceptor)).thenReturn(interceptorRegistration);
        when(interceptorRegistration.addPathPatterns(any(String[].class))).thenReturn(interceptorRegistration);

        // When
        webConfig.addInterceptors(interceptorRegistry);

        // Then
        verify(interceptorRegistration).excludePathPatterns(new String[]{"/actuator/**", "/health/**"});
    }
}