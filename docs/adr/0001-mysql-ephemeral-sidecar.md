# MySQL as ephemeral Fargate sidecar instead of RDS

For the staging environment (`develop` branch), we run MySQL 8.4 as a sidecar container
inside the same ECS Fargate task as the backend, with ephemeral storage — no EFS, no RDS.

Data is lost every time the task restarts. This is acceptable because Flyway migrations
(`V1__init.sql`, `V2__add_staff_to_check_ins.sql`) recreate the schema, and the `dev`
profile seeds demo data (`V900__insert_demo_data.sql`, `V901__hash_demo_passwords.sql`)
automatically on startup.

We chose this over Amazon RDS (which has a 12-month free tier) because:
- No separate infrastructure to manage or forget to stop
- Pause/resume is a single ECS desired-count toggle — no separate RDS stop/start
- Ephemeral data forces us to keep Flyway migrations and seed scripts correct
- Staging is for testing the deployment pipeline, not for persisting test data

When we add the production environment (`main` branch), we will use RDS MySQL with
automated backups and Multi-AZ. That decision lives in its own ADR.

## Considered Options

- **Amazon RDS MySQL free tier**: $0 for 12 months, managed backups. Rejected because
  it adds a separate resource to pause/resume and complicates the "one toggle to stop
  everything" goal.
- **MySQL in a separate ECS Service with EFS**: Persistent storage across restarts.
  Rejected because EFS adds cost (~$0.30/GB/month) and complexity (Cloud Map for
  service discovery) for data we intentionally don't need to keep.
