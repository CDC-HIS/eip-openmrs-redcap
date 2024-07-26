package org.ephi.eip.filters;

import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.Predicate;
import org.hl7.fhir.r4.model.Encounter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class HIVFollowUpEncounterFilter implements Predicate {

    @Value("${eip.openmrs.followUpEncounterTypeUuid}")
    private String encounterTypeUuid;

    @Override
    public boolean matches(Exchange exchange) {
        if (encounterTypeUuid.isEmpty() || encounterTypeUuid.isBlank()) {
            throw new IllegalStateException("The followUpEncounterTypeUuid property is not set");
        }
        Encounter encounter = exchange.getMessage().getBody(Encounter.class);
        if (encounter != null) {
            encounter.getType().forEach(type -> type.getCoding().forEach(coding -> {
                if (encounterTypeUuid.equals(coding.getCode())) {
                    log.info("Found follow-up encounter: {}", encounter.getId());
                    exchange.setProperty("followUpEncounter", encounter);
                }
            }));
        }
        return exchange.getProperty("followUpEncounter") != null;
    }
}
