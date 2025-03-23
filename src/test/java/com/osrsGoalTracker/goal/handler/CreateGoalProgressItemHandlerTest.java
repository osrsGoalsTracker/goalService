package com.osrsGoalTracker.goal.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.osrsGoalTracker.orchestration.events.GoalProgressEvent;
import com.osrsGoalTracker.goal.service.GoalService;

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
        GoalProgressEvent request = new GoalProgressEvent();
        request.setUserId("testUser");
        request.setCharacterName("testCharacter");
        request.setGoalId("testGoalId");
        request.setProgressValue(1000L);

        APIGatewayProxyRequestEvent input = new APIGatewayProxyRequestEvent();
        input.setBody(objectMapper.writeValueAsString(request));
        Context context = mock(Context.class);

        doNothing().when(goalService).createGoalProgress(any(GoalProgressEvent.class));

        // Act
        APIGatewayProxyResponseEvent response = handler.handleRequest(input, context);

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
        verify(goalService).createGoalProgress(any(GoalProgressEvent.class));
    }

    @Test
    void handleRequest_InvalidRequest_ReturnsError() throws Exception {
        // Arrange
        APIGatewayProxyRequestEvent input = new APIGatewayProxyRequestEvent();
        input.setBody("invalid json");
        Context context = mock(Context.class);

        // Act
        APIGatewayProxyResponseEvent response = handler.handleRequest(input, context);

        // Assert
        assertNotNull(response);
        assertEquals(500, response.getStatusCode());
    }
}