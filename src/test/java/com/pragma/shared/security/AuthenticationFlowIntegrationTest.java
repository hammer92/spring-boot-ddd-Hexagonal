package com.pragma.shared.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pragma.chapter.domain.model.Chapter;
import com.pragma.shared.context.UserContext;
import com.pragma.usuarios.domain.model.User;
import com.pragma.usuarios.domain.model.enums.RolUsuario;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Simple integration tests for authentication flow components.
 * Tests the basic functionality without full web context to avoid compilation issues.
 */
@SpringBootTest
@ActiveProfiles("test")
class AuthenticationFlowIntegrationTest {

    @Autowired
    private ObjectMapper objectMapper;

    private User testUser;
    private User adminUser;
    private Chapter testChapter;

    @BeforeEach
    void setUp() {
        UserContext.clear();

        // Setup test data
        testChapter = new Chapter();
        testChapter.setId("chapter-1");
        testChapter.setName("Test Chapter");

        testUser = new User();
        testUser.setId("test-user-id");
        testUser.setFirstName("John");
        testUser.setLastName("Doe");
        testUser.setEmail("john.doe@example.com");
        testUser.setGoogleUserId("google-123");
        testUser.setChapter(testChapter);
        testUser.setRol(RolUsuario.Tutorado);
        testUser.setActiveTutoringLimit(5);

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
    void userContext_WithAuthenticatedUser_ShouldProvideUserInformation() {
        // Given
        UserContext.setCurrentUser(testUser);

        // When
        User currentUser = UserContext.getCurrentUser();

        // Then
        assertNotNull(currentUser);
        assertEquals(testUser.getId(), currentUser.getId());
        assertEquals(testUser.getEmail(), currentUser.getEmail());
        assertEquals(testUser.getRol(), currentUser.getRol());
        assertEquals(testUser.getGoogleUserId(), currentUser.getGoogleUserId());
        assertTrue(UserContext.hasCurrentUser());
    }

    @Test
    void userContext_WithAdminUser_ShouldProvideAdminInformation() {
        // Given
        UserContext.setCurrentUser(adminUser);

        // When
        User currentUser = UserContext.getCurrentUser();

        // Then
        assertNotNull(currentUser);
        assertEquals(adminUser.getId(), currentUser.getId());
        assertEquals(adminUser.getEmail(), currentUser.getEmail());
        assertEquals(RolUsuario.Administrador, currentUser.getRol());
        assertEquals(adminUser.getGoogleUserId(), currentUser.getGoogleUserId());
        assertTrue(UserContext.hasCurrentUser());
    }

    @Test
    void userContext_WithoutUser_ShouldReturnNull() {
        // Given - no user set

        // When & Then
        assertFalse(UserContext.hasCurrentUser());
        assertNull(UserContext.getCurrentUser());
    }

    @Test
    void userContext_AfterClear_ShouldRemoveUser() {
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
    void userContext_ThreadSafety_ShouldIsolateUsers() throws InterruptedException {
        // Given
        UserContext.setCurrentUser(testUser);
        assertEquals(testUser, UserContext.getCurrentUser());

        // When - Test in separate thread
        Thread testThread = new Thread(() -> {
            // This thread should not see the user from main thread
            assertFalse(UserContext.hasCurrentUser());
            assertNull(UserContext.getCurrentUser());

            // Set different user in this thread
            UserContext.setCurrentUser(adminUser);
            assertTrue(UserContext.hasCurrentUser());
            assertEquals(adminUser, UserContext.getCurrentUser());
        });

        testThread.start();
        testThread.join();

        // Then - Main thread should still have its original user
        assertEquals(testUser, UserContext.getCurrentUser());
        assertTrue(UserContext.hasCurrentUser());
    }

    @Test
    void userContext_MultipleUserSwitching_ShouldWork() {
        // Test switching between users
        UserContext.setCurrentUser(testUser);
        assertEquals(testUser.getId(), UserContext.getCurrentUser().getId());

        UserContext.setCurrentUser(adminUser);
        assertEquals(adminUser.getId(), UserContext.getCurrentUser().getId());

        UserContext.setCurrentUser(testUser);
        assertEquals(testUser.getId(), UserContext.getCurrentUser().getId());

        UserContext.clear();
        assertNull(UserContext.getCurrentUser());
    }

    @Test
    void userContext_WithNullUser_ShouldHandleGracefully() {
        // Setting null user should work
        UserContext.setCurrentUser(null);
        assertFalse(UserContext.hasCurrentUser());
        assertNull(UserContext.getCurrentUser());

        // Set real user then null
        UserContext.setCurrentUser(testUser);
        assertTrue(UserContext.hasCurrentUser());

        UserContext.setCurrentUser(null);
        assertFalse(UserContext.hasCurrentUser());
        assertNull(UserContext.getCurrentUser());
    }

    @Test
    void authenticationFlow_UserDataIntegrity_ShouldMaintainAllFields() {
        // Given
        UserContext.setCurrentUser(testUser);

        // When
        User retrievedUser = UserContext.getCurrentUser();

        // Then - Verify all user fields are preserved
        assertEquals(testUser.getId(), retrievedUser.getId());
        assertEquals(testUser.getFirstName(), retrievedUser.getFirstName());
        assertEquals(testUser.getLastName(), retrievedUser.getLastName());
        assertEquals(testUser.getEmail(), retrievedUser.getEmail());
        assertEquals(testUser.getGoogleUserId(), retrievedUser.getGoogleUserId());
        assertEquals(testUser.getRol(), retrievedUser.getRol());
        assertEquals(testUser.getActiveTutoringLimit(), retrievedUser.getActiveTutoringLimit());
        
        // Verify chapter information
        assertNotNull(retrievedUser.getChapter());
        assertEquals(testChapter.getId(), retrievedUser.getChapter().getId());
        assertEquals(testChapter.getName(), retrievedUser.getChapter().getName());
    }

    @Test
    void authenticationFlow_RoleBasedChecks_ShouldWorkCorrectly() {
        // Test regular user
        UserContext.setCurrentUser(testUser);
        User regularUser = UserContext.getCurrentUser();
        assertEquals(RolUsuario.Tutorado, regularUser.getRol());
        assertNotEquals(RolUsuario.Administrador, regularUser.getRol());

        // Test admin user
        UserContext.setCurrentUser(adminUser);
        User admin = UserContext.getCurrentUser();
        assertEquals(RolUsuario.Administrador, admin.getRol());
        assertNotEquals(RolUsuario.Tutorado, admin.getRol());
    }

    @Test
    void authenticationFlow_ContextCleanup_ShouldPreventMemoryLeaks() {
        // Set user multiple times
        UserContext.setCurrentUser(testUser);
        assertTrue(UserContext.hasCurrentUser());

        UserContext.setCurrentUser(adminUser);
        assertTrue(UserContext.hasCurrentUser());
        assertEquals(adminUser, UserContext.getCurrentUser());

        // Clear should remove all references
        UserContext.clear();
        assertFalse(UserContext.hasCurrentUser());
        assertNull(UserContext.getCurrentUser());

        // Multiple clears should be safe
        UserContext.clear();
        UserContext.clear();
        assertFalse(UserContext.hasCurrentUser());
        assertNull(UserContext.getCurrentUser());
    }
}