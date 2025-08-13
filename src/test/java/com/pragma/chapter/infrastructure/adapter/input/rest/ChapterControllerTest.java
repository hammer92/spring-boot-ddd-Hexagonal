package com.pragma.chapter.infrastructure.adapter.input.rest;

import com.pragma.chapter.domain.model.Chapter;
import com.pragma.chapter.domain.port.input.CreateChapterUseCase;
import com.pragma.chapter.domain.port.input.FindChapterUseCase;
import com.pragma.chapter.domain.port.input.GetAllChaptersUseCase;
import com.pragma.chapter.infrastructure.adapter.input.rest.dto.ChapterDto;
import com.pragma.chapter.infrastructure.adapter.input.rest.dto.CreateChapterDto;
import com.pragma.chapter.infrastructure.adapter.input.rest.mapper.ChapterDtoMapper;
import com.pragma.shared.context.UserContextHelper;
import com.pragma.shared.dto.OkResponseDto;
import com.pragma.shared.service.MessageService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@SpringBootTest
public class ChapterControllerTest {

    @InjectMocks
    private ChapterController chapterController;

    @Mock
    private ChapterDtoMapper chapterDtoMapper;

    @Mock
    private CreateChapterUseCase createChapterUseCase;

    @Mock
    private FindChapterUseCase findChapterUseCase;

    @Mock
    private GetAllChaptersUseCase getAllChaptersUseCase;

    @Mock
    private MessageService mockmessageService;

    String successMessage = "Exitoso";



    /**
     * Tests the behavior of getAllChapters when no chapters are found.
     * This test verifies that the method handles the case of an empty list correctly,
     * returning an empty list in the response rather than throwing an exception.
     */
    @SuppressWarnings("null")
    @Test
    public void testGetAllChapters_NoChaptersFound() {
        // Arrange
        when(getAllChaptersUseCase.getAllChapters()).thenReturn(new ArrayList<>());
        when(chapterDtoMapper.toListDto(anyList())).thenReturn(new ArrayList<>());

        // Act
        ResponseEntity<OkResponseDto<List<ChapterDto>>> response = chapterController.getAllChapters();

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().getData().isEmpty());
    }

    /**
     * Test the getFindChapter method when the chapter is not found.
     * This test verifies that when the findChapterById method of FindChapterUseCase
     * returns null (simulating a chapter not found scenario), the controller
     * handles it appropriately by returning a not found response.
     */
    @SuppressWarnings("null")
    @Test
    public void testGetFindChapter_ChapterNotFound() {
        // Arrange
        String nonExistentId = "nonexistent-id";
        when(findChapterUseCase.findChapterById(nonExistentId)).thenThrow();
        

        // Act
        ResponseEntity<OkResponseDto<ChapterDto>> response = chapterController.getFindChapter(nonExistentId);

        // Assert
        assertNotNull(response.getBody());
        assertNull(response.getBody().getData());
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    /**
     * Tests the getAllChapters method of ChapterController.
     * Verifies that the method returns a ResponseEntity with an OkResponseDto
     * containing a success message and a list of ChapterDto objects.
     */
    @SuppressWarnings("null")
    @Test
    public void test_getAllChapters_returnsSuccessResponseWithChapterList() {
        // Arrange
        List<Chapter> chapters = Arrays.asList(
            new Chapter("1", "Chapter 1"),
            new Chapter("2", "Chapter 2")
        );        
        List<ChapterDto> chapterDtos = Arrays.asList(new ChapterDto(), new ChapterDto());

        when(getAllChaptersUseCase.getAllChapters()).thenReturn(chapters);
        when(chapterDtoMapper.toListDto(chapters)).thenReturn(chapterDtos);
        when(mockmessageService.getMessage("general.success")).thenReturn(successMessage);

        // Act
        ResponseEntity<OkResponseDto<List<ChapterDto>>> response = chapterController.getAllChapters();

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(successMessage, response.getBody().getMessage());
        assertEquals(chapterDtos, response.getBody().getData());

        verify(getAllChaptersUseCase).getAllChapters();
        verify(chapterDtoMapper).toListDto(eq(chapters));
    }

    /**
     * Test case for getFindChapter method when a valid chapter ID is provided.
     * It verifies that the method returns a ResponseEntity with an OkResponseDto
     * containing the correct message and ChapterDto.
     */
    @Test
    public void test_getFindChapter_validId() {
        // Arrange
        String chapterId = "123";
        Chapter mockChapter = new Chapter();
        ChapterDto mockChapterDto = new ChapterDto();

        when(findChapterUseCase.findChapterById(chapterId)).thenReturn(Optional.of(mockChapter));
        when(chapterDtoMapper.toDto(mockChapter)).thenReturn(mockChapterDto);
        when(mockmessageService.getMessage("general.success")).thenReturn(successMessage);

        // Act
        ResponseEntity<OkResponseDto<ChapterDto>> response = chapterController.getFindChapter(chapterId);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        OkResponseDto<ChapterDto> responseBody = response.getBody();
        assertNotNull(responseBody);
        assertEquals(successMessage, responseBody.getMessage());
        assertEquals(mockChapterDto, responseBody.getData());

        verify(findChapterUseCase).findChapterById(chapterId);
        verify(chapterDtoMapper).toDto(mockChapter);
    }

    /**
     * Test case for successful chapter creation through the postCreate method.
     * This test verifies that:
     * 1. The chapter is correctly mapped from DTO to domain model
     * 2. The chapter is created using the CreateChapterUseCase
     * 3. The created chapter is mapped back to DTO
     * 4. A ResponseEntity with CREATED status and correct body is returned
     * 5. The location header in the response contains the correct URI
     */
    @SuppressWarnings("null")
    @Test
    public void test_postCreate_successfulChapterCreation() {
        // Arrange
        String messageCreated = "Chapter creado exitosamente";
        CreateChapterDto createChapterDto = new CreateChapterDto();
        Chapter chapter = new Chapter();
        Chapter createdChapter = new Chapter();
        ChapterDto responseDto = new ChapterDto();
        responseDto.setId("123");

        when(chapterDtoMapper.toDomain(createChapterDto)).thenReturn(chapter);
        when(createChapterUseCase.createChapter(chapter)).thenReturn(createdChapter);
        when(chapterDtoMapper.toDto(createdChapter)).thenReturn(responseDto);
        when(mockmessageService.getMessage("chapter.created")).thenReturn(messageCreated);

        // Act & Assert
        try (MockedStatic<UserContextHelper> mockedUserContext = mockStatic(UserContextHelper.class)) {
            mockedUserContext.when(UserContextHelper::getCurrentUserEmail).thenReturn("test@example.com");
            mockedUserContext.when(UserContextHelper::requireAdminRole).thenAnswer(invocation -> null);
            
            ResponseEntity<OkResponseDto<ChapterDto>> response = chapterController.postCreate(createChapterDto);

            // Assert
            verify(chapterDtoMapper).toDomain(createChapterDto);
            verify(createChapterUseCase).createChapter(chapter);
            verify(chapterDtoMapper).toDto(createdChapter);

            assertEquals(HttpStatus.CREATED, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(messageCreated, response.getBody().getMessage());
            assertEquals(responseDto, response.getBody().getData());

            String expectedLocation = ServletUriComponentsBuilder
                    .fromCurrentRequest()
                    .path("/{id}")
                    .buildAndExpand("123")
                    .toUriString();
            assertNotNull(response.getHeaders());
            assertNotNull(response.getHeaders().getLocation());
            assertEquals(expectedLocation, response.getHeaders().getLocation().toString());
        }
    }

}
