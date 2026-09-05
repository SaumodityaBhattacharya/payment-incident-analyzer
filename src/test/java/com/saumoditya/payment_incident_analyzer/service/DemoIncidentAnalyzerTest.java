package com.saumoditya.payment_incident_analyzer.service;

import com.saumoditya.payment_incident_analyzer.dto.IncidentAnalysisRequest;
import com.saumoditya.payment_incident_analyzer.dto.IncidentAnalysisResponse;
import com.saumoditya.payment_incident_analyzer.dto.IncidentCategory;
import com.saumoditya.payment_incident_analyzer.dto.IncidentSeverity;
import org.jetbrains.annotations.TestOnly;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DemoIncidentAnalyzerTest {
    @Test
    void shouldReturnDemoIncidentAnalysis() {
        DemoIncidentAnalyzer analyzer = new DemoIncidentAnalyzer();

        IncidentAnalysisRequest request =
                new IncidentAnalysisRequest(
                        "NEFT transactions are stuck in processing"
                );

        IncidentAnalysisResponse response = analyzer.analyze(request);

        assertEquals(
                IncidentCategory.DOWNSTREAM_SERVICE_FAILURE,
                response.category()
        );
        assertEquals(IncidentSeverity.HIGH, response.severity());
        assertEquals(3, response.recommendedActions().size());
        assertTrue(response.requiresHumanReview());
    }


}
