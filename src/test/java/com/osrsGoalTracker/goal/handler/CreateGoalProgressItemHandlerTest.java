package com.osrsGoalTracker.goal.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.UUID;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.osrsGoalTracker.goal.model.Goal;
import com.osrsGoalTracker.goal.service.GoalService;
import com.osrsGoalTracker.orchestration.events.GoalProgressUpdateEvent;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test class for CreateGoalProgressItemHandler.
 */
public class CreateGoalProgressItemHandlerTest {
    private CreateGoalProgressItemHandler handler;
    private GoalService goalService;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        goalService = mock(GoalService.class);
        objectMapper = new ObjectMapper();
        handler = new CreateGoalProgressItemHandler(goalService);
    }

    @Test
    void handleRequest_Success() throws Exception {
        // Arrange
        String userId = UUID.randomUUID().toString();
        String characterName = "testCharacter";
        String goalId = UUID.randomUUID().toString();
        long progressValue = 1000L;

        GoalProgressUpdateEvent event = new GoalProgressUpdateEvent();
        event.setUserId(userId);
        event.setCharacterName(characterName);
        event.setGoalId(goalId);
        event.setProgressValue(progressValue);

        APIGatewayProxyRequestEvent request = new APIGatewayProxyRequestEvent();
        request.setBody(objectMapper.writeValueAsString(event));

        doNothing().when(goalService).createGoalProgress(any(Goal.class));

        // Act
        APIGatewayProxyResponseEvent response = handler.handleRequest(request, null);

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
        verify(goalService).createGoalProgress(any(Goal.class));
    }

    @Test
    void handleRequest_InvalidRequest_ReturnsError() throws Exception {
        // Arrange
        APIGatewayProxyRequestEvent request = new APIGatewayProxyRequestEvent();
        request.setBody("invalid");

        // Act
        APIGatewayProxyResponseEvent response = handler.handleRequest(request, null);

        // Assert
        assertNotNull(response);
        assertEquals(500, response.getStatusCode());
    }
}