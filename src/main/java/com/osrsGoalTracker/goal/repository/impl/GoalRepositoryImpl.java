package com.osrsGoalTracker.goal.repository.impl;

import java.time.Instant;
import java.util.Arrays;
import java.util.UUID;

import com.google.inject.Inject;
import com.osrsGoalTracker.orchestration.events.GoalProgressEvent;
import com.osrsGoalTracker.goal.model.Goal;
import com.osrsGoalTracker.goal.repository.GoalRepository;
import com.osrsGoalTracker.goal.repository.impl.DynamoItem.DynamoGoalMetadataItem;
import com.osrsGoalTracker.goal.repository.impl.DynamoItem.DynamoGoalProgressItem;
import com.osrsGoalTracker.goal.repository.util.SortKeyUtil;

import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.Put;
import software.amazon.awssdk.services.dynamodb.model.TransactWriteItem;
import software.amazon.awssdk.services.dynamodb.model.TransactWriteItemsRequest;

/**
 * Implementation of the GoalRepository interface.
 */
@Slf4j
public class GoalRepositoryImpl implements GoalRepository {
        private final DynamoDbClient dynamoDbClient;
        private final DynamoDbTable<DynamoGoalMetadataItem> metadataTable;
        private final DynamoDbTable<DynamoGoalProgressItem> progressTable;

        /**
         * Constructor for GoalRepositoryImpl.
         *
         * @param dynamoDbClient The DynamoDB client.
         * @param metadataTable  The DynamoDB table for goal metadata.
         * @param progressTable  The DynamoDB table for goal progress.
         */
        @Inject
        public GoalRepositoryImpl(
                        DynamoDbClient dynamoDbClient,
                        DynamoDbTable<DynamoGoalMetadataItem> metadataTable,
                        DynamoDbTable<DynamoGoalProgressItem> progressTable) {
                this.dynamoDbClient = dynamoDbClient;
                this.metadataTable = metadataTable;
                this.progressTable = progressTable;
        }

        private void validateStringNotEmpty(String value, String fieldName) {
                if (value == null || value.trim().isEmpty()) {
                        throw new IllegalArgumentException(fieldName + " cannot be null or empty");
                }
        }

        private void validateNotNull(Object value, String fieldName) {
                if (value == null) {
                        throw new IllegalArgumentException(fieldName + " cannot be null");
                }
        }

        private void validateNonNegative(long value, String fieldName) {
                if (value < 0) {
                        throw new IllegalArgumentException(fieldName + " cannot be negative");
                }
        }

        private void validateGoal(Goal goal) {
                validateNotNull(goal, "goal");
                validateStringNotEmpty(goal.getUserId(), "userId");
                validateStringNotEmpty(goal.getCharacterName(), "characterName");
                validateStringNotEmpty(goal.getTargetAttribute(), "targetAttribute");
                validateStringNotEmpty(goal.getTargetType(), "targetType");
                validateNotNull(goal.getTargetValue(), "targetValue");
                validateNotNull(goal.getCurrentProgress(), "currentProgress");
                validateNonNegative(goal.getCurrentProgress(), "currentProgress");
        }

        private void validateProgressRequest(GoalProgressEvent request) {
                validateNotNull(request, "request");
                validateStringNotEmpty(request.getUserId(), "userId");
                validateStringNotEmpty(request.getCharacterName(), "characterName");
                validateStringNotEmpty(request.getGoalId(), "goalId");
                validateNotNull(request.getProgressValue(), "progressValue");
                validateNonNegative(request.getProgressValue(), "progressValue");
        }

        private DynamoGoalMetadataItem createMetadataItem(String userId, String characterName, String goalId,
                        Goal goal, Instant timestamp) {
                return DynamoGoalMetadataItem.builder()
                                .pk("USER#" + userId)
                                .sk(SortKeyUtil.buildGoalMetadataSortKey(characterName, goalId))
                                .userId(userId)
                                .characterName(characterName)
                                .goalId(goalId)
                                .targetAttribute(goal.getTargetAttribute())
                                .targetType(goal.getTargetType())
                                .targetValue(goal.getTargetValue())
                                .targetDate(goal.getTargetDate())
                                .notificationChannelType(goal.getNotificationChannelType())
                                .frequency(goal.getFrequency())
                                .createdAt(timestamp)
                                .updatedAt(timestamp)
                                .build();
        }

        private DynamoGoalProgressItem createProgressItem(String userId, String characterName, String goalId,
                        Instant timestamp, String sortKey, long currentValue) {
                return DynamoGoalProgressItem.builder()
                                .pk("USER#" + userId)
                                .sk(sortKey)
                                .userId(userId)
                                .characterName(characterName)
                                .goalId(goalId)
                                .progressValue(currentValue)
                                .createdAt(timestamp)
                                .build();
        }

        private TransactWriteItemsRequest createTransactionRequest(String userId, String characterName, String goalId,
                        DynamoGoalMetadataItem metadataItem, Instant timestamp, long currentValue) {
                // Create progress items
                DynamoGoalProgressItem progressItem = createProgressItem(userId, characterName, goalId,
                                timestamp, SortKeyUtil.buildGoalProgressSortKey(characterName, goalId, timestamp),
                                currentValue);
                DynamoGoalProgressItem latestItem = createProgressItem(userId, characterName, goalId,
                                timestamp, SortKeyUtil.buildGoalLatestSortKey(characterName, goalId), currentValue);
                DynamoGoalProgressItem earliestItem = createProgressItem(userId, characterName, goalId,
                                timestamp, SortKeyUtil.buildGoalEarliestSortKey(characterName, goalId), currentValue);

                return TransactWriteItemsRequest.builder()
                                .transactItems(Arrays.asList(
                                                TransactWriteItem.builder()
                                                                .put(Put.builder()
                                                                                .tableName(metadataTable.tableName())
                                                                                .item(metadataTable.tableSchema()
                                                                                                .itemToMap(metadataItem,
                                                                                                                true))
                                                                                .build())
                                                                .build(),
                                                TransactWriteItem.builder()
                                                                .put(Put.builder()
                                                                                .tableName(progressTable.tableName())
                                                                                .item(progressTable.tableSchema()
                                                                                                .itemToMap(progressItem,
                                                                                                                true))
                                                                                .build())
                                                                .build(),
                                                TransactWriteItem.builder()
                                                                .put(Put.builder()
                                                                                .tableName(progressTable.tableName())
                                                                                .item(progressTable.tableSchema()
                                                                                                .itemToMap(latestItem,
                                                                                                                true))
                                                                                .build())
                                                                .build(),
                                                TransactWriteItem.builder()
                                                                .put(Put.builder()
                                                                                .tableName(progressTable.tableName())
                                                                                .item(progressTable.tableSchema()
                                                                                                .itemToMap(earliestItem,
                                                                                                                true))
                                                                                .build())
                                                                .build()))
                                .build();
        }

        private TransactWriteItemsRequest createProgressTransactionRequest(String userId, String characterName,
                        String goalId,
                        Instant timestamp, long currentValue) {
                // Create progress items
                DynamoGoalProgressItem progressItem = createProgressItem(userId, characterName, goalId,
                                timestamp, SortKeyUtil.buildGoalProgressSortKey(characterName, goalId, timestamp),
                                currentValue);
                DynamoGoalProgressItem latestItem = createProgressItem(userId, characterName, goalId,
                                timestamp, SortKeyUtil.buildGoalLatestSortKey(characterName, goalId), currentValue);

                return TransactWriteItemsRequest.builder()
                                .transactItems(Arrays.asList(
                                                TransactWriteItem.builder()
                                                                .put(Put.builder()
                                                                                .tableName(progressTable.tableName())
                                                                                .item(progressTable.tableSchema()
                                                                                                .itemToMap(progressItem,
                                                                                                                true))
                                                                                .build())
                                                                .build(),
                                                TransactWriteItem.builder()
                                                                .put(Put.builder()
                                                                                .tableName(progressTable.tableName())
                                                                                .item(progressTable.tableSchema()
                                                                                                .itemToMap(latestItem,
                                                                                                                true))
                                                                                .build())
                                                                .build()))
                                .build();
        }

        @Override
        public Goal createGoal(Goal goal) {
                validateGoal(goal);

                log.info("Creating goal for user {} targeting {}", goal.getUserId(), goal.getTargetAttribute());
                log.info("Creating new goal for user: {}, character: {}, targetAttribute: {}",
                                goal.getUserId(), goal.getCharacterName(), goal.getTargetAttribute());

                String goalId = UUID.randomUUID().toString();
                Instant now = Instant.now();

                // Create the goal metadata item
                DynamoGoalMetadataItem metadataItem = createMetadataItem(goal.getUserId(), goal.getCharacterName(),
                                goalId, goal, now);
                log.debug("Created metadata item with goalId: {}, pk: {}, sk: {}",
                                goalId, metadataItem.getPk(), metadataItem.getSk());

                // Create transaction request with all items
                TransactWriteItemsRequest transactionRequest = createTransactionRequest(goal.getUserId(),
                                goal.getCharacterName(), goalId, metadataItem, now, goal.getCurrentProgress());
                log.debug("Initiating transaction to create goal and progress records");

                try {
                        dynamoDbClient.transactWriteItems(transactionRequest);
                        log.info("Successfully created goal with id: {} for user: {}, character: {}",
                                        goalId, goal.getUserId(), goal.getCharacterName());
                } catch (Exception e) {
                        log.error("Failed to create goal for user: {}, character: {}, error: {}",
                                        goal.getUserId(), goal.getCharacterName(), e.getMessage());
                        throw e;
                }

                goal.setGoalId(goalId);
                return goal;
        }

        @Override
        public void createGoalProgress(GoalProgressEvent request) {
                validateProgressRequest(request);

                log.info("Creating goal progress for user: {}, character: {}, goalId: {}",
                                request.getUserId(), request.getCharacterName(), request.getGoalId());

                Instant now = Instant.now();

                // Create transaction request with progress items
                TransactWriteItemsRequest transactionRequest = createProgressTransactionRequest(
                                request.getUserId(),
                                request.getCharacterName(),
                                request.getGoalId(),
                                now,
                                request.getProgressValue());

                log.debug("Initiating transaction to create goal progress records");

                try {
                        dynamoDbClient.transactWriteItems(transactionRequest);
                        log.info("Successfully created goal progress for user: {}, character: {}, goalId: {}",
                                        request.getUserId(), request.getCharacterName(), request.getGoalId());
                } catch (Exception e) {
                        log.error("Failed to create goal progress for user: {}, character: {}, goalId: {}, error: {}",
                                        request.getUserId(), request.getCharacterName(), request.getGoalId(),
                                        e.getMessage());
                        throw e;
                }
        }
}