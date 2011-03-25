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
package org.bitrepository.common;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

/**
 * Provides extra JAXB related utilities
 */
public final class JaxbHelper {

    /** Hides constructor for this utility class to prevent instantiation */
    private JaxbHelper() {}

    /**
     * Uses JAXB to create a object representation of an xml file. The class used to load the XML has been generated
     * based on the xsd for the xml.
     * @param xmlroot The root class to deserialize to.
     * @param inputStream The input stream containing the xml data.
     * @return Returns a new object representation of the xml data. 
     * @throws JAXBException The attempt to load the xml into a new object representation failed
     */
    public static <T> T loadXml(Class<T> xmlroot, InputStream inputStream) throws JAXBException {
        JAXBContext context = JAXBContext.newInstance(xmlroot);
        Unmarshaller unmarshaller = context.createUnmarshaller();
        return xmlroot.cast(unmarshaller.unmarshal(inputStream));
    }

    /**
     * Method for retrieving the content of a JAXB object as a string.
     * @param object The xml-serializable object which should be made into XML.
     * @return The XML representation of the message object.
     * @throws JAXBException If the object could not be serialized as a JAXB object.
     */
    public static String serializeToXml(Object object) throws JAXBException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        JAXBContext.newInstance(object.getClass()).createMarshaller().marshal(object, baos);
        return baos.toString();
    }
}
