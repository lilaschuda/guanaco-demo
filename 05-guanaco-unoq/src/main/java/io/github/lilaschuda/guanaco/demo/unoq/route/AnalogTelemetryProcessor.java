package io.github.lilaschuda.guanaco.demo.unoq.route;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.lilaschuda.guanaco.api.GuanacoRoute;
import io.github.lilaschuda.guanaco.api.Processor;
import io.github.lilaschuda.guanaco.api.RouteOutcome;
import org.apache.camel.Exchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@GuanacoRoute
public class AnalogTelemetryProcessor implements Processor<AnalogPipelineOutcome<?>> {
    
    private static final Logger log = LoggerFactory.getLogger(AnalogTelemetryProcessor.class);
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public AnalogPipelineOutcome<?> process(Exchange exchange) throws Exception {
        // 1. Explicitly fetch the raw string. Do not ask Camel to auto-convert the Record.
        String rawJson = exchange.getIn().getBody(String.class);
        
        ArduinoEvent.AnalogSample sample;
        try {
            // 2. Parse the string manually
            sample = mapper.readValue(rawJson, ArduinoEvent.AnalogSample.class);
        } catch (Exception e) {
            // 3. HARD STOP on poison pills. Instantly return the fault outcome so execution halts.
            log.warn("Discarding malformed analog frame: {}", rawJson);
            return new HardwareFault(
                new ArduinoEvent.Fault(400, "Malformed analog payload")
            );
        }
        log.info("Read sampled voltage: " + sample.voltage() + " V");
        return new IgnoreSample(null);
    }
}

sealed interface AnalogPipelineOutcome<T> extends RouteOutcome<T> permits 
    IgnoreSample, HardwareFault {}

record IgnoreSample(Void body) implements AnalogPipelineOutcome<Void> {}
record HardwareFault(ArduinoEvent.Fault body) implements AnalogPipelineOutcome<ArduinoEvent.Fault> {}