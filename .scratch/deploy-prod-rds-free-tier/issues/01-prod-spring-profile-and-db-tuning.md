# 01: Production Spring Boot Profile and Database Connection Tuning with Contract Tests

**What to build:** Provide a dedicated production configuration profile for the backend service that activates when the runtime profile is set to `prod`. This profile tunes relational database connectivity specifically for a Single-AZ micro RDS MySQL instance under strict free-tier constraints: negotiating opportunistic TLS encryption without manual certificate bundling, capping connection pool limits so the 1 GB database instance never suffers memory or connection exhaustion, silencing verbose SQL and transaction debug logging to preserve the 5 GB monthly CloudWatch log ingestion quota, and retaining both schema and initial demonstration seed migrations so a freshly provisioned production database is immediately ready for user sign-in and evaluation.

**Blocked by:** None (can start immediately)

**Status:** ready-for-agent

- [ ] Define a dedicated `prod` application configuration profile that activates when the active Spring profile is `prod`.
- [ ] Configure the production datasource URL to connect to the external MySQL host and port with `sslMode=PREFERRED`, UTF-8 encoding, and local timezone alignment.
- [ ] Bound the HikariCP connection pool in the `prod` profile to a maximum pool size of 10 and minimum idle count of 2.
- [ ] Disable Hibernate `show-sql` formatting and raise SQL and transaction logging thresholds to `WARN`/`INFO` in production to prevent excessive CloudWatch log volume.
- [ ] Configure Flyway in the `prod` profile to execute both core schema migrations and initial demo seed migrations on startup.
- [ ] Add automated contract tests verifying all production profile invariants (TLS preference, connection pool sizing, log suppression, and Flyway migration locations).
