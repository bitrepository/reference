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
package org.bitrepository.integrityservice.workflow;

import java.util.Arrays;
import java.util.Set;

import org.testng.annotations.Test;
import org.testng.Assert;


public class IntegrityContributorsTest {

    private final static String PILLAR1 = "pillar1";
    private final static String PILLAR2 = "pillar2";
    
    @Test
    public void testConstructor() {
        IntegrityContributors ic = new IntegrityContributors(Arrays.asList(PILLAR1, PILLAR2));
        Set<String> activeContributors = ic.getActiveContributors();
        Assert.assertTrue(activeContributors.contains(PILLAR1));
        Assert.assertTrue(activeContributors.contains(PILLAR2));
        Assert.assertTrue(ic.getFailedContributors().isEmpty());
        Assert.assertTrue(ic.getFinishedContributors().isEmpty());
    }
    
    @Test
    public void testFailContributor() {
        IntegrityContributors ic = new IntegrityContributors(Arrays.asList(PILLAR1, PILLAR2));
        ic.failContributor(PILLAR1);
        Assert.assertTrue(ic.getFailedContributors().contains(PILLAR1));
        Assert.assertTrue(ic.getActiveContributors().contains(PILLAR2));
        Assert.assertTrue(ic.getFinishedContributors().isEmpty());
    }
    
    @Test
    public void testFinishContributor() {
        IntegrityContributors ic = new IntegrityContributors(Arrays.asList(PILLAR1, PILLAR2));
        ic.finishContributor(PILLAR1);
        Assert.assertTrue(ic.getFinishedContributors().contains(PILLAR1));
        Assert.assertTrue(ic.getActiveContributors().contains(PILLAR2));
        Assert.assertTrue(ic.getFailedContributors().isEmpty());
    }
    
    @Test
    public void testReloadContributors() {
        IntegrityContributors ic = new IntegrityContributors(Arrays.asList(PILLAR1, PILLAR2));
        ic.finishContributor(PILLAR1);
        ic.failContributor(PILLAR2);
        Assert.assertTrue(ic.getActiveContributors().isEmpty());
        Assert.assertTrue(ic.getFinishedContributors().contains(PILLAR1));
        Assert.assertTrue(ic.getFailedContributors().contains(PILLAR2));
        
        ic.reloadActiveContributors();
        Assert.assertTrue(ic.getFinishedContributors().isEmpty());
        Assert.assertTrue(ic.getActiveContributors().contains(PILLAR1));
        Assert.assertTrue(ic.getFailedContributors().contains(PILLAR2));
    }
    
}
