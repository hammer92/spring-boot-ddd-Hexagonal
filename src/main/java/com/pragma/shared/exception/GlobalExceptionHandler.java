package com.pragma.shared.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import com.pragma.shared.dto.ErrorResponseDto;
import com.pragma.shared.security.exception.AuthenticationException;
import com.pragma.shared.security.exception.InvalidAuthorizationException;
import com.pragma.shared.security.exception.MissingAuthorizationException;
import com.pragma.shared.security.exception.UserNotFoundException;
import com.pragma.shared.service.MessageService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@RequiredArgsConstructor
@Slf4j
public class GlobalExceptionHandler {

    private final MessageService messageService;

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponseDto handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return ErrorResponseDto.of(messageService.getMessage("general.validation.failed"), errors);
    }
    
    @ExceptionHandler(MissingAuthorizationException.class)
    public ResponseEntity<ErrorResponseDto> handleMissingAuthorizationException(MissingAuthorizationException ex, WebRequest request) {
        log.warn("AUTH_EXCEPTION - Missing authorization handled: uri={}, error={}", 
                getRequestUri(request), ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ErrorResponseDto.of(ex.getMessage()));
    }

    @ExceptionHandler(InvalidAuthorizationException.class)
    public ResponseEntity<ErrorResponseDto> handleInvalidAuthorizationException(InvalidAuthorizationException ex, WebRequest request) {
        log.warn("AUTH_EXCEPTION - Invalid authorization handled: uri={}, error={}", 
                getRequestUri(request), ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ErrorResponseDto.of(ex.getMessage()));
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleUserNotFoundException(UserNotFoundException ex, WebRequest request) {
        log.warn("AUTH_EXCEPTION - User not found handled: uri={}, error={}", 
                getRequestUri(request), ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(ErrorResponseDto.of(ex.getMessage()));
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponseDto> handleAuthenticationException(AuthenticationException ex, WebRequest request) {
        log.warn("AUTH_EXCEPTION - Authentication exception handled: uri={}, error={}", 
                getRequestUri(request), ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ErrorResponseDto.of(ex.getMessage()));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponseDto> handleRuntimeException(RuntimeException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponseDto.of(ex.getMessage()));
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDto> handleGenericException(Exception ex, WebRequest request) {
        log.error("SYSTEM_ERROR - Unhandled exception: uri={}, error={}", 
                getRequestUri(request), ex.getMessage(), ex);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponseDto.of(messageService.getMessage("general.error", ex.getMessage())));
    }

    /**
     * Extracts the request URI from WebRequest for logging purposes.
     * 
     * @param request the web request
     * @return the request URI or "unknown" if not available
     */
    private String getRequestUri(WebRequest request) {
        try {
            return request.getDescription(false).replace("uri=", "");
        } catch (Exception e) {
            return "unknown";
        }
    }
}