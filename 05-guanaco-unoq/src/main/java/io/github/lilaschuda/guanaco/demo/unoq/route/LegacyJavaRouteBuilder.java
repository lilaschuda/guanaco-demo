package io.github.lilaschuda.guanaco.demo.unoq.route;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;

/**
 * A traditional Apache Camel Java Fluent DSL route.
 * Demonstrates how legacy fluent pipelines can consume outputs directly
 * from modern Guanaco processors inside the same engine context.
 */
public class LegacyJavaRouteBuilder extends RouteBuilder {

    @Override
    public void configure() throws Exception {
        from("jserialcomm:/dev/ttyHS1?baud=115200")
                .routeId("uart-byte-framer")
                // 1. Use the Splitter EIP to intercept the continuous byte stream
                // 2. Stream the tokens immediately without buffering the infinite port
                .split().tokenize("\n").streaming()
                    .unmarshal().json(JsonLibrary.Jackson, ArduinoEvent.class)
                    .to("direct:coprocessor-events")
                .end(); // Close the Splitter EIP
    }
}