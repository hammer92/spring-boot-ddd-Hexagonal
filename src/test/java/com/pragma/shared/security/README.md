# Authentication Flow Integration Tests

This directory contains comprehensive integration tests for the Google Authentication middleware implementation. The tests verify the complete authentication flow from HTTP request processing to user context availability in controllers.

## Test Files Overview

### 1. GoogleAuthInterceptorIntegrationTest.java
**Purpose**: Tests the complete HTTP request authentication flow through the GoogleAuthInterceptor.

**Key Test Scenarios**:
- ✅ Successful authentication with valid Google ID
- ✅ Authentication with admin users
- ✅ Missing Authorization header (401 response)
- ✅ Empty/whitespace Authorization header (401 response)
- ✅ Non-existent user (403 response)
- ✅ Database errors (500 response)
- ✅ Multiple endpoints consistency
- ✅ Different HTTP methods (GET, POST, PUT, DELETE)
- ✅ Custom header name configuration
- ✅ Special characters in Google ID
- ✅ Long Google ID handling
- ✅ Excluded paths (actuator endpoints)
- ✅ Concurrent request thread safety
- ✅ Google ID trimming

**Requirements Covered**: 1.1, 1.2, 1.3, 2.3, 4.3, 4.4

### 2. AuthenticationControllerIntegrationTest.java
**Purpose**: Verifies user context availability and proper functioning in actual controllers.

**Key Test Scenarios**:
- ✅ Profile controller with authenticated user
- ✅ Admin user profile access
- ✅ Unauthenticated access returns 401
- ✅ Skills controller access control
- ✅ Admin operations require admin role
- ✅ User controller current user endpoint
- ✅ Role-based access enforcement
- ✅ Multiple controllers with same authentication
- ✅ Context cleanup after requests
- ✅ Error handling with proper responses
- ✅ UserContextHelper integration
- ✅ Consistent role-based access across endpoints

**Requirements Covered**: 1.1, 1.2, 1.3, 2.3, 4.3, 4.4

### 3. InterceptorExistingControllerIntegrationTest.java
**Purpose**: Tests interceptor integration with existing application controllers.

**Key Test Scenarios**:
- ✅ Skills controller CRUD operations with authentication
- ✅ User controller operations with authentication
- ✅ Authentication bypass for unauthorized operations
- ✅ Admin-only operations enforcement
- ✅ Multiple controllers with same authentication session
- ✅ Error scenarios with existing controllers
- ✅ Context isolation between requests
- ✅ Business logic execution after successful authentication

**Requirements Covered**: 1.1, 1.2, 1.3, 2.3, 4.3, 4.4

### 4. AuthenticationFlowIntegrationTest.java
**Purpose**: Basic integration tests for authentication components without full web context.

**Key Test Scenarios**:
- ✅ UserContext basic operations
- ✅ Admin user context handling
- ✅ Context clearing and cleanup
- ✅ Thread safety verification
- ✅ User switching functionality
- ✅ Null user handling
- ✅ User data integrity
- ✅ Role-based checks
- ✅ Memory leak prevention

**Requirements Covered**: 2.2, 2.3

## Configuration Files

### application-integration-test.properties
- Enables GoogleAuthInterceptor for integration tests
- Configures H2 in-memory database
- Sets authentication properties
- Enables debug logging for authentication components

## Test Execution

### Running Integration Tests
```bash
# Run all authentication integration tests
./mvnw test -Dtest="*IntegrationTest" -Dspring.profiles.active=integration-test

# Run specific test class
./mvnw test -Dtest=GoogleAuthInterceptorIntegrationTest -Dspring.profiles.active=integration-test

# Run with debug output
./mvnw test -Dtest=AuthenticationFlowIntegrationTest -Dspring.profiles.active=integration-test -X
```

### Test Requirements Coverage

| Requirement | Test Coverage | Status |
|-------------|---------------|--------|
| 1.1 - Extract Google ID from Authorization header | ✅ All integration tests | Complete |
| 1.2 - Missing header returns 401 | ✅ GoogleAuthInterceptorIntegrationTest | Complete |
| 1.3 - Invalid header returns 401 | ✅ GoogleAuthInterceptorIntegrationTest | Complete |
| 1.4 - Valid header proceeds to validation | ✅ All integration tests | Complete |
| 2.1 - Extract user information | ✅ AuthenticationControllerIntegrationTest | Complete |
| 2.2 - Make user info available to controllers | ✅ All controller integration tests | Complete |
| 2.3 - Provide access to authenticated user | ✅ All integration tests | Complete |
| 2.4 - Handle unavailable user gracefully | ✅ AuthenticationFlowIntegrationTest | Complete |
| 3.1 - Find user by Google ID | ✅ All integration tests | Complete |
| 3.2 - Load complete user profile | ✅ AuthenticationControllerIntegrationTest | Complete |
| 3.3 - Return 403 for unregistered user | ✅ GoogleAuthInterceptorIntegrationTest | Complete |
| 3.4 - Return 500 for database errors | ✅ GoogleAuthInterceptorIntegrationTest | Complete |
| 4.1 - Configure middleware globally | ✅ All integration tests | Complete |
| 4.2 - Apply to API path patterns | ✅ GoogleAuthInterceptorIntegrationTest | Complete |
| 4.3 - Allow requests on successful auth | ✅ All integration tests | Complete |
| 4.4 - Prevent requests on failed auth | ✅ All integration tests | Complete |

## Key Integration Test Features

### 1. Complete HTTP Request Lifecycle
- Tests full request processing from HTTP headers to controller execution
- Verifies interceptor integration with Spring MVC
- Validates proper error response formatting

### 2. User Context Verification
- Confirms user information is available in controllers
- Tests UserContextHelper integration
- Verifies thread-local storage functionality

### 3. Role-Based Access Control
- Tests admin vs regular user access patterns
- Verifies proper authorization enforcement
- Confirms consistent behavior across controllers

### 4. Error Handling Integration
- Tests all authentication failure scenarios
- Verifies proper HTTP status codes
- Confirms error response format consistency

### 5. Thread Safety and Cleanup
- Tests concurrent request handling
- Verifies context isolation between requests
- Confirms proper memory cleanup

### 6. Configuration Testing
- Tests custom header name configuration
- Verifies path pattern inclusion/exclusion
- Tests authentication property integration

## Test Data Setup

All tests use consistent test data:
- **Regular User**: Tutorado role, limited permissions
- **Admin User**: Administrador role, full permissions
- **Test Chapter**: Shared chapter for user association
- **Mock Services**: UserService and MessageService mocked for controlled testing

## Assertions and Verifications

Each test includes comprehensive assertions for:
- HTTP status codes
- Response body content
- Service method invocations
- User context state
- Thread safety
- Error message accuracy
- Authentication flow completion

## Notes

- Tests use `@ActiveProfiles("integration-test")` to enable the interceptor
- MockMvc is configured with full web application context
- All tests include proper setup and teardown for clean state
- Thread safety is verified through concurrent execution tests
- Memory leak prevention is tested through context cleanup verification