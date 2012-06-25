/*
* #%L
* Bitrepository Protocol
*
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

package org.bitrepository.service;

import org.bitrepository.common.settings.SettingsLoader;
import org.bitrepository.common.settings.SettingsProvider;
import org.bitrepository.settings.referencesettings.ReferenceSettings;

public class ServiceSettingsProvider extends SettingsProvider {
    private ServiceType serviceType;

    /**
     * Creates a <code>SettingsProvider</code> which will use the provided <code>SettingsLoader</code> for loading the
     * settings.
     *
     * @param settingsReader Use for loading the settings.
     * @param serviceType Will be used to load a componentID for the service based on the type.
     */
    public ServiceSettingsProvider(SettingsLoader settingsReader, ServiceType serviceType) {
        super(settingsReader, null);
        this.serviceType = serviceType;
    }

    protected String getComponentID(ReferenceSettings referenceSettings) {
        switch (serviceType) {
            case AlarmService:
                return referenceSettings.getAlarmServiceSettings().getID();
            case AuditTrailService:
                return referenceSettings.getAuditTrailServiceSettings().getID();
            case IntegrityService:
                return referenceSettings.getIntegrityServiceSettings().getID();
            case MonitoringService:
                return referenceSettings.getMonitoringServiceSettings().getID();
            default:
                throw new IllegalArgumentException("Unknown service type");
        }
    }
}
