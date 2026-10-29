/*
 * #%L
 * Bitrepository Core
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
package org.bitrepository.common.utils;

import org.bitrepository.TestGroups;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.settings.referencesettings.IntegrityServiceSettings;
import org.bitrepository.settings.referencesettings.PillarIntegrityDetails;
import org.bitrepository.settings.referencesettings.PillarType;
import org.bitrepository.settings.referencesettings.ReferenceSettings;
import org.bitrepository.settings.repositorysettings.Collection;
import org.bitrepository.settings.repositorysettings.Collections;
import org.bitrepository.settings.repositorysettings.PillarIDs;
import org.bitrepository.settings.repositorysettings.RepositorySettings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class SettingsUtilsTest {
    private RepositorySettings repositorySettings;
    private IntegrityServiceSettings integrityServiceSettings;

    @BeforeEach
    void initializeSettings() {
        repositorySettings = new RepositorySettings();
        repositorySettings.setCollections(new Collections());
        integrityServiceSettings = new IntegrityServiceSettings();
        ReferenceSettings referenceSettings = new ReferenceSettings();
        referenceSettings.setIntegrityServiceSettings(integrityServiceSettings);

        Settings settings = Mockito.mock(Settings.class);
        Mockito.when(settings.getRepositorySettings()).thenReturn(repositorySettings);
        Mockito.when(settings.getReferenceSettings()).thenReturn(referenceSettings);
        SettingsUtils.initialize(settings);
    }

    @Test
    @Tag(TestGroups.REGRESSIONTEST)
    void getCollectionIDsForPillarTest() {
        addCollection("c1", null, "pillarA", "pillarB");
        addCollection("c2", null, "pillarB");
        addCollection("c3", null, "pillarA");

        assertEquals(List.of("c1", "c3"), SettingsUtils.getCollectionIDsForPillar("pillarA"));
        assertEquals(List.of("c1", "c2"), SettingsUtils.getCollectionIDsForPillar("pillarB"));
        assertEquals(List.of(), SettingsUtils.getCollectionIDsForPillar("unknownPillar"));
    }

    @Test
    @Tag(TestGroups.REGRESSIONTEST)
    void getAllCollectionsIDsTest() {
        assertEquals(List.of(), SettingsUtils.getAllCollectionsIDs());

        addCollection("c2", null, "pillarA");
        addCollection("c1", null, "pillarA");

        assertEquals(List.of("c2", "c1"), SettingsUtils.getAllCollectionsIDs());
    }

    @Test
    @Tag(TestGroups.REGRESSIONTEST)
    void getCollectionNameTest() {
        addCollection("c1", "First collection", "pillarA");
        addCollection("c2", null, "pillarA");

        assertEquals("First collection", SettingsUtils.getCollectionName("c1"));
        assertEquals("c2", SettingsUtils.getCollectionName("c2"), "Without a name the collection ID is used");
        assertNull(SettingsUtils.getCollectionName("unknownCollection"));
    }

    @Test
    @Tag(TestGroups.REGRESSIONTEST)
    void getCollectionNameUsesFirstMatchingCollectionTest() {
        addCollection("c1", "First", "pillarA");
        addCollection("c1", "Second", "pillarA");

        assertEquals("First", SettingsUtils.getCollectionName("c1"));
    }

    @Test
    @Tag(TestGroups.REGRESSIONTEST)
    void getPillarNameAndTypeTest() {
        addPillarDetails("pillarA", "Pillar A", PillarType.FILE);
        addPillarDetails("pillarB", "Pillar B", PillarType.CHECKSUM);

        assertEquals("Pillar A", SettingsUtils.getPillarName("pillarA"));
        assertEquals("Pillar B", SettingsUtils.getPillarName("pillarB"));
        assertEquals(PillarType.FILE, SettingsUtils.getPillarType("pillarA"));
        assertEquals(PillarType.CHECKSUM, SettingsUtils.getPillarType("pillarB"));
        assertNull(SettingsUtils.getPillarName("unknownPillar"));
        assertNull(SettingsUtils.getPillarType("unknownPillar"));
    }

    @Test
    @Tag(TestGroups.REGRESSIONTEST)
    void getPillarNameAndTypeWithoutPillarDetailsTest() {
        assertNull(SettingsUtils.getPillarName("pillarA"));
        assertNull(SettingsUtils.getPillarType("pillarA"));
    }

    @Test
    @Tag(TestGroups.REGRESSIONTEST)
    void getAllPillarIDsTest() {
        addCollection("c1", null, "pillarB", "pillarA");
        addCollection("c2", null, "pillarC", "pillarB");
        addCollection("c3", null, "pillarA");

        assertEquals(List.of("pillarB", "pillarA", "pillarC"), SettingsUtils.getAllPillarIDs(),
                "Each pillar once, in order of first appearance");
    }

    @Test
    @Tag(TestGroups.REGRESSIONTEST)
    void getPillarIDsForCollectionTest() {
        addCollection("c1", null, "pillarA", "pillarB");
        addCollection("c2", null, "pillarB", "pillarC");

        assertEquals(List.of("pillarA", "pillarB"), SettingsUtils.getPillarIDsForCollection("c1"));
        assertEquals(List.of("pillarB", "pillarC"), SettingsUtils.getPillarIDsForCollection("c2"));
        assertEquals(List.of(), SettingsUtils.getPillarIDsForCollection("unknownCollection"));
    }

    private void addCollection(String collectionID, String name, String... pillarIDs) {
        Collection collection = new Collection();
        collection.setID(collectionID);
        if (name != null) {
            collection.setName(name);
        }
        PillarIDs pillarIDsObject = new PillarIDs();
        pillarIDsObject.getPillarID().addAll(List.of(pillarIDs));
        collection.setPillarIDs(pillarIDsObject);
        repositorySettings.getCollections().getCollection().add(collection);
    }

    private void addPillarDetails(String pillarID, String pillarName, PillarType pillarType) {
        if (integrityServiceSettings.getPillarIntegrityDetails() == null) {
            integrityServiceSettings.setPillarIntegrityDetails(new PillarIntegrityDetails());
        }
        PillarIntegrityDetails.PillarDetails details = new PillarIntegrityDetails.PillarDetails();
        details.setPillarID(pillarID);
        details.setPillarName(pillarName);
        details.setPillarType(pillarType);
        integrityServiceSettings.getPillarIntegrityDetails().getPillarDetails().add(details);
    }
}
