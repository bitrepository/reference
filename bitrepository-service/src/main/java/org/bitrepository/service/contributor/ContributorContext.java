/*
 * #%L
 * Bitrepository Core
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
package org.bitrepository.service.contributor;

import org.bitrepository.common.settings.Settings;
import org.bitrepository.service.AlarmDispatcher;

/**
 * The context for the contributor mediator.
 */
public class ContributorContext {
    private final ResponseDispatcher responseDispatcher;
    private final AlarmDispatcher alarmDispatcher;
    private final Settings settings;

    public ContributorContext(ResponseDispatcher responseDispatcher, AlarmDispatcher alarmDispatcher, Settings settings) {
        this.responseDispatcher = responseDispatcher; this.alarmDispatcher = alarmDispatcher;

        this.settings = settings;
    }

    public ResponseDispatcher getResponseDispatcher() {
        return responseDispatcher;
    }

    public AlarmDispatcher getAlarmDispatcher() {
        return alarmDispatcher;
    }

    public Settings getSettings() {
        return settings;
    }
}
