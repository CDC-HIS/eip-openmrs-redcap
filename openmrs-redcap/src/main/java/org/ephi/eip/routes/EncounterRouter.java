package org.ephi.eip.routes;

import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

@Component
public class EncounterRouter extends RouteBuilder {

    @Override
    public void configure() {
        from("direct:fhir-encounter")
            .routeId("openmrs-redcap-encounter-router")
            .log(LoggingLevel.INFO, "Received FHIR Encounter").end();
    }
}
