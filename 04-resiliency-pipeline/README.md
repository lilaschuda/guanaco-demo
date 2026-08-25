# 04-resiliency-pipeline

A runnable demo of Guanaco's message-stream EIPs and dispatch-wrapping resiliency policies, in one scenario.

For what each of these does and why the orderings are fixed, see the main [Guanaco README](https://github.com/lilaschuda/guanaco#message-stream-eips-idempotent-consumer-resequencer-aggregate) — this demo shows them running, it doesn't re-explain them.

## What this demonstrates

**Phase 1 — `IntakeAggregationProcessor`**: the fixed `Idempotent Consumer → Resequence → Aggregate` pipeline. Four messages are sent under one `orderId` — one is a duplicate, and two arrive out of order. The pipeline drops the duplicate, restores order, and merges all genuine messages into a single exchange before the processor ever runs.

**Phase 2 — `PartnerDispatchProcessor`**: the fixed `Throttle → Delay → Circuit Breaker` binding order, wrapping a deliberately unreliable downstream endpoint (`FlakyPartnerRoute`, a plain native Camel route — also a callback to demo `01`'s coexistence story). The delay is computed per-message via a registered `GuanacoDelayStrategy` doing real exponential backoff from a `retryCount` header, not a fixed constant. Partner failures are sent in a burst large enough to trip the circuit breaker, so you can see the exception type change from a real downstream failure to a circuit-open rejection once the breaker trips — then watch it recover once the partner comes back up and `waitDurationInOpenStateMs` elapses.

## Running it

```bash
mvn clean compile
mvn exec:java
```

No external services required — everything in this demo runs in-process.

## Prerequisites

- Java 17+
- Maven 3.9+

## License and attribution

Licensed under the Apache License, Version 2.0 — see [LICENSE](./LICENSE) and [NOTICE](./NOTICE).

For the library itself, full documentation, and design rationale, see the main [Guanaco repository](https://github.com/lilaschuda/guanaco).
