package io.github.lilaschuda.guanaco.demo.route;

import java.util.List;
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
public class InventoryProcessor implements Processor<InventoryRoute<?>> {

    @Override
    public InventoryRoute process(Exchange exchange) {
        String body = exchange.getIn().getBody(String.class);

        // Naive routing logic for prototype demonstration
        if (body != null && body.contains("damaged")) {
            return new ToInventoryFraudCheck(List.of("fraudCheckObject1", "fraudCheckObject2"));
        }
        if (body != null && body.contains("delivered")) {
            return new ToBookkeeping(body);
        }
        return new ToWarehouse(body);
    }
}
