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
