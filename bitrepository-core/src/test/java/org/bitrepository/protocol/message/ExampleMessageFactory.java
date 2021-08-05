/*
 * #%L
 * Bitrepository Protocol
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
package org.bitrepository.protocol.message;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.apache.activemq.util.ByteArrayInputStream;
import org.apache.commons.io.IOUtils;
import org.bitrepository.common.JaxbHelper;

/** Used to create message objects based on the example xml found in the message-xml module. */
public class ExampleMessageFactory {
    //public static final String XML_MESSAGE_DIR = "target/";
    public static final String SCHEMA_NAME = "BitRepositoryMessages.xsd";
    public static final String PATH_TO_SCHEMA = "xsd/";
    public static final String PATH_TO_EXAMPLES = "examples/messages/";
    public static final String EXAMPLE_FILE_POSTFIX = ".xml";

    public static <T> T createMessage(Class<T> messageType) throws Exception {
        String xmlMessage = loadXMLExample(messageType.getSimpleName());
        JaxbHelper jaxbHelper = new JaxbHelper(PATH_TO_SCHEMA, SCHEMA_NAME);
        jaxbHelper.validate(new ByteArrayInputStream(xmlMessage.getBytes(StandardCharsets.UTF_8)));
        return jaxbHelper.loadXml(messageType,
                new ByteArrayInputStream(xmlMessage.getBytes(StandardCharsets.UTF_8)));
    }

    /**
     * Loads the example XML for the indicated message. Assumes the XML examples are found under the
     * XML_MESSAGE_DIR/examples directory, and the naming convention for the example files are '${messagename}.xml'
     *
     * @param messageName
     * @return
     */
    private static String loadXMLExample(String messageName) throws Exception {
        String filePath = PATH_TO_EXAMPLES + messageName + EXAMPLE_FILE_POSTFIX;
        InputStream fileIS = Thread.currentThread().getContextClassLoader().getResourceAsStream(filePath);
        return IOUtils.toString(fileIS, StandardCharsets.UTF_8);
    }
   
}
