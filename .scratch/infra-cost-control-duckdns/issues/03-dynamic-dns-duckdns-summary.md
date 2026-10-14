# 03: Dynamic DNS Binding with DuckDNS and Dual-Link Summary

**What to build:** Synchronize the newly resolved public IPv4 address of the load balancer to the DuckDNS service upon resumption with a resilient 75-second retry propagation loop and non-blocking diagnostic warnings. Output both the stable DuckDNS domain and the raw cloud load balancer DNS link directly to the execution step summary.

**Blocked by:** 02-state-aware-fast-resume.md

**Status:** ready-for-agent

- [ ] Extract the load balancer DNS hostname and resolve its public IPv4 address with an incremental retry loop of up to 75 seconds.
- [ ] Push the resolved IP to the DuckDNS API using configured repository secrets (`DUCKDNS_DOMAIN`, `DUCKDNS_TOKEN`).
- [ ] Log a non-blocking diagnostic warning instead of failing the workflow if DNS propagation does not complete within the threshold.
- [ ] Display dual links (DuckDNS domain and raw load balancer DNS link) in the execution step summary.
- [ ] Add contract test assertions for DuckDNS configuration, secrets usage, and summary reporting.
