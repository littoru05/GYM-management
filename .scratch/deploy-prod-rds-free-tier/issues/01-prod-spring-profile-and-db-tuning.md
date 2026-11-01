# 01: Production Spring Boot Profile and Database Connection Tuning with Contract Tests

**What to build:** Provide a dedicated production configuration profile for the backend service that activates when the runtime profile is set to `prod`. This profile tunes relational database connectivity specifically for a Single-AZ micro RDS MySQL instance under strict free-tier constraints: negotiating opportunistic TLS encryption without manual certificate bundling, capping connection pool limits so the 1 GB database instance never suffers memory or connection exhaustion, silencing verbose SQL and transaction debug logging to preserve the 5 GB monthly CloudWatch log ingestion quota, and retaining both schema and initial demonstration seed migrations so a freshly provisioned production database is immediately ready for user sign-in and evaluation.

**Blocked by:** None (can start immediately)

**Status:** resolved

- [x] Define a dedicated `prod` application configuration profile that activates when the active Spring profile is `prod`.
- [x] Configure the production datasource URL to connect to the external MySQL host and port with `sslMode=PREFERRED`, UTF-8 encoding, and local timezone alignment.
- [x] Bound the HikariCP connection pool in the `prod` profile to a maximum pool size of 10 and minimum idle count of 2.
- [x] Disable Hibernate `show-sql` formatting and raise SQL and transaction logging thresholds to `WARN`/`INFO` in production to prevent excessive CloudWatch log volume.
- [x] Configure Flyway in the `prod` profile to execute both core schema migrations and initial demo seed migrations on startup.
- [x] Add automated contract tests verifying all production profile invariants (TLS preference, connection pool sizing, log suppression, and Flyway migration locations).

## Answer

- **Production Spring Boot Profile (`application-prod.yaml`)**:
  - Activated via `spring.config.activate.on-profile: prod` when `SPRING_PROFILES_ACTIVE=prod`.
  - Configured datasource URL to connect to external MySQL on standard port 3306 (`${DB_HOST:localhost}:${DB_PORT:3306}/${DB_NAME:gym_management}`) with opportunistic TLS negotiation (`sslMode=PREFERRED`), UTF-8 character encoding, and local timezone alignment (`Asia/Ho_Chi_Minh`).
  - Constrained HikariCP connection pool to `maximum-pool-size: ${DB_POOL_SIZE:10}` and `minimum-idle: ${DB_MIN_IDLE:2}` to safely stay within 1 GB micro RDS instance memory boundaries.
  - Disabled Hibernate SQL formatting (`show-sql: false`, `format_sql: false`, `highlight_sql: false`) and elevated SQL and transaction log thresholds to `WARN`/`INFO` (`org.hibernate.SQL: WARN`, `org.hibernate.orm.jdbc.bind: WARN`, `org.springframework.orm.jpa: WARN`, `org.springframework.transaction: WARN`) to conserve the 5 GB CloudWatch log ingestion quota.
  - Configured Flyway to execute both core schema migrations (`classpath:db/migration`) and initial demo seed migrations (`classpath:db/migration-dev`) on startup.
- **Contract Tests**:
  - Implemented `backend/src/test/java/com/example/GYM_management_api/config/ProdProfileContractTest.java` verifying profile activation, JDBC parameters, pool bounds, log suppression, Flyway locations, and property overriding against base configuration.
  - Implemented `infra/tests/test_backend_prod_profile.py` verifying production profile invariants across repository test suites.
