
# Glass Finder

## Mobile Glass Inventory Management System

Glass Finder is a full-stack inventory management application designed for mobile shops to manage screen-glass inventory and quickly identify compatible glass boxes for specific phone models.

The system provides a centralized interface for searching compatible glass, managing stock, recording sales, and maintaining an immutable history of inventory transactions.

---

## Overview

Mobile shops often manage screen-glass inventory using manual records, making it difficult to quickly determine:

- Which glass is compatible with a particular phone model
- Which box contains the required glass
- How much stock is currently available
- When stock was added or sold
- Whether sufficient stock is available before completing a sale

Glass Finder addresses these problems through a web-based inventory management system.

The application follows a layered backend architecture using Spring Boot and a React-based frontend, with PostgreSQL as the persistence layer.

---

## Features

### Inventory Management

- Create and manage glass boxes
- Assign multiple compatible phone models to each box
- Maintain current inventory levels
- Enforce unique box codes
- Display inventory status

### Phone Model Search

- Search glass inventory using a phone model
- Find all compatible glass boxes
- Display only boxes with available stock
- Support case-insensitive model searches

### Stock Management

- Add stock to an existing box
- Record stock sales
- Prevent negative inventory
- Validate requested quantities against available stock

### Transaction History

- Record every stock addition and sale
- View transaction history for individual boxes
- Track stock after each transaction
- Preserve historical inventory movements

### Dashboard

- Total number of glass boxes
- Total available stock
- Low-stock count
- Inventory table with compatibility and stock status

### Validation and Error Handling

- Duplicate box validation
- Missing box handling
- Insufficient stock validation
- Request validation
- Centralized exception handling
- Appropriate HTTP status codes

---

## Architecture

```text
                         React + TypeScript
                                |
                                | REST API
                                |
                                v
                       Spring Boot Backend
                                |
              +-----------------+-----------------+
              |                 |                 |
         Controllers        Services        Repositories
              |                 |                 |
              +-----------------+-----------------+
                                |
                                | JPA / Hibernate
                                |
                                v
                         PostgreSQL
                          / Supabase
````

---

## Technology Stack

### Frontend

* React
* TypeScript
* Vite
* Axios
* CSS

### Backend

* Java
* Spring Boot
* Spring Web MVC
* Spring Data JPA
* Hibernate
* Jakarta Bean Validation
* Lombok
* Maven

### Database

* PostgreSQL
* Supabase

### Development and Testing

* IntelliJ IDEA
* Git
* GitHub
* Postman

---

## Project Structure

```text
glass-finder/
|
├── backend/
│   ├── src/
│   │   └── main/
│   │       ├── java/
│   │       │   └── com/
│   │       │       └── glassfinder/
│   │       │           ├── config/
│   │       │           ├── controller/
│   │       │           ├── dto/
│   │       │           ├── entity/
│   │       │           ├── exception/
│   │       │           ├── repository/
│   │       │           ├── service/
│   │       │           └── GlassFinderApplication.java
│   │       │
│   │       └── resources/
│   │           └── application.properties
│   │
│   ├── pom.xml
│   ├── mvnw
│   └── mvnw.cmd
│
├── frontend/
│   ├── src/
│   │   ├── api.ts
│   │   ├── App.tsx
│   │   ├── index.css
│   │   └── main.tsx
│   │
│   ├── package.json
│   └── vite.config.ts
│
├── .gitignore
└── README.md
```

---

## Database Design

The application uses a normalized relational database consisting of four primary tables.

### glass_boxes

Stores information about individual glass boxes.

| Column     | Description                   |
| ---------- | ----------------------------- |
| id         | Primary key                   |
| box_code   | Unique identifier for the box |
| created_at | Creation timestamp            |
| updated_at | Last update timestamp         |

### phone_models

Stores supported phone models.

| Column     | Description             |
| ---------- | ----------------------- |
| id         | Primary key             |
| model_name | Unique phone model name |
| created_at | Creation timestamp      |

### glass_compatibility

Represents the relationship between glass boxes and compatible phone models.

| Column     | Description                          |
| ---------- | ------------------------------------ |
| id         | Primary key                          |
| box_id     | Foreign key referencing glass_boxes  |
| model_id   | Foreign key referencing phone_models |
| created_at | Creation timestamp                   |

A unique constraint is applied to:

```text
(box_id, model_id)
```

to prevent duplicate compatibility mappings.

### stock_transactions

Stores all inventory movements.

| Column           | Description                         |
| ---------------- | ----------------------------------- |
| id               | Primary key                         |
| box_id           | Foreign key referencing glass_boxes |
| transaction_type | ADD, SOLD, or ADJUST                |
| quantity         | Transaction quantity                |
| notes            | Optional transaction description    |
| created_at       | Transaction timestamp               |

---

## Stock Management Model

Stock is derived from transaction history rather than being treated as an independently mutable value.

```text
Current Stock =
    SUM(ADD transactions)
    -
    SUM(SOLD transactions)
```

For example:

```text
ADD  10
SOLD  2
SOLD  1
------------
Stock = 7
```

This approach preserves the history of inventory movements and allows the application to reconstruct stock changes.

The backend also validates stock before recording a sale to prevent negative inventory.

---

## REST API

Base URL:

```text
http://localhost:8080/api
```

### Create Glass Box

```http
POST /boxes
```

Request:

```json
{
  "boxCode": "BOX005",
  "models": [
    "v20",
    "rm20i"
  ],
  "quantity": 10
}
```

---

### Get All Boxes

```http
GET /boxes
```

Returns all glass boxes along with their compatible models and current stock.

---

### Search by Phone Model

```http
GET /boxes/search?model=v20
```

Returns compatible boxes with available stock.

Example:

```json
[
  {
    "id": 1,
    "boxCode": "BOX001",
    "models": [
      "rm20i",
      "rm30",
      "v20"
    ],
    "currentStock": 6
  },
  {
    "id": 3,
    "boxCode": "BOX003",
    "models": [
      "v20"
    ],
    "currentStock": 4
  }
]
```

---

### Sell Stock

```http
POST /boxes/sell
```

Request:

```json
{
  "boxCode": "BOX001",
  "quantity": 2,
  "notes": "Customer purchase"
}
```

The backend verifies available stock before recording the transaction.

---

### Add Stock

```http
POST /boxes/add
```

Request:

```json
{
  "boxCode": "BOX001",
  "quantity": 5,
  "notes": "New stock received"
}
```

---

### Get Stock History

```http
GET /boxes/{boxCode}/history
```

Example:

```json
[
  {
    "id": 2,
    "boxCode": "BOX001",
    "transactionType": "SOLD",
    "quantity": 2,
    "stockAfterTransaction": 8,
    "notes": "Customer purchase"
  },
  {
    "id": 1,
    "boxCode": "BOX001",
    "transactionType": "ADD",
    "quantity": 10,
    "stockAfterTransaction": 10,
    "notes": "Initial stock"
  }
]
```

---

## API Error Handling

The backend provides centralized exception handling for common application errors.

| Scenario           | HTTP Status |
| ------------------ | ----------: |
| Invalid request    |         400 |
| Box not found      |         404 |
| Duplicate box code |         409 |
| Insufficient stock |         409 |

Example error response:

```json
{
  "status": 409,
  "error": "Conflict",
  "message": "Insufficient stock. Available: 4, requested: 10",
  "timestamp": "2026-09-22T20:00:00"
}
```

---

## Running the Application Locally

### Prerequisites

Install the following:

* Java 21+
* Node.js
* npm
* Git
* PostgreSQL or Supabase

---

## Backend Setup

Navigate to the backend:

```bash
cd backend
```

Configure the database connection in:

```text
src/main/resources/application.properties
```

Example configuration:

```properties
spring.datasource.url=jdbc:postgresql://<host>:5432/postgres
spring.datasource.username=<username>
spring.datasource.password=${DB_PASSWORD}

spring.jpa.hibernate.ddl-auto=none
spring.jpa.show-sql=true
```

Set the database password as an environment variable:

```bash
export DB_PASSWORD="your-password"
```

Run the Spring Boot application:

```bash
./mvnw spring-boot:run
```

The backend will be available at:

```text
http://localhost:8080
```

The backend can also be run directly from IntelliJ IDEA using the Spring Boot run configuration.

---

## Frontend Setup

Open a new terminal:

```bash
cd frontend
```

Install dependencies:

```bash
npm install
```

Start the Vite development server:

```bash
npm run dev
```

The frontend will be available at:

```text
http://localhost:5173
```

---

## Testing

The REST APIs can be tested using Postman.

Recommended test scenarios include:

### Box Management

* Create a valid box
* Attempt to create a duplicate box
* Create a box with invalid quantity
* Create a box without compatible models

### Search

* Search for an existing phone model
* Search for a non-existing model
* Test case-insensitive model searches

### Stock Operations

* Add valid stock
* Sell valid stock
* Sell all available stock
* Attempt to sell more stock than available
* Add stock to a non-existing box
* Sell stock from a non-existing box

### Transaction History

* Retrieve history for an existing box
* Retrieve history for a non-existing box
* Verify stock after each transaction

---

## Data Integrity

The application enforces several database and application-level constraints.

### Unique Box Codes

Each glass box must have a unique identifier.

```text
BOX001
BOX002
BOX003
```

Duplicate box codes are rejected.

### Unique Compatibility Mapping

The database prevents duplicate mappings between a box and a phone model.

```text
BOX001 + V20
```

can only exist once.

### Positive Quantities

Stock quantities must be greater than zero.

### No Negative Stock

Before recording a sale:

```text
requested quantity <= current stock
```

must be satisfied.

### Transaction History

Stock changes are represented as transactions rather than deleting or overwriting previous inventory movements.

---

## Current MVP Scope

### Implemented

* Add glass box
* Multiple compatible phone models
* Search by phone model
* Add stock
* Sell stock
* Stock validation
* Transaction history
* Dashboard
* Low-stock detection
* REST APIs
* Request validation
* Exception handling
* PostgreSQL/Supabase integration
* React frontend
* Spring Boot backend
* CORS configuration

### Out of Scope

The following features are intentionally excluded from the current MVP:

* Barcode scanning
* Billing and invoicing
* Supplier management
* Multiple shop branches
* Authentication and RBAC
* GST calculations
* Payment integration
* Advanced analytics

---

## Future Improvements

Potential future enhancements include:

* Pagination for large inventories
* Database query optimization
* Authentication and role-based access control
* Barcode and QR-code scanning
* Bulk inventory import
* Advanced inventory analytics
* Automated low-stock notifications
* Docker-based deployment
* CI/CD pipeline
* Automated integration and unit testing
* OpenAPI/Swagger documentation
* Production monitoring

---

## Design Decisions

### Normalized Database Model

Compatibility is represented using a separate mapping table because the relationship between glass boxes and phone models is many-to-many.

```text
Phone Model
     |
     | many-to-many
     |
Glass Box
```

The `glass_compatibility` table represents this relationship.

### Transaction-Based Stock

Instead of directly modifying a stock column, inventory changes are represented through transactions.

```text
ADD 10
SOLD 2
SOLD 1
```

The current stock is derived as:

```text
10 - 2 - 1 = 7
```

This provides an audit trail for inventory changes.

### DTO-Based API

The application uses DTOs to separate API request/response models from JPA entities.

Examples include:

```text
CreateBoxRequest
BoxResponse
SellStockRequest
AddStockRequest
StockResponse
StockHistoryResponse
```

This keeps the API contract independent from the persistence model.

### Layered Backend Architecture

The backend follows a layered architecture:

```text
Controller
    |
    v
Service
    |
    v
Repository
    |
    v
Database
```

Responsibilities are separated between HTTP handling, business logic, data access, and persistence.

