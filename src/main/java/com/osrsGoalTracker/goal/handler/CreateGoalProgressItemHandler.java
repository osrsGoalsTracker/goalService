package com.osrsGoalTracker.goal.handler;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.ScheduledEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.osrsGoalTracker.goal.di.GoalModule;
import com.osrsGoalTracker.goal.model.Goal;
import com.osrsGoalTracker.goal.service.GoalService;
import com.osrsGoalTracker.orchestration.events.GoalProgressUpdateEvent;

import lombok.extern.slf4j.Slf4j;

/**
 * AWS Lambda handler for processing goal progress update events from
 * EventBridge.
 * Validates the event and creates a new progress record for the specified goal.
 */
@Slf4j
public class CreateGoalProgressItemHandler implements RequestHandler<ScheduledEvent, Goal> {
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
     * Handles the scheduled event by validating the event detail and creating a
     * new goal progress record.
     * 
     * @param event
     *            The scheduled event containing the details.
     * @param context
     *            The AWS Lambda context.
     * @return The updated Goal
     * @throws IllegalArgumentException
     *             if the event or event detail is null or if
     *             any required fields are missing.
     */
    @Override
    public Goal handleRequest(ScheduledEvent event, Context context) {
        if (event == null || event.getDetail() == null) {
            throw new IllegalArgumentException("Event or event detail cannot be null");
        }

        validateEventDetail(event);

        GoalProgressUpdateEvent progressEvent = objectMapper.convertValue(event.getDetail(),
                GoalProgressUpdateEvent.class);
        log.info("GoalProgressUpdateEvent: {}", progressEvent);

        Goal goal = Goal.builder()
                .userId(progressEvent.getUserId())
                .characterName(progressEvent.getCharacterName())
                .goalId(progressEvent.getGoalId())
                .currentProgress(progressEvent.getProgressValue())
                .build();

        goalService.createGoalProgress(goal);
        return goal;
    }

    /**
     * Validates the required fields in the event detail.
     *
     * @param event
     *            The scheduled event containing the details.
     * @throws IllegalArgumentException
     *             if any required field is missing.
     */
    private void validateEventDetail(ScheduledEvent event) {
        if (event == null || event.getDetail() == null) {
            throw new IllegalArgumentException("Event or event detail cannot be null");
        }

        List<String> requiredFields = Arrays.asList(
                "userId",
                "characterName",
                "goalId",
                "progressValue");

        List<String> missingFields = requiredFields.stream()
                .filter(field -> !event.getDetail().containsKey(field))
                .collect(Collectors.toList());

        if (!missingFields.isEmpty()) {
            throw new IllegalArgumentException(
                    "Event detail is missing required fields: " + String.join(", ", missingFields));
        }
    }
}