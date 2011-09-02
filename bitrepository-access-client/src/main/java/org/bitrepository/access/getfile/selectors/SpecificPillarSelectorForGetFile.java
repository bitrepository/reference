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
import org.bitrepository.common.ArgumentValidator;
import org.bitrepository.common.exceptions.UnableToFinishException;

/**
 * Selects a pillar based on the supplied pillarID. 
 * 
 * The reason for selection a pillar when we already know the ID is that additional information provided in the 
 * response.
 */
public class SpecificPillarSelectorForGetFile extends PillarSelectorForGetFile {
    private final String pillarToSelect;

    /**
     * Creates the selector based on the provided information.
     * @param pillarToSelect
     */
    public SpecificPillarSelectorForGetFile(String pillarToSelect) {
        ArgumentValidator.checkNotNull(pillarToSelect, "pillarToSelect");
        this.pillarToSelect = pillarToSelect; 
    }

    /**
     * Selects the pillar if the ID corresponds to the one supplied in the constructor. Also marks the selector as 
     * finished if the pillar is selected.
     */
    @Override
    public boolean checkPillarResponseForSelection(IdentifyPillarsForGetFileResponse response) {
        return pillarToSelect.equals(response.getPillarID() );
    }    

    @Override
    public String[] getOutstandingPillars() {
        if (pillarID == null) {
            return new String[] {pillarToSelect};
        } else {
            return new String[0];
        }
    }

    /** Returns true if the indicated pillar has responded, else <code>false</code>. */
    @Override
    public boolean isFinished() throws UnableToFinishException {
        return pillarToSelect != null;
    }
}
