package org.bitrepository.audittrails.collector;

import java.util.Date;
import java.util.TimerTask;

import org.bitrepository.common.utils.SettingsUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AuditTrailCollectionTimerTask extends TimerTask {
    private Logger log = LoggerFactory.getLogger(getClass());

    private final IncrementalCollector collector;
    /** The collection schedule */
    private final CollectionSchedule schedule;
    
    /**
     * @param collector The collector doing the actual work.
     * @param interval The interval between running this timer task.
     * @param gracePeriod The period that should pass before the first scheduled collection
     */
    public AuditTrailCollectionTimerTask(IncrementalCollector collector, long interval, int gracePeriod) {
        this.schedule = new CollectionSchedule(interval, gracePeriod);
        this.collector = collector;
        log.info("Scheduled next collection of audit trails for {}", schedule.getNextRun());
    }
    
    /**
     * @return the next scheduled collection
     */
    public Date getNextScheduledRun() {
        return schedule.getNextRun();
    }
    
    /**
     * @return the date of the last started collection
     */
    public Date getLastCollectionStart() {
        return schedule.getLastStart();
    }
    
    /**
     * @return  the date of the last finished collection
     */
    public Date getLastCollectionFinish() {
        return schedule.getLastFinish();
    }
    
    public long getLastNumberOfCollectedAudits() {
        return collector.getNumberOfCollectedAudits();
    }
    
    /**
     * Run the operation and when finished set the date for the next collection.
     */
    public synchronized void runCollection() {
        log.info("Starting collection of audit trails for collection: '{}'", collector.getCollectionID());
        schedule.start();
        collector.performCollection(SettingsUtils.getAuditContributorsForCollection(collector.getCollectionID()));
        schedule.finish();
        log.info("Scheduled next collection of audit trails from {} for {}", collector.getCollectionID(), schedule.getNextRun());
    }

    @Override
    public void run() {
        if(schedule.getNextRun().getTime() < System.currentTimeMillis()) {
            runCollection();
        }
    }
}
