package com.pragma.chapter.infrastructure.adapter.input.rest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pragma.chapter.domain.model.Chapter;
import com.pragma.chapter.domain.port.input.CreateChapterUseCase;
import com.pragma.chapter.domain.port.input.FindChapterUseCase;
import com.pragma.chapter.domain.port.input.GetAllChaptersUseCase;
import com.pragma.chapter.infrastructure.adapter.input.rest.dto.ChapterDto;
import com.pragma.chapter.infrastructure.adapter.input.rest.dto.CreateChapterDto;
import com.pragma.chapter.infrastructure.adapter.input.rest.mapper.ChapterDtoMapper;
import com.pragma.shared.context.UserContext;
import com.pragma.shared.context.UserContextHelper;
import com.pragma.shared.service.MessageService;
import com.pragma.usuarios.domain.model.User;
import com.pragma.usuarios.domain.model.enums.RolUsuario;

@WebMvcTest(ChapterController.class)
class ChapterControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private MessageService messageService;

    @MockBean
    private CreateChapterUseCase createChapterUseCase;

    @MockBean
    private GetAllChaptersUseCase getAllChaptersUseCase;

    @MockBean
    private FindChapterUseCase findChapterUseCase;

    @MockBean
    private ChapterDtoMapper chapterDtoMapper;

    @BeforeEach
    void setUp() {
        User adminUser = new User();
        adminUser.setId("admin-1");
        adminUser.setEmail("admin@test.com");
        adminUser.setRol(RolUsuario.Administrador);
        UserContext.setCurrentUser(adminUser);
        UserContextHelper.setMessageServiceForTesting(messageService);
    }

    @AfterEach
    void tearDown() {
        UserContext.clear();
    }

    @Test
    @DisplayName("When creating chapter as admin with integration test, should create and return chapter")
    void whenCreatingChapterAsAdminWithIntegrationTest_shouldCreateAndReturnChapter() throws Exception {
        // Arrange
        CreateChapterDto createDto = new CreateChapterDto("Backend");
        Chapter chapter = new Chapter(null, "Backend");
        Chapter createdChapter = new Chapter("1", "Backend");
        ChapterDto responseDto = new ChapterDto("1", "Backend");

        when(chapterDtoMapper.toDomain(any(CreateChapterDto.class))).thenReturn(chapter);
        when(createChapterUseCase.createChapter(any(Chapter.class))).thenReturn(createdChapter);
        when(chapterDtoMapper.toDto(createdChapter)).thenReturn(responseDto);
        when(messageService.getMessage("chapter.created")).thenReturn("Chapter created");

        // Act & Assert
        mockMvc.perform(post("/api/v1/chapter")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").value("1"))
                .andExpect(jsonPath("$.data.name").value("Backend"));

        verify(createChapterUseCase).createChapter(any(Chapter.class));
    }
}
