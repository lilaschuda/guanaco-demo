# Guanaco (v0.1.0-SNAPSHOT)

> Compiler-Checked, Type-Safe Routing Topology for Apache Camel.

Guanaco is an architectural framework layer built on top of Apache Camel. It decouples business routing logic from physical infrastructure topology by replacing string-based predicates, untyped maps, and nested fluent syntax trees with standard Java algebraic data types (**sealed interfaces** and **records**).

Licensed under the **Apache 2.0 License**.

## Core Philosophy

Traditional enterprise integration patterns (EIP) force developers to interleave business analysis and transport infrastructure mapping within dense XML walls or fragile fluent builder chains. 

Guanaco flattens these topologies into explicit **Type Contracts**:
1. **The Processor** executes pure, un-nested Java control flow and returns a precise structural outcome variant.
2. **The YAML Configuration** binds those strongly-typed outcome classes to actual physical infrastructure endpoints (JMS, SQS, HTTP, etc.).

---

## Key Features

* **Elimination of Nesting Hell:** No missing `.end()` or `.endChoice()` builder states will ever break a deployment loop at runtime. Scope boundaries are native Java curly braces `{}`.
* **Declarative Infrastructure Ingestion:** Move endpoints entirely out of the compile path. If transport tiers scale or shift from ActiveMQ to AWS SQS, change the YAML - no code rebuild required.
* **Automatic Multicast Materialization:** Passing an array of destinations to an outcome inside the YAML instructs the underlying engine to safely handle parallel asynchronous broadcasting under the hood.
* **Legacy Coexistence:** Modernize iteratively. The core bootstrap engine isolates, sandboxes, and processes existing legacy Spring XML route contexts running alongside new native Guanaco code.

---

## Quick Start (Programmatic Bootstrap)

Guanaco hooks directly into your application context without forcing heavy external file-scaffolding:

```java
public class Application {
    public static void main(String[] args) throws Exception {
        GuanacoContext ctx = new GuanacoContext(Application.class.getPackageName());
        StaticApplicationContext sac = new StaticApplicationContext();
        
        ctx.setApplicationContext(sac);
        
        // Bind connection components programmatically
        ActiveMQConnectionFactory cf = new ActiveMQConnectionFactory("tcp://localhost:61616");
        ctx.getRegistry().bind("jmsConnectionFactory", cf);
        ctx.addComponent("jms", JmsComponent.jmsComponentAutoAcknowledge(cf));
        
        // Materialize routes and startup
        ctx.wireRoutes();
        ctx.start();
        
        Runtime.getRuntime().addShutdownHook(new Thread(ctx::stop));
        Thread.currentThread().join();
    }
}

