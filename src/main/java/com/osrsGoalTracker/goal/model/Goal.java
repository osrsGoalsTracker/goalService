package com.osrsGoalTracker.goal.model;

import java.time.Instant;

import lombok.Builder;
import lombok.Data;

/**
 * Domain model representing a goal.
 */
@Data
@Builder
public class Goal {
    /**
     * The ID of the user who owns this goal.
     */
    private String userId;

    /**
     * The ID of the goal.
     */
    private String goalId;

    /**
     * The name of the character this goal is for.
     */
    private String characterName;
    
    /**
     * The skill or activity being tracked (e.g., "WOODCUTTING", "BOUNTY_HUNTER").
     */
    private String targetAttribute;

    /**
     * The type of target (e.g., "xp", "level", etc.).
     */
    private String targetType;

    /**
     * The target value to achieve.
     */
    private long targetValue;

    /**
     * The date by which to achieve the goal.
     */
    private Instant targetDate;

    /**
     * The type of notification channel to use.
     */
    private String notificationChannelType;

    /**
     * How often to check/notify about progress.
     */
    private String frequency;
}
