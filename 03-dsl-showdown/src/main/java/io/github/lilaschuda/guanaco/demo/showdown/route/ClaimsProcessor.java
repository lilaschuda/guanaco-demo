package io.github.lilaschuda.guanaco.demo.showdown.route;

import org.apache.camel.Exchange;
import io.github.lilaschuda.guanaco.annotation.GuanacoRoute;
import io.github.lilaschuda.guanaco.dsl.Processor;

@GuanacoRoute
public class ClaimsProcessor implements Processor<ClaimsRoute<?>> {

    @Override
    public ClaimsRoute<?> process(Exchange exchange) throws Exception {
        String region = exchange.getIn().getHeader("Region", "GLOBAL", String.class);
        String body = exchange.getIn().getBody(String.class);
        String claimType = exchange.getIn().getHeader("Type", "GENERAL", String.class);
        
        boolean isHighValue = body != null && body.contains("HIGH_VALUE");
        boolean isFraudSuspect = body != null && body.contains("FRAUD_SUSPECT");

        // Simple, clean, flat Java matching logic.
        if ("EMEA".equals(region)) {
            if (isHighValue) {
                return "AUTO".equals(claimType) 
                    ? new ToEmeaAutoSpeedlane(body) 
                    : new ToEmeaGeneralHighValue(body);
            }
            return new ToEmeaArchive(body);
        }

        if ("AMER".equals(region)) {
            return isFraudSuspect ? new ToAmerFraudHold(body) : new ToAmerGeneral(body);
        }

        return new ToGlobalTriage(body);
    }
}
