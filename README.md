# CasinoProject

## Kafka Overview

The project currently uses one shared Kafka topic for financial events:

- Topic: `financialEvents`
- Shared
  constant: [common/src/main/java/com/casino/CasinoConstants.java](/mnt/c/kool/CasinoProject/common/src/main/java/com/casinoproject/CasinoConstants.java:1)

Multiple event types are published to the same topic. Consumers distinguish them by Kafka type headers and Spring Kafka
JSON type mappings.

## Event Model

All financial events live
under [common/src/main/java/com/casino/event](/mnt/c/kool/CasinoProject/common/src/main/java/com/casinoproject/event).

Base classes:

- `AbstractPlayerEvent`
  Carries `playerProfileId`.
- `AbstractPlayerFinancialEvent`
  Extends `AbstractPlayerEvent` and adds `amount`.

Concrete financial events:

- `BetPlaced`
  Sent when a player places a bet.
  Fields: `playerProfileId`, `amount`
- `BetSettled`
  Sent when a bet/game round is settled.
  Fields: `playerProfileId`, `amount`, `gameEndingType`
- `DepositCompleted`
  Sent when a deposit finishes successfully.
  Fields: `playerProfileId`, `amount`
- `WithdrawalProcessed`
  Sent when a withdrawal has been processed.
  Fields: `playerProfileId`, `amount`

`BetSettled` also includes `GameEndingType`:

- `WIN`
- `LOSE`

## Topic Contract

All four event types are expected on the same topic:

- `financialEvents`

This means producers publish different payload classes to one topic, and consumers must be able to deserialize
polymorphic JSON safely.

In this project, that is done with Spring Kafka type headers plus explicit alias mappings in the ledger service.

## Ledger Consumer

The ledger consumer is implemented
in [ledger/src/main/java/com/casino/ledger/service/KafkaConsumerService.java](/mnt/c/kool/CasinoProject/ledger/src/main/java/com/casinoproject/ledger/service/KafkaConsumerService.java:1).

Current behavior:

- Topic: `financialEvents`
- Listener method payload type: `AbstractPlayerFinancialEvent`
- Effective consumer group: `ledgerService`

## Ledger Persistence Semantics

The ledger service routes each deserialized event to a handler and persists one ledger entry.

Current mapping:

- `DepositCompleted` -> `PLAYER_DEPOSIT_FUNDS`
- `WithdrawalProcessed` -> `PLAYER_WITHDRAW_FUNDS`
- `BetPlaced` -> `PLAYER_BET`
- `BetSettled` with `WIN` -> `PLAYER_WIN`
- `BetSettled` with `LOSE` -> `PLAYER_LOSS`

## Producer Requirements

Any producer writing to `financialEvents` should follow these rules:

- Publish one of the supported event payload classes.
- Include a compatible Spring Kafka type header alias.
- Keep the payload fields aligned with the shared event classes in `common`.
- Use the shared topic constant where possible instead of hardcoding the topic name.

If a producer sends a new event type, the ledger consumer must be updated in two places:

- Add a handler for the new event class.
- Add the event alias to `spring.kafka.consumer.properties.spring.json.type.mapping`.

## Running Notes

The current root modules are:

- `common`
- `ledger`
- `api-gateway`
- `discovery-server`

Other modules

- `game`
- `bonus`
- `profile-service`

For local development, services are run separately. Eureka is available at:

- `http://localhost:8080/eureka/web`

project for enterprise systems integration

# RUN PROJECT

run each service seperately
http://localhost:8761/
for eureka stats

## Service Architecture & Port Mapping

| Service              | Port   | Database            | Description                           |
|:---------------------|:-------|:--------------------|:--------------------------------------|
| **discovery-server** | `8761` | N/A                 | Eureka Service Registry               |
| **api-gateway**      | `8080` | N/A                 | Central entry point                   |
| **game**             | `8082` | `game_db`           | Game logic & financial event producer |
| **ledger**           | `8083` | `ledger_db`         | Financial audit log & event consumer  |
| **bonus**            | `8084` | `game_db`           | Bonus management                      |
| **profile-service**  | `8086` | `profileservice_db` | User profiles & identity              |
| **compliance**       | `8087` | `compliance_db`     | Compliance service                    |
| **payment**          | `8091` | `payment_db`        | Payment service                       |

for swagger add http://localhost:port/swagger-ui/index.html

---

## Prerequisites & Setup

### 1. Infrastructure

Ensure the following are running on `localhost`:

* **PostgreSQL:** Port `5432` (Create databases: `game_db`, `ledger_db`, `profileservice_db`, `compliance_db`)
* **Kafka:** Port `9092`
* **Java:** Version 17+

### 2. Databases

The services use Hibernate `ddl-auto: update` or `validate`. Ensure the databases exist before starting the services:

```sql
CREATE DATABASE game_db;
CREATE DATABASE ledger_db;
CREATE DATABASE profileservice_db;
CREATE DATABASE compliance_db;
```

# HOW TO ADD SERVICE

Add service to project root pom.xml in modules  
Easiest way to add service is copy the already existing ones for structure of properties