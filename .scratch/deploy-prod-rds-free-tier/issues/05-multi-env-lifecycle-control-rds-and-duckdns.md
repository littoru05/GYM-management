# 05: Multi-Environment Infrastructure Lifecycle Controller with RDS Stop/Start and Dynamic DNS Binding

**What to build:** Upgrade the 1-click infrastructure cost-control workflow (`Pause`, `Hibernate`, `Resume`) to support targeting either `staging` or `prod`. When `prod` is selected, **Pause** scales ECS tasks to zero while keeping the load balancer and RDS running; **Hibernate** dismantles the production load balancer and ECS services via targeted destruction while stopping the persistent RDS instance in place (`stop-db-instance`) so zero data is lost and hourly compute/ALB spend drops to $0.00; **Resume** automatically wakes the stopped RDS instance (`start-db-instance` and waits until `db-instance-available`) before restoring or scaling up the load balancer and ECS services, then executes **Dynamic DNS Binding** to synchronize the production load balancer IPv4 address with DuckDNS.

**Blocked by:** 03-prod-environment-composition-and-guardrails.md

**Status:** ready-for-agent

- [ ] Add an `environment` choice input (`staging` or `prod`, defaulting to `staging`) to the infrastructure control workflow and dynamically resolve cluster, service, load balancer, RDS identifier, and working directory names based on the selected environment.
- [ ] In **Pause** mode, scale the selected environment's backend and frontend ECS services to `0` while keeping the load balancer (and RDS in `prod`) active.
- [ ] In **Hibernate** mode, execute targeted destruction of the selected environment's load balancer and ECS services, and when `environment == 'prod'`, inspect the RDS instance status and execute `aws rds stop-db-instance` if available so persistent data is preserved while compute billing stops.
- [ ] In **Resume** mode, when `environment == 'prod'`, inspect the RDS instance status first—starting it with `aws rds start-db-instance` and waiting via `aws rds wait db-instance-available` if stopped—before proceeding to either fast-path ECS scale-up (from **Pause**) or full Terraform reconstruction (from **Hibernate**).
- [ ] Perform **Dynamic DNS Binding** upon **Resume** for both environments, supporting a dedicated `DUCKDNS_PROD_DOMAIN` secret for `prod` with automatic fallback to `DUCKDNS_DOMAIN`, and render dual access links (DuckDNS URL and raw ALB URL) in the GitHub Step Summary.
- [ ] Expand the infrastructure control contract test suite to verify environment selection, RDS stop/start orchestration during production **Hibernate**/**Resume**, backward compatibility with staging assertions, and **Dynamic DNS Binding**.
