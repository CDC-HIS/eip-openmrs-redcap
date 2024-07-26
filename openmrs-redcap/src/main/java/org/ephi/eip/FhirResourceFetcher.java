package org.ephi.eip;

import org.hl7.fhir.r4.model.Base;
import org.hl7.fhir.r4.model.Resource;

import java.lang.reflect.Method;

public class FhirResourceFetcher {

    public static <T> T fetchValue(Resource resource, String fieldPath, T defaultValue) {
        try {
            String[] fields = fieldPath.split("\\.");
            Base current = resource;

            for (String field : fields) {
                Method method = current.getClass().getMethod("get" + capitalize(field));
                current = (Base) method.invoke(current);
                if (current == null) {
                    return defaultValue;
                }
            }
            return (T) current;
        } catch (Exception e) {
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
