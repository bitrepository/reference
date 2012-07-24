package org.bitrepository.protocol;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import org.bitrepository.common.JaxbHelper;
import org.bitrepository.protocolversiondefinition.ProtocolVersionDefinition;
import org.xml.sax.SAXException;

public class ProtocolVersionLoader {

    /**
     * Private constructor 
     */
    private ProtocolVersionLoader() {}
    
    /**
     * Load protocol version definition
     * @return ProtocolVersionDefinition for the protocol version the project has been build against
     */
    public static ProtocolVersionDefinition loadProtocolVersion() {
        String fileLocation = "ProtocolVersionDefinition.xml";
        String schemaLocation = "ProtocolVersionDefinition.xsd";
        JaxbHelper jaxbHelper = new JaxbHelper(null, schemaLocation);

        InputStream configStreamLoad = Thread.currentThread().getContextClassLoader().getResourceAsStream(fileLocation);
        InputStream configStreamValidate = Thread.currentThread().getContextClassLoader().getResourceAsStream(fileLocation);
        if (configStreamLoad == null) {
            try {
                configStreamLoad = new FileInputStream(fileLocation);
                configStreamValidate = new FileInputStream(fileLocation);
            } catch (FileNotFoundException e) {
                throw new RuntimeException("Unable to load settings from " + fileLocation, e);
            }
        }

        try {
            jaxbHelper.validate(configStreamValidate);
            return jaxbHelper.loadXml(ProtocolVersionDefinition.class, configStreamLoad);
        } catch (SAXException e) {
            throw new RuntimeException("Unable to validate settings from " + 
                    Thread.currentThread().getContextClassLoader().getResource(fileLocation), e);
        } catch (Exception e) {
            throw new RuntimeException("Unable to load settings from " + fileLocation, e);
        }
        
    }
}
