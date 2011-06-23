/*
 * #%L
 * Bitrepository Access
 * 
 * $Id$
 * $HeadURL$
 * %%
 * Copyright (C) 2010 - 2011 The State and University Library, The Royal Library and The State Archives, Denmark
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
package org.bitrepository.access.getfile.selectors;

import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileResponse;
import org.bitrepository.protocol.time.TimeMeasureComparator;

/**
 * Selects the pillar which have the quickest estimated delivery as indicated in the <code>timeToDelover</code> in the 
 * response.
 */
public class FastestPillarSelectorForGetFile extends PillarSelectorForGetFile {
    private static final TimeMeasureComparator comparator = new TimeMeasureComparator();

    public FastestPillarSelectorForGetFile(String[] pillarsWhichShouldRespond) {
        super(pillarsWhichShouldRespond);
    }

    @Override
    public boolean checkIfPillarShouldBeSelected(IdentifyPillarsForGetFileResponse response) {
        return 
        getIDForSelectedPillar() == null || 
        comparator.compare(response.getTimeToDeliver(), getTimeToDeliver()) < 0;
    }
}
