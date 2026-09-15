package com.saumoditya.payment_incident_analyzer.service;

import com.saumoditya.payment_incident_analyzer.dto.IncidentAnalysisRequest;
import com.saumoditya.payment_incident_analyzer.dto.IncidentAnalysisResponse;
import com.saumoditya.payment_incident_analyzer.dto.IncidentCategory;
import com.saumoditya.payment_incident_analyzer.dto.IncidentSeverity;
import com.saumoditya.payment_incident_analyzer.exception.AIServiceException;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ClaudeIncidentAnalyzerTest {

    private static final String VALID_MODEL_JSON = """
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

    private ValidatorFactory validatorFactory;
    private Validator validator;

    @BeforeEach
    void setUp() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @AfterEach
    void tearDown() {
        validatorFactory.close();
    }

    @Test
    void shouldConvertModelJsonIntoStructuredResponse() {
        ClaudeIncidentAnalyzer analyzer =
                analyzerReturning(VALID_MODEL_JSON);

        IncidentAnalysisResponse response =
                analyzer.analyze(validRequest());

        assertEquals(
                IncidentCategory.DATABASE_FAILURE,
                response.category()
        );
        assertEquals(
                IncidentSeverity.CRITICAL,
                response.severity()
        );
        assertEquals(
                "The payment database is unavailable.",
                response.summary()
        );
        assertEquals(3, response.recommendedActions().size());
        assertTrue(response.requiresHumanReview());
    }

    @Test
    void shouldWrapModelFailureInAiServiceException() {
        ChatModel failingChatModel = prompt -> {
            throw new RuntimeException("Provider unavailable");
        };

        ClaudeIncidentAnalyzer analyzer = new ClaudeIncidentAnalyzer(
                ChatClient.builder(failingChatModel),
                validator
        );

        AIServiceException exception = assertThrows(
                AIServiceException.class,
                () -> analyzer.analyze(validRequest())
        );

        assertEquals(
                "Unable to analyze the incident using the AI service",
                exception.getMessage()
        );
        assertNotNull(exception.getCause());
        assertEquals(
                "Provider unavailable",
                exception.getCause().getMessage()
        );
    }

    @Test
    void shouldRejectBlankSummary() {
        String invalidModelJson = VALID_MODEL_JSON.replace(
                "The payment database is unavailable.",
                "   "
        );

        assertInvalidModelResponse(invalidModelJson);
    }

    @Test
    void shouldRejectMissingCategory() {
        String invalidModelJson = VALID_MODEL_JSON.replace(
                "\"category\": \"DATABASE_FAILURE\",",
                ""
        );

        assertInvalidModelResponse(invalidModelJson);
    }

    @Test
    void shouldRejectMissingSeverity() {
        String invalidModelJson = VALID_MODEL_JSON.replace(
                "\"severity\": \"CRITICAL\",",
                ""
        );

        assertInvalidModelResponse(invalidModelJson);
    }

    @Test
    void shouldRejectMissingSummary() {
        String invalidModelJson = VALID_MODEL_JSON.replace(
                "\"summary\": \"The payment database is unavailable.\",",
                ""
        );

        assertInvalidModelResponse(invalidModelJson);
    }

    @Test
    void shouldRejectMissingHumanReviewDecision() {
        String invalidModelJson = """
                {
                  "category": "DATABASE_FAILURE",
                  "severity": "CRITICAL",
                  "summary": "The payment database is unavailable.",
                  "recommendedActions": [
                    "Check database connectivity",
                    "Inspect database logs",
                    "Notify the database team"
                  ]
                }
                """;

        assertInvalidModelResponse(invalidModelJson);
    }

    @Test
    void shouldRejectNullRecommendedActions() {
        assertInvalidModelResponse(
                modelJsonWithActions("null")
        );
    }

    @Test
    void shouldRejectTooFewRecommendedActions() {
        String actionsJson = """
                [
                  "Check connectivity",
                  "Notify operations"
                ]
                """;

        assertInvalidModelResponse(
                modelJsonWithActions(actionsJson)
        );
    }

    @Test
    void shouldRejectTooManyRecommendedActions() {
        String actionsJson = """
                [
                  "Check connectivity",
                  "Inspect logs",
                  "Notify operations",
                  "Check database health",
                  "Review queued transactions",
                  "Check failover status"
                ]
                """;

        assertInvalidModelResponse(
                modelJsonWithActions(actionsJson)
        );
    }

    @Test
    void shouldRejectBlankRecommendedAction() {
        String actionsJson = """
                [
                  "Check connectivity",
                  "   ",
                  "Notify operations"
                ]
                """;

        assertInvalidModelResponse(
                modelJsonWithActions(actionsJson)
        );
    }

    @Test
    void shouldRejectNullRecommendedAction() {
        String actionsJson = """
                [
                  "Check connectivity",
                  null,
                  "Notify operations"
                ]
                """;

        assertInvalidModelResponse(
                modelJsonWithActions(actionsJson)
        );
    }

    private ClaudeIncidentAnalyzer analyzerReturning(String modelJson) {
        ChatModel fakeChatModel = prompt -> new ChatResponse(
                List.of(
                        new Generation(
                                new AssistantMessage(modelJson)
                        )
                )
        );

        return new ClaudeIncidentAnalyzer(
                ChatClient.builder(fakeChatModel),
                validator
        );
    }

    private IncidentAnalysisRequest validRequest() {
        return new IncidentAnalysisRequest(
                "Payment transactions are failing because the database is unavailable"
        );
    }

    private void assertInvalidModelResponse(String modelJson) {
        ClaudeIncidentAnalyzer analyzer =
                analyzerReturning(modelJson);

        AIServiceException exception = assertThrows(
                AIServiceException.class,
                () -> analyzer.analyze(validRequest())
        );

        assertEquals(
                "AI service returned an invalid incident analysis",
                exception.getMessage()
        );
        assertNull(exception.getCause());
    }

    private String modelJsonWithActions(String actionsJson) {
        return """
                {
                  "category": "DATABASE_FAILURE",
                  "severity": "CRITICAL",
                  "summary": "The payment database is unavailable.",
                  "recommendedActions": %s,
                  "requiresHumanReview": true
                }
                """.formatted(actionsJson);
    }
}