package org.ephi.eip;

import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.Base;
import org.hl7.fhir.r4.model.Resource;

import java.lang.reflect.Method;

@Slf4j
public class FhirResourceFetcher {

    public static <T> T fetchValue(Resource resource, String fieldPath, T defaultValue) {
        try {
            String[] fields = fieldPath.split("\\.");
            Base current = resource;

            for (String field : fields) {
                Method method = current.getClass().getMethod("get" + capitalize(field));
                Object result = method.invoke(current);
                if (result == null) {
                    return defaultValue;
                }
                if (result instanceof Base) {
                    current = (Base) result;
                } else if (result instanceof String) {
                    return (T) result;
                } else {
                    log.error("Expected instance of Base but got: {}", result.getClass().getName());
                    return defaultValue;
                }
            }
            if (current != null) {
                return (T) current;
            } else {
                return defaultValue;
            }
        } catch (Exception e) {
            log.error("Error fetching value", e);
            return defaultValue;
        }
    }

    private static String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
}
