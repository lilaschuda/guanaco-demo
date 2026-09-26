#define HOST_UART Serial2

const int BUTTON_PIN = 2;
const int ANALOG_PIN = A0;

// 1. Classic Debounce State
boolean currentStableState = HIGH;
boolean lastFlickerState = HIGH;
unsigned long lastDebounceTime = 0;
unsigned long digitalSeq = 0;

// 2. Analog State
unsigned long lastTelemetryTime = 0;

// 3. Paced TX Ring Buffer (Defeats Linux FIFO Overruns)
const int TX_BUFFER_SIZE = 512;
char txBuffer[TX_BUFFER_SIZE];
int txHead = 0;
int txTail = 0;
unsigned long lastTxMicros = 0;
char formatBuf[128];

// Fast memory queuing (Zero blocking)
void enqueueString(const char* str) {
  while (*str) {
    int nextHead = (txHead + 1) % TX_BUFFER_SIZE;
    if (nextHead != txTail) {
      txBuffer[txHead] = *str;
      txHead = nextHead;
    }
    str++;
  }
  // Append standard Guanaco/Camel newline boundary
  int nextHead = (txHead + 1) % TX_BUFFER_SIZE;
  if (nextHead != txTail) {
    txBuffer[txHead] = '\n';
    txHead = nextHead;
  }
}

void setup() {
  HOST_UART.begin(115200);
  pinMode(BUTTON_PIN, INPUT_PULLUP);

  currentStableState = digitalRead(BUTTON_PIN);
  lastFlickerState = currentStableState;
}

void loop() {
  unsigned long currentMillis = millis();
  unsigned long currentMicros = micros();

  // 4. The Micro-Pacer (1 byte every 500 microseconds = 2000 chars/sec)
  // This easily clears our 700 chars/sec telemetry load, but spreads the bytes
  // wide enough that the Qualcomm RX FIFO can never overflow.
  if (txHead != txTail) {
    if (currentMicros - lastTxMicros >= 500) {
      HOST_UART.write(txBuffer[txTail]);
      txTail = (txTail + 1) % TX_BUFFER_SIZE;
      lastTxMicros = currentMicros;
    }
  }

  // 5. The Trailing-Edge State Machine (Runs continuously unblocked)
  boolean reading = digitalRead(BUTTON_PIN);

  if (reading != lastFlickerState) {
    lastDebounceTime = currentMillis;
    lastFlickerState = reading;
  }

  if ((currentMillis - lastDebounceTime) > 10) {
    if (reading != currentStableState) {
      currentStableState = reading;

      snprintf(formatBuf, sizeof(formatBuf),
               "{\"@type\":\"PinChange\",\"pin\":%d,\"state\":%s,\"timestampMs\":%lu,\"seq\":%lu}",
               BUTTON_PIN, currentStableState == LOW ? "true" : "false", currentMillis, ++digitalSeq);

      enqueueString(formatBuf);
    }
  }

  // 6. Analog Telemetry Stream (10Hz)
  if (currentMillis - lastTelemetryTime >= 100) {
    lastTelemetryTime = currentMillis;

    int rawVal = analogRead(ANALOG_PIN);
    double voltage = (rawVal / 1023.0) * 3.3;

    int v_int = (int)voltage;
    int v_frac = (int)((voltage - v_int) * 100);

    snprintf(formatBuf, sizeof(formatBuf),
             "{\"@type\":\"AnalogSample\",\"channel\":0,\"rawValue\":%d,\"voltage\":%d.%02d}",
             rawVal, v_int, v_frac);

    enqueueString(formatBuf);
  }
}
