package io.github.lilaschuda.guanaco.demo.resiliencypipeline;

import org.apache.camel.builder.RouteBuilder;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A deliberately unreliable "partner API," simulated with a plain native
 * Camel RouteBuilder rather than a Guanaco processor — this is intentional,
 * both because there's no business decision here for a sealed hierarchy to
 * express, and as a callback to demo 01's coexistence story: Guanaco routes
 * and raw Camel routes freely coexist in the same CamelContext.
 *
 * Toggle isUp to simulate the partner going down and recovering, so
 * PartnerDispatchProcessor's circuit breaker has something real to react to.
 */
public class FlakyPartnerRoute extends RouteBuilder {

    public static final AtomicBoolean isUp = new AtomicBoolean(true);

    @Override
    public void configure() {
        from("direct:flakyPartner")
                .routeId("flaky-partner")
                .process(exchange -> {
                    if (!isUp.get()) {
                        throw new RuntimeException("Partner API is down");
                    }
                    log.info(">>> Partner API accepted: {}", exchange.getIn().getBody(String.class));
                });
    }
}