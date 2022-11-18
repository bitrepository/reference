package org.bitrepository.audittrails;

import org.bitrepository.audittrails.store.AuditTrailStore;
import org.bitrepository.common.ArgumentValidator;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.common.utils.XmlUtils;

import java.time.Duration;

/**
 * Parent class for classes starting TimerTasks related to audit-trail operations.
 */
public abstract class AuditTrailTaskStarter {
    /** Default duration to pass after system-startup before starting audit trail tasks. */
    protected final Duration DEFAULT_GRACE_PERIOD = Duration.ZERO;
    protected final Settings settings;
    protected final AuditTrailStore store;

    public AuditTrailTaskStarter(Settings settings, AuditTrailStore store) {
        ArgumentValidator.checkNotNull(settings, "settings");
        ArgumentValidator.checkNotNull(store, "AuditTrailStore store");
        this.settings = settings;
        this.store = store;
    }

    /**
     * Get the grace period/delay duration for audit trail tasks from settings/default duration.
     *
     * Changing the grace period allows time for the system to finish startup before it has to start
     * delivering/processing audit trails. Zero duration = start tasks immediately on startup.
     * @return The time to wait before starting audit trail tasks (collection/preservation).
     */
    protected Duration getGracePeriod() {
        if (settings.getReferenceSettings().getAuditTrailServiceSettings().isSetGracePeriod()) {
            javax.xml.datatype.Duration gracePeriod =
                    settings.getReferenceSettings().getAuditTrailServiceSettings().getGracePeriod();
            return XmlUtils.xmlDurationToDuration(gracePeriod);
        } else {
            return DEFAULT_GRACE_PERIOD;
        }
    }

    public abstract void close();
}
