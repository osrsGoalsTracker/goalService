package com.osrsGoalTracker.goal.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.osrsGoalTracker.goal.di.GoalModule;
import com.osrsGoalTracker.goal.model.Goal;
import com.osrsGoalTracker.goal.service.GoalService;
import com.osrsGoalTracker.orchestration.events.GoalProgressEvent;

import lombok.extern.slf4j.Slf4j;

/**
 * AWS Lambda handler for creating goal progress items.
 * This handler receives an API Gateway request with goal progress details
 * and creates a new progress record for the specified goal.
 */
@Slf4j
public class CreateGoalProgressItemHandler
        implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    private final GoalService goalService;
    private final ObjectMapper objectMapper;

    /**
     * Default constructor that initializes dependencies using Guice.
     */
    public CreateGoalProgressItemHandler() {
        Injector injector = Guice.createInjector(new GoalModule());
        this.goalService = injector.getInstance(GoalService.class);
        this.objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    }

    /**
     * Test constructor that accepts a GoalService instance.
     * 
     * @param goalService
     *            The service to use for goal progress creation.
     */
    public CreateGoalProgressItemHandler(GoalService goalService) {
        this.goalService = goalService;
        this.objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    }

    /**
     * Handles the API Gateway request by creating a new goal progress item.
     * 
     * @param request
     *            The API Gateway request containing the goal progress details.
     * @param context
     *            The AWS Lambda context.
     * @return The API Gateway response.
     */
    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent request, Context context) {
        try {
            GoalProgressEvent progressRequest = objectMapper.readValue(request.getBody(),
                    GoalProgressEvent.class);
            log.info("Creating goal progress for user: {}, character: {}, goalId: {}",
                    progressRequest.getUserId(), progressRequest.getCharacterName(), progressRequest.getGoalId());

            // Convert event to Goal object
            Goal goal = Goal.builder()
                    .userId(progressRequest.getUserId())
                    .characterName(progressRequest.getCharacterName())
                    .goalId(progressRequest.getGoalId())
                    .currentProgress(progressRequest.getProgressValue())
                    .build();

            goalService.createGoalProgress(goal);

            APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
            response.setStatusCode(200);
            response.setBody("{\"message\": \"Goal progress created successfully\"}");
            return response;
        } catch (Exception e) {
            log.error("Error creating goal progress: {}", e.getMessage(), e);
            APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
            response.setStatusCode(500);
            response.setBody("{\"error\": \"" + e.getMessage() + "\"}");
            return response;
        }
    }
}