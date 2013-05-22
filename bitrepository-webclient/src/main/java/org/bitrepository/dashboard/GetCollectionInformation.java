package org.bitrepository.dashboard;

import javax.xml.bind.annotation.XmlRootElement;

/*
 * This class should be defined in org.bitrepository.common.webobjects for reuse just like the integrity objects
 * 
 */
@XmlRootElement
public class GetCollectionInformation {

	private int numberOfFiles;
	private String lastIngest;
	private String collectionSize;
	public int getNumberOfFiles() {
		return numberOfFiles;
	}
	public void setNumberOfFiles(int numberOfFiles) {
		this.numberOfFiles = numberOfFiles;
	}
	public String getLastIngest() {
		return lastIngest;
	}
	public void setLastIngest(String lastIngest) {
		this.lastIngest = lastIngest;
	}
	public String getCollectionSize() {
		return collectionSize;
	}
	public void setCollectionSize(String collectionSize) {
		this.collectionSize = collectionSize;
	}
	
}
