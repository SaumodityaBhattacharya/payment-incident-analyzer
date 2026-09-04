package com.saumoditya.payment_incident_analyzer.controller;

import com.saumoditya.payment_incident_analyzer.dto.IncidentAnalysisRequest;
import com.saumoditya.payment_incident_analyzer.dto.IncidentAnalysisResponse;
//import com.saumoditya.payment_incident_analyzer.service.IncidentAnalysisService;

import com.saumoditya.payment_incident_analyzer.service.IncidentAnalyzer;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/incidents")
public class IncidentAnalysisController {
    private final IncidentAnalyzer incidentAnalyzer;

    public IncidentAnalysisController(
            IncidentAnalyzer incidentAnalyzer
    ) {
        this.incidentAnalyzer = incidentAnalyzer;
    }

    @PostMapping("/analyze")
    public IncidentAnalysisResponse analyzeIncident(
            @Valid @RequestBody IncidentAnalysisRequest request
    ) {
        return incidentAnalyzer.analyze(request);
    }
}
