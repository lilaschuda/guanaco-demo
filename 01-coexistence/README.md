# guanaco-demo

A runnable example project demonstrating [Guanaco](https://github.com/lilaschuda/guanaco) in action — a compiler-checked, idiomatic Java DSL layer on top of Apache Camel.

This repository is **not** the Guanaco library itself. It's a small Maven project that depends on Guanaco and shows what a real application using it looks like end to end: a processor, a sealed-interface route topology, a YAML binding file, and (for migration purposes) a legacy Camel XML route loaded alongside it.

> **Not affiliated with the Apache Software Foundation.** This project depends on Apache Camel but is not endorsed by, sponsored by, or otherwise affiliated with the Apache Software Foundation. "Apache," "Camel," "Apache Camel," and the Apache feather logo are trademarks of The Apache Software Foundation.

## What this demonstrates

**A processor with compiler-checked routing.** `OrderProcessor` returns a sealed `OrderRoute<?>` outcome — `ToInventory`, `ToPayment`, or `ToFraudCheck` — and the compiler enforces that every branch is handled:

```java
@GuanacoRoute
public class OrderProcessor implements Processor<OrderRoute<?>> {

    @Override
    public OrderRoute<?> process(GuanacoMessage message) throws Exception {
        String body = message.getBody(String.class);
        if (body.contains("suspicious")) return new ToFraudCheck(body);
        if (body.contains("unpaid"))     return new ToPayment(body);
        return new ToInventory(body);
    }
}
```

**Bindings configured outside of code**, in `src/main/resources/routes.yaml`:

```yaml
routes:
  OrderProcessor:
    from: direct:orders
    bindings:
      ToInventory:  mock:inventory
      ToPayment:    mock:payment
      ToFraudCheck: mock:fraud
```

**Legacy Camel XML route compatibility.** `src/main/resources/META-INF/spring/camel-context.xml` contains a traditional Camel route, loaded by Guanaco alongside the sealed-interface route in the same `CamelContext` — intended as a migration aid for teams transitioning existing Camel XML routes incrementally.

## Project structure

```
src/main/java/org/guanaco/demo/
  Application.java          — entry point: wires routes, starts the context
  example/
    OrderProcessor.java     — the processor
    OrderRoute.java          — sealed interface declaring the route topology
    ToInventory.java         — route outcomes (records)
    ToPayment.java
    ToFraudCheck.java

src/main/resources/
  routes.yaml                          — endpoint bindings for OrderProcessor
  META-INF/spring/camel-context.xml    — legacy XML route, loaded for compatibility
```

## Running it

```bash
mvn clean install
mvn exec:java
```

The application starts a `CamelContext`, registers the Guanaco-managed `OrderProcessor` route and the legacy XML route, and stays running until interrupted (`Ctrl+C`).

## Prerequisites

- Java 17+
- Maven 3.9+

## License and attribution

This demo project is licensed under the Apache License, Version 2.0 — see [LICENSE](./LICENSE). See [NOTICE](./NOTICE) for attribution to Apache Camel.

For the library itself, documentation, and design rationale, see the main [Guanaco repository](https://github.com/lilaschuda/guanaco).

