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
        JAXBContext context = JAXBContext.newInstance(xmlroot);
        Unmarshaller unmarshaller = context.createUnmarshaller();
        return xmlroot.cast(unmarshaller.unmarshal(inputStream));
    }
}
