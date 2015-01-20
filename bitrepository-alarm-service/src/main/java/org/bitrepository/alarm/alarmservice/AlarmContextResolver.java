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

import org.bitrepository.bitrepositoryelements.Alarm;
import org.eclipse.persistence.jaxb.JAXBContextProperties;
import org.glassfish.jersey.moxy.json.MoxyJsonConfig;


@Provider
public class AlarmContextResolver implements ContextResolver<MoxyJsonConfig> {

    private final MoxyJsonConfig config;
    private Class<?>[] types = {Alarm.class};
    
    public AlarmContextResolver() throws Exception {
        this.config = new MoxyJsonConfig();
        config.property(JAXBContextProperties.JSON_WRAPPER_AS_ARRAY_NAME, true);
        config.property(JAXBContextProperties.JSON_INCLUDE_ROOT, true);        
    }
    
    @Override
    public MoxyJsonConfig getContext(Class<?> objectType) {
        for(Class<?> type : types) {
            if(type == objectType) {
                return config;
            }
        }
        
        return null;
    }

}