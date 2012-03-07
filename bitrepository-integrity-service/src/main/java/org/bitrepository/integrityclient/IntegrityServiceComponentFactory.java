/*
 * #%L
 * Bitrepository Protocol
 * *
 * $Id$
 * $HeadURL$
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
package org.bitrepository.integrityclient;

import org.bitrepository.access.getchecksums.GetChecksumsClient;
import org.bitrepository.access.getfileids.GetFileIDsClient;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.integrityclient.cache.IntegrityDatabase;
import org.bitrepository.integrityclient.cache.IntegrityModel;
import org.bitrepository.integrityclient.checking.IntegrityChecker;
import org.bitrepository.integrityclient.checking.SimpleIntegrityChecker;
import org.bitrepository.integrityclient.collector.DelegatingIntegrityInformationCollector;
import org.bitrepository.integrityclient.collector.IntegrityInformationCollector;
import org.bitrepository.integrityclient.scheduler.IntegrityInformationScheduler;
import org.bitrepository.integrityclient.scheduler.TimerIntegrityInformationScheduler;
import org.bitrepository.protocol.messagebus.MessageBus;

/**
 * Provides access to the different component in the integrity module (Spring/IOC wannabe)
 */
public final class IntegrityServiceComponentFactory {

    //---------------------Singleton-------------------------
    private static IntegrityServiceComponentFactory instance;

    /**
     * The singletonic access to the instance of this class
     * @return The one and only instance
     */
    public static synchronized IntegrityServiceComponentFactory getInstance() {
        if (instance == null) {
            instance = new IntegrityServiceComponentFactory();
        }
        return instance;
    }

    /**
     * The singleton constructor
     */
    private IntegrityServiceComponentFactory() {
    }

    // --------------------- Components-----------------------
    /** The integrity information scheduler. */
    private IntegrityInformationScheduler integrityInformationScheduler;
    /** The integrity information collector. */
    private IntegrityInformationCollector integrityInformationCollector;
    /** The integrity information collector. */
    private IntegrityModel cachedIntegrityInformationStorage;
    /** The integrity checker. */
    private IntegrityChecker integrityChecker;

    /**
     * Gets you an <code>IntegrityInformationScheduler</code> that schedules integrity information collection.
     * @param settings The settings for the information scheduler.
     * @return an <code>IntegrityInformationScheduler</code> that schedules integrity information collection.
     */
    public IntegrityInformationScheduler getIntegrityInformationScheduler(Settings settings) {
        if (integrityInformationScheduler == null) {
            integrityInformationScheduler = new TimerIntegrityInformationScheduler(settings);
        }
        return integrityInformationScheduler;
    }

    /**
     * Gets you an <code>IntegrityInformationCollector</code> that collects integrity information.
     * @param cache The cache for storing the collected results.
     * @param checker For checking the results of the collected data.
     * @param getFileIDsClient The client for performing the collecting of the file ids.
     * @param getChecksumsClient The client for performing the collecting of the checksums.
     * @param settings The settings for the integrity information collector. 
     * @param messageBus The messagebus for the communication.
     * @return an <code>IntegrityInformationCollector</code> that collects integrity information.
     */
    public IntegrityInformationCollector getIntegrityInformationCollector(IntegrityModel cache, 
            IntegrityChecker checker, GetFileIDsClient getFileIDsClient, GetChecksumsClient getChecksumsClient, 
            Settings settings, MessageBus messageBus) {
        if (integrityInformationCollector == null) {
            integrityInformationCollector = new DelegatingIntegrityInformationCollector(cache, checker, 
                    getFileIDsClient, getChecksumsClient, settings, messageBus);
        }
        return integrityInformationCollector;
    }
    
    /**
     * Gets you an <code>IntegrityChecker</code> the can perform the integrity checks.
     * @param settings The settings for this instance. 
     * @param cache The cache for the integrity system.
     * @return An <code>IntegrityChecker</code> the can perform the integrity checks.
     */
    public IntegrityChecker getIntegrityChecker(Settings settings, IntegrityModel cache) {
        if(integrityChecker == null) {
            integrityChecker = new SimpleIntegrityChecker(settings, cache);
        }
        return integrityChecker;
    }

    /**
     * Gets you a <code>CachedIntegrityInformationStorage</code> that collects integrity information.
     * TODO implement the database based integrity cache.
     * @return an <code>CachedIntegrityInformationStorage</code> that collects integrity information.
     */
    public IntegrityModel getCachedIntegrityInformationStorage(Settings settings) {
        if (cachedIntegrityInformationStorage == null) {
            cachedIntegrityInformationStorage = new IntegrityDatabase(settings);
        }
        return cachedIntegrityInformationStorage;
    }
}
