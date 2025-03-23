package com.osrsGoalTracker.goal.repository.impl;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.HashMap;
import java.util.UUID;

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
import software.amazon.awssdk.services.dynamodb.model.TransactWriteItemsResponse;

/**
 * Test class for GoalRepositoryImpl.
 */
public class GoalRepositoryImplTest {
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
        void createGoal_Success() {
                // Arrange
                Goal goal = createValidGoal();
                when(dynamoDbClient.transactWriteItems(any(TransactWriteItemsRequest.class)))
                                .thenReturn(TransactWriteItemsResponse.builder().build());

                // Act
                Goal result = repository.createGoal(goal);

                // Assert
                assertNotNull(result);
                assertEquals(goal.getUserId(), result.getUserId());
                assertEquals(goal.getCharacterName(), result.getCharacterName());
                assertEquals(goal.getTargetAttribute(), result.getTargetAttribute());
                verify(dynamoDbClient).transactWriteItems(any(TransactWriteItemsRequest.class));
        }

        @Test
        void createGoal_NullGoal_ThrowsException() {
                // Act & Assert
                assertThrows(IllegalArgumentException.class, () -> repository.createGoal(null));
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

        @Test
        void createGoalProgress_Success() {
                // Arrange
                Goal goal = createValidGoal();
                when(dynamoDbClient.transactWriteItems(any(TransactWriteItemsRequest.class)))
                                .thenReturn(TransactWriteItemsResponse.builder().build());

                // Act & Assert
                assertDoesNotThrow(() -> repository.createGoalProgress(goal));
                verify(dynamoDbClient).transactWriteItems(any(TransactWriteItemsRequest.class));
        }

        @Test
        void createGoalProgress_NullGoal_ThrowsException() {
                // Act & Assert
                assertThrows(IllegalArgumentException.class, () -> repository.createGoalProgress(null));
        }

        @Test
        void createGoalProgress_EmptyUserId_ThrowsException() {
                // Arrange
                Goal goal = createValidGoal();
                goal.setUserId("");

                // Act & Assert
                assertThrows(IllegalArgumentException.class, () -> repository.createGoalProgress(goal));
        }

        @Test
        void createGoalProgress_EmptyCharacterName_ThrowsException() {
                // Arrange
                Goal goal = createValidGoal();
                goal.setCharacterName("");

                // Act & Assert
                assertThrows(IllegalArgumentException.class, () -> repository.createGoalProgress(goal));
        }

        @Test
        void createGoalProgress_EmptyGoalId_ThrowsException() {
                // Arrange
                Goal goal = createValidGoal();
                goal.setGoalId("");

                // Act & Assert
                assertThrows(IllegalArgumentException.class, () -> repository.createGoalProgress(goal));
        }

        @Test
        void createGoalProgress_NegativeProgressValue_ThrowsException() {
                // Arrange
                Goal goal = createValidGoal();
                goal.setCurrentProgress(-1);

                // Act & Assert
                assertThrows(IllegalArgumentException.class, () -> repository.createGoalProgress(goal));
        }

        private Goal createValidGoal() {
                return Goal.builder()
                                .userId(UUID.randomUUID().toString())
                                .characterName("testCharacter")
                                .targetAttribute("Woodcutting")
                                .targetType("xp")
                                .targetValue(1000000L)
                                .currentProgress(0L)
                                .targetDate(Instant.now().plusSeconds(86400))
                                .notificationChannelType("SMS")
                                .frequency("daily")
                                .goalId(UUID.randomUUID().toString())
                                .build();
        }
}