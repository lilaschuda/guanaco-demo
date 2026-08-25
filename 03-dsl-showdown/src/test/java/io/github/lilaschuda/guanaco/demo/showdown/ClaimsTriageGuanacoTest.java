package io.github.lilaschuda.guanaco.demo.showdown;

import io.github.lilaschuda.guanaco.config.BindingTarget;
import io.github.lilaschuda.guanaco.config.GuanacoConfig.ValidationMode;
import io.github.lilaschuda.guanaco.testutils.GuanacoRuntimeEnvironment;
import io.github.lilaschuda.guanaco.testutils.GuanacoTestSupport;
import java.util.List;
import org.apache.camel.component.mock.MockEndpoint;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

public class ClaimsTriageGuanacoTest {

    private GuanacoTestSupport testSupport;
    private GuanacoRuntimeEnvironment env;

    // A sample domain model representing our incoming payload
    record ClaimPayload(String claimId, String category, double amount, boolean highFraudRisk) {}

    @BeforeEach
    void setUp() {
        // Spin up the test support targeting the demo application package
        testSupport = new GuanacoTestSupport("io.github.lilaschuda.guanaco.demo.showdown")
                .withValidation(ValidationMode.STRICT);
    }

    @AfterEach
    void tearDown() {
        if (env != null) {
            env.shutdown();
        }
    }

    @Test
    void emea_autoClaim_routesToSpeedlane() throws Exception {
        // Wire up a programmatic route configuration for the showdown validation
        BindingTarget bt1 = new BindingTarget();
        bt1.setUri("mock:emea-auto-speedlane");
        List<BindingTarget> btl1 = List.of(bt1);
        BindingTarget bt2 = new BindingTarget();
        bt2.setUri("mock:global-triage");
        List<BindingTarget> btl2 = List.of(bt2);
        testSupport.route("ClaimsProcessor", "direct:claims-in", Map.of(
                "ToEmeaAutoSpeedlane", btl1,
                "ToGlobalTriage", btl2
        ));

        env = testSupport.start();

        MockEndpoint speedlaneMock = env.getMock("mock:emea-auto-speedlane");
        MockEndpoint triageMock = env.getMock("mock:global-triage");

        speedlaneMock.expectedMessageCount(1);
        triageMock.expectedMessageCount(0);

        // Scenario: EMEA claim, vehicle/auto category
        ClaimPayload payload = new ClaimPayload("CL-101", "VEHICLE", 1200.00, false);
        
        env.send("direct:claims-in", payload, Map.of("X-Region", "EMEA"));

        speedlaneMock.assertIsSatisfied();
        triageMock.assertIsSatisfied();
    }

    @Test
    void amer_highFraudRisk_routesToFraudHold() throws Exception {
        BindingTarget bt1 = new BindingTarget();
        bt1.setUri("mock:amer-fraud-hold");
        List<BindingTarget> btl1 = List.of(bt1);
        BindingTarget bt2 = new BindingTarget();
        bt2.setUri("mock:amer-general");
        List<BindingTarget> btl2 = List.of(bt2);
        testSupport.route("ClaimsProcessor", "direct:claims-in", Map.of(
                "ToAmerFraudHold", btl1,
                "ToAmerGeneral", btl2
        ));

        env = testSupport.start();

        MockEndpoint fraudHoldMock = env.getMock("mock:amer-fraud-hold");
        MockEndpoint generalMock = env.getMock("mock:amer-general");

        fraudHoldMock.expectedMessageCount(1);
        generalMock.expectedMessageCount(0);

        // Scenario: AMER claim with fraud metrics flagged
        ClaimPayload payload = new ClaimPayload("CL-102", "GENERAL", 4500.00, true);

        env.send("direct:claims-in", payload, Map.of("X-Region", "AMER"));

        fraudHoldMock.assertIsSatisfied();
        generalMock.assertIsSatisfied();
    }

    @Test
    void emea_highValueGeneralClaim_routesToEmeaGeneralHighValue() throws Exception {
        BindingTarget bt1 = new BindingTarget();
        bt1.setUri("mock:emea-high-value");
        List<BindingTarget> btl1 = List.of(bt1);
        BindingTarget bt2 = new BindingTarget();
        bt2.setUri("mock:emea-archive");
        List<BindingTarget> btl2 = List.of(bt2);
        testSupport.route("ClaimsProcessor", "direct:claims-in", Map.of(
                "ToEmeaGeneralHighValue", btl1,
                "ToEmeaArchive", btl2
        ));

        env = testSupport.start();

        MockEndpoint highValueMock = env.getMock("mock:emea-high-value");
        MockEndpoint archiveMock = env.getMock("mock:emea-archive");

        highValueMock.expectedMessageCount(1);
        archiveMock.expectedMessageCount(0);

        // Scenario: EMEA claim, non-vehicle, exceeding high-value threshold (e.g., > $50,000)
        ClaimPayload payload = new ClaimPayload("CL-103", "PROPERTY", 75000.00, false);

        env.send("direct:claims-in", payload, Map.of("X-Region", "EMEA"));

        highValueMock.assertIsSatisfied();
        archiveMock.assertIsSatisfied();
    }

    @Test
    void unknownRegion_routesToGlobalTriageFallback() throws Exception {
        BindingTarget bt = new BindingTarget();
        bt.setUri("mock:global-fallback");
        List<BindingTarget> btl = List.of(bt);
        testSupport.route("ClaimsProcessor", "direct:claims-in", Map.of(
                "ToGlobalTriage", btl
        ));

        env = testSupport.start();

        MockEndpoint fallbackMock = env.getMock("mock:global-fallback");
        fallbackMock.expectedMessageCount(1);

        // Scenario: Missing or unrecognized region header
        ClaimPayload payload = new ClaimPayload("CL-104", "GENERAL", 500.00, false);

        env.send("direct:claims-in", payload, Map.of("X-Region", "APAC"));

        fallbackMock.assertIsSatisfied();
    }
}