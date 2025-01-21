package com.osrsGoalTracker.goal.repository;

import com.osrsGoalTracker.goal.model.Goal;

/**
 * Repository interface for managing goals.
 */
public interface GoalRepository {
    /**
     * Creates a new goal with the current progress.
     *
     * @param goal            The goal to create
     * @param currentProgress The current progress towards the goal
     * @return The created goal
     */
    Goal createGoal(Goal goal, long currentProgress);
}