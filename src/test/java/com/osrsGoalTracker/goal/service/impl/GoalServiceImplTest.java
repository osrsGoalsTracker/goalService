package com.osrsGoalTracker.goal.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.time.Instant;

import com.osrsGoalTracker.goal.model.Goal;
import com.osrsGoalTracker.goal.repository.GoalRepository;
import com.osrsGoalTracker.hiscore.service.HiscoresService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Tests for {@link GoalServiceImpl}.
 */
class GoalServiceImplTest {
    @Mock
    private GoalRepository goalRepository;

    @Mock
    private HiscoresService hiscoresService;

    private GoalServiceImpl goalService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        goalService = new GoalServiceImpl(goalRepository, hiscoresService);
    }

    @Test
    void createGoal_ValidGoal_CreatesGoal() {
        // Arrange
        Goal goal = Goal.builder()
                .userId("testUser")
                .characterName("testChar")
                .targetAttribute("ATTACK")
                .targetType("LEVEL")
                .targetValue(99L)
                .targetDate(Instant.now().plusSeconds(3600))
                .notificationChannelType("DISCORD")
                .frequency("DAILY")
                .build();

        when(goalRepository.createGoal(any(Goal.class), eq(1L))).thenReturn(goal);

        // Act
        Goal result = goalService.createGoal(goal, 1L);

        // Assert
        assertEquals("testUser", result.getUserId());
        assertEquals("testChar", result.getCharacterName());
        assertEquals("ATTACK", result.getTargetAttribute());
        assertEquals(99L, result.getTargetValue());
    }

    @Test
    void createGoal_NullGoal_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> goalService.createGoal(null, 1L));
    }

    @Test
    void createGoal_EmptyUserId_ThrowsException() {
        // Arrange
        Goal goal = Goal.builder()
                .userId("")
                .characterName("testChar")
                .targetAttribute("ATTACK")
                .targetType("LEVEL")
                .targetValue(99L)
                .targetDate(Instant.now().plusSeconds(3600))
                .build();

        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> goalService.createGoal(goal, 1L));
    }

    @Test
    void createGoal_NullCharacterName_ThrowsException() {
        // Arrange
        Goal goal = Goal.builder()
                .userId("testUser")
                .targetAttribute("ATTACK")
                .targetType("LEVEL")
                .targetValue(99L)
                .targetDate(Instant.now().plusSeconds(3600))
                .build();

        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> goalService.createGoal(goal, 1L));
    }

    @Test
    void createGoal_InvalidTargetValue_ThrowsException() {
        // Arrange
        Goal goal = Goal.builder()
                .userId("testUser")
                .characterName("testChar")
                .targetAttribute("ATTACK")
                .targetType("LEVEL")
                .targetValue(0L)
                .targetDate(Instant.now().plusSeconds(3600))
                .build();

        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> goalService.createGoal(goal, 1L));
    }

    @Test
    void createGoal_NegativeCurrentProgress_ThrowsException() {
        // Arrange
        Goal goal = Goal.builder()
                .userId("testUser")
                .characterName("testChar")
                .targetAttribute("ATTACK")
                .targetType("LEVEL")
                .targetValue(99L)
                .targetDate(Instant.now().plusSeconds(3600))
                .build();

        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> goalService.createGoal(goal, -1L));
    }

    @Test
    void createGoal_NullTargetDate_ThrowsException() {
        // Arrange
        Goal goal = Goal.builder()
                .userId("testUser")
                .characterName("testChar")
                .targetAttribute("ATTACK")
                .targetType("LEVEL")
                .targetValue(99L)
                .build();

        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> goalService.createGoal(goal, 1L));
    }
}