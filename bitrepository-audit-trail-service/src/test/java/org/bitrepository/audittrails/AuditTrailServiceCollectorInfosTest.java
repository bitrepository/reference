/*
 * #%L
 * Bitrepository Audit Trail Service
 * %%
 * Copyright (C) 2010 - 2026 Royal Danish Library and The State Archives, Denmark
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
package org.bitrepository.audittrails;

import org.bitrepository.TestGroups;
import org.bitrepository.audittrails.collector.AuditTrailCollector;
import org.bitrepository.audittrails.store.AuditTrailStore;
import org.bitrepository.audittrails.webservice.CollectorInfo;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.common.settings.TestSettingsProvider;
import org.bitrepository.service.contributor.ContributorMediator;
import org.bitrepository.settings.repositorysettings.Collection;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AuditTrailServiceCollectorInfosTest {
    @Test
    @Tag(TestGroups.REGRESSIONTEST)
    void getCollectorInfosReturnsTheInfoOfEachCollectionInSettingsOrderTest() {
        Settings settings = TestSettingsProvider.reloadSettings("AuditTrailServiceCollectorInfosTest");
        List<Collection> collections = settings.getRepositorySettings().getCollections().getCollection();
        collections.clear();
        collections.add(collection("collection2"));
        collections.add(collection("collection1"));
        CollectorInfo info1 = new CollectorInfo();
        CollectorInfo info2 = new CollectorInfo();
        AuditTrailCollector collector = mock(AuditTrailCollector.class);
        when(collector.getCollectorInfo("collection1")).thenReturn(info1);
        when(collector.getCollectorInfo("collection2")).thenReturn(info2);
        AuditTrailService service = new AuditTrailService(mock(AuditTrailStore.class), collector,
                mock(ContributorMediator.class), settings);

        assertEquals(List.of(info2, info1), service.getCollectorInfos());
    }

    @Test
    @Tag(TestGroups.REGRESSIONTEST)
    void getCollectorInfosWithoutCollectionsTest() {
        Settings settings = TestSettingsProvider.reloadSettings("AuditTrailServiceCollectorInfosTest");
        settings.getRepositorySettings().getCollections().getCollection().clear();
        AuditTrailService service = new AuditTrailService(mock(AuditTrailStore.class), mock(AuditTrailCollector.class),
                mock(ContributorMediator.class), settings);

        assertEquals(List.of(), service.getCollectorInfos());
    }

    private Collection collection(String collectionID) {
        Collection collection = new Collection();
        collection.setID(collectionID);
        return collection;
    }
}
