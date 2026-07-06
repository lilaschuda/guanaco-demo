package io.github.lilaschuda.guanaco.demo.coexist.route;

import org.apache.camel.Exchange;
import io.github.lilaschuda.guanaco.annotation.GuanacoRoute;
import io.github.lilaschuda.guanaco.dsl.Processor;
import java.util.logging.Logger;

@GuanacoRoute
public class FraudCheckProcessor implements Processor<FraudCheckRoute<?>> {

    private static final Logger LOG = Logger.getLogger(FraudCheckProcessor.class.getName());

    @Override
    public FraudCheckRoute<?> process(Exchange exchange) {
        String body = exchange.getIn().getBody(String.class);
        
        // Simulating extraction of a risk assessment score (0 to 100)
        int riskScore = body != null && body.contains("CRITICAL_SCORE") ? 95 : 45;
        
        LOG.info(String.format("Evaluating security risk profile. Calculated Score: %d/100", riskScore));

        if (riskScore >= 80) {
            LOG.severe("CRITICAL RISK: Order frozen. Escalating to Fraud Management Team for manual review.");
            return new ToManagement(body);
        }
        
        if (body != null && body.contains("ANOMALY_PATTERN")) {
            LOG.warning("MILD ANOMALY: Forwarding transaction telemetry to the AI behavioral analytics engine.");
            return new ToCollector(body);
        }

        LOG.info("Risk threshold acceptable. Archiving safety report and passing order through.");
        return new ToArchive(body);
    }
}
