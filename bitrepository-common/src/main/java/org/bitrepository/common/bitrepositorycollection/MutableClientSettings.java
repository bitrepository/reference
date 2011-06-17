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
package org.bitrepository.common.bitrepositorycollection;


/**
 * The concrete implementation of the SLAConfiguration interface. Contains set operations for all attributes,
 * eg. it is mutable.
 */
public class MutableClientSettings implements ClientSettings {	
    
    public MutableClientSettings() {
        super();
    }
    
    public MutableClientSettings(ClientSettings settingsToCopy) {
        setBitRepositoryCollectionID(settingsToCopy.getBitRepositoryCollectionID());
        setBitRepositoryCollectionTopicID(settingsToCopy.getBitRepositoryCollectionTopicID());
        setClientTopicID(settingsToCopy.getClientTopicID());
        setPillarIDs(settingsToCopy.getPillarIDs());
        setLocalFileStorage(settingsToCopy.getLocalFileStorage());
    }
    
    private String id;
    @Override
	public String getBitRepositoryCollectionID() {
        return id;
    }
    /**
     * @see #getSlaId()
     */
    public void setBitRepositoryCollectionID(String id) {
        this.id = id;
    }

    private String bitRepositoryCollectionTopicID;
	@Override
	public String getBitRepositoryCollectionTopicID() {
		return bitRepositoryCollectionTopicID;
	}	
	/**
	 * @see #getBitRepositoryCollectionTopicID()
	 */
	public void setBitRepositoryCollectionTopicID(String bitRepositoryCollectionTopicID) {
		this.bitRepositoryCollectionTopicID = bitRepositoryCollectionTopicID;
	}

    private String clientTopicId;
	@Override
	public String getClientTopicID() {
		return clientTopicId;
	}	
	/**
	 * @see #getClientTopicId()
	 */
	public void setClientTopicID(String clientTopicId) {
		this.clientTopicId = clientTopicId;
	}

    private String[] pillarIDs;
	@Override
	public String[] getPillarIDs() {
		return pillarIDs;
	}
	/**
	 * @see #getPillarIDs()
	 */
	public void setPillarIDs(String[] pillarIDs) {
		this.pillarIDs = pillarIDs;
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
		return "MutableClientSettings [bitRepositoryCollectionTopicID=" + bitRepositoryCollectionTopicID + ", clientTopicId=" + clientTopicId
				+ ", pillarIDs=" + pillarIDs + "]";
	}
}
