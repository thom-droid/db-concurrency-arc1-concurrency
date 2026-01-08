# Simple Booking System Skeleton

Minimal Spring Boot project for experimenting with concurrent booking logic using Postgres 16 and HikariCP. The focus is on a tiny domain (users and seats) so you can concentrate on transaction handling strategies.

## Stack

- Java 17, Spring Boot 3.2 (Web + MVC, Spring Data JPA, Validation)
- Postgres 16 running via Docker Compose
- HikariCP connection pool (Spring Boot default)

## Getting Started

1. **Start Postgres**

   ```bash
   docker compose up -d
   ```

   The container exposes `jdbc:postgresql://localhost:5432/booking` with `booking/booking` credentials.

2. **Build & Run the app**

   ```bash
   mvn spring-boot:run
   ```

   (If you prefer the Maven Wrapper, run `mvn -N wrapper:wrapper` once you have internet access to download the wrapper JAR and scripts.)

3. **Verify**

   Query the seeded seats:

   ```bash
   curl http://localhost:8080/api/seats
   ```

   Each seat response includes the optimistic locking `version` you can use during experiments.

## Project Layout

- `Seat` and `User` entities live under `com.example.simplebooking.seat` and `com.example.simplebooking.user` respectively.
- Repositories are plain Spring Data interfaces; inject them wherever you will implement booking flows.
- `DataInitializer` seeds two sample users and 10 seats so you can jump straight into transaction scenarios.

## Next Steps / Ideas

- Implement competing booking flows that update the `Seat.reservedBy` association using different isolation levels.
- Try optimistic locking retry loops leveraging the `@Version` column.
- Introduce pessimistic locks via `@Lock` annotations or custom queries.
- Add integration tests that simulate concurrent requests with `@Transactional` service methods.
