package org.bitrepository.integrityservice.reports;

/**
 * Class containing constants related to integrity reports 
 */
public class IntegrityReportConstants {
    
    public enum ReportPart {
        DELETED_FILE {
            public String getPartname() { return "deletedFile"; }
            public String getHumanString() { return "deleted files"; }
        },
        CHECKSUM_ISSUE {
            public String getPartname() { return "checksumIssue"; }
            public String getHumanString() { return "inconsistent checksums"; }
        },
        MISSING_CHECKSUM {
            public String getPartname() { return "missingChecksum"; }
            public String getHumanString() { return "missing checksums"; }
        },
        OBSOLETE_CHECKSUM {
            public String getPartname() { return "obsoleteChecksum"; }
            public String getHumanString() { return "obsolete checksums"; }
        },
        MISSING_FILE {
            public String getPartname() { return "missingFile"; }
            public String getHumanString() { return "missing files"; }
        };
        public abstract String getPartname();
        public abstract String getHumanString();
    }
    
    public static final String REPORT_FILE = "report";
    
    public static final String SECTION_HEADER_START_STOP = "========";
    public static final String PILLAR_HEADER_START_STOP = "--------";
    public static final String NOISSUE_HEADER_START_STOP = "++++++++";
}
