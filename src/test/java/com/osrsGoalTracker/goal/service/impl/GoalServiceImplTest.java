package com.osrsGoalTracker.goal.service.impl;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.UUID;

import com.osrsGoalTracker.goal.model.Goal;
import com.osrsGoalTracker.goal.repository.GoalRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test class for GoalServiceImpl.
 */
public class GoalServiceImplTest {
        private GoalServiceImpl service;
        private GoalRepository goalRepository;

        @BeforeEach
        void setUp() {
                goalRepository = mock(GoalRepository.class);
                service = new GoalServiceImpl(goalRepository);
        }

        @Test
        void createGoal_Success() {
                // Arrange
                Goal goal = createValidGoal();
                when(goalRepository.createGoal(any(Goal.class))).thenReturn(goal);

                // Act
                Goal result = service.createGoal(goal);

                // Assert
                assertNotNull(result);
                assertEquals(goal.getUserId(), result.getUserId());
                assertEquals(goal.getCharacterName(), result.getCharacterName());
                assertEquals(goal.getTargetAttribute(), result.getTargetAttribute());
                verify(goalRepository).createGoal(goal);
        }

        @Test
        void createGoal_NullGoal_ThrowsException() {
                // Act & Assert
                assertThrows(IllegalArgumentException.class, () -> service.createGoal(null));
        }

        @Test
        void createGoal_EmptyUserId_ThrowsException() {
                // Arrange
                Goal goal = createValidGoal();
                goal.setUserId("");

                // Act & Assert
                assertThrows(IllegalArgumentException.class, () -> service.createGoal(goal));
        }

        @Test
        void createGoal_EmptyCharacterName_ThrowsException() {
                // Arrange
                Goal goal = createValidGoal();
                goal.setCharacterName("");

                // Act & Assert
                assertThrows(IllegalArgumentException.class, () -> service.createGoal(goal));
        }

        @Test
        void createGoal_EmptyTargetAttribute_ThrowsException() {
                // Arrange
                Goal goal = createValidGoal();
                goal.setTargetAttribute("");

                // Act & Assert
                assertThrows(IllegalArgumentException.class, () -> service.createGoal(goal));
        }

        @Test
        void createGoal_NegativeTargetValue_ThrowsException() {
                // Arrange
                Goal goal = createValidGoal();
                goal.setTargetValue(-1);

                // Act & Assert
                assertThrows(IllegalArgumentException.class, () -> service.createGoal(goal));
        }

        @Test
        void createGoal_NegativeCurrentProgress_ThrowsException() {
                // Arrange
                Goal goal = createValidGoal();
                goal.setCurrentProgress(-1);

                // Act & Assert
                assertThrows(IllegalArgumentException.class, () -> service.createGoal(goal));
        }

        @Test
        void createGoal_NullTargetDate_ThrowsException() {
                // Arrange
                Goal goal = createValidGoal();
                goal.setTargetDate(null);

                // Act & Assert
                assertThrows(IllegalArgumentException.class, () -> service.createGoal(goal));
        }

        @Test
        void createGoalProgress_Success() {
                // Arrange
                Goal goal = createValidGoal();
                doNothing().when(goalRepository).createGoalProgress(any(Goal.class));

                // Act & Assert
                assertDoesNotThrow(() -> service.createGoalProgress(goal));
                verify(goalRepository).createGoalProgress(goal);
        }

        @Test
        void createGoalProgress_NullGoal_ThrowsException() {
                // Act & Assert
                assertThrows(IllegalArgumentException.class, () -> service.createGoalProgress(null));
        }

        @Test
        void createGoalProgress_EmptyUserId_ThrowsException() {
                // Arrange
                Goal goal = createValidGoal();
                goal.setUserId("");

                // Act & Assert
                assertThrows(IllegalArgumentException.class, () -> service.createGoalProgress(goal));
        }

        @Test
        void createGoalProgress_EmptyCharacterName_ThrowsException() {
                // Arrange
                Goal goal = createValidGoal();
                goal.setCharacterName("");

                // Act & Assert
                assertThrows(IllegalArgumentException.class, () -> service.createGoalProgress(goal));
        }

        @Test
        void createGoalProgress_EmptyGoalId_ThrowsException() {
                // Arrange
                Goal goal = createValidGoal();
                goal.setGoalId("");

                // Act & Assert
                assertThrows(IllegalArgumentException.class, () -> service.createGoalProgress(goal));
        }

        @Test
        void createGoalProgress_NegativeProgressValue_ThrowsException() {
                // Arrange
                Goal goal = createValidGoal();
                goal.setCurrentProgress(-1);

                // Act & Assert
                assertThrows(IllegalArgumentException.class, () -> service.createGoalProgress(goal));
        }

        private Goal createValidGoal() {
                return Goal.builder()
                                .userId(UUID.randomUUID().toString())
                                .characterName("testCharacter")
                                .targetAttribute("Woodcutting")
                                .targetType("xp")
                                .targetValue(1000000L)
                                .currentProgress(0L)
                                .targetDate(Instant.now().plusSeconds(86400))
                                .notificationChannelType("SMS")
                                .frequency("daily")
                                .goalId(UUID.randomUUID().toString())
                                .build();
        }
}