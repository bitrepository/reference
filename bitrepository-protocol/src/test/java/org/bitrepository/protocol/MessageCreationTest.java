package org.bitrepository.protocol;

import java.io.File;
import java.io.FileInputStream;
import java.util.Iterator;

import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.bitrepository.bitrepositorymessages.GetChecksumsComplete;
import org.bitrepository.protocol.MessageFactory;
import org.testng.annotations.Test;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

/** 
 * Test whether we are able to create message objects from xml. The input XML is the example code defined in the 
 * message-xml, thereby also testing whether this is valid. *
 */
public class MessageCreationTest {

	private static final String XML_MESSAGE_DIR = "target/message-xml/";

	@Test(groups = { "testfirst" })
	public void messageCreationTest() throws Exception {
		String[] messageNames = getMessageNames();
		for (String messageName:messageNames) {
			String xmlMessage = loadXMLExample(messageName);
			Object message = MessageFactory.createMessage(GetChecksumsComplete.class, xmlMessage);	
		}
	}

	/**
	 * Generates the list of messages to test by parsing the message xsd file.
	 * @return List of messages to test
	 */
	private String[] getMessageNames() throws Exception {
		File file = new File(XML_MESSAGE_DIR+"xsd/BitRepositoryMessages.xsd");
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		Document doc = factory.newDocumentBuilder().parse(file);

		XPathFactory xPathFactory = XPathFactory.newInstance();
		XPath xpath = xPathFactory.newXPath();
		xpath.setNamespaceContext(getNamespaceContext ());
		XPathExpression expr = xpath.compile("/xs:schema/xs:element");
		Object result = expr.evaluate(doc, XPathConstants.NODESET);
		NodeList nodes = (NodeList) result;
		System.out.println("Number of nodes = " + nodes.getLength());

		String[] messageNames = new String[nodes.getLength()];
		for (int i = 0; i < nodes.getLength() ; i++) {
			messageNames[i] = nodes.item(i).getAttributes().getNamedItem("name").getNodeValue();
		}
		return messageNames;
	}

	/**
	 * Needed by XPath to handle the namespaces.
	 */
	private NamespaceContext getNamespaceContext () {
		NamespaceContext ctx = new NamespaceContext() {
			public String getNamespaceURI(String prefix) {
				String uri;
				if (prefix.equals("xs"))
					uri = "http://www.w3.org/2001/XMLSchema";
				else if (prefix.equals("xsi"))
					uri = "http://www.w3.org/2001/XMLSchema-instance";
				else if (prefix.equals("bre"))
					uri = "http://bitrepository.org/BitRepositoryElements.xsd";
				else
					uri = null;
				return uri;
			}

			// Dummy implementation - not used!
			@SuppressWarnings("rawtypes")
			public Iterator getPrefixes(String val) {
				return null;
			}

			// Dummy implemenation - not used!
			public String getPrefix(String uri) {
				return null;
			}
		};
		return ctx;
	}

	/**
	 * Loads the example XML for the indicated message. Assumes the XML examples are found under the 
	 * XML_MESSAGE_DIR/examples directory, and the naming convention for the example files are '${messagename}.xml'
	 * 
	 * @param messageName
	 * @return
	 */
	private String loadXMLExample(String messageName) throws Exception {
		String filePath = XML_MESSAGE_DIR + "examples/" + messageName + ".xml";
		byte[] buffer = new byte[(int) new File(filePath).length()];
	    FileInputStream f = new FileInputStream(filePath);
	    f.read(buffer);
	    return new String(buffer);
	}
}
