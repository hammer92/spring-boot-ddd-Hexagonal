package com.pragma.chapter.infrastructure.adapter.input.rest;

import java.net.URI;
import java.util.List;
import java.util.Optional;

import com.pragma.shared.context.UserContextHelper;
import com.pragma.shared.dto.ErrorResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.pragma.chapter.domain.model.Chapter;
import com.pragma.chapter.domain.port.input.CreateChapterUseCase;
import com.pragma.chapter.domain.port.input.FindChapterUseCase;
import com.pragma.chapter.domain.port.input.GetAllChaptersUseCase;
import com.pragma.chapter.infrastructure.adapter.input.rest.dto.ChapterDto;
import com.pragma.chapter.infrastructure.adapter.input.rest.dto.CreateChapterDto;
import com.pragma.chapter.infrastructure.adapter.input.rest.mapper.ChapterDtoMapper;
import com.pragma.shared.dto.OkResponseDto;
import com.pragma.shared.service.MessageService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/chapter")
@Slf4j
public class ChapterController {

    private final MessageService messageService;

    
    private final CreateChapterUseCase createChapterUseCase;

    
    private final GetAllChaptersUseCase getAllChaptersUseCase;

    
    private final FindChapterUseCase findChapterUseCase;

    
    private final ChapterDtoMapper chapterDtoMapper;

    @GetMapping("/")
    public ResponseEntity<OkResponseDto<List<ChapterDto>>> getAllChapters() {
        List<Chapter> chapters = getAllChaptersUseCase.getAllChapters();
        List<ChapterDto> chaptersDto = chapterDtoMapper.toListDto(chapters);
        return ResponseEntity.ok(OkResponseDto.of(messageService.getMessage("general.success"),chaptersDto));
    }


    @PostMapping("/")
    public ResponseEntity<OkResponseDto<ChapterDto>> postCreate(@Valid @RequestBody CreateChapterDto createChapterDto) {
        log.info("User {} creating chapter  to {}",
                UserContextHelper.getCurrentUserEmail(), createChapterDto.getName());

        // Only admins can update user roles
        UserContextHelper.requireAdminRole();

        Chapter chapter = chapterDtoMapper.toDomain(createChapterDto);
        Chapter createdChapter = createChapterUseCase.createChapter(chapter);
        ChapterDto responseDto = chapterDtoMapper.toDto(createdChapter);

        OkResponseDto<ChapterDto> okResponseDto = OkResponseDto.of(messageService.getMessage("chapter.created"), responseDto);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(responseDto.getId())
                .toUri();

        return ResponseEntity.created(location).body(okResponseDto);

    }

    @GetMapping("/{id}")
    public ResponseEntity<OkResponseDto<ChapterDto>> getFindChapter(@PathVariable String id) {
        try {
            Optional<Chapter> chapter = findChapterUseCase.findChapterById(id);
            if(chapter.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(OkResponseDto.of(messageService.getMessage("chapter.not.found"), null));
            }
            ChapterDto chapterDto = chapterDtoMapper.toDto(chapter.get());
            return ResponseEntity.ok(OkResponseDto.of(messageService.getMessage("general.success"),chapterDto));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(OkResponseDto.of(e.getMessage(), null));
        }
        
    }
}