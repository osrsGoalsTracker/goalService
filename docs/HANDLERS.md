# Lambda Handlers

## Overview

The service exposes its functionality through AWS Lambda handlers. Each handler strictly follows a 4-step pattern:

1. Parse input from API Gateway event
2. Validate input
3. Run service function
4. Return formatted response

## Implementation Pattern

All handlers MUST follow this implementation pattern:

```java
public class SomeHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    @Inject
    private SomeService service;
    
    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent input, Context context) {
        try {
            // 1. Parse input
            SomeRequest request = parseInput(input);
            
            // 2. Validate input
            validateRequest(request);
            
            // 3. Run service function
            SomeResponse response = service.doSomething(request);
            
            // 4. Return formatted response
            return ApiGatewayResponse.builder()
                .setStatusCode(200)
                .setObjectBody(response)
                .build();
                
        } catch (Exception e) {
            return handleError(e);
        }
    }
}
```

## Available Handlers

### Goal Management

#### CreateGoalFromGoalCreationEventHandler
- **Source**: EventBridge
- **Package**: `com.osrsGoalTracker.goal.handler.CreateGoalFromGoalCreationEventHandler`
- **Event**: `GoalCreationEvent`
- **Description**: Processes goal creation events from EventBridge, creating goals in the system with their initial progress values.
- **Example Event**:
```json
{
    "userId": "123",
    "characterName": "PlayerOne",
    "metricName": "WOODCUTTING",
    "targetType": "xp",
    "targetValue": 1000000,
    "currentValue": 500000,
    "targetDate": "2024-12-31T23:59:59Z",
    "notificationChannelType": "DISCORD",
    "frequency": "DAILY"
}
```

## Error Handling

All handlers use a standardized error handling approach:

```java
private APIGatewayProxyResponseEvent handleError(Exception e) {
    if (e instanceof ResourceNotFoundException) {
        return ApiGatewayResponse.builder()
            .setStatusCode(404)
            .setObjectBody(new ErrorResponse("NOT_FOUND", e.getMessage()))
            .build();
    }
    
    if (e instanceof BadRequestException) {
        return ApiGatewayResponse.builder()
            .setStatusCode(400)
            .setObjectBody(new ErrorResponse("BAD_REQUEST", e.getMessage()))
            .build();
    }
    
    // Default to internal server error
    return ApiGatewayResponse.builder()
        .setStatusCode(500)
        .setObjectBody(new ErrorResponse("INTERNAL_ERROR", "An unexpected error occurred"))
        .build();
}
```

## Response Format

### Success Response
```json
{
    "statusCode": 200,
    "body": {
        // Response data
    },
    "headers": {
        "Content-Type": "application/json"
    }
}
```

### Error Response
```json
{
    "statusCode": 400,
    "body": {
        "error": {
            "code": "BAD_REQUEST",
            "message": "Invalid input provided",
            "details": {
                // Error details
            }
        }
    },
    "headers": {
        "Content-Type": "application/json"
    }
}
```

## Testing

Each handler must have comprehensive tests covering:

1. Input parsing
2. Input validation
3. Service interaction
4. Response formatting
5. Error handling

Example test structure:

```java
@ExtendWith(MockitoExtension.class)
class CreateUserHandlerTest {
    @Mock
    private UserService userService;
    
    @InjectMocks
    private CreateUserHandler handler;
    
    @Test
    void handleRequest_ValidInput_Success() {
        // Given
        APIGatewayProxyRequestEvent input = createValidInput();
        when(userService.createUser(any())).thenReturn(createUser());
        
        // When
        APIGatewayProxyResponseEvent response = handler.handleRequest(input, null);
        
        // Then
        assertThat(response.getStatusCode()).isEqualTo(200);
        // Additional assertions
    }
    
    @Test
    void handleRequest_InvalidInput_BadRequest() {
        // Test invalid input handling
    }
    
    @Test
    void handleRequest_ServiceError_InternalError() {
        // Test service error handling
    }
} 