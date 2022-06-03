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
package org.bitrepository.common.settings;

import org.jaccept.structure.ExtendedTestCase;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.List;

public class SettingsLoaderTest extends ExtendedTestCase {
    private static final String PATH_TO_SETTINGS = "settings/xml/bitrepository-devel";
    private static final String PATH_TO_EXAMPLE_SETTINGS = "examples/settings";

    @Test(groups = {"regressiontest"})
    public void testDevelopmentCollectionSettingsLoading() {
        SettingsProvider settingsLoader =
                new SettingsProvider(new XMLFileSettingsLoader(PATH_TO_SETTINGS), getClass().getSimpleName());

        Settings settings = settingsLoader.getSettings();
        List<String> expectedPillarIDs = Arrays.asList("Pillar1", "Pillar2");
        Assert.assertEquals(
                settings.getRepositorySettings().getCollections().getCollection().get(0).getPillarIDs().getPillarID(),
                expectedPillarIDs);
    }

    @Test(groups = {"regressiontest"})
    public void testExampleSettingsLoading() {
        SettingsProvider settingsLoader =
                new SettingsProvider(new XMLFileSettingsLoader(PATH_TO_EXAMPLE_SETTINGS), getClass().getSimpleName());

        settingsLoader.getSettings();
    }
}
