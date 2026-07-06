package io.github.lilaschuda.guanaco.demo.showdown.route;

import org.apache.camel.builder.RouteBuilder;

public class LegacyFluentShowdownRoute extends RouteBuilder {
    @Override
    public void configure() throws Exception {
        from("jms:queue:fluent.claims.inbound?connectionFactory=#jmsConnectionFactory")
            .routeId("fluentClaimsPipeline")
            .choice()
                .when(header("Region").isEqualTo("EMEA"))
                    .choice()
                        .when(body().contains("HIGH_VALUE"))
                            .choice()
                                .when(header("Type").isEqualTo("AUTO"))
                                    .multicast()
                                        .to("jms:queue:emea.auto.speedlane?connectionFactory=#jmsConnectionFactory")
                                        .to("jms:queue:emea.compliance.audit?connectionFactory=#jmsConnectionFactory")
                                    .end() // Closes multicast -> back to when(AUTO) context
                                    .endChoice()
                                .otherwise()
                                    .to("jms:queue:emea.general.highvalue?connectionFactory=#jmsConnectionFactory")
                            .end().endChoice() // 1. Closes Type choice -> back to when(HIGH_VALUE) context
                        .otherwise()
                            .to("jms:queue:emea.general.archive?connectionFactory=#jmsConnectionFactory")
                    .end().endChoice() // 2. Closes Value choice -> back to when(Region==EMEA) context
                .when(header("Region").isEqualTo("AMER"))
                    .choice()
                        .when(body().contains("FRAUD_SUSPECT"))
                            .to("jms:queue:amer.fraud.hold?connectionFactory=#jmsConnectionFactory")
                            .endChoice()
                        .otherwise()
                            .to("jms:queue:amer.general?connectionFactory=#jmsConnectionFactory")
                    .end().endChoice() // 3. Closes AMER choice -> back to root choice context
                .otherwise()
                    .to("jms:queue:global.triage.fallback?connectionFactory=#jmsConnectionFactory")
            .end(); // Closes the root choice container cleanly
    }
}