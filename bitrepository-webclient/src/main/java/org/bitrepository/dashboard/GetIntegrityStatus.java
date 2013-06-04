/*
 * #%L
 * Bitrepository Webclient
 * %%
 * Copyright (C) 2010 - 2013 The State and University Library, The Royal Library and The State Archives, Denmark
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
