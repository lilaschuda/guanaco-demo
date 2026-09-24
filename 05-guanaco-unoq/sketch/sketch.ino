#define HOST_UART Serial2

const int BUTTON_PIN = 2;
const int ANALOG_PIN = A0;

// 1. The Native C++ Ring Buffer
const int QUEUE_SIZE = 16;
struct DigitalEvent {
  boolean state;
  unsigned long timestamp;
  unsigned long seq;
};

volatile DigitalEvent eventQueue[QUEUE_SIZE];
volatile int head = 0;
volatile int tail = 0;

// 2. Hybrid State
volatile boolean reportedState = HIGH;
volatile unsigned long lastTransitionTime = 0;
volatile unsigned long digitalSeq = 0;

unsigned long lastTelemetryTime = 0;

// 3. Pre-allocated Static Buffer (Zero Heap Allocation)
char txBuffer[128];

void queueEvent(boolean state, unsigned long timeMs) {
  int nextHead = (head + 1) % QUEUE_SIZE;
  if (nextHead != tail) {
    eventQueue[head].state = state;
    eventQueue[head].timestamp = timeMs;
    eventQueue[head].seq = ++digitalSeq;
    head = nextHead;
  }
}

void hwInterrupt() {
  unsigned long now = millis();
  boolean physicalPin = digitalRead(BUTTON_PIN);

  if (physicalPin != reportedState) {
    if (now - lastTransitionTime > 10) {
      reportedState = physicalPin;
      lastTransitionTime = now;
      queueEvent(reportedState, now);
    }
  }
}

void setup() {
  HOST_UART.begin(115200);
  pinMode(BUTTON_PIN, INPUT_PULLUP);

  reportedState = digitalRead(BUTTON_PIN);
  attachInterrupt(digitalPinToInterrupt(BUTTON_PIN), hwInterrupt, CHANGE);
}

void loop() {
  unsigned long currentMillis = millis();

  // 4. The Self-Healing Sweeper
  boolean physicalPin = digitalRead(BUTTON_PIN);
  if (physicalPin != reportedState) {
    if (currentMillis - lastTransitionTime > 10) {
      noInterrupts();
      if (digitalRead(BUTTON_PIN) != reportedState) {
        reportedState = digitalRead(BUTTON_PIN);
        lastTransitionTime = currentMillis;
        queueEvent(reportedState, currentMillis);
      }
      interrupts();
    }
  }

  // 5. Drain the Queue when the CPU is free
  if (head != tail) {
    noInterrupts();
    DigitalEvent evt;
    evt.state = eventQueue[tail].state;
    evt.timestamp = eventQueue[tail].timestamp;
    evt.seq = eventQueue[tail].seq;
    tail = (tail + 1) % QUEUE_SIZE;
    interrupts();

    // Fast, heap-free string formatting
    snprintf(txBuffer, sizeof(txBuffer),
             "{\"@type\":\"PinChange\",\"pin\":%d,\"state\":%s,\"timestampMs\":%lu,\"seq\":%lu}",
             BUTTON_PIN, evt.state == LOW ? "true" : "false", evt.timestamp, evt.seq);

    HOST_UART.println(txBuffer);
  }

  // 6. Analog Telemetry Stream (10Hz)
  if (currentMillis - lastTelemetryTime >= 100) {
    lastTelemetryTime = currentMillis;

    int rawVal = analogRead(ANALOG_PIN);
    double voltage = (rawVal / 1023.0) * 3.3;

    // Manual float formatting bypasses missing %f support on some bare-metal compilers
    int v_int = (int)voltage;
    int v_frac = (int)((voltage - v_int) * 100);

    snprintf(txBuffer, sizeof(txBuffer),
             "{\"@type\":\"AnalogSample\",\"channel\":0,\"rawValue\":%d,\"voltage\":%d.%02d}",
             rawVal, v_int, v_frac);

    HOST_UART.println(txBuffer);
  }
}
