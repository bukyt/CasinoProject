# CasinoProject

## Kafka Overview

The project currently uses one shared Kafka topic for financial events:

- Topic: `financialEvents`
- Shared constant: [common/src/main/java/com/casino/CasinoConstants.java](/mnt/c/kool/CasinoProject/common/src/main/java/com/casinoproject/CasinoConstants.java:1)

Multiple event types are published to the same topic. Consumers distinguish them by Kafka type headers and Spring Kafka JSON type mappings.

## Event Model

All financial events live under [common/src/main/java/com/casino/event](/mnt/c/kool/CasinoProject/common/src/main/java/com/casinoproject/event).

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

This means producers publish different payload classes to one topic, and consumers must be able to deserialize polymorphic JSON safely.

In this project, that is done with Spring Kafka type headers plus explicit alias mappings in the ledger service.

## Ledger Consumer

The ledger consumer is implemented in [ledger/src/main/java/com/casino/ledger/service/KafkaConsumerService.java](/mnt/c/kool/CasinoProject/ledger/src/main/java/com/casinoproject/ledger/service/KafkaConsumerService.java:1).

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

For local development, services are run separately. Eureka is available at:

- `http://localhost:8080/eureka/web`
