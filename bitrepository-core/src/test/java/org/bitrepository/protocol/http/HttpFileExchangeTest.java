package org.bitrepository.protocol.http;

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

import java.io.File;
import java.net.URL;
import org.bitrepository.common.settings.TestSettingsProvider;
import org.jaccept.structure.ExtendedTestCase;
import org.testng.Assert;
import org.testng.annotations.Test;

public class HttpFileExchangeTest extends ExtendedTestCase {
    
    @Test(groups = { "infrastructure" })
    public void uploadTest() throws Exception {
        addDescription("Test uploading a file.");

        HttpFileExchange hfe = new HttpFileExchange(TestSettingsProvider.reloadSettings("uploadTest"));
        File f = new File("src/test/resources/test-files/default-test-file.txt");
        URL url = hfe.uploadToServer(f);
        
        Assert.assertNotNull(url, "URL url");
    }
}
