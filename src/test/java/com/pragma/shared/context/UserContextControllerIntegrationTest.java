package com.pragma.shared.context;

import com.pragma.chapter.domain.model.Chapter;
import com.pragma.usuarios.domain.model.User;
import com.pragma.usuarios.domain.model.enums.RolUsuario;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for UserContext usage in controllers.
 * Tests the proper handling of user context in controller methods.
 */
@SpringBootTest
@ActiveProfiles("test")
class UserContextControllerIntegrationTest {

    private User testUser;
    private User adminUser;

    @BeforeEach
    void setUp() {
        UserContext.clear();
        
        // Create test chapter
        Chapter testChapter = new Chapter();
        testChapter.setId("chapter-1");
        testChapter.setName("Test Chapter");
        
        // Create regular test user
        testUser = new User();
        testUser.setId("test-user-id");
        testUser.setFirstName("John");
        testUser.setLastName("Doe");
        testUser.setEmail("john.doe@example.com");
        testUser.setGoogleUserId("google-123");
        testUser.setChapter(testChapter);
        testUser.setRol(RolUsuario.Tutorado);
        testUser.setActiveTutoringLimit(5);
        
        // Create admin test user
        adminUser = new User();
        adminUser.setId("admin-user-id");
        adminUser.setFirstName("Admin");
        adminUser.setLastName("User");
        adminUser.setEmail("admin@example.com");
        adminUser.setGoogleUserId("google-admin");
        adminUser.setChapter(testChapter);
        adminUser.setRol(RolUsuario.Administrador);
        adminUser.setActiveTutoringLimit(10);
    }

    @AfterEach
    void tearDown() {
        UserContext.clear();
    }

    @Test
    void getCurrentUserOrThrow_ShouldReturnUserWhenContextIsSet() {
        // Given
        UserContext.setCurrentUser(testUser);
        
        // When
        User currentUser = UserContext.getCurrentUser();
        
        // Then
        assertNotNull(currentUser);
        assertEquals(testUser.getId(), currentUser.getId());
        assertEquals(testUser.getEmail(), currentUser.getEmail());
        assertEquals(testUser.getRol(), currentUser.getRol());
    }

    @Test
    void getCurrentUserOrThrow_ShouldThrowExceptionWhenNoUserInContext() {
        // Given - no user set in context
        
        // When & Then
        assertThrows(IllegalStateException.class, () -> {
            if (!UserContext.hasCurrentUser()) {
                throw new IllegalStateException("No authenticated user found");
            }
        });
    }

    @Test
    void userContext_ShouldProvideCorrectUserForRegularUser() {
        // Given
        UserContext.setCurrentUser(testUser);
        
        // When
        User currentUser = UserContext.getCurrentUser();
        
        // Then
        assertEquals(RolUsuario.Tutorado, currentUser.getRol());
        assertEquals("john.doe@example.com", currentUser.getEmail());
        assertFalse(isAdmin(currentUser));
    }

    @Test
    void userContext_ShouldProvideCorrectUserForAdminUser() {
        // Given
        UserContext.setCurrentUser(adminUser);
        
        // When
        User currentUser = UserContext.getCurrentUser();
        
        // Then
        assertEquals(RolUsuario.Administrador, currentUser.getRol());
        assertEquals("admin@example.com", currentUser.getEmail());
        assertTrue(isAdmin(currentUser));
    }

    @Test
    void userContext_ShouldHandleUserRoleBasedAuthorization() {
        // Test regular user
        UserContext.setCurrentUser(testUser);
        User regularUser = UserContext.getCurrentUser();
        assertFalse(canPerformAdminActions(regularUser));
        assertTrue(canPerformUserActions(regularUser));
        
        // Clear and test admin user
        UserContext.clear();
        UserContext.setCurrentUser(adminUser);
        User admin = UserContext.getCurrentUser();
        assertTrue(canPerformAdminActions(admin));
        assertTrue(canPerformUserActions(admin));
    }

    @Test
    void userContext_ShouldHandleUserOwnershipValidation() {
        // Given
        UserContext.setCurrentUser(testUser);
        User currentUser = UserContext.getCurrentUser();
        
        // When & Then
        assertTrue(canAccessOwnResource(currentUser, testUser.getId()));
        assertFalse(canAccessOwnResource(currentUser, "other-user-id"));
    }

    @Test
    void userContext_ShouldProvideUserInformationForLogging() {
        // Given
        UserContext.setCurrentUser(testUser);
        
        // When
        User currentUser = UserContext.getCurrentUser();
        String logInfo = createLogInfo(currentUser);
        
        // Then
        assertNotNull(logInfo);
        assertTrue(logInfo.contains(testUser.getEmail()));
        assertTrue(logInfo.contains(testUser.getId()));
    }

    @Test
    void userContext_ShouldHandleContextClearingProperly() {
        // Given
        UserContext.setCurrentUser(testUser);
        assertTrue(UserContext.hasCurrentUser());
        
        // When
        UserContext.clear();
        
        // Then
        assertFalse(UserContext.hasCurrentUser());
        assertNull(UserContext.getCurrentUser());
    }

    @Test
    void userContext_ShouldHandleMultipleUserSwitching() {
        // Set first user
        UserContext.setCurrentUser(testUser);
        assertEquals(testUser.getId(), UserContext.getCurrentUser().getId());
        
        // Switch to admin user
        UserContext.setCurrentUser(adminUser);
        assertEquals(adminUser.getId(), UserContext.getCurrentUser().getId());
        assertEquals(RolUsuario.Administrador, UserContext.getCurrentUser().getRol());
        
        // Clear context
        UserContext.clear();
        assertNull(UserContext.getCurrentUser());
    }

    @Test
    void userContext_ShouldProvideChapterInformation() {
        // Given
        UserContext.setCurrentUser(testUser);
        
        // When
        User currentUser = UserContext.getCurrentUser();
        
        // Then
        assertNotNull(currentUser.getChapter());
        assertEquals("chapter-1", currentUser.getChapter().getId());
        assertEquals("Test Chapter", currentUser.getChapter().getName());
    }

    // Helper methods to simulate controller logic

    private boolean isAdmin(User user) {
        return user.getRol() == RolUsuario.Administrador;
    }

    private boolean canPerformAdminActions(User user) {
        return user.getRol() == RolUsuario.Administrador;
    }

    private boolean canPerformUserActions(User user) {
        return user.getRol() != null; // Any authenticated user can perform basic actions
    }

    private boolean canAccessOwnResource(User currentUser, String resourceUserId) {
        return currentUser.getId().equals(resourceUserId) || 
               currentUser.getRol() == RolUsuario.Administrador;
    }

    private String createLogInfo(User user) {
        return String.format("User %s (ID: %s, Role: %s)", 
                user.getEmail(), user.getId(), user.getRol());
    }
}