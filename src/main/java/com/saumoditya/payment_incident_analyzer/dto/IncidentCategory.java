package com.saumoditya.payment_incident_analyzer.dto;

public enum IncidentCategory {
    DOWNSTREAM_SERVICE_FAILURE,
    DATABASE_FAILURE,
    NETWORK_FAILURE,
    MESSAGE_QUEUE_FAILURE,
    VALIDATION_FAILURE,
    AUTHENTICATION_FAILURE,
    CONFIGURATION_ERROR,
}
