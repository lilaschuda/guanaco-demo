//package io.github.lilaschuda.guanaco.demo.unoq.route;
//
//import com.fasterxml.jackson.core.JsonProcessingException;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import io.github.lilaschuda.guanaco.api.GuanacoRoute;
//import io.github.lilaschuda.guanaco.api.Processor;
//import io.github.lilaschuda.guanaco.api.RouteOutcome;
//import org.apache.camel.Exchange;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//@GuanacoRoute
//public class CoprocessorIngressProcessor implements Processor<ArduinoRouteOutcome<?>> {
//    
//    private static final Logger log = LoggerFactory.getLogger(CoprocessorIngressProcessor.class);
//    private final ObjectMapper mapper = new ObjectMapper();
//
//    @Override
//    public ArduinoRouteOutcome<?> process(Exchange exchange) throws Exception {
//        String rawJson = exchange.getIn().getBody(String.class);
//        
//        ArduinoEvent event;
//        try {
//            event = mapper.readValue(rawJson, ArduinoEvent.class);
//        } catch (Exception e) { // <-- Catch ALL exceptions to prevent ErrorHandler jams
//            log.warn("Discarding malformed UART fragment: {}", rawJson);
//            return new ArduinoRouteOutcome.HardwareFault(
//                new ArduinoEvent.Fault(400, "Parse error: " + e.getMessage())
//            );
//        }
//
//        return switch (event) {
//            case ArduinoEvent.AnalogSample a -> new ArduinoRouteOutcome.TelemetrySampled(a);
//            case ArduinoEvent.PinChange p -> {
//                exchange.getMessage().setHeader("hardwareSeq", p.seq());
//                yield new ArduinoRouteOutcome.StateUpdated(p);
//            }
//            case ArduinoEvent.Fault f -> new ArduinoRouteOutcome.HardwareFault(f);
//        };
//    }
//}
//
//// --- The Demultiplexer Routing Contract ---
//sealed interface ArduinoRouteOutcome<T> extends RouteOutcome<T> permits 
//    ArduinoRouteOutcome.TelemetrySampled, 
//    ArduinoRouteOutcome.StateUpdated, 
//    ArduinoRouteOutcome.HardwareFault {
//
//    record TelemetrySampled(ArduinoEvent.AnalogSample body) implements ArduinoRouteOutcome<ArduinoEvent.AnalogSample> {}
//    record StateUpdated(ArduinoEvent.PinChange body) implements ArduinoRouteOutcome<ArduinoEvent.PinChange> {}
//    record HardwareFault(ArduinoEvent.Fault body) implements ArduinoRouteOutcome<ArduinoEvent.Fault> {}
//}