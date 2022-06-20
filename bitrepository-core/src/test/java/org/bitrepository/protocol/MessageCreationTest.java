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
package org.bitrepository.protocol;

import org.apache.activemq.util.ByteArrayInputStream;
import org.apache.commons.io.IOUtils;
import org.bitrepository.bitrepositorymessages.AlarmMessage;
import org.bitrepository.bitrepositorymessages.GetChecksumsFinalResponse;
import org.bitrepository.common.JaxbHelper;
import org.bitrepository.protocol.message.ExampleMessageFactory;
import org.jaccept.structure.ExtendedTestCase;
import org.testng.annotations.Test;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBException;
import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;

/**
 * Test whether we are able to create message objects from xml. The input XML is the example code defined in the
 * message-xml, thereby also testing whether this is valid. *
 */
public class MessageCreationTest extends ExtendedTestCase {
    @Test(groups = {"regressiontest"})
    public void messageCreationTest() throws Exception {
        addDescription("Tests if we are able to create message objects from xml. The input XML is the example code " +
                "defined in the message-xml, thereby also testing whether this is valid.");
        String[] messageNames = getMessageNames();
        for (String messageName : messageNames) {
            addStep("Creating " + messageName + " message",
                    "The test is able to instantiate message based on the example in the message-xml modules");
            ExampleMessageFactory.createMessage(
                    Class.forName(GetChecksumsFinalResponse.class.getPackage().getName() + "." + messageName));
        }
    }

    @Test(groups = {"regressiontest"}, expectedExceptions = SAXException.class)
    public void badDateMessageTest() throws IOException, SAXException, JAXBException {
        addDescription("Test to ensure that messages carrying dates must provide offset.");
        String messagePath = ExampleMessageFactory.PATH_TO_EXAMPLES + "BadMessages/" +
                "BadDateAlarmMessage" + ExampleMessageFactory.EXAMPLE_FILE_POSTFIX;
        InputStream messageIS = Thread.currentThread().getContextClassLoader().getResourceAsStream(messagePath);
        assert messageIS != null;
        String message = IOUtils.toString(messageIS, StandardCharsets.UTF_8);
        JaxbHelper jaxbHelper = new JaxbHelper(ExampleMessageFactory.PATH_TO_SCHEMA, ExampleMessageFactory.SCHEMA_NAME);
        jaxbHelper.validate(new ByteArrayInputStream(message.getBytes(StandardCharsets.UTF_8)));
        AlarmMessage am = jaxbHelper.loadXml(AlarmMessage.class, new ByteArrayInputStream(message.getBytes(StandardCharsets.UTF_8)));
    }

    /**
     * Generates the list of messages to test by parsing the message xsd file.
     *
     * @return List of messages to test
     */
    private String[] getMessageNames() throws Exception {
        InputStream f = Thread.currentThread().getContextClassLoader().getResourceAsStream("xsd/BitRepositoryMessages.xsd");
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        Document doc = factory.newDocumentBuilder().parse(f);

        XPathFactory xPathFactory = XPathFactory.newInstance();
        XPath xpath = xPathFactory.newXPath();
        xpath.setNamespaceContext(getNamespaceContext());
        XPathExpression expr = xpath.compile("/xs:schema/xs:element");
        Object result = expr.evaluate(doc, XPathConstants.NODESET);
        NodeList nodes = (NodeList) result;

        String[] messageNames = new String[nodes.getLength()];
        for (int i = 0; i < nodes.getLength(); i++) {
            messageNames[i] = nodes.item(i).getAttributes().getNamedItem("name")
                    .getNodeValue();
        }
        return messageNames;
    }

    /**
     * Needed by XPath to handle the namespaces.
     */
    private NamespaceContext getNamespaceContext() {
        NamespaceContext ctx = new NamespaceContext() {
            public String getNamespaceURI(String prefix) {
                String uri;
                switch (prefix) {
                    case "xs":
                        uri = "http://www.w3.org/2001/XMLSchema";
                        break;
                    case "xsi":
                        uri = "http://www.w3.org/2001/XMLSchema-instance";
                        break;
                    case "bre":
                        uri = "http://bitrepository.org/BitRepositoryElements.xsd";
                        break;
                    default:
                        uri = null;
                        break;
                }
                return uri;
            }

            // Dummy implementation - not used!
            public Iterator<String> getPrefixes(String val) {
                return null;
            }

            // Dummy implementation - not used!
            public String getPrefix(String uri) {
                return null;
            }
        };
        return ctx;
    }
}
