package org.ephi.eip;

import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Observation;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FhirResourceFetcherTest {

    @DisplayName("Should return value for valid field path")
    @Test
    void shouldReturnsValueForValidFieldPath() {
        Observation observation = new Observation();
        observation.setId("testId");

        String result = FhirResourceFetcher.fetchValue(observation, "id", "defaultId");

        assertEquals("testId", result);
    }

    @DisplayName("Should return default value for invalid field path")
    @Test
    void shouldReturnsDefaultValueForInvalidFieldPath() {
        Observation observation = new Observation();
        String result = FhirResourceFetcher.fetchValue(observation, "invalidField", "defaultValue");
        assertEquals("defaultValue", result);
    }

    @DisplayName("Should return default value for null resource")
    @Test
    void shouldReturnsDefaultValueForNullResource() {
        String result = FhirResourceFetcher.fetchValue(null, "id", "defaultId");
        assertEquals("defaultId", result);
    }

    @DisplayName("Should return default value for null field path")
    @Test
    void shouldReturnsDefaultValueForNullFieldPath() {
        Observation observation = new Observation();

        // Act
        String result = FhirResourceFetcher.fetchValue(observation, null, "defaultId");

        // verify
        assertEquals("defaultId", result);
    }

    @DisplayName("Should return default value for empty field path")
    @Test
    void shouldReturnsDefaultValueForEmptyFieldPath() {
        Observation observation = new Observation();
        // Act
        String result = FhirResourceFetcher.fetchValue(observation, "", "defaultId");
        // verify
        assertEquals("defaultId", result);
    }

    @DisplayName("Should return default value for empty field path")
    @Test
    @Disabled
    void shouldHandlesNestedFields() {
        Observation observation = new Observation();
        IdType id = new IdType("nestedValue");
        observation.setIdElement(id);

        String result = FhirResourceFetcher.fetchValue(observation, "id.value", "defaultValue");

        assertEquals("nestedValue", result);
    }
}
