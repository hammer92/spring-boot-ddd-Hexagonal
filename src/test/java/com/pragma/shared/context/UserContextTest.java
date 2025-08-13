package com.pragma.shared.context;

import com.pragma.chapter.domain.model.Chapter;
import com.pragma.usuarios.domain.model.User;
import com.pragma.usuarios.domain.model.enums.RolUsuario;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class UserContextTest {

    private User testUser;

    @BeforeEach
    void setUp() {
        // Clear any existing context before each test
        UserContext.clear();
        
        // Create a test user
        testUser = new User();
        testUser.setId("test-user-id");
        testUser.setFirstName("John");
        testUser.setLastName("Doe");
        testUser.setEmail("john.doe@example.com");
        testUser.setGoogleUserId("google-123");
        testUser.setRol(RolUsuario.Tutorado);
        testUser.setActiveTutoringLimit(5);
    }

    @AfterEach
    void tearDown() {
        // Clean up after each test
        UserContext.clear();
    }

    @Test
    void setCurrentUser_ShouldSetUserInCurrentThread() {
        // When
        UserContext.setCurrentUser(testUser);
        
        // Then
        User retrievedUser = UserContext.getCurrentUser();
        assertNotNull(retrievedUser);
        assertEquals(testUser.getId(), retrievedUser.getId());
        assertEquals(testUser.getEmail(), retrievedUser.getEmail());
        assertEquals(testUser.getGoogleUserId(), retrievedUser.getGoogleUserId());
    }

    @Test
    void getCurrentUser_ShouldReturnNullWhenNoUserSet() {
        // When
        User retrievedUser = UserContext.getCurrentUser();
        
        // Then
        assertNull(retrievedUser);
    }

    @Test
    void hasCurrentUser_ShouldReturnTrueWhenUserIsSet() {
        // Given
        UserContext.setCurrentUser(testUser);
        
        // When & Then
        assertTrue(UserContext.hasCurrentUser());
    }

    @Test
    void hasCurrentUser_ShouldReturnFalseWhenNoUserSet() {
        // When & Then
        assertFalse(UserContext.hasCurrentUser());
    }

    @Test
    void clear_ShouldRemoveUserFromCurrentThread() {
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
    void userContext_ShouldBeIsolatedBetweenThreads() throws InterruptedException {
        // Given
        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch latch = new CountDownLatch(2);
        AtomicReference<User> thread1User = new AtomicReference<>();
        AtomicReference<User> thread2User = new AtomicReference<>();
        
        User user1 = new User();
        user1.setId("user1");
        user1.setEmail("user1@example.com");
        user1.setGoogleUserId("google-user1");
        
        User user2 = new User();
        user2.setId("user2");
        user2.setEmail("user2@example.com");
        user2.setGoogleUserId("google-user2");
        
        try {
            // When - Set different users in different threads
            executor.submit(() -> {
                try {
                    UserContext.setCurrentUser(user1);
                    // Small delay to ensure both threads are running concurrently
                    Thread.sleep(100);
                    thread1User.set(UserContext.getCurrentUser());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    latch.countDown();
                }
            });
            
            executor.submit(() -> {
                try {
                    UserContext.setCurrentUser(user2);
                    // Small delay to ensure both threads are running concurrently
                    Thread.sleep(100);
                    thread2User.set(UserContext.getCurrentUser());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    latch.countDown();
                }
            });
            
            // Wait for both threads to complete
            assertTrue(latch.await(5, TimeUnit.SECONDS));
            
            // Then - Each thread should have its own user context
            assertNotNull(thread1User.get());
            assertNotNull(thread2User.get());
            assertEquals("user1", thread1User.get().getId());
            assertEquals("user2", thread2User.get().getId());
            assertNotEquals(thread1User.get().getId(), thread2User.get().getId());
            
        } finally {
            executor.shutdown();
        }
    }

    @Test
    void userContext_ShouldNotLeakBetweenThreads() throws InterruptedException {
        // Given
        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch setupLatch = new CountDownLatch(1);
        CountDownLatch verifyLatch = new CountDownLatch(1);
        AtomicReference<User> thread2User = new AtomicReference<>();
        
        try {
            // Thread 1: Set user and signal completion
            executor.submit(() -> {
                UserContext.setCurrentUser(testUser);
                setupLatch.countDown();
            });
            
            // Wait for thread 1 to complete
            assertTrue(setupLatch.await(5, TimeUnit.SECONDS));
            
            // Thread 2: Try to access user context (should be null)
            executor.submit(() -> {
                thread2User.set(UserContext.getCurrentUser());
                verifyLatch.countDown();
            });
            
            // Wait for thread 2 to complete
            assertTrue(verifyLatch.await(5, TimeUnit.SECONDS));
            
            // Then - Thread 2 should not see the user from Thread 1
            assertNull(thread2User.get());
            
        } finally {
            executor.shutdown();
        }
    }

    @Test
    void clear_ShouldNotAffectOtherThreads() throws InterruptedException {
        // Given
        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch setupLatch = new CountDownLatch(2);
        CountDownLatch clearLatch = new CountDownLatch(1);
        CountDownLatch verifyLatch = new CountDownLatch(1);
        AtomicReference<User> thread2UserAfterClear = new AtomicReference<>();
        
        User user1 = new User();
        user1.setId("user1");
        user1.setEmail("user1@example.com");
        
        User user2 = new User();
        user2.setId("user2");
        user2.setEmail("user2@example.com");
        
        try {
            // Thread 1: Set user, wait for signal, then clear
            executor.submit(() -> {
                UserContext.setCurrentUser(user1);
                setupLatch.countDown();
                try {
                    clearLatch.await(5, TimeUnit.SECONDS);
                    UserContext.clear();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
            
            // Thread 2: Set user and wait
            executor.submit(() -> {
                UserContext.setCurrentUser(user2);
                setupLatch.countDown();
                try {
                    clearLatch.await(5, TimeUnit.SECONDS);
                    // Small delay to ensure thread 1 has cleared its context
                    Thread.sleep(100);
                    thread2UserAfterClear.set(UserContext.getCurrentUser());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    verifyLatch.countDown();
                }
            });
            
            // Wait for both threads to set their users
            assertTrue(setupLatch.await(5, TimeUnit.SECONDS));
            
            // Signal thread 1 to clear its context
            clearLatch.countDown();
            
            // Wait for verification to complete
            assertTrue(verifyLatch.await(5, TimeUnit.SECONDS));
            
            // Then - Thread 2 should still have its user after Thread 1 clears
            assertNotNull(thread2UserAfterClear.get());
            assertEquals("user2", thread2UserAfterClear.get().getId());
            
        } finally {
            executor.shutdown();
        }
    }
}