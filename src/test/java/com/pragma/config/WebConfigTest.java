package com.pragma.config;

import com.pragma.shared.security.GoogleAuthInterceptor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.config.annotation.InterceptorRegistration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WebConfigTest {

    @Mock
    private GoogleAuthInterceptor googleAuthInterceptor;

    @Mock
    private InterceptorRegistry interceptorRegistry;

    @Mock
    private InterceptorRegistration interceptorRegistration;

    private WebConfig webConfig;

    @BeforeEach
    void setUp() {
        webConfig = new WebConfig(googleAuthInterceptor);
    }

    @Test
    void shouldRegisterGoogleAuthInterceptorWithCorrectPathPatterns() {
        // Given
        when(interceptorRegistry.addInterceptor(googleAuthInterceptor)).thenReturn(interceptorRegistration);
        when(interceptorRegistration.addPathPatterns("/api/**")).thenReturn(interceptorRegistration);

        // When
        webConfig.addInterceptors(interceptorRegistry);

        // Then
        verify(interceptorRegistry).addInterceptor(googleAuthInterceptor);
        verify(interceptorRegistration).addPathPatterns("/api/**");
        verify(interceptorRegistration).excludePathPatterns("/actuator/**");
    }

    @Test
    void shouldConfigureInterceptorToApplyToAllApiEndpoints() {
        // Given
        when(interceptorRegistry.addInterceptor(googleAuthInterceptor)).thenReturn(interceptorRegistration);
        when(interceptorRegistration.addPathPatterns("/api/**")).thenReturn(interceptorRegistration);

        // When
        webConfig.addInterceptors(interceptorRegistry);

        // Then
        verify(interceptorRegistration).addPathPatterns("/api/**");
    }

    @Test
    void shouldExcludeActuatorEndpointsFromInterceptor() {
        // Given
        when(interceptorRegistry.addInterceptor(googleAuthInterceptor)).thenReturn(interceptorRegistration);
        when(interceptorRegistration.addPathPatterns("/api/**")).thenReturn(interceptorRegistration);

        // When
        webConfig.addInterceptors(interceptorRegistry);

        // Then
        verify(interceptorRegistration).excludePathPatterns("/actuator/**");
    }
}