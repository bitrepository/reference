package org.bitrepository.pillar.integration;

import org.bitrepository.pillar.messagefactories.PutFileMessageFactory;
import org.testng.annotations.BeforeMethod;

public class PerformingPutFileIT extends PillarIntegrationTest {
    protected PutFileMessageFactory msgFactory;


    @BeforeMethod(alwaysRun=true)
    public void initialiseReferenceTest() throws Exception {
        msgFactory = new PutFileMessageFactory(componentSettings, getComponentID(), pillarDestinationId);
    }


}
