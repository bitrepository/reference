/*
 * #%L
 * Bitrepository Webclient
 * %%
 * Copyright (C) 2010 - 2015 The State and University Library, The Royal Library and The State Archives, Denmark
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
package org.bitrepository.webservice;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class WebCollection {

    private String collectionID;
    private String collectionName;
    
    public WebCollection() {}
    
    public WebCollection(String id, String name) {
        this.collectionID = id;
        this.collectionName = name;
    }
    
    @XmlElement(name = "collectionID")
    public String getCollectionID() {
        return collectionID;
    }
    
    public void setCollectionID(String collectionID) {
        this.collectionID = collectionID;
    }
    
    @XmlElement(name = "collectionName")
    public String getCollectionName() {
        return collectionName;
    }
    
    public void setCollectionName(String collectionName) {
        this.collectionName = collectionName;
    }
    
    
}
