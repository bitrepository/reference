/*
 * #%L
 * bitrepository-common
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
package org.bitrepository.common.sla;


/**
 * The concrete implementation of the SLAConfiguration interface. Contains set operations for all attributes,
 * eg. it is mutable.
 */
public class MutableSLAConfiguration implements SLAConfiguration {	
    
    private String slaId;
    @Override
	public String getSlaId() {
        return slaId;
    }
    /**
     * @see #getSlaId()
     */
    public void setSlaId(String slaId) {
        this.slaId = slaId;
    }

    private String slaTopicId;
	@Override
	public String getSlaTopicId() {
		return slaTopicId;
	}	
	/**
	 * @see #getSlaTopicId()
	 */
	public void setSlaTopicId(String slaTopicId) {
		this.slaTopicId = slaTopicId;
	}

    private String clientTopicId;
	@Override
	public String getClientTopicId() {
		return clientTopicId;
	}	
	/**
	 * @see #getClientTopicId()
	 */
	public void setClientTopicId(String clientTopicId) {
		this.clientTopicId = clientTopicId;
	}

    private int numberOfPillars;
	@Override
	public int getNumberOfPillars() {
		return numberOfPillars;
	}
	/**
	 * @see #getClientTopicId()
	 */
	public void setNumberOfPillars(int numberOfPillars) {
		this.numberOfPillars = numberOfPillars;
	}
	
	private String fileStorage;
    @Override
    public String getLocalFileStorage() {
        return fileStorage;
    }
    /**
     * @see #getSlaId()
     */
    public void setLocalFileStorage(String fileStorage) {
        this.fileStorage = fileStorage;
    }

	@Override
	public String toString() {
		return "SLAConfigurationMutable [slaTopicId=" + slaTopicId + ", clientTopicId=" + clientTopicId
				+ ", numberOfPillars=" + numberOfPillars + "]";
	}
}
