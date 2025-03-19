package com.osrsGoalTracker.goal.di;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.osrsGoalTracker.goal.repository.GoalRepository;
import com.osrsGoalTracker.goal.repository.impl.GoalRepositoryImpl;
import com.osrsGoalTracker.goal.service.GoalService;
import com.osrsGoalTracker.hiscore.service.HiscoresService;
import com.osrsGoalTracker.hiscore.service.impl.HiscoresServiceImpl;
import com.osrsGoalTracker.goal.service.impl.GoalServiceImpl;
import com.osrsGoalTracker.goal.repository.impl.DynamoItem.DynamoGoalMetadataItem;
import com.osrsGoalTracker.goal.repository.impl.DynamoItem.DynamoGoalProgressItem;

import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.regions.Region;

/**
 * Guice module for goal-related bindings.
 */
public class GoalModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(GoalService.class).to(GoalServiceImpl.class);
        bind(GoalRepository.class).to(GoalRepositoryImpl.class);
        bind(HiscoresService.class).to(HiscoresServiceImpl.class);
    }

    @Provides
    @Singleton
    DynamoDbClient provideDynamoDbClient() {
        return DynamoDbClient.builder()
                .region(Region.of(Region.US_WEST_2.toString()))
                .build();
    }

    @Provides
    @Singleton
    DynamoDbEnhancedClient provideDynamoDbEnhancedClient(DynamoDbClient dynamoDbClient) {
        return DynamoDbEnhancedClient.builder()
                .dynamoDbClient(dynamoDbClient)
                .build();
    }

    @Provides
    @Singleton
    DynamoDbTable<DynamoGoalMetadataItem> provideMetadataTable(DynamoDbEnhancedClient enhancedClient) {
        String tableName = System.getenv("GOAL_TRACKER_TABLE_NAME");
        return enhancedClient.table(tableName, TableSchema.fromClass(DynamoGoalMetadataItem.class));
    }

    @Provides
    @Singleton
    DynamoDbTable<DynamoGoalProgressItem> provideProgressTable(DynamoDbEnhancedClient enhancedClient) {
        String tableName = System.getenv("GOAL_TRACKER_TABLE_NAME");
        return enhancedClient.table(tableName, TableSchema.fromClass(DynamoGoalProgressItem.class));
    }
}
