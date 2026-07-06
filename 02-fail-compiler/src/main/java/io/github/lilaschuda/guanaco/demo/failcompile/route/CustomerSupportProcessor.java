package io.github.lilaschuda.guanaco.demo.failcompile.route;

import org.apache.camel.Exchange;
import io.github.lilaschuda.guanaco.annotation.GuanacoRoute;
import static io.github.lilaschuda.guanaco.demo.failcompile.route.SupportRouteType.GENERAL_TICKET;
import io.github.lilaschuda.guanaco.dsl.Processor;

@GuanacoRoute
public class CustomerSupportProcessor implements Processor<CustomerSupportRoute<?>> {

    @Override
    public CustomerSupportRoute<?> process(Exchange exchange) throws Exception {
        String priority = exchange.getIn().getHeader("Priority", "STANDARD", String.class);
        SupportRouteType type = SupportRouteType.valueOf(priority);
        String body = exchange.getIn().getBody(String.class);

        /* Missing any cases in this routing schema will simply
        break the compilation. Your IDE will alert you immediately though...
        */
        return switch (type) {
            case VIP_ALERT -> new ToExecutiveEscalation(body);
            case BILLING_ISSUE -> new ToBillingDispute(body);
            case GENERAL_TICKET -> new ToTier1Support(body);
            //default         -> new ToTier1Support(body);
        };
    }
    
}

enum SupportRouteType {
    GENERAL_TICKET, VIP_ALERT, BILLING_ISSUE
}