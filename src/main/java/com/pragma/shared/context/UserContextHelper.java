package com.pragma.shared.context;

import com.pragma.shared.service.MessageService;
import com.pragma.usuarios.domain.model.User;
import com.pragma.usuarios.domain.model.enums.RolUsuario;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Helper utility class for UserContext operations in controllers.
 * Provides common methods for user context validation and authorization checks.
 */
@Slf4j
@Component
public final class UserContextHelper {

    private static MessageService messageService;

    @Autowired
    public void setMessageService(MessageService messageService) {
        UserContextHelper.messageService = messageService;
    }

    private UserContextHelper() {
        // Utility class - prevent instantiation
    }

    /**
     * Gets the current authenticated user from UserContext.
     * 
     * @return the current user
     * @throws IllegalStateException if no user is authenticated
     */
    public static User getCurrentUserOrThrow() {
        if (!UserContext.hasCurrentUser()) {
            log.error("No authenticated user found in context");
            throw new IllegalStateException(messageService.getMessage("auth.user.context.not.found"));
        }
        return UserContext.getCurrentUser();
    }

    /**
     * Checks if the current user is an administrator.
     * 
     * @return true if current user is admin, false otherwise
     * @throws IllegalStateException if no user is authenticated
     */
    public static boolean isCurrentUserAdmin() {
        User currentUser = getCurrentUserOrThrow();
        return currentUser.getRol() == RolUsuario.Administrador;
    }

    /**
     * Checks if the current user can access a resource owned by the specified user ID.
     * Users can access their own resources, and admins can access any resource.
     * 
     * @param resourceUserId the ID of the user who owns the resource
     * @return true if access is allowed, false otherwise
     * @throws IllegalStateException if no user is authenticated
     */
    public static boolean canAccessUserResource(String resourceUserId) {
        User currentUser = getCurrentUserOrThrow();
        return currentUser.getId().equals(resourceUserId) || 
               currentUser.getRol() == RolUsuario.Administrador;
    }

    /**
     * Validates that the current user can perform admin-only operations.
     * 
     * @throws SecurityException if current user is not an admin
     * @throws IllegalStateException if no user is authenticated
     */
    public static void requireAdminRole() {
        User currentUser = getCurrentUserOrThrow();
        if (currentUser.getRol() != RolUsuario.Administrador) {
            log.warn("User {} attempted to perform admin operation without privileges", 
                    currentUser.getEmail());
            throw new SecurityException(messageService.getMessage("auth.admin.privileges.required"));
        }
    }

    /**
     * Validates that the current user can access the specified resource.
     * 
     * @param resourceUserId the ID of the user who owns the resource
     * @throws SecurityException if access is not allowed
     * @throws IllegalStateException if no user is authenticated
     */
    public static void requireResourceAccess(String resourceUserId) {
        if (!canAccessUserResource(resourceUserId)) {
            User currentUser = getCurrentUserOrThrow();
            log.warn("User {} attempted to access resource owned by user {} without permission", 
                    currentUser.getEmail(), resourceUserId);
            throw new SecurityException(messageService.getMessage("auth.access.denied"));
        }
    }

    /**
     * Gets the current user's ID.
     * 
     * @return the current user's ID
     * @throws IllegalStateException if no user is authenticated
     */
    public static String getCurrentUserId() {
        return getCurrentUserOrThrow().getId();
    }

    /**
     * Gets the current user's email for logging purposes.
     * 
     * @return the current user's email
     * @throws IllegalStateException if no user is authenticated
     */
    public static String getCurrentUserEmail() {
        return getCurrentUserOrThrow().getEmail();
    }

    /**
     * Gets the current user's chapter ID.
     * 
     * @return the current user's chapter ID, or null if no chapter is set
     * @throws IllegalStateException if no user is authenticated
     */
    public static String getCurrentUserChapterId() {
        User currentUser = getCurrentUserOrThrow();
        return currentUser.getChapter() != null ? currentUser.getChapter().getId() : null;
    }

    /**
     * Creates a log-safe string representation of the current user for audit purposes.
     * 
     * @return formatted string with user information
     * @throws IllegalStateException if no user is authenticated
     */
    public static String getCurrentUserLogInfo() {
        User currentUser = getCurrentUserOrThrow();
        return String.format("User[id=%s, email=%s, role=%s]", 
                currentUser.getId(), currentUser.getEmail(), currentUser.getRol());
    }

    /**
     * Checks if the current user has the specified role.
     * 
     * @param role the role to check
     * @return true if user has the role, false otherwise
     * @throws IllegalStateException if no user is authenticated
     */
    public static boolean hasRole(RolUsuario role) {
        User currentUser = getCurrentUserOrThrow();
        return currentUser.getRol() == role;
    }

    /**
     * Checks if the current user can act as a tutor.
     * 
     * @return true if user can be a tutor, false otherwise
     * @throws IllegalStateException if no user is authenticated
     */
    public static boolean canActAsTutor() {
        User currentUser = getCurrentUserOrThrow();
        return currentUser.getRol() == RolUsuario.Tutor || 
               currentUser.getRol() == RolUsuario.Administrador;
    }

    /**
     * Checks if the current user can request tutoring.
     * 
     * @return true if user can request tutoring, false otherwise
     * @throws IllegalStateException if no user is authenticated
     */
    public static boolean canRequestTutoring() {
        User currentUser = getCurrentUserOrThrow();
        return currentUser.getRol() == RolUsuario.Tutorado || 
               currentUser.getRol() == RolUsuario.Administrador;
    }
}