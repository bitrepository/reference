/*
 * #%L
 * Bitrepository Integrity Service
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
package org.bitrepository.integrityservice.reports;

/**
 * Class containing constants related to integrity reports
 */
public class IntegrityReportConstants {

    public enum ReportPart {
        DELETED_FILE {
            public String getPartName() {return "deletedFile";}

            public String getHumanString() {return "deleted files";}
        }, CHECKSUM_ISSUE {
            public String getPartName() {return "checksumIssue";}

            public String getHumanString() {return "inconsistent checksums";}
        }, MISSING_CHECKSUM {
            public String getPartName() {return "missingChecksum";}

            public String getHumanString() {return "missing checksums";}
        }, OBSOLETE_CHECKSUM {
            public String getPartName() {return "obsoleteChecksum";}

            public String getHumanString() {return "obsolete checksums";}
        }, MISSING_FILE {
            public String getPartName() {return "missingFile";}

            public String getHumanString() {return "missing files";}
        };

        public abstract String getPartName();

        public abstract String getHumanString();
    }

    public static final String REPORT_FILE = "report";

    public static final String SECTION_HEADER_START_STOP = "========";
    public static final String PILLAR_HEADER_START_STOP = "--------";
    public static final String NO_ISSUE_HEADER_START_STOP = "++++++++";
}
