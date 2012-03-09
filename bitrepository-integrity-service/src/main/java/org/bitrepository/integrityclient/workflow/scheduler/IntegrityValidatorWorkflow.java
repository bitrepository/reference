package org.bitrepository.integrityclient.workflow.scheduler;

import org.bitrepository.bitrepositoryelements.FileIDs;
import org.bitrepository.integrityclient.checking.IntegrityChecker;
import org.bitrepository.integrityclient.checking.IntegrityReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Workflow for validating the integrity of the files.
 * Will no collect any data from the pillars, only go through the already collected integrity data.
 */
public class IntegrityValidatorWorkflow extends IntervalWorkflow {
    /** The log.*/
    private Logger log = LoggerFactory.getLogger(getClass());
    /** The checker for checking the integrity of the data.*/
    private final IntegrityChecker checker;
    
    /**
     * Constructor.
     * @param interval The interval for this workflow.
     * @param checker The checker for validating the integrity of the data. 
     */
    public IntegrityValidatorWorkflow(long interval, IntegrityChecker checker) {
        super(interval);
        this.checker = checker;
    }

    @Override
    public void runWorkflow() {
        FileIDs allFileIDs = getAllFileIDs();
        
        IntegrityReport integrityReport = checker.checkFileIDs(allFileIDs);
        integrityReport.combineWithReport(checker.checkChecksum(allFileIDs));
        
        // TODO perhaps send alarm?
        if(integrityReport.hasIntegrityIssues()) {
            log.warn(integrityReport.generateReport());
        } else {
            log.info(integrityReport.generateReport());            
        }
    }

    /**
     * @return A FileIDs object for all file ids.
     */
    private FileIDs getAllFileIDs() {
        FileIDs res = new FileIDs();
        res.setAllFileIDs("true");
        return res;
    }
}
