/*
 * Copyright (C) 2009 Chair of Artificial Intelligence and Applied Informatics
 * Computer Science VI, University of Wuerzburg
 * 
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option) any
 * later version.
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */

package de.d3web.persistence.tests.utils;

import java.io.IOException;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * @author bates This is a utility-class that helps you generating e.g.
 *         DOM-Nodes from XML-code
 */
public class XMLTagUtils {

	/**
	 * @throws SAXException Method that generates a DOM-Node from XMLCode
	 * 
	 * @param code the XML-code to generate the DOM-Node from
	 * @param nodeName the node-name the DOM-Node shall be created for
	 * @param index tells the converter which appearence of the tag in the
	 *        XML-code shall be taken for conversion
	 * @return generated Node
	 * @throws ParserConfigurationException
	 * @throws IOException
	 * @throws SAXException
	 */
	public static Node generateNodeFromXMLCode(
			String code,
			String nodeName,
			int index) throws ParserConfigurationException, IOException, SAXException {
		DocumentBuilder dBuilder =
				DocumentBuilderFactory.newInstance().newDocumentBuilder();
		StringReader strr = new StringReader(code);

		InputSource is = new InputSource(strr);
		Document doc = dBuilder.parse(is);
		return doc.getElementsByTagName(nodeName).item(index);
	}

}
