package com.pragma.shared.context;

import com.pragma.usuarios.domain.model.User;
import com.pragma.usuarios.domain.model.enums.RolUsuario;
import com.pragma.chapter.domain.model.Chapter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.pragma.shared.service.MessageService;
import org.mockito.Mock;

/**
 * Integration tests for UserContext functionality.
 * Tests thread isolation, context management, and UserContextHelper integration.
 */
@ActiveProfiles("test")
class UserContextIntegrationTest {

    private MessageService mockMessageService;

    private User testUser;
    private User adminUser;
    private Chapter testChapter;

    @BeforeEach
    void setUp() {
        // Mock MessageService for UserContextHelper
        mockMessageService = mock(MessageService.class);
        when(mockMessageService.getMessage(anyString())).thenReturn("Test message");
        UserContextHelper.setMessageServiceForTesting(mockMessageService);
        testChapter = new Chapter();
        testChapter.setId("chapter-1");
        testChapter.setName("Test Chapter");

        testUser = new User();
        testUser.setId("user-1");
        testUser.setFirstName("John");
        testUser.setLastName("Doe");
        testUser.setEmail("john.doe@example.com");
        testUser.setGoogleUserId("google-123");
        testUser.setRol(RolUsuario.Tutorado);
        testUser.setChapter(testChapter);
        testUser.setActiveTutoringLimit(3);

        adminUser = new User();
        adminUser.setId("admin-1");
        adminUser.setFirstName("Admin");
        adminUser.setLastName("User");
        adminUser.setEmail("admin@example.com");
        adminUser.setGoogleUserId("google-admin");
        adminUser.setRol(RolUsuario.Administrador);
        adminUser.setChapter(testChapter);
        adminUser.setActiveTutoringLimit(10);
    }

    @AfterEach
    void tearDown() {
        UserContext.clear();
        UserContextHelper.setMessageServiceForTesting(null);
    }

    @Test
    void userContext_BasicOperations_ShouldWorkCorrectly() {
        // Initially no user should be set
        assertFalse(UserContext.hasCurrentUser());
        assertNull(UserContext.getCurrentUser());

        // Set user and verify
        UserContext.setCurrentUser(testUser);
        assertTrue(UserContext.hasCurrentUser());
        assertEquals(testUser, UserContext.getCurrentUser());
        assertEquals("user-1", UserContext.getCurrentUser().getId());
        assertEquals("john.doe@example.com", UserContext.getCurrentUser().getEmail());

        // Clear and verify
        UserContext.clear();
        assertFalse(UserContext.hasCurrentUser());
        assertNull(UserContext.getCurrentUser());
    }

    @Test
    void userContext_ThreadIsolation_ShouldMaintainSeparateContexts() throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        
        try {
            // Set user in main thread
            UserContext.setCurrentUser(testUser);
            assertEquals(testUser, UserContext.getCurrentUser());

            // Test thread isolation
            CompletableFuture<User> thread1Result = CompletableFuture.supplyAsync(() -> {
                // This thread should not see the user from main thread
                assertFalse(UserContext.hasCurrentUser());
                assertNull(UserContext.getCurrentUser());
                
                // Set different user in this thread
                UserContext.setCurrentUser(adminUser);
                assertTrue(UserContext.hasCurrentUser());
                return UserContext.getCurrentUser();
            }, executor);

            CompletableFuture<User> thread2Result = CompletableFuture.supplyAsync(() -> {
                // This thread should also not see users from other threads
                assertFalse(UserContext.hasCurrentUser());
                assertNull(UserContext.getCurrentUser());
                
                // Set original user in this thread
                UserContext.setCurrentUser(testUser);
                assertTrue(UserContext.hasCurrentUser());
                return UserContext.getCurrentUser();
            }, executor);

            // Wait for completion and verify results
            User user1 = thread1Result.get(5, TimeUnit.SECONDS);
            User user2 = thread2Result.get(5, TimeUnit.SECONDS);

            assertEquals(adminUser.getId(), user1.getId());
            assertEquals(testUser.getId(), user2.getId());

            // Main thread should still have its original user
            assertEquals(testUser, UserContext.getCurrentUser());

        } finally {
            executor.shutdown();
            executor.awaitTermination(5, TimeUnit.SECONDS);
        }
    }

    @Test
    void userContextHelper_WithValidUser_ShouldProvideCorrectInformation() {
        // Set user context
        UserContext.setCurrentUser(testUser);

        // Test basic user information
        assertEquals(testUser, UserContextHelper.getCurrentUserOrThrow());
        assertEquals("user-1", UserContextHelper.getCurrentUserId());
        assertEquals("john.doe@example.com", UserContextHelper.getCurrentUserEmail());
        assertEquals("chapter-1", UserContextHelper.getCurrentUserChapterId());

        // Test role checks
        assertFalse(UserContextHelper.isCurrentUserAdmin());
        assertTrue(UserContextHelper.hasRole(RolUsuario.Tutorado));
        assertFalse(UserContextHelper.hasRole(RolUsuario.Tutor));
        assertFalse(UserContextHelper.hasRole(RolUsuario.Administrador));

        // Test capability checks
        assertFalse(UserContextHelper.canActAsTutor());
        assertTrue(UserContextHelper.canRequestTutoring());

        // Test resource access
        assertTrue(UserContextHelper.canAccessUserResource("user-1")); // Own resource
        assertFalse(UserContextHelper.canAccessUserResource("other-user")); // Other's resource

        // Test log info
        String logInfo = UserContextHelper.getCurrentUserLogInfo();
        assertTrue(logInfo.contains("user-1"));
        assertTrue(logInfo.contains("john.doe@example.com"));
        assertTrue(logInfo.contains("Tutorado"));
    }

    @Test
    void userContextHelper_WithAdminUser_ShouldHaveAdminPrivileges() {
        // Set admin user context
        UserContext.setCurrentUser(adminUser);

        // Test admin-specific functionality
        assertTrue(UserContextHelper.isCurrentUserAdmin());
        assertTrue(UserContextHelper.hasRole(RolUsuario.Administrador));
        assertTrue(UserContextHelper.canActAsTutor());
        assertTrue(UserContextHelper.canRequestTutoring());

        // Admin should be able to access any resource
        assertTrue(UserContextHelper.canAccessUserResource("admin-1")); // Own resource
        assertTrue(UserContextHelper.canAccessUserResource("any-user")); // Any resource

        // Test admin-only operations
        assertDoesNotThrow(() -> UserContextHelper.requireAdminRole());
        assertDoesNotThrow(() -> UserContextHelper.requireResourceAccess("any-user"));
    }

    @Test
    void userContextHelper_WithoutUser_ShouldThrowExceptions() {
        // No user set in context
        assertFalse(UserContext.hasCurrentUser());

        // All operations should throw IllegalStateException
        assertThrows(IllegalStateException.class, () -> UserContextHelper.getCurrentUserOrThrow());
        assertThrows(IllegalStateException.class, () -> UserContextHelper.getCurrentUserId());
        assertThrows(IllegalStateException.class, () -> UserContextHelper.getCurrentUserEmail());
        assertThrows(IllegalStateException.class, () -> UserContextHelper.getCurrentUserChapterId());
        assertThrows(IllegalStateException.class, () -> UserContextHelper.isCurrentUserAdmin());
        assertThrows(IllegalStateException.class, () -> UserContextHelper.hasRole(RolUsuario.Tutorado));
        assertThrows(IllegalStateException.class, () -> UserContextHelper.canActAsTutor());
        assertThrows(IllegalStateException.class, () -> UserContextHelper.canRequestTutoring());
        assertThrows(IllegalStateException.class, () -> UserContextHelper.canAccessUserResource("user-1"));
        assertThrows(IllegalStateException.class, () -> UserContextHelper.requireAdminRole());
        assertThrows(IllegalStateException.class, () -> UserContextHelper.requireResourceAccess("user-1"));
        assertThrows(IllegalStateException.class, () -> UserContextHelper.getCurrentUserLogInfo());
    }

    @Test
    void userContextHelper_AuthorizationChecks_ShouldEnforceCorrectly() {
        // Test with regular user
        UserContext.setCurrentUser(testUser);

        // Should throw SecurityException for admin operations
        assertThrows(SecurityException.class, () -> UserContextHelper.requireAdminRole());

        // Should throw SecurityException for accessing other user's resources
        assertThrows(SecurityException.class, () -> UserContextHelper.requireResourceAccess("other-user"));

        // Should allow access to own resources
        assertDoesNotThrow(() -> UserContextHelper.requireResourceAccess("user-1"));

        // Test with admin user
        UserContext.setCurrentUser(adminUser);

        // Should allow all operations
        assertDoesNotThrow(() -> UserContextHelper.requireAdminRole());
        assertDoesNotThrow(() -> UserContextHelper.requireResourceAccess("any-user"));
        assertDoesNotThrow(() -> UserContextHelper.requireResourceAccess("admin-1"));
    }

    @Test
    void userContext_MemoryLeakPrevention_ShouldClearProperly() {
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

    @Test
    void userContext_NullUser_ShouldHandleGracefully() {
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
    void userContextHelper_ChapterHandling_ShouldWorkWithAndWithoutChapter() {
        // Test with user that has chapter
        UserContext.setCurrentUser(testUser);
        assertEquals("chapter-1", UserContextHelper.getCurrentUserChapterId());

        // Test with user without chapter
        User userWithoutChapter = new User();
        userWithoutChapter.setId("user-2");
        userWithoutChapter.setEmail("user2@example.com");
        userWithoutChapter.setRol(RolUsuario.Tutorado);
        userWithoutChapter.setChapter(null);

        UserContext.setCurrentUser(userWithoutChapter);
        assertNull(UserContextHelper.getCurrentUserChapterId());
    }
}