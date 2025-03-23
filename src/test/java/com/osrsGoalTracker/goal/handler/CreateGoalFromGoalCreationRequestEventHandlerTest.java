package com.osrsGoalTracker.goal.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.ScheduledEvent;
import com.osrsGoalTracker.goal.model.Goal;
import com.osrsGoalTracker.goal.service.GoalService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Tests for {@link CreateGoalFromGoalCreationRequestEventHandler}.
 */
class CreateGoalFromGoalCreationRequestEventHandlerTest {
    @Mock
    private GoalService goalService;

    @Mock
    private Context context;

    private CreateGoalFromGoalCreationRequestEventHandler handler;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        handler = new CreateGoalFromGoalCreationRequestEventHandler(goalService);
    }

    @Test
    void handleRequest_ValidEvent_CreatesGoal() {
        // Arrange
        ScheduledEvent event = new ScheduledEvent();
        Map<String, Object> detail = new HashMap<>();
        detail.put("userId", "testUser");
        detail.put("characterName", "testChar");
        detail.put("targetAttribute", "ATTACK");
        detail.put("targetType", "LEVEL");
        detail.put("targetValue", 99L);
        detail.put("currentValue", 1L);
        detail.put("targetDate", Instant.now().toString());
        detail.put("notificationChannelType", "DISCORD");
        detail.put("frequency", "DAILY");
        event.setDetail(detail);

        Goal expectedGoal = Goal.builder()
                .userId("testUser")
                .characterName("testChar")
                .targetAttribute("ATTACK")
                .targetType("LEVEL")
                .targetValue(99L)
                .currentProgress(1L)
                .build();

        when(goalService.createGoal(any(Goal.class))).thenReturn(expectedGoal);

        // Act
        Goal result = handler.handleRequest(event, context);

        // Assert
        assertNotNull(result);
        assertEquals("testUser", result.getUserId());
        assertEquals("testChar", result.getCharacterName());
        assertEquals("ATTACK", result.getTargetAttribute());
        assertEquals("LEVEL", result.getTargetType());
        assertEquals(99L, result.getTargetValue());
        assertEquals(1L, result.getCurrentProgress());
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