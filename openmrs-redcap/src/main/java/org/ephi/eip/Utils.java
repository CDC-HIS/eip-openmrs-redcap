package org.ephi.eip;

import ca.uhn.fhir.rest.client.api.IGenericClient;
import org.apache.camel.Exchange;
import org.apache.camel.component.fhir.FhirComponent;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public class Utils {

    public static IGenericClient getOpenmrsFhirClient(Exchange exchange) {
        return exchange.getContext().getComponent("fhir", FhirComponent.class).getConfiguration().getClient();
    }

    public static String formatDate(Instant instant) {
        DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
        return OffsetDateTime.ofInstant(instant, ZoneOffset.UTC).format(outputFormatter);
    }
}
