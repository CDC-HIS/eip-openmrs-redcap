package org.ephi.eip.routes;

import lombok.extern.slf4j.Slf4j;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.ephi.eip.filters.HIVFollowUpEncounterFilter;
import org.ephi.eip.processors.HIVFollowUpEncounterProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class FollowUpEncounterListenerRoute extends RouteBuilder {

    @Autowired
    private HIVFollowUpEncounterFilter followUpEncounterFilter;

    @Autowired
    private HIVFollowUpEncounterProcessor followUpEncounterProcessor;

    @Override
    public void configure() {
        from("direct:fhir-encounter")
            .routeId("openmrs-redcap-encounter-router")
            .log(LoggingLevel.DEBUG, "Received encounter")
            .filter(followUpEncounterFilter)
            .process(followUpEncounterProcessor)
            .log(LoggingLevel.INFO, "Sentinel events posted to REDCap successfully").end();
    }
}
