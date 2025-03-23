package com.osrsGoalTracker.goal.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.ScheduledEvent;
import com.osrsGoalTracker.goal.model.Goal;
import com.osrsGoalTracker.goal.service.GoalService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Test class for CreateGoalProgressItemHandler.
 */
class CreateGoalProgressItemHandlerTest {
    @Mock
    private GoalService goalService;

    @Mock
    private Context context;

    private CreateGoalProgressItemHandler handler;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        handler = new CreateGoalProgressItemHandler(goalService);
    }

    @Test
    void handleRequest_ValidEvent_CreatesGoalProgress() {
        // Arrange
        ScheduledEvent event = new ScheduledEvent();
        Map<String, Object> detail = new HashMap<>();
        String userId = UUID.randomUUID().toString();
        String characterName = "testCharacter";
        String goalId = UUID.randomUUID().toString();
        long progressValue = 1000L;

        detail.put("userId", userId);
        detail.put("characterName", characterName);
        detail.put("goalId", goalId);
        detail.put("progressValue", progressValue);
        event.setDetail(detail);

        doNothing().when(goalService).createGoalProgress(any(Goal.class));

        // Act
        Goal result = handler.handleRequest(event, context);

        // Assert
        assertNotNull(result);
        assertEquals(userId, result.getUserId());
        assertEquals(characterName, result.getCharacterName());
        assertEquals(goalId, result.getGoalId());
        assertEquals(progressValue, result.getCurrentProgress());
        verify(goalService).createGoalProgress(any(Goal.class));
    }

    @Test
    void handleRequest_NullEvent_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> handler.handleRequest(null, context));
    }

    @Test
    void handleRequest_MissingRequiredField_ThrowsException() {
        // Arrange
        ScheduledEvent event = new ScheduledEvent();
        Map<String, Object> detail = new HashMap<>();
        detail.put("userId", "testUser");
        // Missing required fields
        event.setDetail(detail);

        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> handler.handleRequest(event, context));
    }
}