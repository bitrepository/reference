/*
 * #%L
 * Bitmagasin integrationstest
 * 
 * $Id$
 * $HeadURL$
 * %%
 * Copyright (C) 2010 The State and University Library, The Royal Library and The State Archives, Denmark
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
package org.bitrepository.pillar;

import java.util.Collection;
import java.util.Date;

import org.bitrepository.pillar.audit.MemorybasedAuditTrailManager;
import org.jaccept.structure.ExtendedTestCase;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Tests for the audit trails.
 */
public class AuditTrailHandlerTester extends ExtendedTestCase {
    /** Constants.*/
    Long YEAR_IN_MILLIS = 31557600000L;
    Long MINUTE_IN_MILLIS = 60000L;
    Long SECOND_IN_MILLIS = 1000L;
    String CURRENT_MESSAGE = "The audit currently.";
    String MESSAGE_IN_ONE_MIN = "The audit in one minute";
    String MESSAGE_ONE_MIN_AGO = "The audit one minute ago";

    @Test( groups = {"regressiontest"})
    public void auditTest() throws Exception {
        addDescription("Tests for the audit trail handler. Inserts some audits with given dates, and tests that they "
                + "can be extracted correct.");
        addStep("Initializing the variables.", "Should not be able to fail here.");
        MemorybasedAuditTrailManager audits = new MemorybasedAuditTrailManager();
        Date now = new Date();

        addStep("Inserting three messages with one minute difference.", "Should work.");
        audits.addMessageReceivedAudit(CURRENT_MESSAGE);
        audits.insertAudit(new Date(now.getTime() + MINUTE_IN_MILLIS) , MESSAGE_IN_ONE_MIN);
        audits.insertAudit(new Date(now.getTime() - MINUTE_IN_MILLIS) , MESSAGE_ONE_MIN_AGO);

        addStep("Test how many audits can be extracted.", "Should be all three.");
        Collection<String> all = audits.getAllAudits();
        Assert.assertEquals(all.size(), 3, "It should contain three autid trails.");
        Assert.assertTrue(all.contains(CURRENT_MESSAGE), "Should contain '" + CURRENT_MESSAGE + "'");
        Assert.assertTrue(all.contains(MESSAGE_IN_ONE_MIN), "Should contain '" + MESSAGE_IN_ONE_MIN + "'");
        Assert.assertTrue(all.contains(MESSAGE_ONE_MIN_AGO), "Should contain '" + MESSAGE_ONE_MIN_AGO + "'");

        // Test one second before test.
        {
            addStep("Test how many audits have been inserted when a time 1 sec before test start is given.", 
            "Should be one.");
            Collection<String> before = audits.getAuditsBeforeDate(new Date(now.getTime() - SECOND_IN_MILLIS));
            Assert.assertEquals(before.size(), 1, "It should contain one autid trail.");
            Assert.assertFalse(before.contains(CURRENT_MESSAGE), "Should not contain '" + CURRENT_MESSAGE + "'");
            Assert.assertFalse(before.contains(MESSAGE_IN_ONE_MIN), "Should not contain '" + MESSAGE_IN_ONE_MIN + "'");
            Assert.assertTrue(before.contains(MESSAGE_ONE_MIN_AGO), "Should contain '" + MESSAGE_ONE_MIN_AGO + "'");

            addStep("Test how many audits are inserted after when a time 1 sec before test start is given and all after is wanted.", 
            "Should be two.");
            Collection<String> after = audits.getAuditsAfterDate(new Date(now.getTime() - SECOND_IN_MILLIS));
            Assert.assertEquals(after.size(), 2, "It should contain two autid trail.");
            Assert.assertTrue(after.contains(CURRENT_MESSAGE), "Should contain '" + CURRENT_MESSAGE + "'");
            Assert.assertTrue(after.contains(MESSAGE_IN_ONE_MIN), "Should contain '" + MESSAGE_IN_ONE_MIN + "'");
            Assert.assertFalse(after.contains(MESSAGE_ONE_MIN_AGO), "Should not contain '" + MESSAGE_ONE_MIN_AGO + "'");
        }

        // TEST one year ago.
        {
            addStep("Test how many was inserted one year prior to the test.", "Should not be any.");
            Collection<String> before = audits.getAuditsBeforeDate(new Date(now.getTime() - YEAR_IN_MILLIS));
            Assert.assertEquals(before.size(), 0, "Should be empty.");

            addStep("Test how many has been inserted since one year prior to the test.", "Should be all three.");
            Collection<String> after = audits.getAuditsAfterDate(new Date(now.getTime() - YEAR_IN_MILLIS));
            Assert.assertEquals(after.size(), 3, "It should contain three autid trails.");
            Assert.assertTrue(after.contains(CURRENT_MESSAGE), "Should contain '" + CURRENT_MESSAGE + "'");
            Assert.assertTrue(after.contains(MESSAGE_IN_ONE_MIN), "Should contain '" + MESSAGE_IN_ONE_MIN + "'");
            Assert.assertTrue(after.contains(MESSAGE_ONE_MIN_AGO), "Should contain '" + MESSAGE_ONE_MIN_AGO + "'");
        }

        // TEST in one year
        {
            addStep("Test how many was inserted one year after to the test.", "Should be all.");
            Collection<String> before = audits.getAuditsBeforeDate(new Date(now.getTime() + YEAR_IN_MILLIS));
            Assert.assertEquals(before.size(), 3, "It should contain three autid trails.");
            Assert.assertTrue(before.contains(CURRENT_MESSAGE), "Should contain '" + CURRENT_MESSAGE + "'");
            Assert.assertTrue(before.contains(MESSAGE_IN_ONE_MIN), "Should contain '" + MESSAGE_IN_ONE_MIN + "'");
            Assert.assertTrue(before.contains(MESSAGE_ONE_MIN_AGO), "Should contain '" + MESSAGE_ONE_MIN_AGO + "'");

            addStep("Test how many has been inserted since one year after the test.", "Should not be any.");
            Collection<String> after = audits.getAuditsAfterDate(new Date(now.getTime() + YEAR_IN_MILLIS));
            Assert.assertEquals(after.size(), 0, "Should be empty.");
        }
    }
}
