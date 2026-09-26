package io.github.lilaschuda.guanaco.demo.unoq.route;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "@type")
@JsonSubTypes({
    @JsonSubTypes.Type(value = ArduinoEvent.PinChange.class, name = "PinChange"),
    @JsonSubTypes.Type(value = ArduinoEvent.AnalogSample.class, name = "AnalogSample"),
    @JsonSubTypes.Type(value = ArduinoEvent.Fault.class, name = "Fault")
})
public sealed interface ArduinoEvent permits 
    ArduinoEvent.PinChange, 
    ArduinoEvent.AnalogSample, 
    ArduinoEvent.Fault {

    // Added 'long seq' to match the C++ payload
    record PinChange(int pin, boolean state, long timestampMs, long seq) implements ArduinoEvent {}
    record AnalogSample(int channel, int rawValue, double voltage) implements ArduinoEvent {}
    record Fault(int code, String message) implements ArduinoEvent {}
}