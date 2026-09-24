package io.github.lilaschuda.guanaco.demo.resiliencypipeline;

import io.github.lilaschuda.guanaco.api.GuanacoRoute;
import io.github.lilaschuda.guanaco.api.RouteOutcome;
import io.github.lilaschuda.guanaco.api.Processor;
import org.apache.camel.Exchange;

/**
 * Demonstrates the fixed Throttle -> Delay -> Circuit Breaker binding order
 * (see routes.yaml). Every message routes to ToPartner, whose binding
 * wraps a deliberately unreliable "partner" endpoint (FlakyPartnerRoute)
 * so the circuit breaker's behavior is actually observable.
 */
@GuanacoRoute
public class PartnerDispatchProcessor implements Processor<PartnerDispatchProcessor.OrderRoute<?>> {

    public sealed interface OrderRoute<T> extends RouteOutcome<T> permits ToPartner {}
    public record ToPartner(String body) implements OrderRoute<String> {}

    @Override
    public OrderRoute<?> process(Exchange exchange) {
        return new ToPartner(exchange.getIn().getBody(String.class));
    }
}