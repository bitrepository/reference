/*
 * #%L
 * Bitmagasin integrationstest
 * 
 * $Id$
 * $HeadURL$
 * %%
 * Copyright (C) 2010 The State and University Library, The Royal Library and The State Archives, Denmark
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

import org.bitrepository.integration.IntegrationComponentFactory;
import org.bitrepository.integration.configuration.integrationconfiguration.IntegrationConfiguration;
import org.bitrepository.protocol.ProtocolComponentFactory;
import org.bitrepository.protocol.configuration.ProtocolConfiguration;

public class ReferencePillarLauncher {

    /**
     * @param args
     */
    public static void main(String[] args) {
        // Retrieve the configurations.
        IntegrationConfiguration iConf = IntegrationComponentFactory.getInstance().getConfig();
        ProtocolConfiguration pConf = ProtocolComponentFactory.getInstance().getProtocolConfiguration();

        MutablePillarSettings settings = new MutablePillarSettings();
        settings.setMessageBusConfiguration(pConf.getMessageBusConfigurations());
        settings.setBitRepositoryCollectionID(iConf.getBitrepositoryCollectionId());
        settings.setBitRepositoryCollectionTopicID(iConf.getBitrepositoryCollectionTopicId());
        settings.setFileDirName(iConf.getFiledir());
        settings.setLocalQueue(iConf.getPillarId());
        settings.setPillarId(iConf.getPillarId());
        
        // TODO use settings instead.
        settings.setTimeToDownloadMeasure("MILLISECONDS");
        settings.setTimeToDownloadValue(1L);
        settings.setTimeToUploadMeasure("MILLISECONDS");
        settings.setTimeToUploadValue(1L);

        // START THE REFERENCE PILLAR!!!!
        IntegrationComponentFactory.getInstance().getPillar(settings);
        
    }

}
