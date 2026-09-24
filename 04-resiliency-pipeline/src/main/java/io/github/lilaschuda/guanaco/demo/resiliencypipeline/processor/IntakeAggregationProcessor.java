package io.github.lilaschuda.guanaco.demo.resiliencypipeline;

import io.github.lilaschuda.guanaco.api.GuanacoRoute;
import io.github.lilaschuda.guanaco.api.RouteOutcome;
import io.github.lilaschuda.guanaco.api.Processor;
import org.apache.camel.Exchange;

/**
 * Demonstrates the fixed Idempotent Consumer -> Resequence -> Aggregate
 * pipeline order (see routes.yaml). Sends a duplicate, out-of-order set of
 * line items under one orderId; the pipeline drops the duplicate, restores
 * arrival order, and merges all items into a single exchange BEFORE this
 * processor ever runs. process() is called exactly once per completed
 * group, not once per incoming message.
 */
@GuanacoRoute
public class IntakeAggregationProcessor implements Processor<IntakeAggregationProcessor.OrderRoute<?>> {

    public sealed interface OrderRoute<T> extends RouteOutcome<T> permits ToMerged {}
    public record ToMerged(String body) implements OrderRoute<String> {}

    @Override
    public OrderRoute<?> process(Exchange exchange) {
        return new ToMerged(exchange.getIn().getBody(String.class));
    }
}