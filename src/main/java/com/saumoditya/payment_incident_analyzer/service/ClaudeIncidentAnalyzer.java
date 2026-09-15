package com.saumoditya.payment_incident_analyzer.service;

import com.saumoditya.payment_incident_analyzer.dto.IncidentAnalysisRequest;
import com.saumoditya.payment_incident_analyzer.dto.IncidentAnalysisResponse;
import com.saumoditya.payment_incident_analyzer.exception.AIServiceException;
import jakarta.validation.Validator;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(
        name = "app.ai.provider",
        havingValue = "claude"
)
public class ClaudeIncidentAnalyzer implements IncidentAnalyzer {

    private static final String SYSTEM_PROMPT = """
            You are a payment-operations incident analyst.

            Classify the supplied payment incident using the available
            category and severity values.

            Provide a concise factual summary and between three and five
            practical recommended actions.

            Set requiresHumanReview to true for HIGH or CRITICAL incidents,
            or whenever there may be financial, security, or customer impact.

            Do not claim that an investigation or corrective action has
            already been performed.
            """;

    private final ChatClient chatClient;
    private final Validator validator;

    public ClaudeIncidentAnalyzer(
            ChatClient.Builder chatClientBuilder,
            Validator validator
    ) {
        this.chatClient = chatClientBuilder
                .defaultSystem(SYSTEM_PROMPT)
                .build();

        this.validator = validator;
    }

    @Override
    public IncidentAnalysisResponse analyze(IncidentAnalysisRequest request) {
        try {
            IncidentAnalysisResponse response = chatClient
                    .prompt()
                    .user(user -> user
                            .text("""
                                    Analyze this payment incident:

                                    <incident>
                                    {incident}
                                    </incident>
                                    """)
                            .param("incident", request.incident())
                    )
                    .call()
                    .entity(IncidentAnalysisResponse.class);

            if (response == null) {
                throw new AIServiceException(
                        "AI service returned an empty incident analysis"
                );
            }

            if (!validator.validate(response).isEmpty()) {
                throw new AIServiceException(
                        "AI service returned an invalid incident analysis"
                );
            }

            return response;

        } catch (AIServiceException exception) {
            throw exception;

        } catch (RuntimeException exception) {
            throw new AIServiceException(
                    "Unable to analyze the incident using the AI service",
                    exception
            );
        }
    }
}