/*
 * #%L
 * Bitrepository Integrity Service
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
package org.bitrepository.integrityservice.mocks;

import org.bitrepository.integrityservice.checking.reports.IntegrityReportModel;

public class MockIntegrityReport implements IntegrityReportModel {

    private boolean state = true; 
    public void setIntegrityIssues(boolean state) {
        this.state = state;
    }
    @Override
    public boolean hasIntegrityIssues() {
        return state;
    }

    @Override
    public String generateReport() {
        return "Integrity issue: " + state;
    }
    
    @Override
    public String generateSummaryOfReport() {
        return "Integrity summary: " + state;
    }
}
