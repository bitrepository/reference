/** Bit Repository Standard Header Open Source License */
package dk.bitmagasin.common;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

/**
 * Class in the mockup for handling the XML.
 * 
 * @author jolf
 *
 */
public class MockupXmlDocument {
	/** The XML-document. */
	Document doc;
	/** The root of the XML-document. */
	Element root;

	/**
	 * Test case.
	 * 
	 * @param args NOT HANDLED!
	 */
	public static void main(String... args) {
		try {
			MockupXmlDocument xmlDoc1 = new MockupXmlDocument();
			xmlDoc1.setLeafValue("message.operationId", "GetTime");
			xmlDoc1.setLeafValue("message.conversationId", "abc");
			xmlDoc1.addNewBranchValue("message.pillarIds", "pillarId", "pillar1");
			xmlDoc1.addNewBranchValue("message.pillarIds", "pillarId", "pillar2");
			xmlDoc1.addNewBranchValue("message.pillarIds", "pillarId", "pillar4");
			xmlDoc1.addNewBranchValue("message.dataIds", "dataId", "id1");
			xmlDoc1.setLeafValue("replyQueueName", "QReplyQueueTime");
//			System.out.println(xmlDoc1.asXML());
//			System.out.println();
//			
//			MockupXmlDocument xmlDoc2 = new MockupXmlDocument();
//			xmlDoc2.addNewLeaf("message.operationId", "GetTimeReply");
//			xmlDoc2.addNewLeaf("message.conversationId", "abc");
//			xmlDoc2.addNewLeaf("message.time.measure", "3600");
//			xmlDoc2.addNewLeaf("message.time.unit", "sec");
//			System.out.println(xmlDoc2.asXML());
//			System.out.println();
//
//			MockupXmlDocument xmlDoc3 = new MockupXmlDocument();
//			xmlDoc3.addNewLeaf("message.operationId", "GetTimeReply");
//			xmlDoc3.addNewLeaf("message.conversationId", "abc");
//			xmlDoc3.addNewLeaf("message.time.measure", "2");
//			xmlDoc3.addNewLeaf("message.time.unit", "sec");
//			System.out.println(xmlDoc3.asXML());
//			System.out.println();
//
//			MockupXmlDocument xmlDoc4 = new MockupXmlDocument();
//			xmlDoc4.addNewLeaf("message.operationId", "GetTimeReply");
//			xmlDoc4.addNewLeaf("message.conversationId", "abc");
//			xmlDoc4.addNewLeaf("message.error", "47");
//			System.out.println(xmlDoc4.asXML());
//			System.out.println();
			
			MockupXmlDocument doc1 = new MockupXmlDocument(
					"<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + "\n"
					+ "<message>"
					+ "<operationId>GetTimeReply</operationId><conversationId>"
					+ "abc</conversationId><time><measure>2</measure><unit>sec"
					+ "</unit></time></message>");
			System.out.println(doc1.asXML());
			
			System.out.println();
			System.out.println(doc1.getLeafValue("message.operationId"));
			System.out.println(xmlDoc1.getLeafValues("message.pillarIds.pillarId"));

		} catch (Exception e) {
			System.err.println(e);
			e.printStackTrace();
		}
	}
	
	/**
	 * Creates a new and empty XML-document.
	 */
	public MockupXmlDocument() {
		doc = DocumentHelper.createDocument();
		root = doc.addElement("message");
	}
	
	/**
	 * Creates the document from XML-code.
	 * 
	 * @param content The content of XML-document.
	 * @throws Exception If it cannot parse the content.
	 */
	public MockupXmlDocument(String content) throws Exception {
		doc = DocumentHelper.parseText(content);
		root = doc.getRootElement();
	}
	
	/**
	 * Retrieves the value of the leaf along a specified path. If several 
	 * leafs exists along this path, then the first leaf is chosen.
	 * 
	 * @param path The path to the leaf.
	 * @return The value of the leaf at the end of the path.
	 */
	public String getLeafValue(String path) {
		// remove the path to the root node.
		if(path.startsWith("message")) {
			path = path.replaceFirst("message.", "");
		}
		
		Element e = root;
		for(String p : path.split("[.]")) {
			if(e.element(p) != null) {
				e = e.element(p);
			} else {
				return null;
			}
		}
		
		return e.getText();
	}
	
	/**
	 * Method for retrieving all the values along a given path.
	 * Removes the path to the root node (if any), and then start the iteration
	 * along the path for retrieval of all the leaf values.
	 * 
	 * @param path The path where the values should exist.
	 * @return The list of values at the leaf node of all possible leafs for 
	 * the given path.
	 */
	public List<String> getLeafValues(String initPath) {
		// remove the path to the root node.
		if(initPath.startsWith("message")) {
			initPath = initPath.replaceFirst("message.", "");
		}
		
		List<String> path = new ArrayList<String>();
		Collections.addAll(path, initPath.split("[.]"));
		
		return getLeafValues(root, path);
	}
	
	/**
	 * Goes recursively to all leafs along the path to collect all their 
	 * values.
	 * 
	 * @param currentElement The current element, where we take our stand.
	 * @param path The path to the leaf. Allowed to be an empty list.
	 * @return The list of values at the leaf from this element.
	 */
	@SuppressWarnings("unchecked")
	protected List<String> getLeafValues(Element currentElement, List<String> path) {
		List<String> resultSet = new ArrayList<String>();
		// if no more branches, then the path is to leafs.
		if(path.size() < 1) {
			if(currentElement.isTextOnly()) {
				resultSet.add(currentElement.getText());
			} else {
				System.err.println("Tries to retrieve values for "
						+ "non-leaf: " + path);
			}
			return resultSet;
		}
		
		// Follow all possible branches along the path.
		for(Element branch : (List<Element>) currentElement.elements(path.get(0))) {
			List<String> subRes = getLeafValues(branch, path.subList(1, path.size()));
			// add results from branch to total resulting list.
			if(subRes != null && !subRes.isEmpty()) {
				resultSet.addAll(subRes);
			}
		}
		
		return resultSet;
	}
	
	/**
	 * Ensures that the path to a given element exists, and returns the element
	 * at the end of the path.
	 * 
	 * @param path The path to the element.
	 * @return The element at the end of the path.
	 */
	protected Element makePath(String path) {
		// remove the path to the root node.
		if(path.startsWith("message")) {
			path = path.replaceFirst("message.", "");
		}
		
		Element e = root;
		for(String p : path.split("[.]")) {
			if(e.element(p) != null) {
				e = e.element(p);
			} else {
				e = e.addElement(p);
			}
		}
		return e;
	}
	
	/**
	 * Goes through the path and if an element is missing, then it is created.
	 * At the end is the leaf, which is given the value.
	 *  
	 * @param path The path to the leaf.
	 * @param value The value of the leaf.
	 */
	public void setLeafValue(String path, String value) {
		Element e = makePath(path);
		e.addText(value);
	}
	
	/**
	 * 
	 * Gives the possibility to create several branches at the same level.
	 * 
	 * @param pathToBranch
	 * @param branchPath
	 * @param value
	 */
	public void addNewBranchValue(String pathToBranch, String branchPath, 
			String value) {
		Element e = makePath(pathToBranch);
		
		// create the branch from the pathToBranch
		for(String p : branchPath.split("[.]")) {
			e = e.addElement(p);
		}
		
		// add the content to the final destination
		e.addText(value);
	}
	
	/**
	 * Returns the entire structure as XML.
	 * 
	 * @return The document as XML.
	 */
	public String asXML() {
		return doc.asXML();
	}
}
