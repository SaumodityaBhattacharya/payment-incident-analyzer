package com.saumoditya.payment_incident_analyzer.service;

import com.saumoditya.payment_incident_analyzer.dto.IncidentAnalysisRequest;
import com.saumoditya.payment_incident_analyzer.dto.IncidentAnalysisResponse;
import com.saumoditya.payment_incident_analyzer.dto.IncidentCategory;
import com.saumoditya.payment_incident_analyzer.dto.IncidentSeverity;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class IncidentAnalysisService {
    public IncidentAnalysisResponse analyze(IncidentAnalysisRequest request){
        return new IncidentAnalysisResponse(
                IncidentCategory.DOWNSTREAM_SERVICE_FAILURE,
                IncidentSeverity.HIGH,
                "The downstream payment service is currently unavailable.",
                List.of(
                        "Check downstream service connectivity",
                        "Inspect queued transactions",
                        "Notify the payment operations team"
                ),
                true
        );
    }
}
