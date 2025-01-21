# Layered Architecture Rule Set

This rule set defines how to organize and implement a modular layered architecture for a product, including both domain services (e.g., `UserService`, `CharacterService`) and orchestration Lambdas for event-driven workflows. It outlines layers, interaction rules, service boundaries, and specific patterns to handle cross-cutting concerns like events and shared resources.

---

## Rule Set

### Layer Definitions

#### Domain Layer (Service-Specific)

Each domain (e.g., `User`, `Character`, `Goal`) has its own service responsible for managing the business logic and data access for its entities. It is structured into three sub-layers:

1. **Handler Layer**
   - Contains entry points for APIs (e.g., Lambda handlers or controllers).
   - Handles input (e.g., HTTP requests or events) and delegates logic to the service layer.
   - Only calls the service layer.

2. **Service Layer**
   - Encapsulates business logic for the domain.
   - Uses repository interfaces to fetch or persist data. Uses external layer interfaces to communicate with external systems.
   - Only depends on the repository layer or other services via interfaces.

3. **Repository Layer**
   - Abstracts data access logic.
   - Interacts with the underlying data store (e.g., DynamoDB, RDBMS).
   - Provides methods scoped to the domain (e.g., `findUserById`, `saveGoal`).
   - Does not call the service layer.

4. ** External layer**
   - Responsible for managing communication with third-party systems, APIs, or services. This layer:
   - Abstracts details like HTTP request/response formats, authentication, and API endpoints.
   - Provides a clean and reusable interface for external communication.
   - Decouples external systems from domain logic.
   - Calls no other layers.

5. **Common Layer**
   - Contains reusable components across domains:
   - Shared models - Defines domain-agnostic objects (e.g., events like `GoalCreationEvent`) that are shared across services. 
   - Shared utilities - Includes helpers for common tasks (e.g., event publishing, validation).

---

### Rules for Inter-Layer Interaction

1. **Handler Layer**
   - Allowed to call: Service layer.
   - Not allowed to call directly: Repository layer or shared utilities.

2. **Service Layer**
   - Allowed to call: Repository layer, other services via interfaces, external layer via interfaces, shared utilities.
   - Not allowed to call directly: Handlers.

3. **Repository Layer**
   - Allowed to call: Data store (e.g., DynamoDB SDK).
   - Not allowed to call: Handlers, service layer, or orchestration modules.

4. **External Layer**
   - Allowed to call: external systems via interfaces.
   - Not allowed to call directly: Repository layer, handlers, service layer.

---

### Rules for Inter-Service Interaction

1. **Direct Service-to-Service Interaction**
   - Use interfaces to define contracts for interactions (e.g., `CharacterService` interface for fetching character data).
   - Use dependency injection to bind the correct implementation.

2. **Event-Driven Interaction**
   - Services can publish domain events (e.g., `GoalCreatedEvent`) to the event bus.
   - Other services or orchestration Lambdas consume these events and react accordingly.

3. **Avoid Cross-Service Repository Access**
   - Services should never directly query another service’s repository. Always go through the service layer or use events for decoupled communication.

---

### Example Tree Structure: Deep Dive into CharacterService

**Folder Structure** (character service example)

```
src/
├── main/
│   ├── java/
│   │   ├── com/
│   │   │   ├── example/
│   │   │   │   ├── character/
│   │   │   │   │   ├── handler/
│   │   │   │   │   │   ├── CharacterHandler.java
│   │   │   │   │   ├── service/
│   │   │   │   │   │   ├── CharacterService.java
│   │   │   │   │   │   ├── impl/
│   │   │   │   │   │   │   ├── CharacterServiceImpl.java
│   │   │   │   │   ├── repository/
│   │   │   │   │   │   ├── CharacterRepository.java
│   │   │   │   │   │   ├── impl/
│   │   │   │   │   │   │   ├── CharacterRepositoryImpl.java
│   │   │   │   │   ├── model/
│   │   │   │   │   │   ├── Character.java
│   │   │   │   │   │   ├── CreateCharacterRequest.java
│   │   │   │   │   │   ├── CreateCharacterResponse.java
│   │   │   │   │   ├── di/
│   │   │   │   │   │   ├── CharacterModule.java
│   │   │   │   │   ├── external/
│   │   │   │   │   │   ├── GameApiClient.java
│   │   │   │   │   │   ├── impl/
│   │   │   │   │   │   │   ├── ExternalGameApiClientImpl.java
│   │   │   │   │   │   ├── model/
│   │   │   │   │   │   │   ├── GameCharacterResponse.java
```

## Development Guidelines

### 1. Handler Implementation
```java
public class AddCharacterToUserHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    @Inject
    private CharacterService characterService;
    
    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent input, Context context) {
        AddCharacterToUserRequest request = parseInput(input);
        String userId = input.getPathParameters().get("userId");
        
        validateRequest(request);
        
        Character character = characterService.addCharacterToUser(userId, request);
        
        return ApiGatewayResponse.builder()
            .setStatusCode(200)
            .setObjectBody(character)
            .build();
    }
} 