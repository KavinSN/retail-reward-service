# Retail Reward Service

Spring Boot application that calculates reward points for a retail customer and returns both the summary and transaction-level details in a single API response.

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
- Synchronous in-memory repositories for customers and transactions

## Validation Notes
- `customerId` is required.
- `startDate`/`endDate` format will be `yyyy-mm-dd`
- When neither `months` nor `startDate`/`endDate` is provided, consider the latest 3 months available for that customer.
- Use either `months` or `startDate`/`endDate`, not both.
- `months` must be greater than `0` when provided.
- When only `startDate` is provided, the service uses a 3-month inclusive window starting from that date.
- When only `endDate` is provided, the service uses a 3-month inclusive window ending on that date.
- If both `startDate` and `endDate` are provided, `startDate` must be on or before `endDate`.

## Project Structure
- `src/main/java/com/retailrewards/controller`
- `src/main/java/com/retailrewards/service`
- `src/main/java/com/retailrewards/repository`
- `src/main/java/com/retailrewards/dto/response`
- `src/main/java/com/retailrewards/model`
- `src/main/java/com/retailrewards/exception`

## API
### Customer rewards and transactions
Endpoint:
```http
GET /api/v1/rewards/customers/{customerId}
```
Sample request for the default three-month period:

```bash
curl "http://localhost:8080/api/v1/rewards/customers/C1001"
```

Sample response:

```json
{
  "customerId": "C1001",
  "customerName": "Kavin",
  "startDate": "2026-01-01",
  "endDate": "2026-03-31",
  "monthlyPoints": [
    {
      "year": 2026,
      "month": "March",
      "rewardPoints": 271
    },
    {
      "year": 2026,
      "month": "February",
      "rewardPoints": 110
    },
    {
      "year": 2026,
      "month": "January",
      "rewardPoints": 115
    }
  ],
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

```bash
curl "http://localhost:8080/api/v1/rewards/customers/C1002?startDate=2026-02-01&endDate=2026-03-31"
```

Sample response:

```json
{
  "customerId": "C1002",
  "customerName": "Prabhu",
  "startDate": "2026-02-01",
  "endDate": "2026-03-31",
  "monthlyPoints": [
    {
      "year": 2026,
      "month": "March",
      "rewardPoints": 495
    },
    {
      "year": 2026,
      "month": "February",
      "rewardPoints": 179
    }
  ],
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

Sample request with only `startDate`:

```bash
curl "http://localhost:8080/api/v1/rewards/customers/C1001?startDate=2026-02-01"
```

This resolves to the inclusive period `2026-02-01` through `2026-04-30`.

Sample request with only `endDate`:

```bash
curl "http://localhost:8080/api/v1/rewards/customers/C1001?endDate=2026-03-31"
```

This resolves to the inclusive period `2026-01-01` through `2026-03-31`.

## Build
1. Open Command Prompt in the project root.
2. Verify Java and Maven are available in the machine:

```cmd
java -version
mvn -version
```

3. Run the build and tests:

```cmd
mvn clean test
```

## Run
1. Use the same Command Prompt session.
2. Start the application:

```cmd
mvn spring-boot:run
```