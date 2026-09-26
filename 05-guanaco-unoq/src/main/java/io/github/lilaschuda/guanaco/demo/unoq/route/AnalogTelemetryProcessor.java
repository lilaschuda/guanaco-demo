package io.github.lilaschuda.guanaco.demo.unoq.route;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.lilaschuda.guanaco.api.GuanacoRoute;
import io.github.lilaschuda.guanaco.api.Processor;
import org.apache.camel.Exchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@GuanacoRoute
public class AnalogTelemetryProcessor implements Processor<ArduinoRouteOutcome<?>> {
    
    private static final Logger log = LoggerFactory.getLogger(AnalogTelemetryProcessor.class);
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public ArduinoRouteOutcome<?> process(Exchange exchange) throws Exception {
        String rawJson = exchange.getIn().getBody(String.class);
        ArduinoEvent.AnalogSample sample;
        try {
            sample = mapper.readValue(rawJson, ArduinoEvent.AnalogSample.class);
        } catch (Exception e) {
            log.warn("Discarding malformed analog frame: {}", rawJson);
            return new HardwareFault(
                new ArduinoEvent.Fault(400, "Malformed analog payload")
            );
        }
        return new RecordSample(sample);
    }
}