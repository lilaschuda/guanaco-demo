package io.github.lilaschuda.guanaco.demo.route;

import io.github.lilaschuda.guanaco.core.RouteOutcome;

/**
 * Declares all possible routing outcomes for the OrderProcessor.
 * The compiler enforces that every branch is covered.
 */
public sealed interface FraudCheckRoute<T> extends RouteOutcome permits ToArchive, ToManagement, ToCollector {
    T body();
}

record ToManagement(String body) implements FraudCheckRoute<String> {}

record ToArchive(String body) implements FraudCheckRoute<String> {}

record ToCollector(String body) implements FraudCheckRoute<String> {}