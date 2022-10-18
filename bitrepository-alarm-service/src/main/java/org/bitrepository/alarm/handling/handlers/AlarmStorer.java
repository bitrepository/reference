/*
 * #%L
 * Bitrepository Alarm Service
 * %%
 * Copyright (C) 2010 - 2012 The State and University Library, The Royal Library and The State Archives, Denmark
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
package org.bitrepository.alarm.handling.handlers;

import org.bitrepository.alarm.handling.AlarmHandler;
import org.bitrepository.alarm.store.AlarmStore;
import org.bitrepository.bitrepositoryelements.Alarm;
import org.bitrepository.bitrepositorymessages.AlarmMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The alarm handler, which just stores all the alarms in the AlarmStore.
 */
public class AlarmStorer implements AlarmHandler {
    /**
     * The logger.
     */
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    /**
     * The store for storing the alarms.
     */
    private final AlarmStore store;

    /**
     * Constructor.
     *
     * @param store The AlarmStore to store the alarms.
     */
    public AlarmStorer(AlarmStore store) {
        this.store = store;
    }

    @Override
    public void handleAlarm(AlarmMessage message) {
        log.debug("Adding alarm from message '{}'", message);
        Alarm alarm = message.getAlarm();
        alarm.setCollectionID(message.getCollectionID());
        store.addAlarm(alarm);
    }

    @Override
    public void close() {
        log.debug("Closing the AlarmHandler '{}'", this.getClass().getCanonicalName());
        store.shutdown();
    }
}
