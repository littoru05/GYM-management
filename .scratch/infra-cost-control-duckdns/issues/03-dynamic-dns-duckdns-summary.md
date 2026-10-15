# 03: Dynamic DNS Binding with DuckDNS and Dual-Link Summary

**What to build:** Synchronize the newly resolved public IPv4 address of the load balancer to the DuckDNS service upon resumption with a resilient 75-second retry propagation loop and non-blocking diagnostic warnings. Output both the stable DuckDNS domain and the raw cloud load balancer DNS link directly to the execution step summary.

**Blocked by:** 02-state-aware-fast-resume.md

**Status:** resolved

- [x] Extract the load balancer DNS hostname and resolve its public IPv4 address with an incremental retry loop of up to 75 seconds.
- [x] Push the resolved IP to the DuckDNS API using configured repository secrets (`DUCKDNS_DOMAIN`, `DUCKDNS_TOKEN`).
- [x] Log a non-blocking diagnostic warning instead of failing the workflow if DNS propagation does not complete within the threshold.
- [x] Display dual links (DuckDNS domain and raw load balancer DNS link) in the execution step summary.
- [x] Add contract test assertions for DuckDNS configuration, secrets usage, and summary reporting.

## Answer

- Added `Update DuckDNS and Publish Dual Links` step to `.github/workflows/infra-control.yml` executed on any `resume` action.
- Implemented a 15-iteration x 5s (75s max) retry loop querying `dig +short "$ALB_DNS"` for public IPv4 resolution.
- Integrated DuckDNS update API call via `curl` with `DUCKDNS_DOMAIN` and `DUCKDNS_TOKEN` repository secrets.
- Configured non-blocking warnings via GitHub Actions workflow commands (`::warning::`) if IP resolution or DuckDNS update fails, preventing workflow abortion.
- Rendered dual links in the GitHub Step Summary: primary fixed DuckDNS URL and raw AWS ALB DNS fallback URL, along with health check endpoints and DNS cache flush advice.
- Added `test_workflow_duckdns_and_dual_link_logic` in `infra/tests/test_infra_control.py`. All 26 unit tests pass.
