package io.github.lilaschuda.guanaco.demo.unoq.route;

import io.github.lilaschuda.guanaco.api.GuanacoRoute;
import io.github.lilaschuda.guanaco.api.Processor;
import io.github.lilaschuda.guanaco.api.RouteOutcome;
import org.apache.camel.Exchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@GuanacoRoute(name = "DigitalStatePipeline")
public class DigitalStateProcessor implements Processor<DigitalStateOutcome<?>> {
    private static final Logger log = LoggerFactory.getLogger(DigitalStateProcessor.class);

    @Override
    public DigitalStateOutcome<?> process(Exchange exchange) throws Exception {
        ArduinoEvent.PinChange state = exchange.getMessage().getBody(ArduinoEvent.PinChange.class);
        
        log.info("Physical State Settled -> Pin: D{}, Active: {}", state.pin(), state.state());
        
        // Publish the clean state to the ActiveMQ broker
        return new ChangedState(state);
    }
}

// --- The Sealed Outcome Hierarchy ---
sealed interface DigitalStateOutcome<T> extends RouteOutcome<T> permits ChangedState {}

record ChangedState(ArduinoEvent.PinChange body) implements DigitalStateOutcome<ArduinoEvent.PinChange> {}