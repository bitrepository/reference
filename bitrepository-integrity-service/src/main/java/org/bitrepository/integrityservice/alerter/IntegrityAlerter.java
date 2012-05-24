package org.bitrepository.integrityservice.alerter;

import org.bitrepository.integrityservice.checking.IntegrityReport;

/**
 * The integrity alerter, for creating alarms based on an integrity report.
 */
public interface IntegrityAlerter {
    /**
     * Sends an alarm based on an integrity report.
     * @param report The report to base the alarm upon.
     */
    void integrityFailed(IntegrityReport report);
}
