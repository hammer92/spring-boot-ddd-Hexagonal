package com.pragma.usuarios.infrastructure.adapter.input.rest.mapper;

import com.pragma.chapter.domain.model.Chapter;
import com.pragma.chapter.domain.port.input.FindChapterUseCase;
import com.pragma.chapter.infrastructure.adapter.input.rest.mapper.ChapterDtoMapper;
import com.pragma.usuarios.domain.model.User;
import com.pragma.usuarios.infrastructure.adapter.input.rest.dto.CreateUserDto;
import com.pragma.usuarios.infrastructure.adapter.input.rest.dto.UpdateUserRequestDto;
import com.pragma.usuarios.infrastructure.adapter.input.rest.dto.UserDto;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

@Mapper(componentModel = "spring", 
        uses = {ChapterDtoMapper.class},
        injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public abstract class UserDtoMapper {

    @Autowired
    private FindChapterUseCase findChapterUseCase;

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "rol", ignore = true)
    @Mapping(target = "activeTutoringLimit", ignore = true)
    @Mapping(target = "chapter", ignore = true)
    public abstract User toModel(CreateUserDto dto);

    @Mapping(target = "email", ignore = true)
    @Mapping(target = "rol", ignore = true)
    @Mapping(target = "activeTutoringLimit", ignore = true)
    @Mapping(target = "googleUserId", ignore = true)
    @Mapping(target = "chapter", ignore = true)
    public abstract User toModel(UpdateUserRequestDto dto);
    
    public abstract UserDto toDto(User user);

    @AfterMapping
    protected void findAndSetChapter(CreateUserDto dto, @MappingTarget User user) {
        if (dto.getChapterId() != null && !dto.getChapterId().isEmpty()) {
            Optional<Chapter> chapterOpt = findChapterUseCase.findChapterById(dto.getChapterId());
            chapterOpt.ifPresent(user::setChapter);
        }
    }
    
    @AfterMapping
    protected void findAndSetChapterForUpdate(UpdateUserRequestDto dto, @MappingTarget User user) {
        if (dto.getChapterId() != null && !dto.getChapterId().isEmpty()) {
            Optional<Chapter> chapterOpt = findChapterUseCase.findChapterById(dto.getChapterId());
            chapterOpt.ifPresent(user::setChapter);
        }
    }
}