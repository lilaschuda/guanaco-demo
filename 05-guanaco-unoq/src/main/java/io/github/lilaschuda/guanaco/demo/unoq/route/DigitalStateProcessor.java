package io.github.lilaschuda.guanaco.demo.unoq.route;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.lilaschuda.guanaco.api.GuanacoRoute;
import io.github.lilaschuda.guanaco.api.Processor;
import org.apache.camel.Exchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@GuanacoRoute
public class DigitalStateProcessor implements Processor<ArduinoRouteOutcome<?>> {
    
    private static final Logger log = LoggerFactory.getLogger(DigitalStateProcessor.class);
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public ArduinoRouteOutcome<?> process(Exchange exchange) throws Exception {
        String rawJson = exchange.getIn().getBody(String.class);
        ArduinoEvent.PinChange pinChange;
        try {
            pinChange = mapper.readValue(rawJson, ArduinoEvent.PinChange.class);
        } catch (Exception e) {
            log.warn("Discarding malformed digital frame: {}", rawJson);
            return new HardwareFault(
                new ArduinoEvent.Fault(400, "Malformed digital payload")
            );
        }
        return new StatePublished(pinChange);
    }
    
}