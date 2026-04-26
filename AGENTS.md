# Kafka Configuration

## Topic

- `financialEvents`
  Shared constant: `com.casino.CasinoConstants.FINANCIAL_EVENTS_TOPIC_NAME`

## Events On `financialEvents`

- `BetPlaced`
  Fields: `playerProfileId`, `amount`
- `BetSettled`
  Fields: `playerProfileId`, `amount`, `gameEndingType`
- `DepositCompleted`
  Fields: `playerProfileId`, `amount`
- `WithdrawalProcessed`
  Fields: `playerProfileId`, `amount`

## Event Base Types

- `AbstractPlayerEvent`
  Field: `playerProfileId`
- `AbstractPlayerFinancialEvent`
  Extends `AbstractPlayerEvent`
  Adds: `amount`

## Bet Settlement Values

- `GameEndingType.WIN`
- `GameEndingType.LOSE`

## Ledger Consumer

- Module: `ledger`
- Bootstrap servers: `localhost:9092`
- Consumer group: `ledgerService`
- Auto offset reset: `earliest`
- Listener topic: `financialEvents`

## Deserialization

- Key deserializer: `ErrorHandlingDeserializer`
- Value deserializer: `ErrorHandlingDeserializer`
- Key delegate: `StringDeserializer`
- Value delegate: `JsonDeserializer`
- Trusted package: `com.casino.event`

## Type Header Aliases

- `betPlaced`, `betplaced` -> `com.casino.event.BetPlaced`
- `betSettled`, `betsettled` -> `com.casino.event.BetSettled`
- `depositCompleted`, `depositcompleted` -> `com.casino.event.DepositCompleted`
- `withdrawalProcessed`, `withdrawalprocessed` -> `com.casino.event.WithdrawalProcessed`

## Ledger Entry Mapping

- `BetPlaced` -> `PLAYER_BET`
- `BetSettled` + `WIN` -> `PLAYER_WIN`
- `BetSettled` + `LOSE` -> `PLAYER_LOSS`
- `DepositCompleted` -> `PLAYER_DEPOSIT_FUNDS`
- `WithdrawalProcessed` -> `PLAYER_WITHDRAW_FUNDS`

## Producer Contract

- Publish all financial events to `financialEvents`.
- Include a matching Spring Kafka type header alias.
- Keep payloads aligned with the event classes in `common`.
