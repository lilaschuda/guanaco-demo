package io.github.lilaschuda.guanaco.demo.route;

import org.apache.camel.Exchange;
import io.github.lilaschuda.guanaco.annotation.GuanacoRoute;
import io.github.lilaschuda.guanaco.dsl.Processor;

/**
 * Example processor demonstrating camel-guanaco's idiomatic routing model.
 *
 * The compiler enforces that every permitted OrderRoute subtype
 * is a possible return value — route topology as a type contract.
 */
@GuanacoRoute
public class PaymentProcessor implements Processor<PaymentRoute<?>> {

    @Override
    public PaymentRoute process(Exchange exchange) {
        String body = exchange.getIn().getBody(String.class);

        // Naive routing logic for prototype demonstration
        if (body != null && body.contains("cash")) {
            return new ToRegister(body);
        }
        if (body != null && body.contains("transfer")) {
            return new ToWireTransfer(body);
        }
        return new ToCredit(body);
    }
}
