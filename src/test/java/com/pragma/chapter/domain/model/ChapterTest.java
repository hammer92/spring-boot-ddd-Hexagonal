package com.pragma.chapter.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ChapterTest {

    @Test
    @DisplayName("When creating chapter with all args constructor, should set all fields")
    void whenCreatingChapterWithAllArgsConstructor_shouldSetAllFields() {
        // Arrange & Act
        Chapter chapter = new Chapter("1", "Backend");

        // Assert
        assertEquals("1", chapter.getId());
        assertEquals("Backend", chapter.getName());
    }

    @Test
    @DisplayName("When creating chapter with no args constructor, should create empty chapter")
    void whenCreatingChapterWithNoArgsConstructor_shouldCreateEmptyChapter() {
        // Arrange & Act
        Chapter chapter = new Chapter();

        // Assert
        assertNotNull(chapter);
    }

    @Test
    @DisplayName("When setting id, should update id field")
    void whenSettingId_shouldUpdateIdField() {
        // Arrange
        Chapter chapter = new Chapter();

        // Act
        chapter.setId("123");

        // Assert
        assertEquals("123", chapter.getId());
    }

    @Test
    @DisplayName("When setting name, should update name field")
    void whenSettingName_shouldUpdateNameField() {
        // Arrange
        Chapter chapter = new Chapter();

        // Act
        chapter.setName("Frontend");

        // Assert
        assertEquals("Frontend", chapter.getName());
    }

    @Test
    @DisplayName("When comparing equal chapters, should return true")
    void whenComparingEqualChapters_shouldReturnTrue() {
        // Arrange
        Chapter chapter1 = new Chapter("1", "Backend");
        Chapter chapter2 = new Chapter("1", "Backend");

        // Act & Assert
        assertEquals(chapter1, chapter2);
        assertEquals(chapter1.hashCode(), chapter2.hashCode());
    }

    @Test
    @DisplayName("When comparing different chapters, should return false")
    void whenComparingDifferentChapters_shouldReturnFalse() {
        // Arrange
        Chapter chapter1 = new Chapter("1", "Backend");
        Chapter chapter2 = new Chapter("2", "Frontend");

        // Act & Assert
        assertNotEquals(chapter1, chapter2);
    }

    @Test
    @DisplayName("When calling toString, should return string representation")
    void whenCallingToString_shouldReturnStringRepresentation() {
        // Arrange
        Chapter chapter = new Chapter("1", "Backend");

        // Act
        String result = chapter.toString();

        // Assert
        assertNotNull(result);
        assertEquals("Chapter(id=1, name=Backend)", result);
    }
}
