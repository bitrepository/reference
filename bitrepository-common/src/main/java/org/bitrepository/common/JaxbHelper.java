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

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.SAXParser;
import javax.xml.transform.sax.SAXSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.URL;
import java.util.List;

/**
 * Provides extra JAXB related utilities
 */
public final class JaxbHelper {

	private String pathToSchema;
	/** Hides constructor for this utility class to prevent instantiation */
	public JaxbHelper(String pathToSchema) {
		ArgumentValidator.checkNotNullOrEmpty(pathToSchema, "pathToSchema");
		this.pathToSchema = pathToSchema;
	}

	/**
	 * Uses JAXB to create a object representation of an xml file. The class used to load the XML has been generated
	 * based on the xsd for the xml.
	 * @param xmlroot The root class to deserialize to.
	 * @param inputStream The input stream containing the xml data.
	 * @return Returns a new object representation of the xml data. 
	 * @throws JAXBException The attempt to load the xml into a new object representation failed
	 * @throws SAXException 
	 */
	public <T> T loadXml(Class<T> xmlroot, InputStream inputStream) throws JAXBException {
		ArgumentValidator.checkNotNull(xmlroot, "xmlroot");
		ArgumentValidator.checkNotNull(inputStream, "inputStream");
		JAXBContext context = JAXBContext.newInstance(xmlroot);
		Unmarshaller unmarshaller = context.createUnmarshaller();
		return xmlroot.cast(unmarshaller.unmarshal(inputStream));
	}

	public void validate(InputStream inputStream) throws SAXException{
		InputStream schemaStream = ClassLoader.getSystemResourceAsStream(pathToSchema);
		LSResourceResolver resourceResolver = new ResourceResolver();
		SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
		schemaFactory.setResourceResolver(resourceResolver);
		Schema schema;
		schema = schemaFactory.newSchema(new SAXSource(new InputSource(schemaStream)));

		try {
			schema.newValidator().validate(new SAXSource(new InputSource(inputStream)));
		
		} catch (IOException e) {
			e.printStackTrace();

			return;
		}
		return;
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

	public static class ResourceResolver implements LSResourceResolver {
		public ResourceResolver() {
		}

		@Override
		public LSInput resolveResource(String type, String namespaceURI, String publicId, String systemId,
				String baseURI) {
			//The key here is SystemID.
			if (systemId == null) {
				return null;
			}
			
			URL schema = ClassLoader.getSystemResource(systemId);
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
