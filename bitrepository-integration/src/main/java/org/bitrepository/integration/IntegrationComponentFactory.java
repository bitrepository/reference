package org.bitrepository.integration;

import org.bitrepository.common.ConfigurationFactory;
import org.bitrepository.common.ModuleCharacteristics;
import org.bitrepository.integration.configuration.integrationconfiguration.IntegrationConfiguration;
import org.bitrepository.pillar.PillarSettings;
import org.bitrepository.pillar.ReferencePillar;
import org.bitrepository.protocol.messagebus.MessageBus;
import org.bitrepository.protocol.messagebus.MessageBusFactory;

/**
 * Component factory for this module.
 */
public class IntegrationComponentFactory {
    /** The singleton instance. */
    private static IntegrationComponentFactory instance;

    /**
     * Instantiation of this singleton.
     *
     * @return The singleton instance of this factory class.
     */
    public static IntegrationComponentFactory getInstance() {
        // ensure singleton.
        if(instance == null) {
            instance = new IntegrationComponentFactory();
        }
        return instance;
    }

    /** The characteristics for this module.*/
    private ModuleCharacteristics moduleCharacter;
    /** The configuration for this module.*/
    private IntegrationConfiguration config;

    /**
     * Private constructor for initialization of the singleton.
     */
    private IntegrationComponentFactory() {
        moduleCharacter = new ModuleCharacteristics("integration-client");
    }

    /**
     * Method for retrieving the characteristics for this module.
     * @return The characteristics for this module.
     */
    public ModuleCharacteristics getModuleCharacteristics() {
        return moduleCharacter;
    }

    /**
     * Method for extracting the configuration for the integration module.
     * @return The integration module configuration.
     */
    public IntegrationConfiguration getConfig() {
        if (config == null) {
            ConfigurationFactory configurationFactory = new ConfigurationFactory();
            config = configurationFactory.loadConfiguration(getModuleCharacteristics(), 
                    IntegrationConfiguration.class);
        }
        return config;
    }

    /**
     * Method for retrieving a reference pillar.
     * @param settings The settings for the pillar.
     * @return The reference requested pillar.
     */
    public ReferencePillar getPillar(PillarSettings settings) {
        return new ReferencePillar(
                MessageBusFactory.createMessageBus(settings.getMessageBusConfiguration()), settings);
    }
}
