/*
 * #%L
 * Bitrepository Core
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
package org.bitrepository.protocol.http;

import org.bitrepository.common.settings.Settings;
import org.bitrepository.common.settings.TestSettingsProvider;
import org.bitrepository.settings.referencesettings.FileExchangeSettings;
import org.bitrepository.settings.referencesettings.ProtocolType;
import org.jaccept.structure.ExtendedTestCase;
import org.testng.annotations.Test;

import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;

import static org.testng.Assert.assertEquals;

public class HttpFileExchangeTest extends ExtendedTestCase {
    @Test(groups = { "regressiontest" })
    public void checkUrlEncodingOfFilenamesTest() throws MalformedURLException {
        addDescription("Tests that the filename is url-encoded correctly for a configured webdav server");
        Settings mySettings = TestSettingsProvider.reloadSettings("uploadTest");
        FileExchangeSettings fileExchangeSettings = mySettings.getReferenceSettings().getFileExchangeSettings();
        fileExchangeSettings.setProtocolType(ProtocolType.HTTP);
        fileExchangeSettings.setServerName("http:testserver.org");
        fileExchangeSettings.setPort(BigInteger.valueOf(8000));
        fileExchangeSettings.setPath("dav");
        HttpFileExchange fe = new HttpFileExchange(fileExchangeSettings);
        String serverPathPrefix = fileExchangeSettings.getPath() + "/";
        
        addStep("Check plain filename (a filename that does not see any changes due to urlencoding", "The filename " +
                "should be unmodified");
        String plainFilename = "testfile";
        URL plainFilenameUrl = fe.getURL(plainFilename);
        
        assertEquals(plainFilenameUrl.getFile(), serverPathPrefix + plainFilename);
        
        addStep("Check that + is encoded as expected", "Filenames with a + is correctly encoded");
        String plusFilename = "test+file";
        URL plusFilenameUrl = fe.getURL(plusFilename);
        String expectedEncodedPlusFilename = "test%2Bfile";
        assertEquals(plusFilenameUrl.getFile(), serverPathPrefix + expectedEncodedPlusFilename);
    }
}