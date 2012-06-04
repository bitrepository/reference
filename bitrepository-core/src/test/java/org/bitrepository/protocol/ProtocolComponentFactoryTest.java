/*
 * #%L
 * Bitrepository Protocol
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
package org.bitrepository.protocol;

import org.bitrepository.common.settings.Settings;
import org.bitrepository.common.settings.SettingsProvider;
import org.bitrepository.common.settings.XMLFileSettingsLoader;
import org.bitrepository.protocol.ProtocolComponentFactory;
import org.bitrepository.protocol.messagebus.MessageBus;
import org.bitrepository.protocol.security.DummySecurityManager;
import org.bitrepository.protocol.security.SecurityManager;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class ProtocolComponentFactoryTest {
    private Settings settings;
    /** The mocked SecurityManager */
    private SecurityManager securityManager;
    
    @BeforeMethod(alwaysRun = true)
    public void beforeMethodSetup() throws Exception {
        setupSettings();
        securityManager = new DummySecurityManager();
    }
    
    protected void setupSettings() throws Exception {
        SettingsProvider settingsLoader = new SettingsProvider(new XMLFileSettingsLoader("settings/xml/bitrepository-devel"));
        settings = settingsLoader.getSettings();
    }
    
    @SuppressWarnings("unused")
    @Test(groups = { "regressiontest" })    
    /**
     * Validates that only one message bus instance is created for each collection ID.
     */
    public void getMessageTest() throws Exception {
        MessageBus bus = ProtocolComponentFactory.getInstance().getMessageBus(settings, securityManager);
    }
}
