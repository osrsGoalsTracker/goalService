package com.osrsGoalTracker.goal.service;

import com.osrsGoalTracker.goal.model.Goal;

/**
 * Service interface for managing goals.
 */
public interface GoalService {
    /**
     * Creates a new goal with the current progress.
     *
     * @param goal            The goal to create
     * @param currentProgress The current progress towards the goal
     * @return The created goal
     * @throws IllegalArgumentException if the goal is invalid
     */
    Goal createGoal(Goal goal, long currentProgress);
}