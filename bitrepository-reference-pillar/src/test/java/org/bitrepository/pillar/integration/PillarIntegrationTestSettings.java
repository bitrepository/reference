package org.bitrepository.pillar.integration;

import java.io.IOException;
import java.util.Properties;
import org.bitrepository.protocol.fileexchange.HttpServerConfiguration;

public class PillarIntegrationTestSettings {

    private final Properties properties = new Properties();

    public PillarIntegrationTestSettings(String propertiesFilePath) throws IOException {
        loadProperties(propertiesFilePath);
    }

    public HttpServerConfiguration getHttpServerConfig() {
        HttpServerConfiguration config = new HttpServerConfiguration();
        int portNumber =
                Integer.parseInt(properties.getProperty("pillar.integrationtest.httpserver.port", "80"));
        config.setPortNumber(portNumber);
        config.setHttpServerName(properties.getProperty("pillar.integrationtest.httpserver.name"));
        config.setHttpServerPath(properties.getProperty("pillar.integrationtest.httpserver.path"));
        return config;
    }

    private void loadProperties(String propertiesFilePath) throws IOException {
        properties.load(Thread.currentThread().getContextClassLoader()
                .getResourceAsStream(propertiesFilePath));
    }

    public boolean useEmbeddedPillar() {
        return properties.getProperty("pillar.integrationtest.useembeddedpillar", "false").equals("true");
    }
}
