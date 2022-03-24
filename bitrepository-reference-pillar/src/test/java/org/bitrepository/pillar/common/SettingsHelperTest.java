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

package org.bitrepository.pillar.common;

import org.bitrepository.pillar.integration.func.Assert;
import org.bitrepository.settings.repositorysettings.Collection;
import org.bitrepository.settings.repositorysettings.PillarIDs;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SettingsHelperTest {
    @Test( groups = {"regressiontest"})
    public void getPillarCollectionsTest() {
        String myPillarID = "myPillarID";
        String otherPillarID = "OtherPillar";
        List<Collection> collection = new ArrayList<>();

        collection.add(createCollection("myFirstCollection", new String[] {myPillarID}));
        collection.add(createCollection("mySecondCollection", new String[] {myPillarID, otherPillarID}));
        collection.add(createCollection("otherCollection", new String[] {otherPillarID}));

        List<String> myCollections = SettingsHelper.getPillarCollections(myPillarID, collection);
        Assert.assertEquals(myCollections.size(), 2);
        Assert.assertEquals("myFirstCollection", myCollections.get(0));
        Assert.assertEquals("mySecondCollection", myCollections.get(1));

        List<String> otherCollections = SettingsHelper.getPillarCollections(otherPillarID, collection);
        Assert.assertEquals(otherCollections.size(), 2);
        Assert.assertEquals("mySecondCollection", otherCollections.get(0));
        Assert.assertEquals("otherCollection", otherCollections.get(1));
    }

    private Collection createCollection(String collectionID, String[] pillarIDs) {
        Collection collection = new Collection();
        collection.setID(collectionID);
        PillarIDs pillarIDObjs = new PillarIDs();
        pillarIDObjs.getPillarID().addAll(Arrays.asList(pillarIDs));
        collection.setPillarIDs(pillarIDObjs);
        return collection;
    }
}
