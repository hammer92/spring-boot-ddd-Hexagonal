package com.pragma.shared.context;

import com.pragma.usuarios.domain.model.User;
import com.pragma.usuarios.domain.model.enums.RolUsuario;
import com.pragma.chapter.domain.model.Chapter;

public class TestUserContextHelper {

    public static User createTestUser() {
        User user = new User();
        user.setId("test-user-id");
        user.setFirstName("Test");
        user.setLastName("User");
        user.setEmail("test@example.com");
        user.setGoogleUserId("google-test-id");
        user.setRol(RolUsuario.Administrador);
        user.setActiveTutoringLimit(5);
        
        Chapter chapter = new Chapter();
        chapter.setId("test-chapter-id");
        chapter.setName("Test Chapter");
        user.setChapter(chapter);
        
        return user;
    }

    public static void setTestUserContext() {
        UserContext.setCurrentUser(createTestUser());
    }

    public static void clearUserContext() {
        UserContext.clear();
    }
}