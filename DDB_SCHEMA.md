
# DynamoDB Schema for Goal Tracking and Notification System

## Overview

This schema is designed to support a goal tracking and notification platform for RuneScape characters. The system allows users to:
- Create and manage RuneScape-related goals.
- Track progress towards goals over time.
- Configure notification channels for goal updates.
- Support efficient queries for metadata, progress tracking, and notifications.

### Key Features
1. **Flexible Data Organization**: Supports storing metadata, goals, progress, and notifications using structured keys.
2. **Efficient Access Patterns**: Enables querying for user-specific data, goals, and progress.
3. **Scalable Design**: Accommodates daily progress updates for multiple goals while adhering to DynamoDB’s scalability limits.
4. **Low Maintenance**: Designed with minimal data duplication and optional TTL for data retention.

---

## DynamoDB Schema

### Primary Table: Goals Table
- **Partition Key (PK):** `USER#<user_id>`
- **Sort Key (SK):** Encodes various entity types and their metadata/data using structured prefixes.

---

### Sort Key Structure and Examples

#### 1. **Goal Metadata**
   - **Sort Key:** `CHARACTER#<character_name>#GOAL#METADATA#<goal_id>`
   - **Purpose:** This is the metadata for the goal. It is used to store information about the goal such as the character name, skill/activity, targetXp, targetDate, notificationChannel, frequency, createdAt, and updatedAt.
   - **Example Item:**
     ```json
     {
       "PK": "USER#12345",
       "SK": "CHARACTER#Character123#GOAL#METADATA#a4cae247-df47-45ec-a16d-5c51ec16fe23",
       "userId": "12345",
       "characterName": "Character123",
       "goalId": "a4cae247-df47-45ec-a16d-5c51ec16fe23",
       "targetAttribute": "Woodcutting",
       "targetType": "xp",
       "targetValue": 13034431,
       "targetDate": "2025-03-01",
       "notificationChannelType": "SMS",
       "frequency": "daily",
       "createdAt": "2025-01-01T00:00:00Z",
       "updatedAt": "2025-01-01T00:00:00Z"
     }
     ```

#### 2. **Progress Records**
   - **Sort Key:** `CHARACTER#<character_name>#GOAL#<goal_id>#<timestamp>`
   - **Purpose:** This is the progress record for the goal. It is used to store information about the progress for the goal such as the progress value, timestamp, and createdAt.
   - **Example Item:**
     ```json
     {
       "PK": "USER#12345",
       "SK": "CHARACTER#Character123#GOAL#a4cae247-df47-45ec-a16d-5c51ec16fe23#2025-01-01T00:00:00Z",
       "userId": "12345",
       "characterName": "Character123",
       "goalId": "a4cae247-df47-45ec-a16d-5c51ec16fe23",
       "progressValue": 12000000,
       "timestamp": "2025-01-01T00:00:00Z",
       "createdAt": "2025-01-01T00:00:00Z",
       "updatedAt": "2025-01-01T00:00:00Z"
     }
     ```

#### 3. **Latest Progress**
   - **Sort Key:** `CHARACTER#<character_name>#GOAL#<goal_id>#LATEST`
   - **Purpose:** This is the latest progress record for the goal. It is used to store information about the latest progress for the goal such as the progress value, timestamp, and createdAt. This provides a quick way to get the latest progress for a goal.
   - **Example Item:**
     ```json
     {
       "PK": "USER#12345",
       "SK": "CHARACTER#Character123#GOAL#a4cae247-df47-45ec-a16d-5c51ec16fe23#LATEST",
       "userId": "12345",
       "characterName": "Character123",
       "goalId": "a4cae247-df47-45ec-a16d-5c51ec16fe23",
       "progressValue": 12500000,
       "timestamp": "2025-01-02T00:00:00Z",
       "createdAt": "2025-01-02T00:00:00Z",
       "updatedAt": "2025-01-02T00:00:00Z"
     }
     ```  

#### 4. **Earliest Progress**
   - **Sort Key:** `CHARACTER#<character_name>#GOAL#<goal_id>#EARLIEST`
   - **Purpose:** This is the earliest progress record for the goal. It is used to store information about the earliest progress for the goal such as the timestamp, and createdAt. This provides a quick way to get the earliest progress for a goal.
   - **Example Item:**
     ```json
     {
       "PK": "USER#12345",
       "SK": "CHARACTER#Character123#GOAL#a4cae247-df47-45ec-a16d-5c51ec16fe23#EARLIEST",
       "userId": "12345",
       "characterName": "Character123",
       "goalId": "a4cae247-df47-45ec-a16d-5c51ec16fe23",
       "timestamp": "2025-01-01T00:00:00Z",
       "createdAt": "2025-01-01T00:00:00Z",
       "updatedAt": "2025-01-01T00:00:00Z"
     }
     ```

---

### Indexes

#### Primary Index
- **PK:** `USER#<user_id>`
- **SK:** Encodes metadata, notification channels, goals, and progress.

#### Secondary Index
- **PK:** `email`
- **SK:** `METADATA`
- **Purpose:** This is the secondary index for the user. It is used to quickly query for a user by their email.
