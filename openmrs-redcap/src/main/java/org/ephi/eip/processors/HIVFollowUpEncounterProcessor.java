package org.ephi.eip.processors;

import ca.uhn.fhir.rest.client.api.IGenericClient;
import com.fasterxml.jackson.databind.JsonNode;
import io.netty.util.internal.ThreadLocalRandom;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.ephi.eip.ConfigLoader;
import org.ephi.eip.Utils;
import org.ephi.eip.config.RedcapClient;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Encounter;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Patient;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Slf4j
@Component
public class HIVFollowUpEncounterProcessor implements Processor {

    @Value("${eip.openmrs.HIVCaseReportSerialIdentifierTypeUuid}")
    private String identifierTypeUuid;

    @Autowired
    private RedcapClient redcapClient;

    @Override
    public void process(Exchange exchange) throws IOException {
        Encounter encounter = exchange.getMessage().getBody(Encounter.class);
        IGenericClient client = Utils.getOpenmrsFhirClient(exchange);
        // Process the follow-up encounter
        if (encounter != null) {
            // Read the patient associated with the encounter
            String patientUuid = encounter.getSubject().getReference().split("/")[1];
            Patient patient = client.read()
                    .resource(Patient.class)
                    .withId(patientUuid)
                    .execute();
            // Has REDCap data been collected for this patient?
            if (hasBeenRecordedInRedcap(patient)) {
                exchange.setProperty("followUpEncounter", encounter);

                JsonNode redcapConfig = ConfigLoader.loadRedcapConfig();
                JSONObject data = new JSONObject();
                List<String> codes = new ArrayList<>();
                redcapConfig.get("sentinel_events").forEach(event -> {
                    String code = event.get("data").get("code").asText();
                    codes.add(code);
                });

                // Fetch the most recent observations for the patient
                List<Observation> recentObservations = fetchMostRecentObservations(client, patient, codes);
                data.put("record_id", getHIVCaseReportSerialNumberPatientIdentifier(patient));
                data.put("redcap_repeat_instance", generateRedcapRepeatInstance(encounter));
                data.put("redcap_repeat_instrument", redcapConfig.get("redcap_repeat_instrument").asText());
                data.put("lcbs_visit_date", Utils.formatDate(encounter.getPeriod().getStart().toInstant()));

                redcapConfig.get("sentinel_events").forEach(event -> {
                    String key = event.get("key").asText();
                    String code = event.get("data").get("code").asText();
                    String dataType = event.get("data").get("dataType").asText();
                    boolean hasMapping = event.get("data").has("dataMapping");

                    recentObservations.forEach(observation -> {
                        if (observation.getCode().getCoding().stream().anyMatch(coding -> coding.getCode().equals(code))) {
                            Object value = extractObservationValue(observation, dataType);
                            if (hasMapping) {
                                event.get("data").get("dataMapping").forEach(mapping -> {
                                    String mappingCode = mapping.get("code").asText();
                                    String mappingValue = mapping.get("value").asText();
                                    assert value != null;
                                    if (value.equals(mappingCode)) {
                                        data.put(key, mappingValue);
                                    }
                                });
                            } else {
                                data.put(key, value);
                            }
                        }
                    });
                });

                log.info("Data: {}", data);
                // Post the data to REDCap
                redcapClient.post(data);
            }
        }
    }

    private boolean hasBeenRecordedInRedcap(Patient patient) {
        if (identifierTypeUuid.isEmpty() || identifierTypeUuid.isBlank()) {
            throw new IllegalStateException("The identifierTypeUuid property is not set");
        }
        if (patient.hasIdentifier()) {
            return patient.getIdentifier()
                    .stream()
                    .anyMatch(identifier -> identifier.getType().getCoding()
                            .stream()
                            .anyMatch(coding -> coding.getCode().equals(identifierTypeUuid)));
        }
        return false;
    }

    private String getHIVCaseReportSerialNumberPatientIdentifier(Patient patient) {
        return patient.getIdentifier()
                .stream()
                .filter(identifier -> identifier.getType().getCoding()
                        .stream()
                        .anyMatch(coding -> coding.getCode().equals(identifierTypeUuid)))
                .findFirst()
                .map(Identifier::getValue)
                .orElse(null);
    }

    private int generateRedcapRepeatInstance(Encounter encounter) {
        LocalDate startDate = LocalDate.of(2019, 1, 1);
        LocalDate encounterDate = encounter.getPeriod().getStart().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        long daysBetween = ChronoUnit.DAYS.between(startDate, encounterDate);
        return (int) daysBetween + ThreadLocalRandom.current().nextInt(1, 100);
    }

    List<Observation> fetchMostRecentObservations(IGenericClient client, Patient patient, List<String> codeableConcepts) {
        List<Observation> recentObservations = new ArrayList<>();
        for (String codeableConcept : codeableConcepts) {
            Bundle bundle = client.search()
                    .forResource(Observation.class)
                    .where(Observation.SUBJECT.hasId(patient.getIdElement().getIdPart()))
                    .and(Observation.CODE.exactly().code(codeableConcept))
                    .returnBundle(Bundle.class)
                    .execute();

            bundle.getEntry().stream()
                    .map(entry -> (Observation) entry.getResource())
                    .max(Comparator.comparing(o -> o.getEffectiveDateTimeType().getValue()))
                    .ifPresent(recentObservations::add);
        }

        return recentObservations;
    }

    private Object extractObservationValue(Observation observation, String dataType) {
        return switch (dataType) {
            case "Text" -> observation.getValueStringType().getValue();
            case "Numeric" -> observation.getValueQuantity().getValue().doubleValue();
            case "Boolean" -> observation.getValueBooleanType().getValue();
            case "Date" -> Utils.formatDate(observation.getValueDateTimeType().getValue().toInstant());
            case "Coded" -> observation.getValueCodeableConcept().getCodingFirstRep().getDisplay();
            case "IsNotEmpty" -> {
                if (observation.hasValue()) {
                    yield 1;
                } else {
                    yield 0;
                }
            }
            default -> null;
        };
    }
}
