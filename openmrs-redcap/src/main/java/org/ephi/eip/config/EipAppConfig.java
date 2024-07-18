package org.ephi.eip.config;

import org.openmrs.eip.fhir.spring.OpenmrsFhirAppConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Import the {@link OpenmrsFhirAppConfig} class to ensure that the required beans are created.
 */
@Configuration
@Import({OpenmrsFhirAppConfig.class})
public class EipAppConfig {}
