#include <ArduinoJson.h>

// Bypass the RPC layer and target the raw internal hardware interconnect
#define HOST_UART Serial2

// Target the physical onboard button
const int BUTTON_PIN = USER_BTN;
const int ANALOG_PIN = A0;

boolean lastButtonState = LOW;
unsigned long lastTelemetryTime = 0;

void setup() {
  HOST_UART.begin(115200);
  
  // Standard input to respect the hardware pull-down resistor (Active-HIGH)
  pinMode(BUTTON_PIN, INPUT);
}

void loop() {
  unsigned long currentMillis = millis();

  // 1. Digital State Trigger (Event-Driven)
  // Evaluate against HIGH since the STM32 user button is wired with a pull-down
  boolean currentButtonState = (digitalRead(BUTTON_PIN) == HIGH);
  
  if (currentButtonState != lastButtonState) {
    lastButtonState = currentButtonState;
    
    JsonDocument doc;
    doc["@type"] = "PinChange";
    doc["pin"] = BUTTON_PIN;
    doc["state"] = currentButtonState;
    doc["timestampMs"] = currentMillis;
    
    serializeJson(doc, HOST_UART);
    HOST_UART.println();
    
    // Brief software debounce
    delay(50);
  }

  // 2. Analog Telemetry Stream (Throttled to 10Hz)
  if (currentMillis - lastTelemetryTime >= 100) {
    lastTelemetryTime = currentMillis;
    
    int rawVal = analogRead(ANALOG_PIN);
    double voltage = (rawVal / 1023.0) * 3.3;
    
    JsonDocument doc;
    doc["@type"] = "AnalogSample";
    doc["channel"] = 0;
    doc["rawValue"] = rawVal;
    doc["voltage"] = voltage;
    
    serializeJson(doc, HOST_UART);
    HOST_UART.println();
  }
}
