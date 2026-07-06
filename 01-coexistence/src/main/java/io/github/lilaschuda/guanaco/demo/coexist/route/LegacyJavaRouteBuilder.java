package io.github.lilaschuda.guanaco.demo.coexist.route;

import org.apache.camel.builder.RouteBuilder;

/**
 * A traditional Apache Camel Java Fluent DSL route.
 * Demonstrates how legacy fluent pipelines can consume outputs directly
 * from modern Guanaco processors inside the same engine context.
 */
public class LegacyJavaRouteBuilder extends RouteBuilder {

    @Override
    public void configure() throws Exception {
        
        from("jms:queue:inventory.allocation?connectionFactory=#jmsConnectionFactory")
            .routeId("legacyJavaWarehouseDispatcher")
            .log("Traditional Java DSL: Intercepted raw stock allocation payload from Guanaco.")
            
            // Old-school fluent conditional manipulation
            .choice()
                .when(body().contains("QTY:99"))
                    .log("Traditional Java DSL: Flagging high-volume dispatch priority header.")
                    .setHeader("DispatchSpeed", constant("EXPRESS_OVERNIGHT"))
                .otherwise()
                    .setHeader("DispatchSpeed", constant("STANDARD_GROUND"))
            .end() // The infamous syntax tax (.end) that Guanaco eliminates!
            
            .log("Traditional Java DSL: Shipping final dispatch payload to terminal execution queues.")
            .to("jms:queue:warehouse.terminal.dispatch?connectionFactory=#jmsConnectionFactory");
    }
}