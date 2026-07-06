package io.github.lilaschuda.guanaco.demo.coexist.route;

import java.util.List;
import io.github.lilaschuda.guanaco.core.RouteOutcome;

/**
 * Declares all possible routing outcomes for the InventoryProcessor
 * The compiler enforces that every branch is covered.
 */
public sealed interface InventoryRoute<T> extends RouteOutcome permits ToBookkeeping, ToInventoryFraudCheck, ToWarehouse {
    T body();
}

record ToBookkeeping(String body) implements InventoryRoute<String> {}

record ToInventoryFraudCheck(String body) implements InventoryRoute<String> {}

record ToWarehouse(String body) implements InventoryRoute<String> {}