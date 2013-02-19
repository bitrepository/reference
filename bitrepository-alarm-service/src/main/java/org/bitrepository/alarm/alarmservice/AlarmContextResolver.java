package org.bitrepository.alarm.alarmservice;

import javax.ws.rs.ext.ContextResolver;
import javax.xml.bind.JAXBContext;

import org.bitrepository.bitrepositoryelements.Alarm;

import com.sun.jersey.api.json.JSONConfiguration;
import com.sun.jersey.api.json.JSONJAXBContext;

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
