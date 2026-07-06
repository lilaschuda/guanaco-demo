package io.github.lilaschuda.guanaco.demo.coexist.route;

import io.github.lilaschuda.guanaco.core.RouteOutcome;

/**
 * Declares all possible routing outcomes for the PaymentProcessor.
 * The compiler enforces that every branch is covered.
 */
public sealed interface PaymentRoute<T> extends RouteOutcome permits ToRegister, ToWireTransfer, ToCredit {
    T body();
}

record ToRegister(String body) implements PaymentRoute<String> {}

record ToWireTransfer(String body) implements PaymentRoute<String> {}

record ToCredit(String body) implements PaymentRoute<String> {}
