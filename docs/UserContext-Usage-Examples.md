# UserContext Usage Examples

This document demonstrates how to use UserContext in controllers to access authenticated user information and implement proper authorization checks.

## Overview

The UserContext system provides thread-local storage for authenticated user information, making it available throughout the request lifecycle. The system includes:

- `UserContext`: Core thread-local storage for user information
- `UserContextHelper`: Utility class with common operations and authorization checks
- Integration with existing controllers for seamless user access

## Basic Usage Patterns

### 1. Getting Current User Information

```java
@RestController
@RequestMapping("/api/v1/example")
@RequiredArgsConstructor
@Slf4j
public class ExampleController {

    @GetMapping("/profile")
    public ResponseEntity<UserDto> getCurrentUserProfile() {
        // Get current user using helper (recommended approach)
        User currentUser = UserContextHelper.getCurrentUserOrThrow();
        
        log.info("User {} accessing profile", currentUser.getEmail());
        
        // Use user information
        return ResponseEntity.ok(userMapper.toDto(currentUser));
    }
}
```

### 2. Authorization Checks

```java
@PostMapping("/admin-only")
public ResponseEntity<String> adminOnlyEndpoint() {
    // Require admin role - throws SecurityException if not admin
    UserContextHelper.requireAdminRole();
    
    User currentUser = UserContextHelper.getCurrentUserOrThrow();
    log.info("Admin {} performing admin operation", currentUser.getEmail());
    
    return ResponseEntity.ok("Admin operation completed");
}

@GetMapping("/user/{userId}/data")
public ResponseEntity<UserDataDto> getUserData(@PathVariable String userId) {
    // Check if user can access this resource (own resource or admin)
    UserContextHelper.requireResourceAccess(userId);
    
    // Proceed with operation
    return ResponseEntity.ok(getUserDataById(userId));
}
```

### 3. Role-Based Operations

```java
@PostMapping("/tutoring")
public ResponseEntity<TutoringDto> createTutoring(@RequestBody CreateTutoringDto dto) {
    User currentUser = UserContextHelper.getCurrentUserOrThrow();
    
    // Check if user can act as tutor
    if (!UserContextHelper.canActAsTutor()) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(OkResponseDto.of("No tiene permisos para crear tutorías", null));
    }
    
    // Set current user as the tutor
    dto.setTutorId(currentUser.getId());
    
    // Proceed with creation
    return createTutoringInternal(dto);
}
```

### 4. Graceful Error Handling

```java
@GetMapping("/optional-user-info")
public ResponseEntity<Map<String, Object>> getOptionalUserInfo() {
    Map<String, Object> response = new HashMap<>();
    
    // Check if user context is available without throwing exceptions
    if (UserContext.hasCurrentUser()) {
        try {
            User currentUser = UserContext.getCurrentUser();
            response.put("authenticatedUser", currentUser.getEmail());
            response.put("userRole", currentUser.getRol().name());
        } catch (Exception e) {
            log.warn("Error accessing user context: {}", e.getMessage());
            response.put("userContextError", e.getMessage());
        }
    } else {
        response.put("message", "No authenticated user");
    }
    
    return ResponseEntity.ok(response);
}
```

## UserContextHelper Utility Methods

### User Information Access

```java
// Get current user (throws IllegalStateException if no user)
User user = UserContextHelper.getCurrentUserOrThrow();

// Get user ID
String userId = UserContextHelper.getCurrentUserId();

// Get user email (useful for logging)
String email = UserContextHelper.getCurrentUserEmail();

// Get user's chapter ID (may be null)
String chapterId = UserContextHelper.getCurrentUserChapterId();

// Get formatted log information
String logInfo = UserContextHelper.getCurrentUserLogInfo();
```

### Role and Permission Checks

```java
// Check if current user is admin
boolean isAdmin = UserContextHelper.isCurrentUserAdmin();

// Check specific role
boolean isStudent = UserContextHelper.hasRole(RolUsuario.Tutorado);
boolean isTutor = UserContextHelper.hasRole(RolUsuario.Tutor);

// Check capabilities
boolean canTutor = UserContextHelper.canActAsTutor();
boolean canRequest = UserContextHelper.canRequestTutoring();

// Check resource access
boolean canAccess = UserContextHelper.canAccessUserResource(resourceUserId);
```

### Authorization Enforcement

```java
// Require admin role (throws SecurityException if not admin)
UserContextHelper.requireAdminRole();

// Require resource access (throws SecurityException if denied)
UserContextHelper.requireResourceAccess(resourceUserId);
```

## Controller Examples

### Enhanced SkillController

The SkillController demonstrates comprehensive UserContext integration:

```java
@PostMapping
public ResponseEntity<OkResponseDto<SkillDto>> createSkill(@Valid @RequestBody CreateSkillDto createSkillDto) {
    User currentUser = UserContextHelper.getCurrentUserOrThrow();
    log.info("User {} creating skill: {}", currentUser.getEmail(), createSkillDto.getName());
    
    // Only admins can create skills
    UserContextHelper.requireAdminRole();
    
    Skill skill = skillDtoMapper.toModel(createSkillDto);
    Skill createdSkill = createSkillUseCase.createSkill(skill);
    SkillDto skillDto = skillDtoMapper.toDto(createdSkill);
    
    log.info("User {} successfully created skill with ID: {}", currentUser.getEmail(), createdSkill.getId());
    
    return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(OkResponseDto.of("Habilidad creada exitosamente", skillDto));
}
```

### ProfileController Examples

The ProfileController shows various UserContext usage patterns:

```java
@GetMapping("/permissions")
public ResponseEntity<OkResponseDto<Map<String, Object>>> getUserPermissions() {
    try {
        User currentUser = UserContextHelper.getCurrentUserOrThrow();
        
        Map<String, Object> permissions = new HashMap<>();
        permissions.put("isAdmin", UserContextHelper.isCurrentUserAdmin());
        permissions.put("canActAsTutor", UserContextHelper.canActAsTutor());
        permissions.put("canRequestTutoring", UserContextHelper.canRequestTutoring());
        
        return ResponseEntity.ok(OkResponseDto.of("Permisos obtenidos exitosamente", permissions));
        
    } catch (IllegalStateException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(OkResponseDto.of("Usuario no autenticado", null));
    }
}
```

## Best Practices

### 1. Use UserContextHelper

Always prefer `UserContextHelper` methods over direct `UserContext` access:

```java
// ✅ Recommended
User user = UserContextHelper.getCurrentUserOrThrow();

// ❌ Avoid direct access
User user = UserContext.getCurrentUser();
```

### 2. Handle Authentication Gracefully

Always handle cases where user context might not be available:

```java
// ✅ Good error handling
try {
    User currentUser = UserContextHelper.getCurrentUserOrThrow();
    // ... use currentUser
} catch (IllegalStateException e) {
    log.error("No authenticated user: {}", e.getMessage());
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(OkResponseDto.of("Usuario no autenticado", null));
}
```

### 3. Use Appropriate Authorization Methods

Choose the right authorization method for your use case:

```java
// For admin-only operations
UserContextHelper.requireAdminRole();

// For resource access validation
UserContextHelper.requireResourceAccess(resourceUserId);

// For conditional logic
if (UserContextHelper.canActAsTutor()) {
    // Allow tutor operations
}
```

### 4. Log User Actions

Always log user actions for audit purposes:

```java
User currentUser = UserContextHelper.getCurrentUserOrThrow();
log.info("User {} performing operation: {}", currentUser.getEmail(), operationName);
```

### 5. Set User Information in DTOs

When creating resources, set the current user as the owner:

```java
User currentUser = UserContextHelper.getCurrentUserOrThrow();
createDto.setUserId(currentUser.getId());
```

## Thread Safety

UserContext uses ThreadLocal storage, ensuring thread safety in multi-threaded environments:

- Each thread has its own user context
- Context is automatically isolated between requests
- Context cleanup happens after each request (via interceptor)

## Error Handling

The system provides specific exceptions for different scenarios:

- `IllegalStateException`: No user in context
- `SecurityException`: Authorization denied
- `MissingAuthorizationException`: Missing authorization header
- `InvalidAuthorizationException`: Invalid authorization header
- `UserNotFoundException`: User not found in database

## Testing

When testing controllers with UserContext:

```java
@Test
void testWithAuthenticatedUser() {
    // Set up user context for test
    UserContext.setCurrentUser(testUser);
    
    // Perform test
    // ...
    
    // Clean up (or use @AfterEach)
    UserContext.clear();
}
```

## Integration with Existing Code

The UserContext system integrates seamlessly with existing controllers:

1. **UserController**: Already uses UserContextHelper for user operations
2. **TutoringController**: Enhanced with better authorization checks
3. **FeedbackController**: Uses UserContext for evaluator identification
4. **ChapterController**: Uses UserContextHelper for admin operations
5. **SkillController**: Fully integrated with UserContext for all operations

This provides a consistent and secure way to handle user authentication and authorization across the entire application.