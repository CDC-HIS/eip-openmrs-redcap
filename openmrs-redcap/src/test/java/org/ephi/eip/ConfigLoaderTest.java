package org.ephi.eip;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ConfigLoaderTest {

    private static final String BASE_CONFIG_PATH = "config/redcap_config.json";

    private static final String OVERRIDE_CONFIG_PATH = "config/redcap_config_override.json";

    @BeforeEach
    void setUp() {
        // Reset any static mocks if necessary
    }

    @Test
    @Disabled
    void shouldLoadRedcapConfigWithOverride() throws IOException {
        JsonNode baseConfig = mock(JsonNode.class);
        JsonNode overrideConfig = mock(JsonNode.class);
        JsonNode mergedConfig = mock(JsonNode.class);

        try (MockedStatic<ConfigLoader> mockedConfigLoader = mockStatic(ConfigLoader.class)) {
            mockedConfigLoader.when(() -> ConfigLoader.loadConfigFile(BASE_CONFIG_PATH)).thenReturn(baseConfig);
            mockedConfigLoader.when(() -> ConfigLoader.loadConfigFile(OVERRIDE_CONFIG_PATH)).thenReturn(overrideConfig);
            mockedConfigLoader.when(() -> ConfigLoader.mergeConfigs(baseConfig, overrideConfig)).thenReturn(mergedConfig);

            JsonNode result = ConfigLoader.loadRedcapConfig();

            assertEquals(mergedConfig, result);
        }
    }

    @Test
    @Disabled
    void shouldLoadRedcapConfigWithoutOverride() throws IOException {
        JsonNode baseConfig = mock(JsonNode.class);

        try (MockedStatic<ConfigLoader> mockedConfigLoader = mockStatic(ConfigLoader.class)) {
            mockedConfigLoader.when(() -> ConfigLoader.loadConfigFile(BASE_CONFIG_PATH)).thenReturn(baseConfig);
            mockedConfigLoader.when(() -> ConfigLoader.loadConfigFile(OVERRIDE_CONFIG_PATH)).thenThrow(new FileNotFoundException());
            mockedConfigLoader.when(() -> ConfigLoader.mergeConfigs(baseConfig, null)).thenReturn(baseConfig);

            JsonNode result = ConfigLoader.loadRedcapConfig();

            assertEquals(baseConfig, result);
        }
    }

    @Test
    @Disabled
    void shouldLoadConfigFileFromFileSystem() throws IOException {
        JsonNode config = mock(JsonNode.class);
        String configPath = "some/file/system/path.json";

        try (MockedStatic<Files> mockedFiles = mockStatic(Files.class);
             MockedStatic<ConfigLoader> mockedConfigLoader = mockStatic(ConfigLoader.class)) {
            mockedConfigLoader.when(() -> ConfigLoader.isFileSystemPath(configPath)).thenReturn(true);
            mockedFiles.when(() -> Files.newInputStream(Paths.get(configPath))).thenReturn(mock(InputStream.class));
            //mockedConfigLoader.when(() -> ConfigLoader.objectMapper.readTree(any(InputStream.class))).thenReturn(config);

            JsonNode result = ConfigLoader.loadConfigFile(configPath);

            assertEquals(config, result);
        }
    }

    @Test
    void shouldMergeConfigsWithOverride() {
        ObjectNode baseConfig = mock(ObjectNode.class);
        JsonNode overrideConfig = mock(JsonNode.class);
        JsonNode overrideField = mock(JsonNode.class);

        when(overrideConfig.fields()).thenReturn(Collections.singletonMap("key", overrideField).entrySet().iterator());

        JsonNode result = ConfigLoader.mergeConfigs(baseConfig, overrideConfig);

        verify(baseConfig).set("key", overrideField);
        assertEquals(baseConfig, result);
    }

    @Test
    void shouldMergeConfigsWithoutOverride() {
        JsonNode baseConfig = mock(JsonNode.class);

        JsonNode result = ConfigLoader.mergeConfigs(baseConfig, null);

        assertEquals(baseConfig, result);
    }
}
