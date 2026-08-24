/*
 * #%L
 * Bitrepository Protocol
 *
 * $Id: DefaultFixturePillarTest.java 452 2011-11-10 09:59:11Z mss $
 * $HeadURL: https://sbforge.org/svn/bitrepository/bitrepository-reference/trunk/bitrepository-reference-pillar/src/test/java/org/bitrepository/pillar/DefaultFixturePillarTest.java $
 * %%
 * Copyright (C) 2010 - 2011 The State and University Library, The Royal Library and The State Archives, Denmark
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 2.1 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 *
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */
package org.bitrepository.pillar;

import jakarta.jms.JMSException;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.pillar.integration.ArtemisFixedPortContainer;
import org.bitrepository.protocol.IntegrationTest;
import org.bitrepository.protocol.activemq.ActiveMQMessageBus;
import org.bitrepository.protocol.bus.MessageReceiver;
import org.bitrepository.protocol.messagebus.MessageBusManager;
import org.bitrepository.settings.repositorysettings.Collection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.activemq.ArtemisContainer;
import org.testcontainers.containers.InternetProtocol;
import org.testcontainers.junit.jupiter.Container;

import java.util.List;

public abstract class DefaultFixturePillarIT extends IntegrationTest {
    private static final Logger log = LoggerFactory.getLogger(DefaultFixturePillarIT.class);
    protected static String pillarDestinationId;

    protected String clientDestinationId;
    protected MessageReceiver clientReceiver;

    @Container
    static ArtemisContainer activemq = new ArtemisFixedPortContainer("apache/artemis:2.55.0")
                                            .withFixedExposedPort(9999, 61616, InternetProtocol.TCP)
                                            .withEnv("ANONYMOUS_LOGIN","true");

    /**
     * Replaces the pillarID references in the settings will test specific pillarIDs.
     */
    @Override
    protected Settings loadSettings(String componentID) {
        Settings settings = super.loadSettings(componentID);
        updateSettingsWithSpecificPillarID(settings, componentID);
        return settings;
    }

    @Override
    protected void registerMessageReceivers() {
        super.registerMessageReceivers();

        clientDestinationId = settingsForTestClient.getReceiverDestinationID();
        clientReceiver = new MessageReceiver(clientDestinationId);
        addReceiver(clientReceiver);

        pillarDestinationId = settingsForCUT.getContributorDestinationID();
    }

    @Override
    protected void setupMessageBus() {
        activemq.start();
        while (!activemq.isRunning()) {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        messageBus = new ActiveMQMessageBus(settingsForTestClient, securityManager);
        MessageBusManager.clear();
        MessageBusManager.injectCustomMessageBus(MessageBusManager.DEFAULT_MESSAGE_BUS, messageBus);
    }

    /**
     * Shutdown the message bus.
     */
    @Override
    protected void teardownMessageBus() {
        MessageBusManager.clear();
        if (messageBus != null) {
            try {
                messageBus.setComponentFilter(List.of());
                messageBus.setCollectionFilter(List.of());

                messageBus.close();
                messageBus = null;
            } catch (JMSException e) {
                log.warn("Failed to close messageBus", e);
            }
        }
    }

    protected String getPillarID() {
        return getComponentID();
    }

    /** The default pillar id in the settings to replace.*/
    private static final String DEFAULT_PILLAR_ID_TO_REPLACE = "Pillar1";

    /**
     * Sets the given id to be the pillar id, also in the collections.
     * @param settings The settings.
     * @param pillarID The new pillar id.
     */
    private void updateSettingsWithSpecificPillarID(Settings settings, String pillarID) {
        settings.getReferenceSettings().getPillarSettings().setPillarID(pillarID);
        for(Collection collection : settings.getRepositorySettings().getCollections().getCollection()) {
            if(collection.getPillarIDs().getPillarID().remove(DEFAULT_PILLAR_ID_TO_REPLACE)) {
                collection.getPillarIDs().getPillarID().add(pillarID);
            }
        }
    }


}
