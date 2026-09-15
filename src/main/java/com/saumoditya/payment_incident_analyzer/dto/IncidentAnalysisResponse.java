package com.saumoditya.payment_incident_analyzer.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record IncidentAnalysisResponse(
        @NotNull(message = "Category is required")
        IncidentCategory category,

        @NotNull(message = "Severity is required")
        IncidentSeverity severity,

        @NotBlank(message = "Summary must not be blank")
        String summary,

        @NotNull(message = "Recommended actions are required")
        @Size(
                min = 3,
                max = 5,
                message = "Provide between three and five recommended actions"
        )
        List<@NotBlank(message = "Recommended action must not be blank")
                String> recommendedActions,

        @NotNull(message = "Human-review decision is required")
        Boolean requiresHumanReview
) {
}