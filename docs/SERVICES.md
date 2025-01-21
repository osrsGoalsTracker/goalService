# Service Layer

## Overview

The service layer contains the core business logic of the application. Services follow these principles:

1. Interface-based design
2. Constructor injection for dependencies
3. Clear separation of concerns
4. Transaction management
5. Comprehensive error handling

## Service Interfaces

### User Service

```java
public interface UserService {
    /**
     * Creates a new user.
     *
     * @param request The user creation request
     * @return The created user
     * @throws BadRequestException if email is invalid
     * @throws ConflictException if email already exists
     */
    User createUser(CreateUserRequest request);

    /**
     * Retrieves a user by ID.
     *
     * @param userId The user's ID
     * @return The user
     * @throws ResourceNotFoundException if user doesn't exist
     */
    User getUser(String userId);
}
```

### Hiscores Service

```java
public interface HiscoresService {
    /**
     * Gets current hiscores for a character.
     *
     * @param characterName The character name
     * @return Current hiscores
     * @throws ResourceNotFoundException if character not found
     * @throws ServiceException if OSRS API unavailable
     */
    CharacterHiscores getCharacterHiscores(String characterName);
}
```

### Goal Service
Service interface for managing goals.

```java
public interface GoalService {
    /**
     * Creates a new goal with the current progress.
     *
     * @param goal The goal to create
     * @param currentProgress The current progress towards the goal
     * @return The created goal
     * @throws IllegalArgumentException if the goal is invalid
     */
    Goal createGoal(Goal goal, long currentProgress);
}
```

## Implementation Pattern

Services follow this implementation pattern:

```java
@Singleton
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final ValidationService validationService;

    @Inject
    public UserServiceImpl(
            UserRepository userRepository,
            ValidationService validationService) {
        this.userRepository = userRepository;
        this.validationService = validationService;
    }

    @Override
    public User createUser(CreateUserRequest request) {
        // 1. Validate
        validationService.validate(request);

        // 2. Check business rules
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("Email already exists");
        }

        // 3. Create domain object
        User user = User.builder()
            .userId(UUID.randomUUID().toString())
            .email(request.getEmail())
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();

        // 4. Persist
        return userRepository.save(user);
    }
}
```

## Error Handling

Services use custom exceptions for different error cases:

```java
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}

public class ConflictException extends RuntimeException {
    public ConflictException(String message) {
        super(message);
    }
}

public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) {
        super(message);
    }
}

public class ServiceException extends RuntimeException {
    public ServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
```

## Validation

Services use a common validation service:

```java
@Singleton
public class ValidationService {
    private final Validator validator;

    @Inject
    public ValidationService(Validator validator) {
        this.validator = validator;
    }

    public <T> void validate(T object) {
        Set<ConstraintViolation<T>> violations = validator.validate(object);
        if (!violations.isEmpty()) {
            throw new BadRequestException(formatViolations(violations));
        }
    }
}
```

## Transaction Management

Services handle transactional boundaries:

```java
@Singleton
public class CharacterServiceImpl implements CharacterService {
    @Override
    @Transactional
    public Character addCharacterToUser(String userId, AddCharacterToUserRequest request) {
        // 1. Validate user exists
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found");
        }

        // 2. Check if character already exists
        if (characterRepository.existsByUserIdAndName(userId, request.getCharacterName())) {
            throw new ConflictException("Character already associated");
        }

        // 3. Create character
        Character character = Character.builder()
            .userId(userId)
            .name(request.getCharacterName())
            .lastUpdated(LocalDateTime.now())
            .build();

        // 4. Save and return
        return characterRepository.save(character);
    }
}
```

## Testing

Services must have comprehensive unit tests:

```java
@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private ValidationService validationService;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void createUser_ValidRequest_Success() {
        // Given
        CreateUserRequest request = new CreateUserRequest();
        request.setEmail("test@example.com");

        // When
        User result = userService.createUser(request);

        // Then
        assertThat(result).isNotNull();
        verify(userRepository).save(any(User.class));
    }

    @Test
    void createUser_ExistingEmail_ThrowsConflict() {
        // Given
        CreateUserRequest request = new CreateUserRequest();
        request.setEmail("existing@example.com");
        when(userRepository.existsByEmail(request.getEmail())).thenReturn(true);

        // Then
        assertThrows(ConflictException.class, () -> userService.createUser(request));
    }
}
``` 