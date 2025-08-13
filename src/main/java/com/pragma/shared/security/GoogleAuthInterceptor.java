package com.pragma.shared.security;

import com.pragma.shared.config.AuthenticationProperties;
import com.pragma.shared.context.UserContext;
import com.pragma.shared.security.exception.InvalidAuthorizationException;
import com.pragma.shared.security.exception.MissingAuthorizationException;
import com.pragma.shared.security.exception.UserNotFoundException;
import com.pragma.shared.service.MessageService;
import com.pragma.usuarios.application.service.UserService;
import com.pragma.usuarios.domain.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Optional;

/**
 * Interceptor that handles Google-based authentication for all API requests.
 * Extracts Google user ID from the configured authorization header and sets up user context.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class GoogleAuthInterceptor implements HandlerInterceptor {
    
    private final UserService userService;
    private final MessageService messageService;
    private final AuthenticationProperties authProperties;

    /**
     * Pre-handle method that processes authentication before the request reaches the controller.
     * 
     * @param request current HTTP request
     * @param response current HTTP response
     * @param handler chosen handler to execute
     * @return true if the execution chain should proceed with the next interceptor or the handler itself
     * @throws Exception in case of errors
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        try {
            // Extract Google ID from Authorization header
            String googleUserId = extractGoogleUserId(request);
            
            // Find user in database
            User user = findUserByGoogleId(googleUserId);
            
            // Set user context for the current thread
            UserContext.setCurrentUser(user);
            
            // Log successful authentication
            log.info("User authenticated successfully: googleUserId={}, userId={}, email={}", 
                    googleUserId, user.getId(), user.getEmail());
            
            return true;
            
        } catch (Exception e) {
            // Log authentication failure
            log.warn("Authentication failed for request to {}: {}", request.getRequestURI(), e.getMessage());
            throw e;
        }
    }

    /**
     * After completion method that cleans up user context after request processing.
     * 
     * @param request current HTTP request
     * @param response current HTTP response
     * @param handler the handler that was executed
     * @param ex any exception thrown on handler execution
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        // Clear user context to prevent memory leaks and ensure thread safety
        UserContext.clear();
        
        if (log.isDebugEnabled()) {
            log.debug("User context cleared for request to {}", request.getRequestURI());
        }
    }

    /**
     * Extracts Google user ID from the configured authorization header.
     * 
     * @param request the HTTP request
     * @return the Google user ID
     * @throws MissingAuthorizationException if the authorization header is missing
     * @throws InvalidAuthorizationException if the authorization header is empty or malformed
     */
    private String extractGoogleUserId(HttpServletRequest request) {
        String headerName = authProperties.getHeaderName();
        String authorizationHeader = request.getHeader(headerName);
        
        // Check if authorization header is present
        if (authorizationHeader == null) {
            throw new MissingAuthorizationException(
                messageService.getMessage("auth.header.missing", headerName + " header is required")
            );
        }
        
        // Check if authorization header is not empty and properly formatted
        if (!StringUtils.hasText(authorizationHeader)) {
            throw new InvalidAuthorizationException(
                messageService.getMessage("auth.header.empty", headerName + " header cannot be empty")
            );
        }
        
        // For this implementation, we expect the Google user ID to be sent directly in the header
        // In a real-world scenario, this might be a JWT token that needs to be parsed
        String googleUserId = authorizationHeader.trim();
        
        // Basic validation - Google user IDs are typically non-empty strings
        if (googleUserId.isEmpty()) {
            throw new InvalidAuthorizationException(
                messageService.getMessage("auth.header.invalid", "Invalid " + headerName.toLowerCase() + " header format")
            );
        }
        
        return googleUserId;
    }

    /**
     * Finds a user by Google user ID in the internal database.
     * 
     * @param googleUserId the Google user ID to search for
     * @return the User object if found
     * @throws UserNotFoundException if no user is found with the given Google ID
     */
    private User findUserByGoogleId(String googleUserId) {
        try {
            Optional<User> userOptional = userService.findUserByGoogleId(googleUserId);
            
            return userOptional.orElseThrow(() -> new UserNotFoundException(
                messageService.getMessage("auth.user.not.found", "User not registered in the system")
            ));
            
        } catch (UserNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Database error while looking up user with Google ID: {}", googleUserId, e);
            throw new RuntimeException(
                messageService.getMessage("auth.database.error", "Internal server error occurred"), e
            );
        }
    }
}