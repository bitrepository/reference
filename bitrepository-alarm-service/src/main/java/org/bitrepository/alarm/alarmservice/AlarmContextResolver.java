/*
 * #%L
 * Bitrepository Alarm Service
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
package org.bitrepository.alarm.alarmservice;

import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;
import javax.xml.bind.JAXBContext;

import org.bitrepository.bitrepositoryelements.Alarm;

import com.sun.jersey.api.json.JSONConfiguration;
import com.sun.jersey.api.json.JSONJAXBContext;

@Provider
public class AlarmContextResolver implements ContextResolver<JAXBContext> {

    private JAXBContext context;
    private Class[] types = {Alarm.class};
    
    public AlarmContextResolver() throws Exception {
         this.context = new JSONJAXBContext(JSONConfiguration.mapped().arrays("Alarm").build(), types);
    }
    
    @Override
    public JAXBContext getContext(Class objectType) {
        for(Class type : types) {
            if(type == objectType) {
                return context;
            }
        }
        
        return null;
    }

}
