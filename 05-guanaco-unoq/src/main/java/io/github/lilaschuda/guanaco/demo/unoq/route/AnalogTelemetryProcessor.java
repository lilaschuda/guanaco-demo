package io.github.lilaschuda.guanaco.demo.unoq.route;

import io.github.lilaschuda.guanaco.api.Drop;
import io.github.lilaschuda.guanaco.api.GuanacoRoute;
import io.github.lilaschuda.guanaco.api.Processor;
import io.github.lilaschuda.guanaco.api.RouteOutcome;
import org.apache.camel.Exchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@GuanacoRoute(name = "AnalogTelemetryPipeline")
public class AnalogTelemetryProcessor implements Processor<AnalogPipelineOutcome<?>> {
    private static final Logger log = LoggerFactory.getLogger(AnalogTelemetryProcessor.class);

    @Override
    public AnalogPipelineOutcome<?> process(Exchange exchange) throws Exception {
        ArduinoEvent.AnalogSample sample = exchange.getMessage().getBody(ArduinoEvent.AnalogSample.class);
        
        log.info("Received Throttled Telemetry -> Channel: A{}, Voltage: {}V", sample.channel(), sample.voltage());
        
        // Terminate the message gracefully
        return new IgnoreSample(null);
    }
}

sealed interface AnalogPipelineOutcome<T> extends RouteOutcome<T> permits 
    IgnoreSample {}

record IgnoreSample(Void body) implements AnalogPipelineOutcome<Void> {}