# Retail Reward Service

Spring Boot 2.7 / Java 8 application that calculates reward points for retail customers from purchase transactions.

## Problem Statement

A customer receives:

- 2 points for every dollar spent above 100 dollars in a transaction
- 1 point for every dollar spent between 51 and 100 dollars in a transaction

Example:

- `120` dollars = `(2 x 20) + (1 x 50) = 90` points

The application returns:

- monthly reward points per customer
- total reward points per customer
- transaction-level reward details through a separate endpoint

## Design Overview

- Default behavior returns rewards for the latest 3 months available in the dataset.
- Clients can request:
  - a rolling number of months through `months`
  - a custom date range through `startDate` and `endDate`
  - all customers when `customerId` is omitted
  - one customer when `customerId` contains a single value
  - multiple customers when `customerId` contains comma-separated values
- Reward summary and transaction details use separate endpoints.
- Transaction loading is simulated asynchronously with `CompletableFuture` and a small artificial delay.
- Validation is kept simple:
  - `months` must be greater than `0`
  - use either `months` or `startDate` and `endDate`
  - `startDate` must be on or before `endDate`

## Technical Details

- Java: 8
- Framework: Spring Boot 2.7.18
- Build tool: Maven
- Logging: SLF4J with Spring Boot logging
- Validation: utility-based request validation
- Exception handling: centralized with `@RestControllerAdvice`
- Dataset: in-memory repository
- Async simulation: `CompletableFuture` with repository-level delay

## Project Structure

- `controller`: REST endpoints
- `service`: reward orchestration and calculation logic
- `repository`: in-memory customer and transaction data
- `model`: internal data models
- `dto/request`: request payloads
- `dto/response`: response payloads
- `util`: constants and validation helpers
- `exception`: custom exceptions and global exception handling
