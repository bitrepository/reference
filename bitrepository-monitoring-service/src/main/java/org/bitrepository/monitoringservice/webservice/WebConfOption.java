package org.bitrepository.monitoringservice.webservice;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class WebConfOption {

    private String confOption;
    private String confValue;
    
    public WebConfOption() {}
    
    public WebConfOption(String option, String value) {
        this.confOption = option;
        this.confValue = value;
    }
    
    @XmlElement(name = "confOption")
    public String getConfOption() {
        return confOption;
    }
    
    public void setConfOption(String confOption) {
        this.confOption = confOption;
    }
    
    @XmlElement(name = "confValue")
    public String getConfValue() {
        return confValue;
    }
    
    public void setConfValue(String confValue) {
        this.confValue = confValue;
    }
    
    
    
}
