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
