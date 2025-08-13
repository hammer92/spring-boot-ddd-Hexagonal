package com.pragma.chapter.application.service;

import com.pragma.chapter.domain.model.Chapter;
import com.pragma.chapter.domain.port.output.ChapterRepository;
import com.pragma.shared.service.MessageService;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
public class ChapterServiceTest {

    @Mock
    private ChapterRepository chapterRepository;

    @InjectMocks
    private ChapterService chapterService;

    @Mock
    private MessageService mockmessageService;

    /**
     * Test the findChapterById method when the chapter is not found.
     * This test verifies that an empty Optional is returned when attempting to find a chapter with a non-existent ID.
     */
    @Test
    public void testFindChapterById_ChapterNotFound() {
        // Arrange
        String nonExistentId = "non-existent-id";
        when(chapterRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // Act
        Optional<Chapter> result = chapterService.findChapterById(nonExistentId);

        // Assert
        assertFalse(result.isPresent());
        verify(chapterRepository).findById(nonExistentId);
    }

    /**
     * Test case for createChapter method when a new chapter is successfully
     * created.
     * It verifies that the method calls the repository's save method and returns
     * the saved chapter.
     */
    @Test
    public void test_createChapter_returnsSavedChapter() {
        Chapter inputChapter = new Chapter();
        inputChapter.setId("1");
        inputChapter.setName("Test Chapter");

        when(chapterRepository.save(any(Chapter.class))).thenReturn(inputChapter);

        Chapter result = chapterService.createChapter(inputChapter);

        verify(chapterRepository).save(inputChapter);
        assertEquals(inputChapter, result);
    }

    /**
     * Test case for findChapterById when the chapter exists.
     * It verifies that the method returns the correct chapter when found in the
     * repository.
     */
    @Test
    public void test_findChapterById_whenChapterExists() {
        String id = "123";
        Chapter expectedChapter = new Chapter();
        expectedChapter.setId(id);

        when(chapterRepository.findById(id)).thenReturn(Optional.of(expectedChapter));

        Optional<Chapter> result = chapterService.findChapterById(id);

        assertNotNull(result);
        assertTrue(result.isPresent());
        assertEquals(id, result.get().getId());
        verify(chapterRepository, times(1)).findById(id);
    }

    /**
     * Test that getAllChapters returns all chapters from the repository.
     * This test verifies that the method correctly delegates to the repository's
     * findAll method
     * and returns the result without modification.
     */
    @Test
    public void test_getAllChapters_returnsAllChaptersFromRepository() {
        // Arrange
        List<Chapter> expectedChapters = Arrays.asList(
                new Chapter("1", "Chapter 1"),
                new Chapter("2", "Chapter 2"));
        when(chapterRepository.findAll()).thenReturn(expectedChapters);

        // Act
        List<Chapter> actualChapters = chapterService.getAllChapters();

        // Assert
        assertEquals(expectedChapters, actualChapters);
        verify(chapterRepository).findAll();
    }

}
