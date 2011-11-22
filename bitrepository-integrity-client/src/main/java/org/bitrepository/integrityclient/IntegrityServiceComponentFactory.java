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

import org.bitrepository.access.AccessComponentFactory;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.integrityclient.cache.CachedIntegrityInformationStorage;
import org.bitrepository.integrityclient.cache.MemoryBasedIntegrityCache;
import org.bitrepository.integrityclient.checking.IntegrityChecker;
import org.bitrepository.integrityclient.checking.SystematicIntegrityValidator;
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
    private CachedIntegrityInformationStorage cachedIntegrityInformationStorage;
    /** The integrity checker. */
    private IntegrityChecker integrityChecker;

    /**
     * Gets you an <code>IntegrityInformationScheduler</code> that schedules integrity information collection.
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
     * @return an <code>IntegrityInformationCollector</code> that collects integrity information.
     */
    public IntegrityInformationCollector getIntegrityInformationCollector(MessageBus messageBus, Settings settings) {
        if (integrityInformationCollector == null) {
            integrityInformationCollector = new DelegatingIntegrityInformationCollector(
                    getCachedIntegrityInformationStorage(),
                    AccessComponentFactory.getInstance().createGetFileIDsClient(settings),
                    AccessComponentFactory.getInstance().createGetChecksumsClient(settings));
        }
        return integrityInformationCollector;
    }
    
    /**
     * Gets you an <code>IntegrityChecker</code> the can perform the integrity checks.
     * @param settings The settings for this instance. 
     * @return An <code>IntegrityChecker</code> the can perform the integrity checks.
     */
    public IntegrityChecker getIntegrityChecker(Settings settings) {
        if(integrityChecker == null) {
            integrityChecker = new SystematicIntegrityValidator(settings, getCachedIntegrityInformationStorage());
        }
        return integrityChecker;
    }

    /**
     * Gets you a <code>CachedIntegrityInformationStorage</code> that collects integrity information.
     * @return an <code>CachedIntegrityInformationStorage</code> that collects integrity information.
     */
    public CachedIntegrityInformationStorage getCachedIntegrityInformationStorage() {
        if (cachedIntegrityInformationStorage == null) {
            cachedIntegrityInformationStorage = MemoryBasedIntegrityCache.getInstance();
        }
        return cachedIntegrityInformationStorage;
    }
}
