# Example 03: The Enterprise DSL Showdown

This module provides the ultimate architectural contrast. It implements a complex, multi-variable enterprise scenario using three completely separate design implementations side-by-side to expose the maintenance patterns and failure rates of each.

## The Test Scenario: Inbound Claims Triage Pipeline

Every incoming claim must be scrutinized across region lines (EMEA vs AMER), validated for extreme valuation figures, audited for vehicle category types, and fanned out to distinct fulfillment targets or compliance streams simultaneously.

```text
                             ┌── [EMEA] ── [HIGH VALUE?] ── [AUTO?] ──> MULTICAST: Speedlane & Audit
                             │                                 └──> General High Value
                             │         └── [STANDARD] ──────────────> Archive
                             │
[Claims Inbound Queue] ──────┼── [AMER] ── [FRAUD SUSPECT?] ──────────> Fraud Hold Hold
                             │         └── [CLEAN] ──────────────────> General AMER
                             │
                             └── [OTHER] ────────────────────────────> Global Fallback Triage
```
---

## Compared Paradigms

### 1. Legacy Spring XML DSL (camel-context-xml-showdown.xml)
* The Reality: Highly verbose, structurally bloated configuration file.
* The Failure: Zero type checking and absolute opacity to standard refactoring tools. String typographical errors inside expression nodes bypass compile loops entirely, resulting in deployment runtime crashes.

### 2. Traditional Java Fluent DSL (LegacyFluentShowdownRoute.java)
* The Reality: A deep, nested fluent chain of builder syntax trees.
* The Failure: Introduces the severe "Camel Syntax Tax." Mismanaging or misplacing fluent tokens (like using a single .endChoice() instead of an explicit .end().endChoice() closure to pop inner choice builder state scopes) compiles flawlessly, but breaks the routing engine topology layout on startup, raising IllegalArgumentException blocks.

### 3. The Guanaco Paradigm (ClaimsProcessor.java)
* The Reality: Pure, un-nested Java logical blocks interacting with sealed algebraic hierarchies.
* The Victory: Scope bounds are defined using regular Java braces {}. Any structure errors are surfaced directly by the IDE and halted immediately by the compiler. Infrastructure concerns like Parallel Multicasting are extracted cleanly to array items inside routes-showdown.yaml and handled safely behind the framework boundary.

---

## Executing the Showdown Demo

Run the main application class programmatically via Maven to watch all three parallel environments initialize concurrently:

```bash
mvn exec:java
