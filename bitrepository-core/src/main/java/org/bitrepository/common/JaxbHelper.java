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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.sax.SAXSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Provides extra JAXB related utilities
 * This class should not be considered light-weight, as it loads XSD from specified source!
 */
public final class JaxbHelper {
    private final Logger log = LoggerFactory.getLogger(getClass());

    private final Schema schema;
    
    /**
     * Used for creating a JaxbHelper instance for a specific schema.
     * @param pathToSchema The location of the schema in the classpath.
     * @param schemaName The name of the schema to use.
     */
    public JaxbHelper(String pathToSchema, String schemaName) {
        ArgumentValidator.checkNotNullOrEmpty(schemaName, "schemaName");
        if(pathToSchema == null) {
            pathToSchema = "";
        }

        InputStream schemaStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(pathToSchema + schemaName);
        log.debug("Creating JAXBHelper based on schema from: " + 
                Thread.currentThread().getContextClassLoader().getResource(pathToSchema + schemaName));
        LSResourceResolver resourceResolver = new ResourceResolver(pathToSchema);
        SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        schemaFactory.setResourceResolver(resourceResolver);
        try {
            schema = schemaFactory.newSchema(new SAXSource(new InputSource(schemaStream)));
        } catch (SAXException e) {
            throw new IllegalArgumentException("Unable to parse schema " + schemaName, e);
        }
    }
    
    /**
     * Uses JAXB to create a object representation of an xml file. The class used to load the XML has been generated
     * based on the xsd for the xml.
     * @param <T> The root class to deserialize to.
     * @param xmlroot The root class to deserialize to.
     * @param inputStream The input stream containing the xml data.
     * @return Returns a new object representation of the xml data. 
     * @throws JAXBException The attempt to load the xml into a new object representation failed.
     */
    public <T> T loadXml(Class<T> xmlroot, InputStream inputStream) throws JAXBException {
        ArgumentValidator.checkNotNull(xmlroot, "xmlroot");
        ArgumentValidator.checkNotNull(inputStream, "inputStream");
        JAXBContext context = JAXBContext.newInstance(xmlroot);
        Unmarshaller unmarshaller = context.createUnmarshaller();
        return xmlroot.cast(unmarshaller.unmarshal(inputStream));
    }

    /**
     * Validates the xml in the inputstream
     * @param inputStream The stream containing the xml to validate
     * @throws SAXException The xml didn't validate.
     * @throws IOException Problems accessing the input stream.
     */
    public void validate(InputStream inputStream) throws SAXException, IOException {
        Validator schemaValidator = schema.newValidator();
        schemaValidator.validate(new SAXSource(new InputSource(inputStream)));
    }

    /**
     * Method for retrieving the content of a JAXB object as a string.
     * @param object The xml-serializable object which should be made into XML.
     * @return The XML representation of the message object.
     * @throws JAXBException If the object could not be serialized as a JAXB object.
     */
    public String serializeToXml(Object object) throws JAXBException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        JAXBContext.newInstance(object.getClass()).createMarshaller().marshal(object, baos);
        String baosContent;
        try {
            baosContent = baos.toString(StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            throw new AssertionError("UTF-8 not supported");
        }
        return baosContent;
    }

    private static class ResourceResolver implements LSResourceResolver {
        private final String prefix;

        public ResourceResolver(String prefix) {
            this.prefix = prefix;
        }

        @Override
        public LSInput resolveResource(String type, String namespaceURI, String publicId, String systemId,
                String baseURI) {
            //The key here is SystemID.
            if (systemId == null) {
                return null;
            }
            
            URL schema = Thread.currentThread().getContextClassLoader().getResource(prefix + systemId);
            LSInput input = new MyLSInput(schema);
            input.setBaseURI(baseURI);
            input.setPublicId(publicId);
            input.setBaseURI(baseURI);
            return input;
        }

        public static class MyLSInput implements LSInput {
            URL url;

            public MyLSInput(URL url) {
                this.url = url;
            }

            private String systemId, publicId, baseURI;

            @Override
            public Reader getCharacterStream() {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void setCharacterStream(Reader characterStream) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public InputStream getByteStream() {
                try {
                    return url.openStream();
                } catch (IOException e) {
                    return null;
                }
            }

            @Override
            public void setByteStream(InputStream byteStream) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public String getStringData() {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void setStringData(String stringData) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public String getSystemId() {
                return systemId;
            }

            @Override
            public void setSystemId(String systemId) {
                this.systemId = systemId;
            }

            @Override
            public String getPublicId() {
                return publicId;
            }

            @Override
            public void setPublicId(String publicId) {
                this.publicId = publicId;
            }

            @Override
            public String getBaseURI() {
                return baseURI;
            }

            @Override
            public void setBaseURI(String baseURI) {
                this.baseURI = baseURI;
            }

            @Override
            public String getEncoding() {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void setEncoding(String encoding) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public boolean getCertifiedText() {
                return false;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void setCertifiedText(boolean certifiedText) {
                //To change body of implemented methods use File | Settings | File Templates.
            }
        };
    }
}
