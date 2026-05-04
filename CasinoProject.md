# Casino Project - Enterprise Integration

A microservices-based casino platform built with Spring Boot, Spring Cloud, and Kafka.

## Service Architecture & Port Mapping

| Service | Port | Database | Description |
| :--- | :--- | :--- | :--- |
| **discovery-server** | `8761` | N/A | Eureka Service Registry |
| **api-gateway** | `8080` | N/A | Central entry point |
| **game** | `8082` | `game_db` | Game logic & financial event producer |
| **ledger** | `8083` | `ledger_db` | Financial audit log & event consumer |
| **bonus** | `8084` | `game_db` | Bonus management |
| **profile-service** | `8086` | `profileservice_db` | User profiles & identity |

---

## Kafka Messaging Contract

The system uses a shared Kafka topic for all financial transactions to ensure a centralized audit trail in the Ledger service.

- **Topic:** `financialEvents`
- **Shared Constant:** `com.casino.CasinoConstants.FINANCIAL_EVENTS`

### Event Type Mappings
The Ledger service uses polymorphic JSON deserialization. Producers must ensure the `__TypeId__` header matches one of the following aliases:

| Alias | Target Class | Event Trigger |
| :--- | :--- | :--- |
| `betplaced` | `BetPlaced` | Player places a bet |
| `betsettled` | `BetSettled` | Game result (WIN/LOSE) is determined |
| `depositcompleted` | `DepositCompleted` | Funds added to account |
| `withdrawalprocessed` | `WithdrawalProcessed` | Funds removed from account |

### Ledger Persistence Semantics
Events consumed from Kafka are mapped to these internal ledger types:
* `DepositCompleted` → `PLAYER_DEPOSIT_FUNDS`
* `WithdrawalProcessed` → `PLAYER_WITHDRAW_FUNDS`
* `BetPlaced` → `PLAYER_BET`
* `BetSettled (WIN)` → `PLAYER_WIN`
* `BetSettled (LOSE)` → `PLAYER_LOSS`

---

## Prerequisites & Setup

### 1. Infrastructure
Ensure the following are running on `localhost`:
* **PostgreSQL:** Port `5432` (Create databases: `game_db`, `ledger_db`, `profileservice_db`)
* **Kafka:** Port `9092`
* **Java:** Version 17+

### 2. Databases
The services use Hibernate `ddl-auto: update` or `validate`. Ensure the databases exist before starting the services:
```sql
CREATE DATABASE game_db;
CREATE DATABASE ledger_db;
CREATE DATABASE profileservice_db;