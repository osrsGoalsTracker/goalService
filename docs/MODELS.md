# Data Models

## Overview

All models in the service follow these principles:
- Immutable using Lombok `@Value`
- Builder pattern using Lombok `@Builder`
- Request objects use `@Data` with `@NoArgsConstructor`
- Clear separation between domain models and DTOs
- Validation using Jakarta Validation annotations

## Domain Models

### Goal
Represents a goal for a character's skill or activity progress.

```java
@Value
@Builder
public class Goal {
    String userId;              // The ID of the user who owns this goal
    String goalId;             // The ID of the goal
    String characterName;      // The name of the character this goal is for
    String targetAttribute;    // The skill or activity being tracked
    String targetType;         // The type of target (e.g., "xp", "level")
    long targetValue;          // The target value to achieve
    Instant targetDate;        // The date by which to achieve the goal
    String notificationChannelType; // The type of notification channel to use
    String frequency;          // How often to check/notify about progress
    Instant createdAt;         // When this goal was created
    Instant updatedAt;         // When this goal was last updated
}
```

Note: The current progress towards a goal is tracked separately from the goal itself, as it represents the dynamic state rather than the goal's configuration.

### User Endpoints


## JSON Serialization

```java
@Value
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CharacterHiscores {
    @JsonProperty("player_name")
    String playerName;
    
    @JsonFormat(shape = JsonFormat.Shape.OBJECT)
    Map<String, SkillStats> skills;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    LocalDateTime timestamp;
}
```

## Model Conversion

Each domain should provide mapper methods for converting between different model representations:

```java
public class UserMapper {
    public static User fromEntity(UserEntity entity) {
        return User.builder()
            .userId(entity.getUserId())
            .email(entity.getEmail())
            .createdAt(parseDateTime(entity.getCreatedAt()))
            .updatedAt(parseDateTime(entity.getUpdatedAt()))
            .build();
    }
    
    public static UserEntity toEntity(User user) {
        UserEntity entity = new UserEntity();
        entity.setUserId(user.getUserId());
        entity.setEmail(user.getEmail());
        entity.setCreatedAt(formatDateTime(user.getCreatedAt()));
        entity.setUpdatedAt(formatDateTime(user.getUpdatedAt()));
        return entity;
    }
}
``` 