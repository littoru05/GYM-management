# Infrastructure Cost Control with Pause, Hibernate, and Dynamic DNS Binding

Status: ready-for-agent

## Problem Statement

Running cloud staging environments 24/7 generates unnecessary hourly charges—primarily from Application Load Balancers and continuous container compute—even when the development team is off-hours or away for multiple days. While a short pause mechanism exists to scale compute tasks down to zero, the load balancer continues to accrue charges (~$16+/month) and its raw AWS DNS hostname is unwieldy. Conversely, destroying the entire cloud environment deletes VPC networking and container images, resulting in slow and fragile multi-step recovery. Furthermore, developers need a consistent, memorable domain name without incurring Route 53 or dedicated Elastic IP costs, and need deployments to fail safely when the environment is powered down.

## Solution

Provide a 1-click infrastructure cost control workflow with three distinct operational modes:
1. **Pause**: Temporarily scale compute tasks to zero for short breaks, keeping networking and load balancer active for near-instant (under 30 seconds) reactivation.
2. **Hibernate**: Safely dismantle hourly-billed resources (the load balancer, target groups, and container services) while leaving VPC networking, container images, and cluster scaffolding intact, reducing hourly idle spend to $0.00.
3. **Resume**: An intelligent state-aware restart procedure that inspects the surviving infrastructure: if suspended via Pause, it scales compute tasks back up in seconds without modifying infrastructure; if suspended via Hibernate, it re-provisions the load balancer and container services.

Upon restart, the procedure binds the load balancer's dynamic IPv4 address to a persistent, zero-cost domain via Dynamic DNS Binding (DuckDNS) with multi-retry resilience, prints both the custom domain and fallback AWS URLs to the execution summary, and ensures continuous deployment pipelines bypass service updates gracefully during hibernation.

## User Stories

1. As a developer taking a short break during the day, I want to execute a Pause action, so that compute tasks stop running and reduce Fargate compute charges while preserving the load balancer.
2. As a developer resuming after a short pause, I want the system to detect the active load balancer and scale services back up within 30 seconds, so that I can resume testing without waiting for infrastructure provisioning.
3. As a project owner finishing work for the night or weekend, I want to execute a Hibernate action, so that hourly charges for both compute and load balancers drop to $0.00.
4. As a project owner initiating Hibernate, I want persistent assets such as container images in the registry, VPC subnets, cluster configurations, and secret parameters to remain preserved, so that the environment can be faithfully reconstructed.
5. As a developer returning after an extended break, I want to execute a Resume action, so that hibernated load balancers and services are automatically detected and re-provisioned without manual intervention.
6. As a QA engineer testing the staging environment, I want to access the web application through a consistent, memorable DuckDNS URL, so that I do not need to update bookmarks or client configuration every time the load balancer is recreated.
7. As a frontend client, I want API requests routed automatically to the backend service through the load balancer, so that cross-origin communication works transparently regardless of the active host.
8. As a developer resuming the environment, I want the workflow summary to display both the primary DuckDNS URL and the raw AWS load balancer DNS link, so that I have an immediate fallback if local DNS caching or propagation causes delays.
9. As a DevOps engineer running the Resume action, I want the Dynamic DNS Binding step to retry resolving the load balancer IP up to 75 seconds, so that transient DNS propagation delays on new load balancers do not cause false failures.
10. As a DevOps engineer running the Resume action, I want DNS update failures to log warnings rather than terminating the entire workflow, so that the restored services remain usable via the direct load balancer URL even if external DNS services experience downtime.
11. As a developer pushing code to the staging branch while the environment is in Hibernate, I want the delivery pipeline to build and push container images to the registry successfully, so that new code artifacts are ready for subsequent startup.
12. As a developer pushing code while staging is hibernating, I want the delivery pipeline to gracefully skip updating missing container services without reporting an error, so that build pipelines remain green and inform the team of the hibernated state.
13. As an engineer reviewing infrastructure spending, I want a verifiable zero-cost idle state during hibernation, so that budget thresholds are strictly respected.
14. As an automated test suite, I want to validate the workflow definition, input choices, security secret requirements, and scaling logic, so that configuration regressions are detected before pull requests are merged.

## Implementation Decisions

- **Three-Mode Operational Model**: The infrastructure control interface exposes three explicit actions: `pause`, `hibernate`, and `resume`.
- **Targeted Teardown for Hibernation**: Hibernation selectively destroys only the Application Load Balancer, listener rules, target groups, and container services, intentionally isolating core networking (VPC, subnets, route tables, internet gateways), image repositories, and IAM roles.
- **State-Aware Resume Inspection**: The resume process queries the cloud provider API to determine whether the load balancer currently exists prior to running any configuration management tools. If present, it executes an immediate container scale-up; if absent, it initiates full infrastructure convergence.
- **Dynamic DNS Binding Integration**: A dedicated post-provisioning step resolves the active public IPv4 address of the load balancer and pushes updates to the DuckDNS API using secured credential secrets.
- **Resilient DNS Retry with Non-Blocking Warning**: The DNS resolution mechanism employs an incremental retry loop (up to 75 seconds). If resolution does not complete within the threshold, a non-blocking diagnostic warning is emitted, leaving the workflow in a successful state.
- **Dual-Link Execution Reporting**: The workflow summary outputs markdown links for both the stable DuckDNS domain and the raw load balancer hostname, along with diagnostic health check endpoints.
- **Graceful Pipeline Bypass**: Continuous delivery pipelines check service existence before attempting service revisions, bypassing service updates with an informative notice when hibernation is active.

## Testing Decisions

- **Good Test Principle**: Tests must evaluate the observable external behavior and structural contracts of the control workflows and deployment pipelines (such as accepted input options, presence of authentication hooks, conditional branches, and target declarations) without mocking out core shell syntax or relying on flaky live cloud credentials in unit runs.
- **Tested Modules**:
  - The infrastructure control workflow contract and parameter choices.
  - The continuous delivery staging pipeline deployment resilience.
- **Prior Art**: The existing Python unit test suite in the infrastructure testing directory provides patterns for reading workflow definitions, validating dispatch inputs, asserting conditional execution logic, and verifying target service definitions.

## Out of Scope

- Elastic IP allocation or Network Load Balancer provisioning (to avoid recurring hourly AWS charges).
- SSL/TLS certificate management and HTTPS binding on DuckDNS domains (HTTP on port 80 is retained for staging).
- Automated cron-based scheduling for pause/hibernate (manual dispatch remains the intended control plane).
- Persistent volume database storage across restarts (the staging database remains an ephemeral container sidecar reseeded via Flyway on startup as governed by prior architectural decisions).

## Further Notes

- Aligns directly with the project glossary definitions for `Pause`, `Hibernate`, `Resume`, and `Dynamic DNS Binding`.
- Operates under the architectural decision recorded in ADR 0002.
