package com.saumoditya.payment_incident_analyzer.service;

import com.saumoditya.payment_incident_analyzer.dto.IncidentAnalysisRequest;
import com.saumoditya.payment_incident_analyzer.dto.IncidentAnalysisResponse;
import com.saumoditya.payment_incident_analyzer.dto.IncidentCategory;
import com.saumoditya.payment_incident_analyzer.dto.IncidentSeverity;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import com.saumoditya.payment_incident_analyzer.exception.AIServiceException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ClaudeIncidentAnalyzerTest {

    @Test
    void shouldConvertModelJsonIntoStructuredResponse() {
        String modelJson = """
                {
                  "category": "DATABASE_FAILURE",
                  "severity": "CRITICAL",
                  "summary": "The payment database is unavailable.",
                  "recommendedActions": [
                    "Check database connectivity",
                    "Inspect database logs",
                    "Notify the database team"
                  ],
                  "requiresHumanReview": true
                }
                """;

        ChatModel fakeChatModel = prompt ->
                new ChatResponse(
                        List.of(
                                new Generation(
                                        new AssistantMessage(modelJson)
                                )
                        )
                );

        ChatClient.Builder chatClientBuilder =
                ChatClient.builder(fakeChatModel);

        ClaudeIncidentAnalyzer analyzer =
                new ClaudeIncidentAnalyzer(chatClientBuilder);

        IncidentAnalysisResponse response = analyzer.analyze(
                new IncidentAnalysisRequest(
                        "Payment transactions are failing because the database is unavailable"
                )
        );

        assertEquals(IncidentCategory.DATABASE_FAILURE, response.category());
        assertEquals(IncidentSeverity.CRITICAL, response.severity());
        assertEquals(3, response.recommendedActions().size());
        assertTrue(response.requiresHumanReview());
    }
}