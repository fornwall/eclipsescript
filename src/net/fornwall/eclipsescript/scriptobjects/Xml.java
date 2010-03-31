package net.fornwall.eclipsescript.scriptobjects;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class Xml {

	private final Resources resources;

	public Xml(Resources resources) {
		this.resources = resources;
	}

	public Document parse(Object sourceObject) throws Exception {
		String source;
		if (sourceObject instanceof String) {
			source = (String) sourceObject;
		} else {
			source = resources.read(sourceObject);
		}
		DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
		builderFactory.setValidating(false);
		builderFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false); //$NON-NLS-1$
		DocumentBuilder builder = builderFactory.newDocumentBuilder();
		return builder.parse(new ByteArrayInputStream(source.getBytes("utf-8"))); //$NON-NLS-1$
	}

	public List<Element> xpath(Document doc, String expression) throws Exception {
		XPathFactory factory = XPathFactory.newInstance();
		XPath xpath = factory.newXPath();
		NodeList resultNodeList = (NodeList) xpath.evaluate(expression, doc, XPathConstants.NODESET);
		List<Element> resultList = new ArrayList<Element>(resultNodeList.getLength());
		for (int i = 0; i < resultNodeList.getLength(); i++) {
			resultList.add((Element) resultNodeList.item(i));
		}
		return resultList;
	}
}
