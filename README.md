# Candidate Management Service API

## 🛠️ Project Overview

This project implements a **Java-based Microservice** designed to manage customer data efficiently and securely.  
The main responsibilities of the Microservice are:

- 📝 **Handle customer registration** and store their information in a **MySQL database**.
- 🔍 **Provide customer data queries** to support different business needs.
- 📊 **Perform data analysis** to generate insights from stored customer information.

This architecture ensures **scalability**, **security**, and follows **software development best practices**, enabling a better experience for end users.

## 📦 Tech Stack

This project uses the following technologies:

- ☕ **Java 17** – Core language for implementing the microservice logic.
- 🌱 **Spring Boot** – Framework for building and managing the microservice.
- 🐬 **MySQL 8** – Relational database for storing customer information.
- 🧠 **Redis** – Used as a cache layer to improve query performance.
- 📬 **Amazon SQS** – Message queue for asynchronous communication.
- ☁️ **AWS Elastic Beanstalk** – Deployment and scaling of the microservice in production.
- 🗄️ **Amazon RDS (MySQL)** – Managed database service in production.
- ⚡ **Redis Labs** – Managed Redis service used as a cost-effective cache solution in production.

### 🔧 Developer Tools

- 🧪 **LocalStack** – Local AWS cloud service emulator for development and testing.
- 🐳 **Docker** – Containerization of the application for consistent environments.
- 📦 **Docker Compose** – Orchestration of services in local development.

## ✅ Prerequisites

Before running the project, make sure you have the following installed:

- 🐳 [Docker](https://www.docker.com/) – Required to run the services in containers.
- 📦 [Docker Compose](https://docs.docker.com/compose/) – To orchestrate multi-container applications.
- 🧠 (Optional) Familiarity with LocalStack, MySQL, and Redis is recommended for debugging and testing purposes.

> 💡 Tip: If you're on Windows or macOS, consider installing [Docker Desktop](https://www.docker.com/products/docker-desktop/) which includes Docker and Docker Compose.

## 🧱 Project Architecture

Both the **Java** services follow the principles of **Clean Architecture**, ensuring separation of concerns, testability, and scalability.

### ☕ Java Service Architecture

The Java service is structured into clearly defined layers:

```bash
├── dockerfiles/
│   ├── dev/                           # Docker setup for local development
│   └── prod/                          # Docker setup for production
│
├── src/main/java/com/cristhianfdx/candidatemanagement/
│   ├── application/                   # Application layer (use cases, services, ports)
│   │   ├── dto/                       # Data Transfer Objects
│   │   │   ├── CandidateMetricsResponse.java
│   │   │   ├── CandidateResponse.java
│   │   │   ├── CreateCandidateRequest.java
│   │   │   └── ErrorResponse.java
│   │   │
│   │   ├── event/                     # Domain events
│   │   │   └── CandidateRecalculateEvent.java
│   │   │
│   │   ├── port/                      # Interfaces (hexagonal architecture ports)
│   │   │   └── CandidatePort.java
│   │   │
│   │   └── service/                   # Application services and business logic
│   │       ├── CandidateListener.java
│   │       ├── CandidateServiceImpl.java
│   │       └── SqsManagementService.java
│   │
│   ├── config/                        # Application configurations
│   │   ├── AwsConfig.java             # AWS SQS and cloud setup
│   │   ├── RedisConfig.java           # Redis cache configuration
│   │   ├── SecurityConfig.java        # Security setup
│   │   └── SwaggerConfig.java         # OpenAPI/Swagger documentation
│   │
│   ├── domain/                        # Core domain layer
│   │   ├── exception/                 # Custom domain exceptions
│   │   ├── model/                     # Domain entities (e.g., Candidate)
│   │   └── repository/                # Repository interfaces
│   │       └── CandidateRepository.java
│   │
│   └── infrastructure/entrypoint/     # Entry points to interact with the system
│       ├── controllers/               # REST controllers
│       │   └── CandidateController.java
│       └── exception/                 # Global error handling
│           └── GlobalExceptionHandler.java
│
├── src/main/resources/
│   ├── db/migration/                  # Database migrations (Flyway)
│   │   └── V1_initial_db_schema.sql
│   └── application.yaml               # Main configuration file
│
└── CandidateManagementApplication.java # Main Spring Boot application entry point
```

## ✨ Key Features

- 📊 **Real-time Metrics with Redis + SQS**  
  When a candidate is created, an event is published to **Amazon SQS**.  
  A listener consumes the message, recalculates metrics, and stores the results in **Redis**.  
  This ensures **fast metric queries** without hitting the database repeatedly.

- 🔐 **Basic Authentication**  
  All API endpoints are protected using **Basic Auth** to ensure that only authorized users can access the system.

- ⚡ **Scalability**  
  The combination of **SQS** (for decoupled communication) and **Redis** (for caching) provides a highly scalable and efficient architecture.

## 🚀 Getting Started

Follow these steps to run the project locally:

1. **Clone the repository:**

```bash
git clone https://github.com/cristhianfdx/candidatemanagement.git
```

2. **Navigate into the project directory:**

```bash
cd candidatemanagement
```

3. **Start the services using Docker Compose:**

```bash
docker-compose up --build
```

## 🧪 Observability & UIs

This project provides web UIs for easier visualization and debugging:

- **API Swagger:** [http://localhost:8080/swagger/index.html](http://localhost:8080/swagger/index.html)

**Basic Auth**
(Local environment)
- `username`: admin
- `password`: admin

## 📂 API Collections

The project includes a dedicated **`/apicollections`** folder that contains:

- 📬 **Postman Collections** – Predefined requests for testing and exploring the API endpoints.
- 🌍 **Postman Environment** – Ready-to-use environment variables (e.g., base URL, authentication) to simplify local and production testing.

> 💡 Import these collections and environment files directly into **Postman** to quickly start testing the Candidate Management Service API.

## 🧪 Unit Tests

### CI Workflow
Every time a push or pull request is made to the main branch (or any branch, depending on your config), the CI 
pipeline runs the unit tests using Maven and Java 17 (Java-Service)

### 🚀 CD Workflow

This project uses **GitHub Actions** for Continuous Deployment (CD).  
Whenever changes are **merged into the `main` branch**, the pipeline automatically:

1. 🏗️ **Builds the application** with Maven.
2. 🐳 **Packages the service into a JAR file**.
3. ☁️ **Deploys the new version to AWS Elastic Beanstalk**.

This guarantees that production is always running the latest stable release. 


