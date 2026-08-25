package io.github.lilaschuda.guanaco.demo.resiliencypipeline;

import io.github.lilaschuda.guanaco.core.GuanacoContext;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.component.jms.JmsComponent;
import org.springframework.context.support.StaticApplicationContext;

public class Application {

    public static void main(String[] args) throws Exception {
        GuanacoContext ctx = new GuanacoContext(Application.class.getPackageName());
        StaticApplicationContext sac = new StaticApplicationContext();
        ctx.setApplicationContext(sac);
        
        ctx.registerAggregationStrategy("mergeStrategy", (oldExchange, newExchange) -> {
            if (oldExchange == null) return newExchange;
            String oldBody = oldExchange.getIn().getBody(String.class);
            String newBody = newExchange.getIn().getBody(String.class);
            newExchange.getIn().setBody(oldBody + "," + newBody);
            return newExchange;
        });

        ctx.registerDelayStrategy("backoffStrategy", exchange -> {
            int attempt = exchange.getIn().getHeader("retryCount", 0, Integer.class);
            long delay = Math.min(200L * (1L << attempt), 2000L);
            System.out.println(">>> backoffStrategy: retryCount=" + attempt + " -> delaying " + delay + "ms");
            return delay;
        });
        
        ctx.addRoutes(new FlakyPartnerRoute());
        ctx.wireRoutes();        
        ctx.start();
                
        ProducerTemplate producer = ctx.createProducerTemplate();

        System.out.println("\n=== Phase 1: Idempotent -> Resequence -> Aggregate ===");
        System.out.println("Sending 4 messages: one duplicate, arriving out of order.");
        System.out.println("Expect exactly one merged log line: A,B,C\n");

        producer.send("direct:intake", e -> {
            e.getIn().setHeader("messageId", "m1");
            e.getIn().setHeader("orderId", "order-1");
            e.getIn().setHeader("sequenceNumber", 2);
            e.getIn().setBody("B");
        });
        producer.send("direct:intake", e -> {
            e.getIn().setHeader("messageId", "m2");
            e.getIn().setHeader("orderId", "order-1");
            e.getIn().setHeader("sequenceNumber", 1);
            e.getIn().setBody("A");
        });
        producer.send("direct:intake", e -> { // duplicate of the first message
            e.getIn().setHeader("messageId", "m1");
            e.getIn().setHeader("orderId", "order-1");
            e.getIn().setHeader("sequenceNumber", 2);
            e.getIn().setBody("B");
        });
        producer.send("direct:intake", e -> {
            e.getIn().setHeader("messageId", "m3");
            e.getIn().setHeader("orderId", "order-1");
            e.getIn().setHeader("sequenceNumber", 3);
            e.getIn().setBody("C");
        });

        Thread.sleep(500);

        System.out.println("\n=== Phase 2: Throttle -> Delay -> Circuit Breaker ===");
        System.out.println("Partner is UP. Sending 3 calls with increasing retryCount (watch the backoff grow).\n");

        for (int i = 0; i < 3; i++) {
            sendToPartner(producer, "order-" + i, i);
        }

        Thread.sleep(500);

        System.out.println("\nPartner goes DOWN. Sending 6 calls — expect failures, then the circuit opens" +
                " and starts rejecting immediately without even reaching the partner.\n");
        FlakyPartnerRoute.isUp.set(false);

        for (int i = 0; i < 6; i++) {
            sendToPartner(producer, "failing-" + i, 0);
        }

        System.out.println("\nPartner recovers. Waiting for the circuit breaker's open-state timeout before probing again...\n");
        FlakyPartnerRoute.isUp.set(true);

        // NOTE: exact wait time here depends on whether
        // waitDurationInOpenStateMs gets added to GuanacoCircuitBreakerConfig
        // (see conversation) — currently relies on Resilience4j's 60s default.
        Thread.sleep(65_000);

        System.out.println("\nProbing again after recovery — expect success.\n");
        sendToPartner(producer, "recovered", 0);

        ctx.stop();
    }

    private static void sendToPartner(ProducerTemplate producer, String label, int retryCount) {
        Exchange result = producer.send("direct:partnerOrders", e -> {
            e.getIn().setHeader("retryCount", retryCount);
            e.getIn().setBody(label);
        });

        if (result.getException() != null) {
            System.out.println("[" + label + "] REJECTED: "
                    + result.getException().getClass().getSimpleName() + " - " + result.getException().getMessage());
        } else {
            System.out.println("[" + label + "] delivered.");
        }
    }

}