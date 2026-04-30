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
import org.bitrepository.pillar.common.ChecksumDatabaseCreator;
import org.bitrepository.pillar.common.PillarAuditTrailDatabaseCreator;
import org.bitrepository.service.database.DerbyDatabaseDestroyer;
import org.bitrepository.settings.referencesettings.DatabaseSpecifics;

/**
 * Contains functionality for maintenance of the reference pillar databases. This includes functionality for
 * creating and upgrading the databases.
 */
public class ReferencePillarDBTestUtils {
    public static final String POSTGRESQL_DRIVER = "org.postgresql.Driver";
    public static final String DERBY_EMBEDDED_DRIVER = "org.apache.derby.jdbc.EmbeddedDriver";
    private final Settings pillarSettings;

    public ReferencePillarDBTestUtils(Settings pillarSettings) {
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
        var pillarAuditTrailDBCreator = new PillarAuditTrailDatabaseCreator();

        switch (auditTrailDB.getDriverClass()) {
            case POSTGRESQL_DRIVER: {
                pillarAuditTrailDBCreator
                        .createAuditTrailContributorDatabase(pillarSettings,
                                                             "sql/postgres/auditContributorDBCreation.sql");
                break;
            }
            case DERBY_EMBEDDED_DRIVER:
            default: {
                DerbyDatabaseDestroyer.deleteDatabase(auditTrailDB);
                pillarAuditTrailDBCreator
                        .createAuditTrailContributorDatabase(pillarSettings,
                                                             "sql/derby/auditContributorDBCreation.sql");
                break;
            }
        }
        DatabaseSpecifics checksumDB =
                pillarSettings.getReferenceSettings().getPillarSettings().getChecksumDatabase();
        var checksumDBCreator = new ChecksumDatabaseCreator();
        switch (checksumDB.getDriverClass()) {
            case POSTGRESQL_DRIVER: {
                checksumDBCreator.createChecksumDatabase(pillarSettings, "sql/postgres/checksumDBCreation.sql");
                break;
            }
            case DERBY_EMBEDDED_DRIVER:
            default: {
                DerbyDatabaseDestroyer.deleteDatabase(checksumDB);
                checksumDBCreator.createChecksumDatabase(pillarSettings, "sql/derby/checksumDBCreation.sql");
                break;
            }
        }

    }
}
