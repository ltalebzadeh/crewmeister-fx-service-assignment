# Foreign Exchange Rate Service

## 📋 Table of Contents

- [Project Overview](#-project-overview)
- [Challenge Requirements](#-challenge-requirements)
- [Quick Start](#-quick-start)
- [API Endpoints](#-api-endpoints)
- [Request/Response Examples](#-requestresponse-examples)
- [Architecture & Key Decisions](#-architecture--key-decisions)
- [Project Structure](#-project-structure)
- [Key Features](#-key-features)
- [Deployment](#-deployment)
- [Future Improvements](#-future-improvements)
- [Testing Strategy](#-testing-strategy)

## 📖 Project Overview

This is a **Spring Boot microservice** that provides foreign exchange rate data sourced from the **European Central
Bank (ECB)**. The service offers RESTful endpoints to retrieve currency information, exchange rates, and currency
conversion functionality.

## 🎯 Challenge Requirements

This project implements the following user stories as specified in the original challenge:

- **As a client, I want to get a list of all available currencies**
- **As a client, I want to get all EUR-FX exchange rates at all available dates as a collection**
- **As a client, I want to get the EUR-FX exchange rate at particular day**
- **As a client, I want to get a foreign exchange amount for a given currency converted to EUR on a particular day**

## 🚀 Quick Start

### Prerequisites

- **Java 11** (compatible with OpenJDK 15)
- **Maven 3.x**
- Internet connection (for fetching ECB data)

### 📥 Installation & Setup

```command
# Clone the repository
git clone <repository-url>
cd foreign-exchange-service

# Compile the project
mvn clean compile

# Package the application
mvn clean package

# Run the application
mvn spring-boot:run
```

### 🔧 Testing

#### Unit Tests

```command
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=ExchangeRateServiceTest
```

#### Integration Tests

```command
# Run integration tests
mvn test -Dtest=*IntegrationTest
```

#### Manual Testing with H2 Console

```config
# Access H2 Console at: http://localhost:8080/h2-console
# JDBC URL: jdbc:h2:mem:testdb
# Username: sa
# Password: (leave empty)
```

## 🛠️ API Endpoints

### Base URL: `http://localhost:8080/api`

| Method | Endpoint                              | Description                          |
|--------|---------------------------------------|--------------------------------------|
| `GET`  | `/currencies`                         | Get all available currencies         |
| `GET`  | `/exchange-rates`                     | Get all exchange rates for all dates |
| `GET`  | `/exchange-rates/{currency}/{date}`   | Get specific exchange rate           |
| `GET`  | `/convert/{amount}/{currency}/{date}` | Convert currency to EUR              |

## 📋 Request/Response Examples

### Get All Currencies

```json
GET /api/currencies

Response:
[
{
"currency_code": "USD",
"currency_name": "US Dollar"
},
{
"currency_code": "GBP",
"currency_name": "British Pound"
},
{
"currency_code": "JPY",
"currency_name": "Japanese Yen"
}
]
```

### Get All Exchange Rates

```json
GET /api/exchange-rates

Response:
[
{
"currency_code": "USD",
"currency_name": "US Dollar",
"rate_date": "2025-06-04",
"rate": 1.1411
},
{
"currency_code": "GBP",
"currency_name": "British Pound",
"rate_date": "2025-06-04",
"rate": 0.8654
}
]
```

### Get Exchange Rate for Specific Currency and Date

```json
GET /api/exchange-rates/USD/2025-06-04

Response:
{
"currency_code": "USD",
"currency_name": "US Dollar",
"rate_date": "2025-06-04",
"rate": 1.1411
}
```

### Currency Conversion

```json
GET /api/convert/100.50/USD/2025-06-04

Response:
{
"original_amount": 100.50,
"original_currency": "USD",
"converted_amount": 88.06,
"target_currency": "EUR",
"exchange_rate": 1.1411,
"conversion_date": "2025-06-04"
}
```

## 🏗️ Architecture & Key Decisions

### 📊 Technology Stack

- **Framework**: Spring Boot 2.7.18
- **Database**: H2 (in-memory for development, configurable for production)
- **HTTP Client**: Spring WebFlux (reactive)
- **Caching**: Spring Cache with in-memory caching
- **Validation**: Spring Validation with Bean Validation
- **Testing**: JUnit 5, Mockito, Spring Test
- **Build Tool**: Maven
- **Data Source**: European Central Bank (ECB) XML API

### 🎯 Key Design Decisions

#### 1. **Database Strategy**

- **Decision**: H2 for development, JPA for data access
- **Rationale**: Easy setup, in-memory for tests, production-ready with configuration
- **Entities**: `Currency`, `ExchangeRate` with proper relationships

#### 2. **API Design**

- **Decision**: RESTful endpoints following REST principles
- **Validation**: Comprehensive path variable validation
- **Error Handling**: Global exception handler with structured responses
- **Date Format**: ISO 8601 (`yyyy-MM-dd`)

#### 3. **Reactive HTTP Client**

- **Decision**: Used `WebClient` (Spring WebFlux)
- **Rationale**: Non-blocking, better performance, future-proof
- **Implementation**: `BundesbankApiClient` uses reactive streams

#### 4. **Data Source Integration**

- **Decision**: European Central Bank (ECB) as primary data source
- **URL**: `https://www.ecb.europa.eu/stats/eurofxref/eurofxref-daily.xml`
- **Format**: XML parsing with Jackson XML mapper
- **Fallback**: Robust error handling and validation

#### 5. **BigDecimal for Financial Calculations**

- **Decision**: Used `BigDecimal` for all monetary amounts and exchange rates
- **Rationale**: Precise decimal arithmetic, avoids floating-point precision issues
- **Implementation**: Critical for accurate currency conversion calculations

#### 6. **Caching Strategy**

- **Decision**: Spring Cache with `@Cacheable` and `@CacheEvict` annotations
- **Rationale**: Improve application performance by reducing database queries, faster response times for frequently
  accessed data
- **Implementation**: Method-level caching for exchange rates and currency data to minimize database calls

#### 7. **Validation Strategy**

- **Decision**: Multi-layer validation approach
- **Implementation**:
    - Path variable validation for API inputs
    - Entity validation for data integrity

#### 8. **Asynchronous Processing**

- **Decision**: Async data fetching and storage
- **Rationale**: Non-blocking operations, better performance
- **Implementation**: Reactive streams with comprehensive error handling

### 🔧 Design Patterns Implemented

1. **Repository Pattern**: Clean data access abstraction
2. **Service Layer Pattern**: Business logic separation
3. **DTO Pattern**: Data transfer objects for API responses
4. **Builder Pattern**: Lombok builders for clean object creation
6. **Publisher-Subscriber Pattern**: Reactive streams for async operations

## 📂 Project Structure

```
src/
├── main/
│   ├── java/com/crewmeister/cmcodingchallenge/
│   │   ├── client/          # External API clients
│   │   ├── controller/      # REST controllers
│   │   ├── dto/            # Data transfer objects
│   │   ├── entity/         # JPA entities
│   │   ├── exception/      # Custom exceptions & handlers
│   │   ├── repository/     # Data access layer
│   │   ├── service/        # Business logic layer
│   │   └── config/         # Configuration classes
│   └── resources/
│       └── application.yml          # Main configuration
└── test/
    ├── java/                       # Test classes
    └── resources/
        └── application-test.yml    # Test configuration
```

## 🔍 Key Features

### ✅ Implemented Features

- **Currency Management**: Retrieve and store available currencies
- **Exchange Rate Retrieval**: Real-time data from ECB with caching
- **Currency Conversion**: Accurate conversion calculations
- **Caching Layer**: In-memory caching for improved performance
- **Data Persistence**: H2 database with JPA
- **Comprehensive Validation**: Input validation with clear error messages
- **Async Processing**: Non-blocking data fetching and storage
- **Error Handling**: Global exception handling with structured responses
- **Logging**: Comprehensive logging for monitoring and debugging
- **Testing**: Unit tests and integration tests

## 🚀 Deployment

### 🐳 Docker Support

```dockerfile
FROM openjdk:11-jre-slim
COPY target/cm-coding-challenge-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

```command
# Build and run with Docker
docker build -t foreign-exchange-service .
docker run -p 8080:8080 foreign-exchange-service
```

## 🔮 Future Improvements

### 🎯 Short-term Enhancements

1. **Advanced Caching**
    - Redis integration for distributed caching
    - TTL-based cache invalidation
    - Cache metrics and monitoring

2. **API Documentation**
    - OpenAPI 3.0 specification
    - Swagger UI integration

3. **Monitoring & Metrics**
    - Micrometer integration
    - Prometheus metrics
    - Health check endpoints

4. **Security Enhancements**
    - API key authentication
    - Rate limiting
    - HTTPS enforcement

5. **Scheduled Updates**
    - Automated exchange rate updates
    - Configurable refresh intervals
    - Background job scheduling

### 🚀 Long-term Roadmap

1. **Multi-Source Data Integration**
    - Multiple central bank APIs
    - Data source fallback mechanisms
    - Data quality validation

2. **Advanced Features**
    - Historical rate analysis
    - Currency trend predictions
    - Bulk conversion operations
    - Manual refresh for exchange rates

3. **Performance Optimization**
    - Database indexing strategy
    - Connection pooling optimization
    - Async batch processing

4. **Production Readiness**
    - Circuit breaker pattern
    - Distributed tracing

## 🧪 Testing Strategy

### Unit Tests

- Service layer business logic
- Controller input validation
- Data transformation logic
- Error handling scenarios

### Integration Tests

- Database operations
- External API integration
- End-to-end API workflows

### Test Coverage

- Comprehensive test suite
- Mockito for external dependencies
- H2 in-memory database for integration tests
- Reactive test utilities for async operations
