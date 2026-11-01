# Infrastructure Cost Control via Pause, Hibernate, and Dynamic DNS Sync

For the staging environment, we control cloud spend by implementing a three-mode lifecycle (`pause`, `hibernate`, `resume`) on GitHub Actions, combining targeted Terraform destruction of hourly-billed components (ALB and ECS Services) with automated DuckDNS synchronization to provide a persistent, zero-cost access URL.

We chose this model over destroying the entire environment or running 24/7 because:
- **Cost elimination**: Destroying ALB and ECS services reduces idle hourly AWS spend to $0.00 while preserving VPC, ECR images, and SSM secrets.
- **Fast recovery**: `pause` scales compute tasks to zero and recovers in under 30 seconds; `hibernate` recovers in ~2-3 minutes via automated state-aware `resume`.
- **Zero-cost domain mapping**: DuckDNS dynamically synchronizes with ephemeral ALB IPv4 addresses, avoiding the ongoing cost of Route 53 or dedicated Elastic IPs.

## Considered Options

- **Destroying entire infrastructure (`terraform destroy`)**: Eliminates costs, but deletes VPC, Subnets, and ECR images, making recovery take 10+ minutes and requiring full image rebuilds.
- **24/7 Always-On with Route 53**: Simplifies DNS management with Route 53 ALIAS records, but incurs continuous charges for ALB (~$16+/month) plus Route 53 hosted zone fees ($0.50/month).
- **Manual IP updating**: Requires engineers to check AWS console and manually update DNS records after every restart, creating developer friction and error risk.

## Consequences

- DuckDNS relies on A-record resolution against one of the ALB's dynamic IPv4 addresses; in the event of AWS IP rotation or DNS caching lag, the raw ALB DNS name remains available as an immediate fallback.
- The CD pipeline (`cd-staging.yml`) gracefully bypasses service updates if a commit is merged while staging is hibernated, preserving deployment safety.
