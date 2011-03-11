package org.bitrepository.common;

import java.io.InputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

/**
 * Provides extra JAXB related utilities
 */
public final class JaxbHelper {
    
    /** Hides constructor for this utility class to prevent instantiation */
    private JaxbHelper() {}
    
    /**
     * Uses JAXB to create a object representation of an xml file. The class used to load the XML has been generated
     * based on the xsd for the xml.
     * @param namespace The name space for the xsd defining the xml should adhere to.
     * @param file The file containing the xml data.
     * @return Returns a new object representation of the xml data. 
     * @throws JAXBException The attempt to load the xml into a new object representation failed
     */
    public static <T> T loadXml(Class<T> xmlroot, InputStream inputStream) throws JAXBException {
        try {
            JAXBContext context = JAXBContext.newInstance(xmlroot);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            return xmlroot.cast(unmarshaller.unmarshal(inputStream));
        } catch(JAXBException jex) {
            throw new JAXBException("Unable to load xml from " + inputStream, jex);
        } 
    }
}
