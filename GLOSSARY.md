# Gym Management Infrastructure

Infrastructure lifecycle and deployment terms for managing staging and production environments under strict cost and availability constraints.

## Language

**Pause**:
A temporary suspension of application services where active compute tasks are scaled to zero while preserving networking and routing components.
_Avoid_: Stop, Shutdown

**Hibernate**:
A cost-saving state where stateless hourly-billed routing and compute components are dismantled while persistent data stores are suspended in place and network foundations are preserved.
_Avoid_: Deep Pause, Terminate, Teardown

**Resume**:
The restoration of application operations from a suspended or hibernated state, waking any suspended data stores and reconstructing missing routing and compute components based on the currently surviving infrastructure.
_Avoid_: Start, Restart, Re-run

**Dynamic DNS Binding**:
The mechanism of synchronizing the changing network address of a load balancer to a persistent domain name.
_Avoid_: Static Binding, Hardcoded DNS

**Artifact Version**:
An immutable release identifier associating container images, runtime metadata, and frontend asset bundles with a specific source code revision.
_Avoid_: Image Tag, Build Number, Latest Tag

**Last Stable Version**:
The most recent Artifact Version that passed all post-deployment health checks and is recorded as the recovery target for automated rollback.
_Avoid_: Previous Version, Backup Image

**Expand-Contract Migration**:
A two-phase database schema evolution pattern where forward migrations only add backward-compatible structures (Expand) so older application versions remain functional if rolled back, deferring destructive removals (Contract) to a subsequent verified release.
_Avoid_: Undo Migration, Destructive Migration, Down Script

**Pre-Migration Backup**:
An automated, non-locking logical database snapshot captured and verified immediately before starting a new application release and executing schema migrations.
_Avoid_: Manual Dump, Post-Deploy Backup

**Auto-Rollback**:
The automated recovery procedure triggered when a newly deployed release fails post-deployment health checks within the verification window, reverting application containers to the Last Stable Version while preserving database state under Expand-Contract Migration rules.
_Avoid_: Manual Revert, Database Restore Rollback
