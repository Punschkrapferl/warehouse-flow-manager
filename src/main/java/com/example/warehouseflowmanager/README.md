# Warehouse Flow Manager

Warehouse Flow Manager is a full stack warehouse operations demo inspired by intralogistics software systems.

The goal of this project is to model core warehouse workflows such as product management, inventory tracking, warehouse order processing, and AGV task assignment in a clean and maintainable architecture.

This project is being built step by step to demonstrate backend, frontend, API, database, and software design skills in a realistic business context.

## Why this project

I built this project as a portfolio application for a Java Full Stack role in the logistics and warehouse software domain.

The focus is not on overengineering, but on building a clean and extensible system that reflects real-world software concerns:

- clear module boundaries
- maintainable code structure
- REST API design
- relational data modeling
- incremental feature development
- transparent engineering decisions through commit history

## Project goals

The project is intended to demonstrate:

- Java backend development with Spring Boot
- Angular frontend development
- REST API design
- relational database modeling with PostgreSQL
- clean modular architecture
- testable and maintainable business logic
- structured project evolution visible through Git commits

## Tech stack

### Backend
- Java
- Spring Boot
- Maven
- Spring Web
- Spring Validation

### Planned backend additions
- Spring Data JPA
- PostgreSQL
- OpenAPI / Swagger
- JUnit
- Testcontainers

### Frontend
- Angular
- TypeScript
- Angular Material

### Tooling
- Git / GitHub
- IntelliJ IDEA
- Docker Compose later in the project

## Architecture

The system is planned as a **modular monolith**.

This means the application is built as one deployable backend, but internally separated into clean business modules. This keeps the project easier to understand, develop, and test, while still allowing future extraction into independent services if needed.

### Planned backend modules

- Inventory
- Orders
- AGV
- Tasks
- Shared infrastructure

### Planned package structure

```text
src/main/java/com/example/warehouseflowmanager/
  inventory/
  orders/
  agv/
  task/
  shared/
  controller/
```

### Core business flow
The intended workflow of the application is:
Create products
Create storage locations
Add stock to inventory
Create warehouse orders
Generate picking tasks
Assign tasks to available AGVs
Track progress of tasks and orders
Mark completed warehouse operations

## Current project status
### Completed
- Spring Boot project initialized with Maven
- Backend application bootstrapped
- First health check endpoint added

### In progress
- Understanding and documenting project structure
- Planning core domain model and feature order

### Next steps
- Add project README and architecture documentation
- Define first core entity: Product
- Implement first real feature module
- Reintroduce persistence layer with JPA and PostgreSQL

## Roadmap
### Phase 1: Backend foundation
- ✅ Initialize Spring Boot project 
- ✅ Add first health endpoint
- Clean up project structure
- Add documentation and design notes

### Phase 2: Product and location management
- Add Product entity
- Add StorageLocation entity
- Add controllers and service layer
- Add validation rules

### Phase 3: Inventory
- Add InventoryItem entity
- Implement stock-in and stock-out logic
- Add low-stock overview

### Phase 4: Orders
- Add WarehouseOrder and OrderLine
- Create order workflow
- Validate stock before assignment

### Phase 5: Tasks and AGVs
- Add PickingTask and AGV entities
- Implement simple task assignment logic
- Track task and order status

### Phase 6: Frontend
- Initialize Angular frontend
- Add dashboard
- Add inventory page
- Add order management UI
- Add AGV/task monitoring UI

### Phase 7: Quality improvements
- Add JUnit tests
- Add integration tests
- Add OpenAPI documentation
- Add Docker Compose setup

## Running the backend
### Requirements
- Java 21
- Maven Wrapper included in the project

### Start the application
```bash
./mvnw spring-boot:run
```
The backend should be available at:
```
http://localhost:8080
```
### Health endpoint
```
GET /api/v1/health
```
Expected response:
```
Warehouse Flow Manager backend is running
```

## API overview
This section will grow as features are added.

### Current endpoint
- `GET /api/v1/health`

### Planned endpoints
- `GET /api/v1/products`
- `POST /api/v1/products`
- `GET /api/v1/locations`
- `POST /api/v1/locations`
- `GET /api/v1/inventory`
- `POST /api/v1/orders`
- `GET /api/v1/orders`
- `GET /api/v1/agvs`
- `GET /api/v1/tasks`

## Design decisions
### Why a modular monolith?

A modular monolith was chosen to keep the project simple, maintainable, and realistic for a portfolio application. It allows clean separation of concerns without the operational overhead of microservices.

### Why this domain?
Warehouse and intralogistics software is a strong fit for the target role. This project focuses on business processes that are directly relevant to inventory handling, task orchestration, and operational efficiency.

### Why build incrementally?
The project is intentionally built in small steps so the commit history reflects real engineering progress, from setup and understanding to design and implementation.

### Learning notes
This project is also a structured learning journey into:
- Spring Boot
- Maven
- REST APIs
- backend architecture
- database integration
- full stack application design

The repository will therefore show both implementation progress and the reasoning behind technical decisions.

## Author
Punschkrapferl