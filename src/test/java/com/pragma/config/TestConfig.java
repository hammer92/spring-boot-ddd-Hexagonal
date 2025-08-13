package com.pragma.config;

import com.pragma.usuarios.application.service.UserService;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;

@TestConfiguration
public class TestConfig {
    
    @MockBean
    private UserService userService;
}