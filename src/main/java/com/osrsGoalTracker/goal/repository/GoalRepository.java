package com.osrsGoalTracker.goal.repository;

import com.osrsGoalTracker.goal.model.Goal;

/**
 * Repository interface for managing goals.
 */
public interface GoalRepository {
    /**
     * Creates a new goal.
     *
     * @param goal The goal to create
     * @return The created goal
     */
    Goal createGoal(Goal goal);
}