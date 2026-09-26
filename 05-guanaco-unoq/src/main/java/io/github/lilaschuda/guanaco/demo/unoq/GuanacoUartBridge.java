package io.github.lilaschuda.guanaco.demo.unoq;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fazecast.jSerialComm.SerialPort;
import io.github.lilaschuda.guanaco.context.GuanacoContext;
import io.github.lilaschuda.guanaco.demo.unoq.route.ArduinoEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import org.apache.camel.ProducerTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GuanacoUartBridge {

    private static final Logger log = LoggerFactory.getLogger(GuanacoUartBridge.class);

    public static void start(GuanacoContext context) {
        ProducerTemplate producer = context.createProducerTemplate();
        ObjectMapper mapper = new ObjectMapper();

        Thread bridgeThread = new Thread(() -> {
            SerialPort port = SerialPort.getCommPort("/dev/ttyHS1");
            port.setComPortParameters(460800, 8, 1, SerialPort.NO_PARITY);
            port.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 0, 0);

            if (!port.openPort()) {
                log.error("Failed to open internal interconnect /dev/ttyHS1");
                return;
            }
            log.info("Opened raw UART connection to STM32 Coprocessor.");

            try (java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(port.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.isBlank()) {
                        continue;
                    }

                    try {
                        try {
                            // Demultiplex at the bare-metal edge using ultra-fast string matching
                            if (line.contains("\"PinChange\"")) {
                                producer.sendBody("seda:digital-ingress?discardWhenFull=true", line);
                            } else if (line.contains("\"AnalogSample\"")) {
                                producer.sendBody("seda:analog-ingress?discardWhenFull=true", line);
                            } else {
                                log.error("No matching type found in message!");
                            }
                        } catch (Exception e) {
                            log.error("Failed to hand off UART frame to Camel", e);
                        }
                    } catch (Exception e) {
                        // If Camel throws an endpoint or routing exception, log it safely 
                        // without killing the hardware reader loop.
                        log.error("Failed to hand off UART frame to Camel: {}", e.getMessage());
                    }
                }
            } catch (Exception e) {
                log.error("Fatal hardware connection error on /dev/ttyHS1", e);
            }
        });

        bridgeThread.setName("uart-ingress-daemon");
        bridgeThread.setDaemon(true); // Allows the JVM to shut down gracefully
        bridgeThread.start();
    }
}
