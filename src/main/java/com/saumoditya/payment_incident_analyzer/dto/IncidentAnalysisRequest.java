package com.saumoditya.payment_incident_analyzer.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record IncidentAnalysisRequest(
        @NotBlank(message = "Incident description is required")
        @Size(
                min = 10,
                max = 2000,
                message = "Incident description must contain between 10 and 2000 characters"
        )
        String incident

) {
}