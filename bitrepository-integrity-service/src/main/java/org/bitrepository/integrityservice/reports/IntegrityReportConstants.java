package org.bitrepository.integrityservice.reports;

/**
 * Class containing constants related to integrity reports 
 */
public class IntegrityReportConstants {
    
    public enum ReportPart {
        DELETED_FILE {
            public String getPostFix() { return "deletedFile"; }
            public String getHumanString() { return "deleted files"; }
        },
        CHECKSUM_ISSUE {
            public String getPostFix() { return "checksumIssue"; }
            public String getHumanString() { return "checksum errors"; }
        },
        MISSING_CHECKSUM {
            public String getPostFix() { return "missingChecksum"; }
            public String getHumanString() { return "missing checksums"; }
        },
        OBSOLETE_CHECKSUM {
            public String getPostFix() { return "obsoleteChecksum"; }
            public String getHumanString() { return "obsolete checksums"; }
        },
        MISSING_FILE {
            public String getPostFix() { return "missingFile"; }
            public String getHumanString() { return "missing files"; }
        };
        public abstract String getPostFix();
        public abstract String getHumanString();
    }
    /*public static final String DELETED_FILE = "deletedFile";
    public static final String CHECKSUM_ISSUE = "checksumIssue";
    public static final String MISSING_CHECKSUM = "missingChecksum";
    public static final String OBSOLETE_CHECKSUM = "obsoleteChecksum";
    public static final String MISSING_FILE = "missingFile";*/
    public static final String REPORT_FILE = "report";
    
    public static final String SECTION_HEADER_START_STOP = "========";
    public static final String PILLAR_HEADER_START_STOP = "--------";
    public static final String NOISSUE_HEADER_START_STOP = "++++++++";
}
