# Single-AZ RDS MySQL Free Tier with Stop-on-Hibernate for Production

For the production environment (`main` branch), we run MySQL 8.4 on a Single-AZ Amazon RDS `db.t4g.micro` (or `db.t3.micro`) instance with 20 GB `gp2` storage, 7-day automated backups, and storage autoscaling disabled, rather than a Multi-AZ deployment or an ephemeral Fargate sidecar. During production hibernation, the RDS instance is stopped in place (`stop-db-instance`) rather than destroyed, while stateless ALB and ECS services are dismantled.

We chose this configuration to guarantee persistent relational storage for production while staying strictly within the 100% AWS 12-Month Free Tier ceiling (750 instance hours, 20 GB storage, 20 GB backup storage per month):
- **Multi-AZ violates Free Tier**: Enabling Multi-AZ doubles instance hour consumption (~1,488 hours/month) and incurs standby charges outside the 750-hour Free Tier allowance.
- **Persistent state across hibernation**: Unlike staging's ephemeral sidecar, production data must survive cost-control cycles. Stopping RDS halts compute billing (or preserves Free Tier hours) while retaining the underlying 20 GB EBS volume intact.
- **Smaller Fargate footprint**: Offloading MySQL from the ECS task allows the production backend task to drop from `512 CPU / 2048 MiB` to `256 CPU / 512 MiB`.

## Considered Options

- **RDS Multi-AZ Deployment**: High availability with automatic failover, originally anticipated in ADR 0001. Rejected because it exceeds the 750-hour monthly Free Tier limit and incurs immediate monthly costs.
- **Destroying RDS on Hibernate with Snapshot Restore**: Eliminates even storage allocation during hibernation. Rejected because 20 GB storage is already 100% covered by the Free Tier 24/7, and snapshot restore adds 10–15 minutes of recovery latency and state drift risk in Terraform.
- **Ephemeral MySQL Sidecar (as in Staging)**: Zero RDS management overhead. Rejected because production requires durable data persistence across task restarts and deployments.

## Consequences

- AWS automatically restarts a stopped RDS instance after 7 consecutive days; if production remains hibernated longer than 7 days, the instance will resume consuming Free Tier hours (which is still $0 as long as total monthly runtime stays under 750 hours).
- Production recovery from hibernation takes slightly longer than staging (~4–6 minutes total) because the workflow must wait for the RDS instance to transition from `stopped` to `available` before ECS backend tasks run Flyway migrations.
