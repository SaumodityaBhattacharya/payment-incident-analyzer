package com.saumoditya.payment_incident_analyzer.service;

import com.saumoditya.payment_incident_analyzer.dto.IncidentAnalysisRequest;
import com.saumoditya.payment_incident_analyzer.dto.IncidentAnalysisResponse;

    public interface IncidentAnalyzer {

        IncidentAnalysisResponse analyze(IncidentAnalysisRequest request);

    }

