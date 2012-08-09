package org.bitrepository.pillar.integration;


import org.bitrepository.common.settings.Settings;
import org.bitrepository.common.settings.XMLFileSettingsLoader;
import org.bitrepository.pillar.PillarSettingsProvider;
import org.bitrepository.pillar.referencepillar.ReferencePillar;
import org.bitrepository.protocol.ProtocolComponentFactory;
import org.bitrepository.protocol.messagebus.MessageBus;
import org.bitrepository.protocol.security.DummySecurityManager;

public class EmbeddedReferencePillar {

    public EmbeddedReferencePillar(String pathToReferencePillarSettings, String pillarID) {

        PillarSettingsProvider settingsLoader =
                new PillarSettingsProvider(new XMLFileSettingsLoader(pathToReferencePillarSettings), pillarID);
        Settings settings = settingsLoader.getSettings();
        ReferencePillarDerbyDBTestUtils.createEmptyDatabases(settings);
        MessageBus messageBus =
                ProtocolComponentFactory.getInstance().getMessageBus(settings, new DummySecurityManager());
        new ReferencePillar(messageBus, settings);
    }
}
