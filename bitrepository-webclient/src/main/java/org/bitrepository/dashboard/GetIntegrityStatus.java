package org.bitrepository.dashboard;

import javax.xml.bind.annotation.XmlRootElement;

/*
 * This class should be defined in org.bitrepository.common.webobjects for reuse just like the integrity objects
 * 
 */
@XmlRootElement
public class GetIntegrityStatus {

	private int totalFileCount;
	private String pillarID;
	private int checksumErrorCount;
	private int missingFilesCount;
	
	public int getTotalFileCount() {
		return totalFileCount;
	}
	public void setTotalFileCount(int totalFileCount) {
		this.totalFileCount = totalFileCount;
	}
	public String getPillarID() {
		return pillarID;
	}
	public void setPillarID(String pillarID) {
		this.pillarID = pillarID;
	}
	public int getChecksumErrorCount() {
		return checksumErrorCount;
	}
	public void setChecksumErrorCount(int checksumErrorCount) {
		this.checksumErrorCount = checksumErrorCount;
	}
	public int getMissingFilesCount() {
		return missingFilesCount;
	}
	public void setMissingFilesCount(int missingFilesCount) {
		this.missingFilesCount = missingFilesCount;
	}

}
