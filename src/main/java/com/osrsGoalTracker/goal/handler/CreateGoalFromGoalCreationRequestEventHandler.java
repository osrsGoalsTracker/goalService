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
import com.osrsGoalTracker.orchestration.events.GoalCreationRequestEvent;

import lombok.extern.slf4j.Slf4j;

/**
 * AWS Lambda handler for processing goal creation request events from
 * EventBridge.
 * Validates the event and creates a new goal with initial progress.
 */
@Slf4j
public class CreateGoalFromGoalCreationRequestEventHandler implements RequestHandler<ScheduledEvent, Goal> {
    private final GoalService goalService;
    private final ObjectMapper objectMapper;

    /**
     * Default constructor that initializes dependencies using Guice.
     */
    public CreateGoalFromGoalCreationRequestEventHandler() {
        Injector injector = Guice.createInjector(new GoalModule());
        this.goalService = injector.getInstance(GoalService.class);
        this.objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    }

    /**
     * Test constructor that accepts a GoalService instance.
     * 
     * @param goalService The service to use for goal creation.
     */
    public CreateGoalFromGoalCreationRequestEventHandler(GoalService goalService) {
        this.goalService = goalService;
        this.objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    }

    /**
     * Handles the scheduled event by validating the event detail and creating a
     * new goal.
     * 
     * @param event   The scheduled event containing the details.
     * @param context The AWS Lambda context.
     * @return The created Goal
     * @throws IllegalArgumentException if the event or event detail is null or if
     *                                  any required fields are missing.
     */
    @Override
    public Goal handleRequest(ScheduledEvent event, Context context) {
        if (event == null || event.getDetail() == null) {
            throw new IllegalArgumentException("Event or event detail cannot be null");
        }

        validateEventDetail(event);

        GoalCreationRequestEvent goalEvent = objectMapper.convertValue(event.getDetail(),
                GoalCreationRequestEvent.class);
        log.info("GoalCreationEvent: {}", goalEvent);
        Goal goal = Goal.builder()
                .userId(goalEvent.getUserId())
                .characterName(goalEvent.getCharacterName())
                .targetAttribute(goalEvent.getTargetAttribute())
                .targetType(goalEvent.getTargetType())
                .targetValue(goalEvent.getTargetValue())
                .targetDate(goalEvent.getTargetDate())
                .notificationChannelType(goalEvent.getNotificationChannelType())
                .frequency(goalEvent.getFrequency())
                .build();

        return goalService.createGoal(goal, goalEvent.getCurrentValue());
    }

    /**
     * Validates the required fields in the event detail.
     *
     * @param event The scheduled event containing the details.
     * @throws IllegalArgumentException if any required field is missing.
     */
    @SuppressWarnings({ "checkstyle:cyclomaticcomplexity", "checkstyle:booleanexpressioncomplexity" })
    private void validateEventDetail(ScheduledEvent event) {
        if (event == null || event.getDetail() == null) {
            throw new IllegalArgumentException("Event or event detail cannot be null");
        }

        List<String> requiredFields = Arrays.asList(
                "userId",
                "characterName",
                "targetAttribute",
                "targetType",
                "targetValue",
                "currentValue",
                "targetDate",
                "notificationChannelType",
                "frequency");

        List<String> missingFields = requiredFields.stream()
                .filter(field -> !event.getDetail().containsKey(field))
                .collect(Collectors.toList());

        if (!missingFields.isEmpty()) {
            throw new IllegalArgumentException(
                    "Event detail is missing required fields: " + String.join(", ", missingFields));
        }
    }
}
