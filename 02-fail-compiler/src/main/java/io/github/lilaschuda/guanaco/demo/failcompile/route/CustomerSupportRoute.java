package io.github.lilaschuda.guanaco.demo.failcompile.route;

import io.github.lilaschuda.guanaco.core.RouteOutcome;

public sealed interface CustomerSupportRoute<T> extends RouteOutcome 
    permits ToTier1Support, ToExecutiveEscalation, ToBillingDispute {
    T body();
}

record ToTier1Support(String body) implements CustomerSupportRoute<String> {}
record ToExecutiveEscalation(String body) implements CustomerSupportRoute<String> {}
record ToBillingDispute(String body) implements CustomerSupportRoute<String> {}