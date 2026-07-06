package io.github.lilaschuda.guanaco.demo.coexist.route;

import org.apache.camel.Exchange;
import io.github.lilaschuda.guanaco.annotation.GuanacoRoute;
import io.github.lilaschuda.guanaco.dsl.Processor;
import java.util.logging.Logger;

@GuanacoRoute
public class PaymentProcessor implements Processor<PaymentRoute<?>> {

    private static final Logger LOG = Logger.getLogger(PaymentProcessor.class.getName());

    @Override
    public PaymentRoute<?> process(Exchange exchange) {
        String body = exchange.getIn().getBody(String.class);
        
        // Simulating parsing of transaction payment types
        String paymentType = "CREDIT_CARD";
        if (body != null && body.contains("TX_WIRE")) paymentType = "WIRE_TRANSFER";
        if (body != null && body.contains("TX_GIFT")) paymentType = "STORE_CREDIT";

        LOG.info(String.format("Resolving clearing house for settlement type: %s", paymentType));

        return switch (paymentType) {
            case "WIRE_TRANSFER" -> {
                LOG.info("Processing bank-to-bank SWIFT/SEPA transfer. Forwarding to B2B Wire Processing desk.");
                yield new ToWireTransfer(body);
            }
            case "STORE_CREDIT" -> {
                LOG.info("Local internal ledger balance update. Adjusting client registry records.");
                yield new ToRegister(body);
            }
            default -> {
                LOG.info("Standard consumer digital transaction. Relaying to Merchant Credit Gateway.");
                yield new ToCredit(body);
            }
        };
    }
}
