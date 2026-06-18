package io.github.lilaschuda.guanaco.demo.route;

import java.util.logging.Logger;
import org.apache.camel.Exchange;
import org.apache.commons.logging.impl.Log4JLogger;
import io.github.lilaschuda.guanaco.annotation.GuanacoRoute;
import io.github.lilaschuda.guanaco.dsl.Processor;

/**
 * Example processor demonstrating camel-guanaco's idiomatic routing model.
 *
 * The compiler enforces that every permitted OrderRoute subtype
 * is a possible return value — route topology as a type contract.
 */
@GuanacoRoute
public class OrderProcessor implements Processor<OrderRoute<?>> {
    
    private static final Logger LOG = Logger.getLogger("OrderProcessor");
    
    @Override
    public OrderRoute process(Exchange exchange) {
        String body = exchange.getIn().getBody(String.class);
        LOG.info("Received Message: " + body );
        // Naive routing logic for prototype demonstration
        if (body != null && body.contains("suspicious")) {
            return new ToFraudCheck(body);
        }
        if (body != null && body.contains("unpaid")) {
            return new ToPayment(body);
        }
        return new ToInventory(body);
    }
}
