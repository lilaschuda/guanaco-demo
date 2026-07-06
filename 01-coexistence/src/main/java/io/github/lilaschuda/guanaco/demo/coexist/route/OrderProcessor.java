package io.github.lilaschuda.guanaco.demo.coexist.route;

import org.apache.camel.Exchange;
import io.github.lilaschuda.guanaco.annotation.GuanacoRoute;
import io.github.lilaschuda.guanaco.dsl.Processor;
import java.util.logging.Logger;

@GuanacoRoute
public class OrderProcessor implements Processor<OrderRoute<?>> {
    
    private static final Logger LOG = Logger.getLogger(OrderProcessor.class.getName());
    
    @Override
    public OrderRoute<?> process(Exchange exchange) throws Exception {
        String body = exchange.getIn().getBody(String.class);
        String customerTier = exchange.getIn().getHeader("CustomerTier", "STANDARD", String.class);
        
        LOG.info(String.format("Processing incoming order [Tier: %s], Payload: %s", customerTier, body));
        
        // High-risk transaction rules
        if (body != null && (body.contains("FLAG_RISK") || body.contains("suspicious"))) {
            LOG.warning("Risk signature detected! Routing directly to Fraud Audit Pipeline.");
            return new ToFraudCheck(body);
        }
        
        // Settlement check
        if (body != null && body.contains("UNPAID_INVOICE")) {
            LOG.info("Order requires pre-payment clearance. Routing to Payment Gateway.");
            return new ToPayment(body);
        }
        
        // Standard fulfillment line
        LOG.info("Order cleared for immediate allocation. Routing to Inventory System.");
        return new ToInventory(body);
    }
}
