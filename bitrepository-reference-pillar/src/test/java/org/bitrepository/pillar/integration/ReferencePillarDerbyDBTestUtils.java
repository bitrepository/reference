/*
 * #%L
 * Bitrepository Reference Pillar
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
package org.bitrepository.pillar.integration;

import org.bitrepository.common.settings.Settings;
import org.bitrepository.pillar.common.PillarAuditTrailDatabaseCreator;
import org.bitrepository.pillar.common.ChecksumDatabaseCreator;
import org.bitrepository.service.database.DerbyDatabaseDestroyer;
import org.bitrepository.settings.referencesettings.DatabaseSpecifics;

/**
 * Contains functionality for maintenance of the reference pillar databases. This includes functionality for
 * creating and upgrading the databases.
 */
public class ReferencePillarDerbyDBTestUtils {
    private final Settings pillarSettings;

    public ReferencePillarDerbyDBTestUtils(Settings pillarSettings) {
        this.pillarSettings = pillarSettings;
    }

    /**
     * Creates the Derby databases needed by the reference pillar,
     * as specified in the settings.
     *
     * Will also remove existing databases.
     */
    public void createEmptyDatabases() {
        DatabaseSpecifics auditTrailDB =
            pillarSettings.getReferenceSettings().getPillarSettings().getAuditTrailContributerDatabase();
        DerbyDatabaseDestroyer.deleteDatabase(auditTrailDB);
        PillarAuditTrailDatabaseCreator pillarAuditTrailDatabaseCreator =
                new PillarAuditTrailDatabaseCreator();
        pillarAuditTrailDatabaseCreator.createAuditTrailContributorDatabase(pillarSettings, null);

        DatabaseSpecifics checksumDB =
            pillarSettings.getReferenceSettings().getPillarSettings().getChecksumDatabase();
        DerbyDatabaseDestroyer.deleteDatabase(checksumDB);

        ChecksumDatabaseCreator checksumDatabaseCreator = new ChecksumDatabaseCreator();
        checksumDatabaseCreator.createChecksumDatabase(pillarSettings, null);
    }
}
