/*
 * #%L
 * Bitrepository Access
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
package org.bitrepository.access.getaudittrails.client;

import org.bitrepository.bitrepositoryelements.ResultingAuditTrails;
import org.bitrepository.client.eventhandler.ContributorCompleteEvent;

/**
 * Returns the result of an Audit Trail final response for a single contributor.
 */
public class AuditTrailResult extends ContributorCompleteEvent {
    private final ResultingAuditTrails auditTrailEvents;
    private final boolean isPartialResult;

    /**
     * @param pillarID         The ID of the pillar supplying the audit trails.
     * @param collectionID     The ID of the collection
     * @param auditTrailEvents The list of audit trail events
     * @param isPartialResult  <code>true</code> if the result return encountered a maxResultLimit and therefore
     *                         only returned a subset of results is returned. See {@link org.bitrepository.access.ContributorQuery} for
     *                         details.
     */
    public AuditTrailResult(String pillarID, String collectionID, ResultingAuditTrails auditTrailEvents,
                            boolean isPartialResult) {
        super(pillarID, collectionID);
        this.auditTrailEvents = auditTrailEvents;
        this.isPartialResult = isPartialResult;
    }

    /**
     * @return The auditTrails returned from the component.
     */
    public ResultingAuditTrails getAuditTrailEvents() {
        return auditTrailEvents;
    }

    /**
     * @return An indication of the results being partial or not.
     * @see #AuditTrailResult(String, String, org.bitrepository.bitrepositoryelements.ResultingAuditTrails, boolean)
     */
    public boolean isPartialResult() {
        return isPartialResult;
    }

    @Override
    public String additionalInfo() {
        StringBuilder infoSB = new StringBuilder(super.additionalInfo());
        if (auditTrailEvents != null && auditTrailEvents.getAuditTrailEvents() != null
                && auditTrailEvents.getAuditTrailEvents().getAuditTrailEvent() != null) {
            infoSB.append(", NumberOfAuditTrailEvents=").append(auditTrailEvents.getAuditTrailEvents().getAuditTrailEvent().size());
        }
        infoSB.append(", PartialResult=").append(isPartialResult);
        return infoSB.toString();
    }
}
