package net.fornwall.eclipsescript.scriptobjects;

import java.io.ByteArrayInputStream;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;

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
		return DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(
				new ByteArrayInputStream(source.getBytes("utf-8"))); //$NON-NLS-1$
	}

}
