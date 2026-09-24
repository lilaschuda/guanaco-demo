//package io.github.lilaschuda.guanaco.demo.unoq.route;
//
//import io.github.lilaschuda.guanaco.api.GuanacoRoute;
//import io.github.lilaschuda.guanaco.api.Processor;
//import io.github.lilaschuda.guanaco.api.RouteOutcome;
//import org.apache.camel.Exchange;
//
//@GuanacoRoute(name = "CoprocessorIngress")
//public class CoprocessorIngressRoute implements Processor<ArduinoRouteOutcome<?>> {
//
//    @Override
//    public ArduinoRouteOutcome<?> process(Exchange exchange) throws Exception {
//        // Instantly type-safe. The Camel bridge handled the byte framing and parsing.
//        ArduinoEvent event = exchange.getMessage().getBody(ArduinoEvent.class);
//
//        return switch (event) {
//            case ArduinoEvent.AnalogSample a -> new TelemetrySampled(a);
//            case ArduinoEvent.PinChange p -> new StateUpdated(p);
//            case ArduinoEvent.Fault f -> new HardwareFault(f);
//        };
//    }
//}
//
//// --- The Sealed Outcome Hierarchy ---
//sealed interface ArduinoRouteOutcome<T> extends RouteOutcome<T> 
//    permits StateUpdated, TelemetrySampled, HardwareFault {}
//
//record StateUpdated(ArduinoEvent.PinChange body) implements ArduinoRouteOutcome<ArduinoEvent.PinChange> {}
//record TelemetrySampled(ArduinoEvent.AnalogSample body) implements ArduinoRouteOutcome<ArduinoEvent.AnalogSample> {}
//record HardwareFault(ArduinoEvent.Fault body) implements ArduinoRouteOutcome<ArduinoEvent.Fault> {}