package com.pragma.shared.context;

import com.pragma.chapter.domain.model.Chapter;
import com.pragma.shared.service.MessageService;
import com.pragma.usuarios.domain.model.User;
import com.pragma.usuarios.domain.model.enums.RolUsuario;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests for UserContextHelper utility class.
 * Verifies helper methods for UserContext operations and authorization checks.
 */
class UserContextHelperTest {

    private User regularUser;
    private User adminUser;
    private User tutorUser;
    private Chapter testChapter;

    @BeforeEach
    void setUp() {
        UserContext.clear();
        
        // Mock MessageService for testing
        MessageService mockMessageService = mock(MessageService.class);
        when(mockMessageService.getMessage(anyString())).thenReturn("Test message");
        UserContextHelper.setMessageServiceForTesting(mockMessageService);
        
        testChapter = new Chapter();
        testChapter.setId("chapter-1");
        testChapter.setName("Test Chapter");
        
        regularUser = new User();
        regularUser.setId("regular-user-id");
        regularUser.setFirstName("John");
        regularUser.setLastName("Doe");
        regularUser.setEmail("john.doe@example.com");
        regularUser.setGoogleUserId("google-123");
        regularUser.setChapter(testChapter);
        regularUser.setRol(RolUsuario.Tutorado);
        regularUser.setActiveTutoringLimit(5);
        
        adminUser = new User();
        adminUser.setId("admin-user-id");
        adminUser.setFirstName("Admin");
        adminUser.setLastName("User");
        adminUser.setEmail("admin@example.com");
        adminUser.setGoogleUserId("google-admin");
        adminUser.setChapter(testChapter);
        adminUser.setRol(RolUsuario.Administrador);
        adminUser.setActiveTutoringLimit(10);
        
        tutorUser = new User();
        tutorUser.setId("tutor-user-id");
        tutorUser.setFirstName("Jane");
        tutorUser.setLastName("Smith");
        tutorUser.setEmail("jane.smith@example.com");
        tutorUser.setGoogleUserId("google-tutor");
        tutorUser.setChapter(testChapter);
        tutorUser.setRol(RolUsuario.Tutor);
        tutorUser.setActiveTutoringLimit(3);
    }

    @AfterEach
    void tearDown() {
        UserContext.clear();
        UserContextHelper.setMessageServiceForTesting(null);
    }

    @Test
    void getCurrentUserOrThrow_ShouldReturnUserWhenContextIsSet() {
        // Given
        UserContext.setCurrentUser(regularUser);
        
        // When
        User result = UserContextHelper.getCurrentUserOrThrow();
        
        // Then
        assertNotNull(result);
        assertEquals(regularUser.getId(), result.getId());
        assertEquals(regularUser.getEmail(), result.getEmail());
    }

    @Test
    void getCurrentUserOrThrow_ShouldThrowExceptionWhenNoUserInContext() {
        // Given - no user set in context
        
        // When & Then
        assertThrows(IllegalStateException.class, UserContextHelper::getCurrentUserOrThrow);
    }

    @Test
    void isCurrentUserAdmin_ShouldReturnTrueForAdmin() {
        // Given
        UserContext.setCurrentUser(adminUser);
        
        // When & Then
        assertTrue(UserContextHelper.isCurrentUserAdmin());
    }

    @Test
    void isCurrentUserAdmin_ShouldReturnFalseForRegularUser() {
        // Given
        UserContext.setCurrentUser(regularUser);
        
        // When & Then
        assertFalse(UserContextHelper.isCurrentUserAdmin());
    }

    @Test
    void canAccessUserResource_ShouldAllowUserToAccessOwnResource() {
        // Given
        UserContext.setCurrentUser(regularUser);
        
        // When & Then
        assertTrue(UserContextHelper.canAccessUserResource(regularUser.getId()));
    }

    @Test
    void canAccessUserResource_ShouldDenyUserAccessToOtherResource() {
        // Given
        UserContext.setCurrentUser(regularUser);
        
        // When & Then
        assertFalse(UserContextHelper.canAccessUserResource("other-user-id"));
    }

    @Test
    void canAccessUserResource_ShouldAllowAdminToAccessAnyResource() {
        // Given
        UserContext.setCurrentUser(adminUser);
        
        // When & Then
        assertTrue(UserContextHelper.canAccessUserResource("any-user-id"));
        assertTrue(UserContextHelper.canAccessUserResource(regularUser.getId()));
    }

    @Test
    void requireAdminRole_ShouldPassForAdmin() {
        // Given
        UserContext.setCurrentUser(adminUser);
        
        // When & Then
        assertDoesNotThrow(UserContextHelper::requireAdminRole);
    }

    @Test
    void requireAdminRole_ShouldThrowExceptionForRegularUser() {
        // Given
        UserContext.setCurrentUser(regularUser);
        
        // When & Then
        assertThrows(SecurityException.class, UserContextHelper::requireAdminRole);
    }

    @Test
    void requireResourceAccess_ShouldPassForOwnResource() {
        // Given
        UserContext.setCurrentUser(regularUser);
        
        // When & Then
        assertDoesNotThrow(() -> UserContextHelper.requireResourceAccess(regularUser.getId()));
    }

    @Test
    void requireResourceAccess_ShouldThrowExceptionForOtherResource() {
        // Given
        UserContext.setCurrentUser(regularUser);
        
        // When & Then
        assertThrows(SecurityException.class, () -> UserContextHelper.requireResourceAccess("other-user-id"));
    }

    @Test
    void requireResourceAccess_ShouldPassForAdminAccessingAnyResource() {
        // Given
        UserContext.setCurrentUser(adminUser);
        
        // When & Then
        assertDoesNotThrow(() -> UserContextHelper.requireResourceAccess("any-user-id"));
    }

    @Test
    void getCurrentUserId_ShouldReturnCurrentUserId() {
        // Given
        UserContext.setCurrentUser(regularUser);
        
        // When
        String userId = UserContextHelper.getCurrentUserId();
        
        // Then
        assertEquals(regularUser.getId(), userId);
    }

    @Test
    void getCurrentUserEmail_ShouldReturnCurrentUserEmail() {
        // Given
        UserContext.setCurrentUser(regularUser);
        
        // When
        String email = UserContextHelper.getCurrentUserEmail();
        
        // Then
        assertEquals(regularUser.getEmail(), email);
    }

    @Test
    void getCurrentUserChapterId_ShouldReturnChapterId() {
        // Given
        UserContext.setCurrentUser(regularUser);
        
        // When
        String chapterId = UserContextHelper.getCurrentUserChapterId();
        
        // Then
        assertEquals(testChapter.getId(), chapterId);
    }

    @Test
    void getCurrentUserChapterId_ShouldReturnNullWhenNoChapter() {
        // Given
        regularUser.setChapter(null);
        UserContext.setCurrentUser(regularUser);
        
        // When
        String chapterId = UserContextHelper.getCurrentUserChapterId();
        
        // Then
        assertNull(chapterId);
    }

    @Test
    void getCurrentUserLogInfo_ShouldReturnFormattedString() {
        // Given
        UserContext.setCurrentUser(regularUser);
        
        // When
        String logInfo = UserContextHelper.getCurrentUserLogInfo();
        
        // Then
        assertNotNull(logInfo);
        assertTrue(logInfo.contains(regularUser.getId()));
        assertTrue(logInfo.contains(regularUser.getEmail()));
        assertTrue(logInfo.contains(regularUser.getRol().toString()));
    }

    @Test
    void hasRole_ShouldReturnTrueForMatchingRole() {
        // Given
        UserContext.setCurrentUser(regularUser);
        
        // When & Then
        assertTrue(UserContextHelper.hasRole(RolUsuario.Tutorado));
        assertFalse(UserContextHelper.hasRole(RolUsuario.Administrador));
    }

    @Test
    void canActAsTutor_ShouldReturnTrueForTutor() {
        // Given
        UserContext.setCurrentUser(tutorUser);
        
        // When & Then
        assertTrue(UserContextHelper.canActAsTutor());
    }

    @Test
    void canActAsTutor_ShouldReturnTrueForAdmin() {
        // Given
        UserContext.setCurrentUser(adminUser);
        
        // When & Then
        assertTrue(UserContextHelper.canActAsTutor());
    }

    @Test
    void canActAsTutor_ShouldReturnFalseForRegularUser() {
        // Given
        UserContext.setCurrentUser(regularUser);
        
        // When & Then
        assertFalse(UserContextHelper.canActAsTutor());
    }

    @Test
    void canRequestTutoring_ShouldReturnTrueForTutorado() {
        // Given
        UserContext.setCurrentUser(regularUser);
        
        // When & Then
        assertTrue(UserContextHelper.canRequestTutoring());
    }

    @Test
    void canRequestTutoring_ShouldReturnTrueForAdmin() {
        // Given
        UserContext.setCurrentUser(adminUser);
        
        // When & Then
        assertTrue(UserContextHelper.canRequestTutoring());
    }

    @Test
    void canRequestTutoring_ShouldReturnFalseForTutor() {
        // Given
        UserContext.setCurrentUser(tutorUser);
        
        // When & Then
        assertFalse(UserContextHelper.canRequestTutoring());
    }

    @Test
    void allMethods_ShouldThrowExceptionWhenNoUserInContext() {
        // Given - no user set in context
        
        // When & Then
        assertThrows(IllegalStateException.class, UserContextHelper::getCurrentUserOrThrow);
        assertThrows(IllegalStateException.class, UserContextHelper::isCurrentUserAdmin);
        assertThrows(IllegalStateException.class, () -> UserContextHelper.canAccessUserResource("any-id"));
        assertThrows(IllegalStateException.class, UserContextHelper::requireAdminRole);
        assertThrows(IllegalStateException.class, () -> UserContextHelper.requireResourceAccess("any-id"));
        assertThrows(IllegalStateException.class, UserContextHelper::getCurrentUserId);
        assertThrows(IllegalStateException.class, UserContextHelper::getCurrentUserEmail);
        assertThrows(IllegalStateException.class, UserContextHelper::getCurrentUserChapterId);
        assertThrows(IllegalStateException.class, UserContextHelper::getCurrentUserLogInfo);
        assertThrows(IllegalStateException.class, () -> UserContextHelper.hasRole(RolUsuario.Tutorado));
        assertThrows(IllegalStateException.class, UserContextHelper::canActAsTutor);
        assertThrows(IllegalStateException.class, UserContextHelper::canRequestTutoring);
    }
}