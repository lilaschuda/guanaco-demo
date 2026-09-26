package io.github.lilaschuda.guanaco.demo.unoq.route;

import io.github.lilaschuda.guanaco.api.RouteOutcome;

sealed interface ArduinoRouteOutcome<T> extends RouteOutcome<T> permits 
    IgnoreSample, RecordSample, StatePublished, HardwareFault {}

record IgnoreSample(Void body) implements ArduinoRouteOutcome<Void> {}
record RecordSample(ArduinoEvent.AnalogSample body) implements ArduinoRouteOutcome<ArduinoEvent.AnalogSample> {}
record StatePublished(ArduinoEvent.PinChange body) implements ArduinoRouteOutcome<ArduinoEvent.PinChange> {}
record HardwareFault(ArduinoEvent.Fault body) implements ArduinoRouteOutcome<ArduinoEvent.Fault> {}
