# Retail Reward Service

Spring Boot 2.7 / Java 8 application that calculates reward points for a retail customer and returns both the summary and transaction-level details in a single API response.

## Application Overview

A customer receives:

- 2 points for every dollar spent above 100 dollars in a transaction
- 1 point for every dollar spent between 51 and 100 dollars in a transaction

Example:

- `120` dollars = `(2 x 20) + (1 x 50) = 90` points

The application exposes:

- one customer-specific rewards endpoint
- a default latest three-month calculation window per customer
- an option to override the default period with `months` or `startDate` and `endDate`
- both monthly summary points and transaction-level reward details in the same response

## Tech Stack

- Java 8
- Spring Boot 2.7.18
- Maven
- In-memory repositories for customers and transactions
- Bean validation with `@Valid`
- Centralized exception handling with `@RestControllerAdvice`

## Build
```powershell
mvn clean test
```

## Run
```powershell
mvn spring-boot:run
```
The application starts on `http://localhost:8080`.

## Validation Notes
- `customerId` is required.
- When neither `months` nor `startDate`/`endDate` is provided, consider the latest 3 months available for that customer.
- When both `months` and `startDate`/`endDate` are provided, uses `months` and ignores the date range.
- `months` must be greater than `0` when provided.
- `startDate` must be on or before `endDate`.

## Project Structure
- `src/main/java/com/retailrewards/controller`
- `src/main/java/com/retailrewards/service`
- `src/main/java/com/retailrewards/repository`
- `src/main/java/com/retailrewards/dto`
- `src/main/java/com/retailrewards/model`
- `src/main/java/com/retailrewards/exception`

## API
### Customer rewards and transactions
Endpoint:
```http
POST /api/v1/rewards/customers
Content-Type: application/json
```
Sample request for the default three-month period:

```json
{
  "customerId": "C1001"
}
```

Sample response:

```json
{
  "customerId": "C1001",
  "customerName": "Kavin",
  "startDate": "2026-01-01",
  "endDate": "2026-03-31",
  "monthlyPoints": {
    "2026-Mar": 271,
    "2026-Feb": 110,
    "2026-Jan": 115
  },
  "totalPoints": 496,
  "transactions": [
    {
      "transactionId": "T10001",
      "transactionDate": "2026-01-05",
      "description": "Grocery order",
      "amount": "120.00",
      "rewardPoints": 90
    },
    {
      "transactionId": "T10002",
      "transactionDate": "2026-01-21",
      "description": "Home supplies",
      "amount": "75.00",
      "rewardPoints": 25
    },
    {
      "transactionId": "T10003",
      "transactionDate": "2026-02-04",
      "description": "Pharmacy purchase",
      "amount": "45.00",
      "rewardPoints": 0
    },
    {
      "transactionId": "T10004",
      "transactionDate": "2026-02-16",
      "description": "Electronics accessories",
      "amount": "130.00",
      "rewardPoints": 110
    },
    {
      "transactionId": "T10005",
      "transactionDate": "2026-03-12",
      "description": "Appliance order",
      "amount": "210.00",
      "rewardPoints": 270
    },
    {
      "transactionId": "T10006",
      "transactionDate": "2026-03-22",
      "description": "Pet supplies",
      "amount": "51.25",
      "rewardPoints": 1
    }
  ]
}
```

Sample request for an explicit date range:

```json
{
  "customerId": "C1002",
  "startDate": "2026-02-01",
  "endDate": "2026-03-31"
}
```

Sample response:

```json
{
  "customerId": "C1002",
  "customerName": "Prabhu",
  "startDate": "2026-02-01",
  "endDate": "2026-03-31",
  "monthlyPoints": {
    "2026-Mar": 495,
    "2026-Feb": 179
  },
  "totalPoints": 674,
  "transactions": [
    {
      "transactionId": "T20003",
      "transactionDate": "2026-02-10",
      "description": "Department store",
      "amount": "99.00",
      "rewardPoints": 49
    },
    {
      "transactionId": "T20004",
      "transactionDate": "2026-02-23",
      "description": "Furniture deposit",
      "amount": "140.00",
      "rewardPoints": 130
    },
    {
      "transactionId": "T20005",
      "transactionDate": "2026-03-02",
      "description": "Garden supplies",
      "amount": "55.00",
      "rewardPoints": 5
    },
    {
      "transactionId": "T20006",
      "transactionDate": "2026-03-19",
      "description": "Renovation materials",
      "amount": "320.75",
      "rewardPoints": 490
    }
  ]
}
```
