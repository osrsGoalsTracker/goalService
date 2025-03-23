package com.osrsGoalTracker.goal.repository.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.HashMap;

import com.osrsGoalTracker.goal.model.Goal;
import com.osrsGoalTracker.goal.repository.impl.DynamoItem.DynamoGoalMetadataItem;
import com.osrsGoalTracker.goal.repository.impl.DynamoItem.DynamoGoalProgressItem;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.TransactWriteItemsRequest;

/**
 * Tests for {@link GoalRepositoryImpl}.
 */
class GoalRepositoryImplTest {
        @Mock
        private DynamoDbClient dynamoDbClient;

        @Mock
        private DynamoDbTable<DynamoGoalMetadataItem> metadataTable;

        @Mock
        private DynamoDbTable<DynamoGoalProgressItem> progressTable;

        @Mock
        private TableSchema<DynamoGoalMetadataItem> metadataSchema;

        @Mock
        private TableSchema<DynamoGoalProgressItem> progressSchema;

        @Captor
        private ArgumentCaptor<TransactWriteItemsRequest> transactionCaptor;

        private GoalRepositoryImpl repository;

        @BeforeEach
        void setUp() {
                MockitoAnnotations.openMocks(this);
                when(metadataTable.tableSchema()).thenReturn(metadataSchema);
                when(progressTable.tableSchema()).thenReturn(progressSchema);
                when(metadataSchema.itemToMap(any(DynamoGoalMetadataItem.class), anyBoolean()))
                                .thenReturn(new HashMap<>());
                when(progressSchema.itemToMap(any(DynamoGoalProgressItem.class), anyBoolean()))
                                .thenReturn(new HashMap<>());
                when(metadataTable.tableName()).thenReturn("goals-metadata");
                when(progressTable.tableName()).thenReturn("goals-progress");
                repository = new GoalRepositoryImpl(dynamoDbClient, metadataTable, progressTable);
        }

        @Test
        void createGoal_ValidGoal_CreatesGoalAndProgress() {
                // Arrange
                Goal goal = Goal.builder()
                                .userId("testUser")
                                .characterName("testChar")
                                .targetAttribute("ATTACK")
                                .targetType("LEVEL")
                                .targetValue(99L)
                                .currentProgress(1L)
                                .targetDate(Instant.now().plusSeconds(3600))
                                .notificationChannelType("DISCORD")
                                .frequency("DAILY")
                                .build();

                // Act
                Goal result = repository.createGoal(goal);

                // Assert
                assertNotNull(result);
                assertNotNull(result.getGoalId());
                assertEquals("testUser", result.getUserId());
                assertEquals("testChar", result.getCharacterName());
                assertEquals("ATTACK", result.getTargetAttribute());
                assertEquals(99L, result.getTargetValue());
                assertEquals(1L, result.getCurrentProgress());

                verify(dynamoDbClient).transactWriteItems(transactionCaptor.capture());
                TransactWriteItemsRequest transaction = transactionCaptor.getValue();
                assertEquals(4, transaction.transactItems().size()); // Metadata + 3 progress items
        }

        @Test
        void createGoal_NullGoal_ThrowsException() {
                // Act & Assert
                IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                                () -> repository.createGoal(null));
                assertEquals("goal cannot be null", exception.getMessage());
        }

        @Test
        void createGoal_EmptyUserId_ThrowsException() {
                // Arrange
                Goal goal = Goal.builder()
                                .userId("")
                                .characterName("testChar")
                                .targetAttribute("ATTACK")
                                .targetType("LEVEL")
                                .targetValue(99L)
                                .currentProgress(1L)
                                .targetDate(Instant.now().plusSeconds(3600))
                                .build();

                // Act & Assert
                IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                                () -> repository.createGoal(goal));
                assertEquals("userId cannot be null or empty", exception.getMessage());
        }

        @Test
        void createGoal_NullCharacterName_ThrowsException() {
                // Arrange
                Goal goal = Goal.builder()
                                .userId("testUser")
                                .targetAttribute("ATTACK")
                                .targetType("LEVEL")
                                .targetValue(99L)
                                .currentProgress(1L)
                                .targetDate(Instant.now().plusSeconds(3600))
                                .build();

                // Act & Assert
                IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                                () -> repository.createGoal(goal));
                assertEquals("characterName cannot be null or empty", exception.getMessage());
        }

        @Test
        void createGoal_NullTargetType_ThrowsException() {
                // Arrange
                Goal goal = Goal.builder()
                                .userId("testUser")
                                .characterName("testChar")
                                .targetAttribute("ATTACK")
                                .targetValue(99L)
                                .currentProgress(1L)
                                .targetDate(Instant.now().plusSeconds(3600))
                                .build();

                // Act & Assert
                IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                                () -> repository.createGoal(goal));
                assertEquals("targetType cannot be null or empty", exception.getMessage());
        }

        @Test
        void createGoal_DynamoDbError_PropagatesException() {
                // Arrange
                Goal goal = Goal.builder()
                                .userId("testUser")
                                .characterName("testChar")
                                .targetAttribute("ATTACK")
                                .targetType("LEVEL")
                                .targetValue(99L)
                                .currentProgress(1L)
                                .targetDate(Instant.now().plusSeconds(3600))
                                .build();

                when(dynamoDbClient.transactWriteItems(any(TransactWriteItemsRequest.class)))
                                .thenThrow(new RuntimeException("DynamoDB error"));

                // Act & Assert
                assertThrows(RuntimeException.class,
                                () -> repository.createGoal(goal));
        }
}