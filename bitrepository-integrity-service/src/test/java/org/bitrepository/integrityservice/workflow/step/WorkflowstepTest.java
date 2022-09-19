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

package org.bitrepository.integrityservice.workflow.step;

import org.bitrepository.common.settings.Settings;
import org.bitrepository.common.settings.TestSettingsProvider;
import org.bitrepository.common.utils.SettingsUtils;
import org.bitrepository.integrityservice.alerter.IntegrityAlerter;
import org.bitrepository.integrityservice.cache.IntegrityModel;
import org.bitrepository.integrityservice.collector.IntegrityInformationCollector;
import org.bitrepository.integrityservice.workflow.IntegrityContributors;
import org.bitrepository.service.audit.AuditTrailManager;
import org.jaccept.structure.ExtendedTestCase;
import org.testng.annotations.BeforeMethod;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;

import static org.mockito.Mockito.mock;

public class WorkflowstepTest extends ExtendedTestCase {
    protected Settings settings;

    public static final String TEST_PILLAR_1 = "test-pillar-1";

    protected String TEST_COLLECTION;
    protected IntegrityAlerter alerter;
    protected IntegrityModel model;
    protected IntegrityInformationCollector collector;
    protected AuditTrailManager auditManager;
    protected IntegrityContributors integrityContributors;

    @BeforeMethod(alwaysRun = true)
    public void setup() throws DatatypeConfigurationException {
        settings = TestSettingsProvider.reloadSettings(this.getClass().getSimpleName());
        settings.getRepositorySettings().getCollections().getCollection().get(0).getPillarIDs().getPillarID().clear();
        settings.getRepositorySettings().getCollections().getCollection().get(0).getPillarIDs().getPillarID().add(TEST_PILLAR_1);
        DatatypeFactory factory = DatatypeFactory.newInstance();
        settings.getReferenceSettings().getIntegrityServiceSettings().setTimeBeforeMissingFileCheck(factory.newDuration(0));
        TEST_COLLECTION = settings.getRepositorySettings().getCollections().getCollection().get(0).getID();
        SettingsUtils.initialize(settings);

        alerter = mock(IntegrityAlerter.class);
        model = mock(IntegrityModel.class);
        collector = mock(IntegrityInformationCollector.class);
        auditManager = mock(AuditTrailManager.class);
        integrityContributors = mock(IntegrityContributors.class);
    }
}
