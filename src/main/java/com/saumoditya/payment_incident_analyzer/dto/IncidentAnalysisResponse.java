package com.saumoditya.payment_incident_analyzer.dto;
import java.util.List;
public record IncidentAnalysisResponse(
        IncidentCategory category,
        IncidentSeverity severity,
        String summary,
        List<String> recommendedActions,
        boolean requiresHumanReview
) {
}
