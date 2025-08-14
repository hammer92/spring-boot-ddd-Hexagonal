package com.pragma.shared.controller;

import com.pragma.shared.context.UserContext;
import com.pragma.shared.context.UserContextHelper;
import com.pragma.shared.dto.OkResponseDto;
import com.pragma.usuarios.domain.model.User;
import com.pragma.usuarios.domain.model.enums.RolUsuario;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Example controller demonstrating various UserContext usage patterns.
 * This controller shows how to access authenticated user information
 * and handle cases where user context might be unavailable.
 */
@RestController
@RequestMapping("/api/v1/profile")
@RequiredArgsConstructor
@Slf4j
public class ProfileController {

    /**
     * Gets the current user's profile information.
     * Demonstrates basic UserContext usage with UserContextHelper.
     */
    @GetMapping("/me")
    public ResponseEntity<OkResponseDto<Map<String, Object>>> getCurrentUserProfile() {
        try {
            User currentUser = UserContextHelper.getCurrentUserOrThrow();
            log.debug("User {} requesting own profile", currentUser.getEmail());
            
            Map<String, Object> profile = new HashMap<>();
            profile.put("id", currentUser.getId());
            profile.put("firstName", currentUser.getFirstName());
            profile.put("lastName", currentUser.getLastName());
            profile.put("email", currentUser.getEmail());
            profile.put("role", currentUser.getRol().name());
            profile.put("chapterId", UserContextHelper.getCurrentUserChapterId());
            profile.put("activeTutoringLimit", currentUser.getActiveTutoringLimit());
            
            return ResponseEntity.ok(OkResponseDto.of("Perfil obtenido exitosamente", profile));
            
        } catch (IllegalStateException e) {
            log.error("Failed to get current user profile: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(OkResponseDto.of("Usuario no autenticado", null));
        }
    }

    /**
     * Gets user permissions and capabilities.
     * Demonstrates using UserContextHelper utility methods.
     */
    @GetMapping("/permissions")
    public ResponseEntity<OkResponseDto<Map<String, Object>>> getUserPermissions() {
        try {
            User currentUser = UserContextHelper.getCurrentUserOrThrow();
            log.debug("User {} requesting permissions", currentUser.getEmail());
            
            Map<String, Object> permissions = new HashMap<>();
            permissions.put("isAdmin", UserContextHelper.isCurrentUserAdmin());
            permissions.put("canActAsTutor", UserContextHelper.canActAsTutor());
            permissions.put("canRequestTutoring", UserContextHelper.canRequestTutoring());
            permissions.put("hasStudentRole", UserContextHelper.hasRole(RolUsuario.Tutorado));
            permissions.put("hasTutorRole", UserContextHelper.hasRole(RolUsuario.Tutor));
            permissions.put("hasAdminRole", UserContextHelper.hasRole(RolUsuario.Administrador));
            
            return ResponseEntity.ok(OkResponseDto.of("Permisos obtenidos exitosamente", permissions));
            
        } catch (IllegalStateException e) {
            log.error("Failed to get user permissions: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(OkResponseDto.of("Usuario no autenticado", null));
        }
    }

    /**
     * Gets user context status information.
     * Demonstrates direct UserContext usage and graceful error handling.
     */
    @GetMapping("/context-status")
    public ResponseEntity<OkResponseDto<Map<String, Object>>> getContextStatus() {
        Map<String, Object> status = new HashMap<>();
        
        // Check if user context is available
        boolean hasUser = UserContext.hasCurrentUser();
        status.put("hasAuthenticatedUser", hasUser);
        
        if (hasUser) {
            try {
                User currentUser = UserContext.getCurrentUser();
                status.put("userId", currentUser.getId());
                status.put("userEmail", currentUser.getEmail());
                status.put("userRole", currentUser.getRol().name());
                status.put("contextAvailable", true);
                
                log.debug("Context status requested by user: {}", currentUser.getEmail());
                
            } catch (Exception e) {
                log.warn("Error accessing user context: {}", e.getMessage());
                status.put("contextAvailable", false);
                status.put("error", "Error accessing user context");
            }
        } else {
            status.put("contextAvailable", false);
            status.put("message", "No authenticated user in context");
            log.debug("Context status requested but no user authenticated");
        }
        
        return ResponseEntity.ok(OkResponseDto.of("Estado del contexto obtenido", status));
    }

    /**
     * Admin-only endpoint demonstrating role-based access control.
     * Shows how to use UserContextHelper for authorization.
     */
    @GetMapping("/admin-info")
    public ResponseEntity<OkResponseDto<Map<String, Object>>> getAdminInfo() {
        try {
            // This will throw SecurityException if user is not admin
            UserContextHelper.requireAdminRole();
            
            User currentUser = UserContextHelper.getCurrentUserOrThrow();
            log.info("Admin {} accessing admin information", currentUser.getEmail());
            
            Map<String, Object> adminInfo = new HashMap<>();
            adminInfo.put("adminUserId", currentUser.getId());
            adminInfo.put("adminEmail", currentUser.getEmail());
            adminInfo.put("logInfo", UserContextHelper.getCurrentUserLogInfo());
            adminInfo.put("accessTime", System.currentTimeMillis());
            
            return ResponseEntity.ok(OkResponseDto.of("Información de administrador obtenida", adminInfo));
            
        } catch (SecurityException e) {
            log.warn("Non-admin user attempted to access admin info: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(OkResponseDto.of("Acceso denegado: se requieren privilegios de administrador", null));
        } catch (IllegalStateException e) {
            log.error("Failed to get admin info: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(OkResponseDto.of("Usuario no autenticado", null));
        }
    }

    /**
     * Endpoint demonstrating resource access validation.
     * Shows how to validate user access to specific resources.
     */
    @GetMapping("/user/{userId}/access-check")
    public ResponseEntity<OkResponseDto<Map<String, Object>>> checkUserResourceAccess(@PathVariable String userId) {
        try {
            User currentUser = UserContextHelper.getCurrentUserOrThrow();
            log.debug("User {} checking access to user resource: {}", currentUser.getEmail(), userId);
            
            Map<String, Object> accessInfo = new HashMap<>();
            accessInfo.put("requestedUserId", userId);
            accessInfo.put("currentUserId", currentUser.getId());
            accessInfo.put("canAccess", UserContextHelper.canAccessUserResource(userId));
            accessInfo.put("isOwnResource", currentUser.getId().equals(userId));
            accessInfo.put("isAdmin", UserContextHelper.isCurrentUserAdmin());
            
            // Demonstrate access validation
            try {
                UserContextHelper.requireResourceAccess(userId);
                accessInfo.put("accessValidation", "GRANTED");
            } catch (SecurityException e) {
                accessInfo.put("accessValidation", "DENIED");
                accessInfo.put("denialReason", e.getMessage());
            }
            
            return ResponseEntity.ok(OkResponseDto.of("Verificación de acceso completada", accessInfo));
            
        } catch (IllegalStateException e) {
            log.error("Failed to check resource access: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(OkResponseDto.of("Usuario no autenticado", null));
        }
    }

    /**
     * Demonstrates graceful handling when UserContext is not available.
     * This endpoint works even without authentication (for demonstration purposes).
     */
    @GetMapping("/public-info")
    public ResponseEntity<OkResponseDto<Map<String, Object>>> getPublicInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("endpoint", "public-info");
        info.put("timestamp", System.currentTimeMillis());
        
        // Safely check for user context without throwing exceptions
        if (UserContext.hasCurrentUser()) {
            try {
                User currentUser = UserContext.getCurrentUser();
                info.put("authenticatedUser", currentUser.getEmail());
                info.put("userRole", currentUser.getRol().name());
                log.debug("Public info accessed by authenticated user: {}", currentUser.getEmail());
            } catch (Exception e) {
                log.warn("Error accessing user context in public endpoint: {}", e.getMessage());
                info.put("userContextError", e.getMessage());
            }
        } else {
            info.put("authenticatedUser", null);
            info.put("message", "Accessed without authentication");
            log.debug("Public info accessed without authentication");
        }
        
        return ResponseEntity.ok(OkResponseDto.of("Información pública obtenida", info));
    }
}