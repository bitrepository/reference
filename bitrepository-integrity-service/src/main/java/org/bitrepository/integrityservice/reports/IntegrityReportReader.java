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
package org.bitrepository.integrityservice.reports;

import java.io.File;
import java.io.FileNotFoundException;

/**
 * Class to support reading integrity reports and parts of them. 
 */
public class IntegrityReportReader {

    private final File reportDir;
    
    public IntegrityReportReader(File reportDir) {
        this.reportDir = reportDir;
    }
    
    /**
     * Retrieve the full report
     * @return the full report
     * @throws FileNotFoundException if no report could be found
     */
    public File getFullReport() throws FileNotFoundException {
         File report = new File(reportDir, IntegrityReportConstants.REPORT_FILE);
         if(report.exists()) {
             return report;
         } else {
             throw new FileNotFoundException("Could not locate report file");
         }
    }
    
    /**
     * Retrieve a part of the report
     * @param part The part of the report that is requested
     * @param pillarID The pillar for which the part is for
     * @return a part of the report
     * @throws FileNotFoundException if no report part could be found
     */
    public File getReportPart(String part, String pillarID) throws FileNotFoundException {
        String reportFileName = part + "-" + pillarID;
        File reportPart = new File(reportDir, reportFileName);
        if(reportPart.exists()) {
            return reportPart;
        } else {
            throw new FileNotFoundException("Could not retrieve report part '" + part + "' for '" + pillarID + "'");
        }
    }
    
}
