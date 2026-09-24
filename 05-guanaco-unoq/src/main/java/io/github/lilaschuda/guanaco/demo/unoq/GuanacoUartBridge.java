package io.github.lilaschuda.guanaco.demo.unoq;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fazecast.jSerialComm.SerialPort;
import io.github.lilaschuda.guanaco.context.GuanacoContext;
import io.github.lilaschuda.guanaco.demo.unoq.route.ArduinoEvent;
import org.apache.camel.ProducerTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Scanner;

public class GuanacoUartBridge {
    private static final Logger log = LoggerFactory.getLogger(GuanacoUartBridge.class);

    public static void start(GuanacoContext context) {
        ProducerTemplate producer = context.createProducerTemplate();
        ObjectMapper mapper = new ObjectMapper();

        Thread bridgeThread = new Thread(() -> {
            SerialPort port = SerialPort.getCommPort("/dev/ttyHS1");
            port.setBaudRate(115200);
            port.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 0, 0);

            if (!port.openPort()) {
                log.error("Failed to open internal interconnect /dev/ttyHS1");
                return;
            }
            log.info("Opened raw UART connection to STM32 Coprocessor.");

            try (Scanner scanner = new Scanner(port.getInputStream())) {
                while (scanner.hasNextLine()) {
                    String line = scanner.nextLine();
                    if (line.isBlank()) continue;

                    try {
                        // 1. Instantly type-safe deserialization via Jackson
                        ArduinoEvent event = mapper.readValue(line, ArduinoEvent.class);
                        
                        // 2. Dispatch to the Guanaco routing context
                        producer.sendBody("direct:coprocessor-events", event);
                        
                    } catch (Exception e) {
                        log.warn("Dropped malformed UART frame: {}", e.getMessage());
                    }
                }
            }
        });
        
        bridgeThread.setName("uart-ingress-daemon");
        bridgeThread.setDaemon(true); // Allows the JVM to shut down gracefully
        bridgeThread.start();
    }
}