package com.saumoditya.payment_incident_analyzer.controller;

import com.saumoditya.payment_incident_analyzer.dto.IncidentAnalysisResponse;
import com.saumoditya.payment_incident_analyzer.dto.IncidentCategory;
import com.saumoditya.payment_incident_analyzer.dto.IncidentSeverity;
import com.saumoditya.payment_incident_analyzer.service.IncidentAnalyzer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.Mockito.verifyNoInteractions;

@WebMvcTest(IncidentAnalysisController.class)
class IncidentAnalysisControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private IncidentAnalyzer incidentAnalyzer;
    @Test
    void shouldReturnBadRequestForInvalidIncident() throws Exception {
        mockMvc.perform(
                        post("/api/incidents/analyze")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                    {
                                      "incident": "Failed"
                                    }
                                    """)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error")
                        .value("Request validation failed"))
                .andExpect(jsonPath("$.fieldErrors.incident")
                        .value(
                                "Incident description must contain between 10 and 2000 characters"
                        ));
        verifyNoInteractions(incidentAnalyzer);
    }

    @Test
    void shouldReturnAnalysisForValidRequest() throws Exception {
        IncidentAnalysisResponse response =
                new IncidentAnalysisResponse(
                        IncidentCategory.DOWNSTREAM_SERVICE_FAILURE,
                        IncidentSeverity.HIGH,
                        "The downstream service is unavailable.",
                        List.of(
                                "Check connectivity",
                                "Inspect queued transactions",
                                "Notify operations"
                        ),
                        true
                );

        when(incidentAnalyzer.analyze(any()))
                .thenReturn(response);

        mockMvc.perform(
                        post("/api/incidents/analyze")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "incident": "NEFT transactions are stuck in processing"
                                        }
                                        """)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.category")
                        .value("DOWNSTREAM_SERVICE_FAILURE"))
                .andExpect(jsonPath("$.severity").value("HIGH"))
                .andExpect(jsonPath("$.requiresHumanReview").value(true));
        //verifyNoInteractions(incidentAnalyzer);
    }
}