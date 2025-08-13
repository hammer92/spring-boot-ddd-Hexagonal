package com.pragma.shared.context;

import com.pragma.usuarios.domain.model.User;

/**
 * Thread-local storage for authenticated user information.
 * Provides a way to access the current authenticated user throughout the request lifecycle.
 */
public class UserContext {
    
    private static final ThreadLocal<User> currentUser = new ThreadLocal<>();
    
    /**
     * Sets the authenticated user for the current thread.
     * 
     * @param user the authenticated user to set
     */
    public static void setCurrentUser(User user) {
        currentUser.set(user);
    }
    
    /**
     * Retrieves the authenticated user for the current thread.
     * 
     * @return the authenticated user, or null if no user is set
     */
    public static User getCurrentUser() {
        return currentUser.get();
    }
    
    /**
     * Removes the user context from the current thread.
     * This should be called at the end of each request to prevent memory leaks.
     */
    public static void clear() {
        currentUser.remove();
    }
    
    /**
     * Checks if there is an authenticated user in the current thread.
     * 
     * @return true if a user is set, false otherwise
     */
    public static boolean hasCurrentUser() {
        return currentUser.get() != null;
    }
}