/*
 * #%L
 * Bitrepository Common
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
package org.bitrepository.common;

import javax.swing.JFrame;

import org.jaccept.TestEventManager;
import org.jaccept.gui.ComponentTestFrame;
import org.jaccept.structure.ExtendedTestCase;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeTest;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.util.StatusPrinter;

/**
 * Contains the generic parts for integration testing.
 */
public abstract class IntegrationTest extends ExtendedTestCase {
    protected TestEventManager testEventManager = TestEventManager.getInstance();

    // Experimental, use at own risk.
    @BeforeTest (alwaysRun = true)
    public void startTestGUI() {  
        if (System.getProperty("enableTestGUI", "false").equals("true") ) {
            JFrame hmi = new ComponentTestFrame();
            hmi.pack();
            hmi.setVisible(true);
        }
    }
    
    @BeforeTest (alwaysRun = true)
    public void writeLogStatus() {  
        if (System.getProperty("enableLogStatus", "false").equals("true")) {
            LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
            StatusPrinter.print(lc);
        }
    }
}
