package com.osrsGoalTracker.goal.service;

import com.osrsGoalTracker.goal.model.Goal;

/**
 * Service interface for managing goals.
 */
public interface GoalService {
    /**
     * Creates a new goal.
     *
     * @param goal The goal to create
     * @return The created goal
     * @throws IllegalArgumentException if the goal is invalid
     */
    Goal createGoal(Goal goal);
}