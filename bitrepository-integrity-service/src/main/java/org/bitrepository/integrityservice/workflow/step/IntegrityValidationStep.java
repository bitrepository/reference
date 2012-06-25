package org.bitrepository.integrityservice.workflow.step;

import org.bitrepository.bitrepositoryelements.FileIDs;
import org.bitrepository.integrityservice.alerter.IntegrityAlerter;
import org.bitrepository.integrityservice.checking.IntegrityChecker;
import org.bitrepository.integrityservice.checking.IntegrityReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The integrity validation step.
 * Starts by validating the file ids and then the checksums, which generates an integrity reports.
 * Based on this integrity report, it is decided whether to dispatch an alarm.
 */
public class IntegrityValidationStep implements WorkflowStep {
    /** The log.*/
    private Logger log = LoggerFactory.getLogger(getClass());
    /** The constant for all file ids.*/
    private static final String ALL_FILE_IDS = "true";
    /** Checker for performing the integrity checks.*/
    private final IntegrityChecker checker;
    /** The dispatcher of alarms.*/
    private final IntegrityAlerter dispatcher;
    
    /**
     * Constructor.
     * @param checker The checker for performing the integrity checks.
     * @param alarmDispatcher The dispatcher of alarms.
     */
    public IntegrityValidationStep(IntegrityChecker checker, IntegrityAlerter alarmDispatcher) {
        this.checker = checker;
        this.dispatcher = alarmDispatcher;
    }
    
    @Override
    public String getName() {
        return "Validating the integrity.";
    }

    @Override
    public void performStep() {
        FileIDs fileIds = createAllFileIDs();
        IntegrityReport report = new IntegrityReport();
        
        log.debug("Checking the file ids.");
        report.combineWithReport(checker.checkFileIDs(fileIds));
        
        log.debug("Checking the checksums");
        report.combineWithReport(checker.checkChecksum(fileIds));
        
        if(report.hasIntegrityIssues()) {
            log.warn("Integrity issues found: " + report.generateReport());
            dispatcher.integrityFailed(report);
        } else {
            log.info("No integrity issues found: " + report.generateReport());
        }
    }

    /**
     * @return A FileIDs object for all file ids.
     */
    private FileIDs createAllFileIDs() {
        FileIDs res = new FileIDs();
        res.setAllFileIDs(ALL_FILE_IDS);
        return res;
    }
}
