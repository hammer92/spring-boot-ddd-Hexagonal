package com.pragma.usuarios.infrastructure.adapter.input.rest;

import com.pragma.shared.context.UserContext;
import com.pragma.shared.context.UserContextHelper;
import com.pragma.usuarios.domain.model.User;
import com.pragma.usuarios.domain.model.enums.RolUsuario;
import com.pragma.usuarios.domain.port.input.CreateUserUseCase;
import com.pragma.usuarios.domain.port.input.FindUserByIdUseCase;
import com.pragma.usuarios.domain.port.input.UpdateTutoringLimitUseCase;
import com.pragma.usuarios.domain.port.input.UpdateUserRoleUseCase;
import com.pragma.usuarios.domain.port.input.UpdateUserUseCase;
import com.pragma.usuarios.infrastructure.adapter.input.rest.dto.CreateUserDto;
import com.pragma.usuarios.infrastructure.adapter.input.rest.dto.UpdateTutoringLimitDto;
import com.pragma.usuarios.infrastructure.adapter.input.rest.dto.UpdateUserRequestDto;
import com.pragma.usuarios.infrastructure.adapter.input.rest.dto.UpdateUserRoleDto;
import com.pragma.usuarios.infrastructure.adapter.input.rest.dto.UserDto;
import com.pragma.usuarios.infrastructure.adapter.input.rest.mapper.UserDtoMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final CreateUserUseCase createUserUseCase;
    private final UpdateUserUseCase updateUserUseCase;
    private final FindUserByIdUseCase findUserByIdUseCase;
    private final UpdateUserRoleUseCase updateUserRoleUseCase;
    private final UpdateTutoringLimitUseCase updateTutoringLimitUseCase;
    private final UserDtoMapper userDtoMapper;

    @PostMapping
    public ResponseEntity<UserDto> createUser(@Valid @RequestBody CreateUserDto createUserDto) {
        // Only admins can create users - check this BEFORE validation
        UserContextHelper.requireAdminRole();
        
        log.info("Creating new user: {}", createUserDto.getEmail());
        
        User user = userDtoMapper.toModel(createUserDto);
        User createdUser = createUserUseCase.createUser(user);
        log.info("Successfully created user with ID: {}", createdUser.getId());
        return new ResponseEntity<>(userDtoMapper.toDto(createdUser), HttpStatus.CREATED);
    }

    @PutMapping
    public ResponseEntity<UserDto> updateUser(@Valid @RequestBody UpdateUserRequestDto requestDto) {
        log.info("User {} updating user with ID: {}", UserContextHelper.getCurrentUserEmail(), requestDto.getId());
        
        // Users can only update themselves unless they are admin
        UserContextHelper.requireResourceAccess(requestDto.getId());
        
        User userToUpdate = userDtoMapper.toModel(requestDto);
        return updateUserUseCase.updateUser(requestDto.getId(), userToUpdate)
                .map(updatedUser -> {
                    log.info("User {} successfully updated user with ID: {}", UserContextHelper.getCurrentUserEmail(), updatedUser.getId());
                    return ResponseEntity.ok(userDtoMapper.toDto(updatedUser));
                })
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getUserById(@PathVariable String id) {
        log.debug("User {} requesting user with ID: {}", UserContextHelper.getCurrentUserEmail(), id);
        
        return findUserByIdUseCase.findUserById(id)
                .map(user -> ResponseEntity.ok(userDtoMapper.toDto(user)))
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/me")
    public ResponseEntity<com.pragma.shared.dto.OkResponseDto<UserDto>> getCurrentUser() {
        User currentUser = UserContextHelper.getCurrentUserOrThrow();
        log.debug("User {} requesting own profile", currentUser.getEmail());
        UserDto userDto = userDtoMapper.toDto(currentUser);
        return ResponseEntity.ok(com.pragma.shared.dto.OkResponseDto.of("Usuario obtenido exitosamente", userDto));
    }
    
    @PatchMapping("/role")
    public ResponseEntity<UserDto> updateUserRole(@Valid @RequestBody UpdateUserRoleDto requestDto) {
        log.info("User {} updating role for user {} to {}", 
                UserContextHelper.getCurrentUserEmail(), requestDto.getId(), requestDto.getRole());
        
        // Only admins can update user roles
        UserContextHelper.requireAdminRole();
        
        return updateUserRoleUseCase.updateUserRole(requestDto.getId(), requestDto.getRole())
                .map(updatedUser -> {
                    log.info("User {} successfully updated role for user {} to {}", 
                            UserContextHelper.getCurrentUserEmail(), updatedUser.getId(), updatedUser.getRol());
                    return ResponseEntity.ok(userDtoMapper.toDto(updatedUser));
                })
                .orElse(ResponseEntity.notFound().build());
    }
    
    @PatchMapping("/tutoring-limit")
    public ResponseEntity<UserDto> updateTutoringLimit(@Valid @RequestBody UpdateTutoringLimitDto requestDto) {
        log.info("User {} updating tutoring limit for user {} to {}", 
                UserContextHelper.getCurrentUserEmail(), requestDto.getId(), requestDto.getActiveTutoringLimit());
        
        // Only admins can update tutoring limits
        UserContextHelper.requireAdminRole();
        
        return updateTutoringLimitUseCase.updateTutoringLimit(
                requestDto.getId(), 
                requestDto.getActiveTutoringLimit())
                .map(updatedUser -> {
                    log.info("User {} successfully updated tutoring limit for user {} to {}", 
                            UserContextHelper.getCurrentUserEmail(), updatedUser.getId(), updatedUser.getActiveTutoringLimit());
                    return ResponseEntity.ok(userDtoMapper.toDto(updatedUser));
                })
                .orElse(ResponseEntity.status(HttpStatus.FORBIDDEN).build());
    }
}