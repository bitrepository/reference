/*
 * #%L
 * Bitrepository Monitoring Service
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
