package org.bitrepository.modify.putfile;

import java.net.URL;

import org.bitrepository.modify.put.PutClient;
import org.jaccept.TestEventManager;

public class PutClientTestWrapper implements PutClient {

	private PutClient wrappedPutClient;
	private TestEventManager testEventManager;
	
	public PutClientTestWrapper(PutClient putClientInstance, TestEventManager eventManager) {
		this.wrappedPutClient = putClientInstance;
		this.testEventManager = eventManager;
	}
	
	@Override
	public void putFileWithId(URL url, String fileId, String slaId) {
		testEventManager.addStimuli("Calling PutFileWithId(" + url + ", " + fileId + ", " + slaId + ")");
		wrappedPutClient.putFileWithId(url, fileId, slaId);
	}

}
