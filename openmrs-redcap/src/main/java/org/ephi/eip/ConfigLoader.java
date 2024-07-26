package org.ephi.eip;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.Map;

@Slf4j
public class ConfigLoader {

    static final ObjectMapper objectMapper = new ObjectMapper();
    private static final String BASE_CONFIG_PATH = "config/redcap_config.json";
    private static final String OVERRIDE_CONFIG_PATH = "config/redcap_config_override.json";

    public static JsonNode loadRedcapConfig() throws IOException {
        JsonNode baseConfig = loadConfigFile(BASE_CONFIG_PATH);
        JsonNode overrideConfig = null;

        try {
            overrideConfig = loadConfigFile(OVERRIDE_CONFIG_PATH);
        } catch (FileNotFoundException e) {
            log.warn("No override configuration file found. Using base configuration only.");
        }

        return mergeConfigs(baseConfig, overrideConfig);
    }

    static JsonNode loadConfigFile(String configPath) throws IOException {
        if (isFileSystemPath(configPath)) {
            return objectMapper.readTree(Files.newInputStream(Paths.get(configPath)));
        } else {
            return objectMapper.readTree(new ClassPathResource(configPath).getInputStream());
        }
    }

    static boolean isFileSystemPath(String path) {
        return new File(path).exists();
    }

    static JsonNode mergeConfigs(JsonNode baseConfig, JsonNode overrideConfig) {
        if (overrideConfig == null) {
            return baseConfig;
        }

        Iterator<Map.Entry<String, JsonNode>> fields = overrideConfig.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> field = fields.next();
            ((ObjectNode) baseConfig).set(field.getKey(), field.getValue());
        }
        return baseConfig;
    }
}
