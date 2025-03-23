package com.osrsGoalTracker.goal.service.impl;

import java.time.Instant;

import com.google.inject.Inject;
import com.osrsGoalTracker.orchestration.events.GoalProgressEvent;
import com.osrsGoalTracker.goal.model.Goal;
import com.osrsGoalTracker.goal.repository.GoalRepository;
import com.osrsGoalTracker.goal.service.GoalService;

import lombok.extern.slf4j.Slf4j;

/**
 * Implementation of the GoalService interface.
 */
@Slf4j
public class GoalServiceImpl implements GoalService {
    private final GoalRepository goalRepository;

    /**
     * Constructor for GoalServiceImpl.
     * 
     * @param goalRepository The goal repository.
     */
    @Inject
    public GoalServiceImpl(GoalRepository goalRepository) {
        this.goalRepository = goalRepository;
    }

    /**
     * Creates a new goal.
     *
     * @param goal The goal to create
     * @return The created goal
     * @throws IllegalArgumentException if the goal is invalid
     */
    @Override
    public Goal createGoal(Goal goal) {
        validateGoal(goal);
        log.info("Creating goal for user {} targeting {}", goal.getUserId(), goal.getTargetAttribute());
        return goalRepository.createGoal(goal);
    }

    /**
     * Creates a new goal progress item.
     *
     * @param request The request containing the goal progress details
     * @throws IllegalArgumentException if the request is invalid
     */
    @Override
    public void createGoalProgress(GoalProgressEvent request) {
        validateProgressRequest(request);
        log.info("Creating goal progress for user {} goal {}", request.getUserId(), request.getGoalId());
        goalRepository.createGoalProgress(request);
    }

    private void validateGoal(Goal goal) {
        validateGoalNotNull(goal);
        validateRequiredFields(goal);
        validateTargetValue(goal.getTargetValue());
        validateCurrentProgress(goal.getCurrentProgress());
        validateTargetDate(goal.getTargetDate());
    }

    private void validateProgressRequest(GoalProgressEvent request) {
        if (request == null) {
            throw new IllegalArgumentException("request cannot be null");
        }
        validateField(request.getUserId(), "userId");
        validateField(request.getCharacterName(), "characterName");
        validateField(request.getGoalId(), "goalId");
        validateCurrentProgress(request.getProgressValue());
    }

    private void validateGoalNotNull(Goal goal) {
        if (goal == null) {
            throw new IllegalArgumentException("goal cannot be null");
        }
    }

    private void validateRequiredFields(Goal goal) {
        validateField(goal.getUserId(), "userId");
        validateField(goal.getCharacterName(), "characterName");
        validateField(goal.getTargetAttribute(), "targetAttribute");
    }

    private void validateField(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " cannot be null or empty");
        }
    }

    private void validateTargetValue(long targetValue) {
        if (targetValue <= 0) {
            throw new IllegalArgumentException("targetValue must be greater than 0");
        }
    }

    private void validateCurrentProgress(long currentProgress) {
        if (currentProgress < 0) {
            throw new IllegalArgumentException("currentProgress cannot be negative");
        }
    }

    private void validateTargetDate(Instant targetDate) {
        if (targetDate == null) {
            throw new IllegalArgumentException("targetDate cannot be null");
        }
    }
}