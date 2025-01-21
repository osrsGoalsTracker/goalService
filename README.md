# OSRS Goals Service

A Java service for tracking Old School RuneScape player goals and progress.

## Overview

This service provides a set of AWS Lambda functions for managing OSRS player goals and tracking progress. It follows a Layered Service Architecture (LSA) pattern and is built with Java 21.

## Documentation

- [Project Structure and Architecture](docs/ARCHITECTURE.md) - Detailed overview of the project's layered architecture and development guidelines
- [Handler Interfaces](docs/HANDLERS.md) - Lambda function entry points, inputs, and outputs
- [Service Interfaces](docs/SERVICES.md) - Service layer interfaces and functionality
- [Data Models](docs/MODELS.md) - Core data models and their relationships

## Architecture

### Domain Layer
The service is organized into domain-specific modules (e.g., User, Character, Goal) that handle core business logic.

### Orchestration Layer
The orchestration layer manages cross-domain interactions through events. It includes:
- Event models (e.g., GoalCreationEvent)
- Event handlers
- Event-driven workflows

## Requirements

- JDK 21
- Gradle 8.x

## Quick Start

1. Install dependencies:
```bash
./gradlew build
```

2. Run tests:
```bash
./gradlew test
```

3. Build all Lambda handlers:
```bash
./gradlew buildAllHandlers
```

Each handler will be built into its own JAR file in `build/libs/`.

## Dependencies

- AWS Lambda Core - Lambda function support
- AWS Lambda Events - Event handling
- AWS DynamoDB - Database operations
- Google Guice - Dependency injection
- Jackson - JSON serialization
- Log4j2 - Logging
- Lombok - Boilerplate reduction
- JUnit 5 - Testing
- Mockito - Mocking for tests

## Infrastructure

The service is deployed using AWS CDK with the following components:

- API Gateway for REST endpoints
- Lambda functions for business logic
- DynamoDB tables for data storage 
