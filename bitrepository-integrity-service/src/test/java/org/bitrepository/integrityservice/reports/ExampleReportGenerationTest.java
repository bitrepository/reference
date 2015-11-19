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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.jaccept.structure.ExtendedTestCase;
import org.testng.annotations.Test;


public class ExampleReportGenerationTest extends ExtendedTestCase {

    private final String CHECKSUM_ISSUE1 = "checksum-issue-file1";
    private final String CHECKSUM_ISSUE2 = "checksum-issue-file2";
    
    private final String DELETED_FILE = "deleted-file";
    
    private final String MISSING_FILE1 = "missing-file1";
    private final String MISSING_FILE2 = "missing-file2";
    
    private final String MISSING_CHECKSUM1 = "missing-checksum-file1";
    private final String MISSING_CHECKSUM2 = "missing-checksum-file2";
    
    private final String OBSOLETE_CHECKSUM1 = "obsolete-checksum-file1";
    private final String OBSOLETE_CHECKSUM2 = "obsolete-checksum-file2";
    
    private final String PILLAR1 = "dummy-pillar1";
    private final String PILLAR2 = "dummy-pillar2";
    
    @Test
    public void generateExampleReport() throws IOException {
        IntegrityReporter reporter = new BasicIntegrityReporter("dummy-collection", "test-class", new File("target/"));
        
        reporter.reportChecksumIssue(CHECKSUM_ISSUE1, PILLAR1);
        reporter.reportChecksumIssue(CHECKSUM_ISSUE2, PILLAR1);
        reporter.reportChecksumIssue(CHECKSUM_ISSUE1, PILLAR2);
        reporter.reportChecksumIssue(CHECKSUM_ISSUE2, PILLAR2);
        
        reporter.reportDeletedFile(DELETED_FILE, PILLAR1);
        reporter.reportDeletedFile(DELETED_FILE, PILLAR2);
        
        reporter.reportMissingFile(MISSING_FILE1, PILLAR1);
        reporter.reportMissingFile(MISSING_FILE2, PILLAR2);
        
        reporter.reportMissingChecksum(MISSING_CHECKSUM1, PILLAR1);
        reporter.reportMissingChecksum(MISSING_CHECKSUM2, PILLAR2);
        reporter.reportMissingChecksum(MISSING_CHECKSUM1, PILLAR2);
        
        reporter.reportObsoleteChecksum(OBSOLETE_CHECKSUM1, PILLAR2);
        reporter.reportObsoleteChecksum(OBSOLETE_CHECKSUM2, PILLAR1);
        
        reporter.generateReport();
        printReport(reporter.getReport());
        
    }
    
    @Test 
    public void generateEmptyReport() throws IOException {
        IntegrityReporter reporter = new BasicIntegrityReporter("dummy-collection", "test-class", new File("target/"));
        reporter.generateReport();
        printReport(reporter.getReport());
    }
    
    
    private void printReport(File report) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(report));
        String line;
        while ((line = br.readLine()) != null) {
            System.out.println(line);
        }
        br.close();
    }
    
}
