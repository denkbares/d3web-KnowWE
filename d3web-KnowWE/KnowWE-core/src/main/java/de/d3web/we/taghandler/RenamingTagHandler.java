/*
 * Copyright (C) 2009 Chair of Artificial Intelligence and Applied Informatics
 *                    Computer Science VI, University of Wuerzburg
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package de.d3web.we.taghandler;


import java.io.StringWriter;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;


import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.wikiConnector.KnowWEUserContext;

/**
 * <p>This class handles the appearance of the ReanmingTool tag.</p>
 */
public class RenamingTagHandler extends AbstractTagHandler {
	
	public RenamingTagHandler() {
		super("RenamingTool");

	}

	@Override
	public String getDescription(KnowWEUserContext user) {
		return KnowWEEnvironment.getInstance().getKwikiBundle(user).getString("KnowWE.RenamingTagHandler.description");
	}
	
	/**
	 * <p>Returns a HTML representation of the renaming tool form.</p>
	 */
	@Override
	public String render(String topic, KnowWEUserContext user, Map<String,String> values, String web) {
		StringBuffer html = new StringBuffer();
		
		ResourceBundle rb = KnowWEEnvironment.getInstance().getKwikiBundle(user);
		
		html.append("<div id=\"rename-panel\" class=\"panel\"><h3>" + rb.getString("KnowWE.renamingtool.redefine") + "</h3>");
		
		html.append("<form method='post' action=''>");
		html.append("<fieldset>");
//		html.append("<legend> " + rb.getString("KnowWE.renamingtool.redefine") + " </legend>");
				
		html.append("<div class='left'>");
		html.append("<label for='renameInputField'>" + rb.getString("KnowWE.renamingtool.searchterm") + "</label>");
		html.append("<input id='renameInputField' type='text' name='TargetNamespace' value='' tabindex='1' class='field' title=''/>");
		html.append("</div>");	
		
		html.append("<div class='left'>");
		html.append("<label for='replaceInputField'>" + rb.getString("KnowWE.renamingtool.replace") + "</label>");
		html.append("<input id='replaceInputField' type='text' name='replaceTerm'  tabindex='2' class='field' title=''/>");
		html.append("</div>");
		
		html.append("<div id='search-button'>");
		html.append("<input type='button' value='" + rb.getString("KnowWE.renamingtool.preview") + "' name='submit' tabindex='3' class='button' title=''/>");
		html.append("</div>");
		
		html.append("<div style='clear:both'></div>");
		
		html.append("<p id='rename-show-extend' class='show-extend pointer extend-panel-down'>");
		html.append(rb.getString("KnowWE.renamingtool.settings") + "</p>");

		html.append("<div id='rename-extend-panel' class='hidden'>");
		
		html.append("<div class='left'>");
		html.append("<label for='renamePreviousInputContext'>" + rb.getString("KnowWE.renamingtool.previous") + "</label>"); 
		html.append("<input id='renamePreviousInputContext' type='text' name='' value='' tabindex='5' class='field'/>");
		html.append("</div>");
		
		html.append("<div class='left'>");
		html.append("<label for='renameAfterInputContext'>" + rb.getString("KnowWE.renamingtool.after") + "</label>");
		html.append("<input id='renameAfterInputContext' type='text' name='' value='' tabindex='6' class='field'/>");
		html.append("</div>");
		
		html.append("<div class='left'>");
	    html.append("<label for='search-sensitive'>" + rb.getString("KnowWE.renamingtool.case") + "</label>");
	    html.append("<input id='search-sensitive' type='checkbox' name='search-sensitive' tabindex='7' checked='checked'/>");
		html.append("</div>");		
		// includes the section selection tree. uses external yahoo yui files (should be integrated)
		html.append("" +
				"<!-- Combo-handled YUI CSS files: -->" +
				"<link rel='stylesheet' type='text/css' href='http://yui.yahooapis.com/combo?2.8.0r4/build/treeview/assets/skins/sam/treeview.css'>" +
				"<!-- Combo-handled YUI JS files: -->" +
				"<script type='text/javascript' src='http://yui.yahooapis.com/combo?2.8.0r4/build/yahoo-dom-event/yahoo-dom-event.js&amp;2.8.0r4/build/treeview/treeview-min.js'></script>" +
				"");
		html.append("\n<script type='text/javascript' src='"
				+ "KnowWEExtension/scripts/"
				+ "TreeView.js"
				+ "'></script>\n");
		html.append("<br><br>" + printULTree()+"\n");
		
		html.append("</div>");
		
		html.append("<input type='hidden' value='RenamingRenderer' name='action' />");
		html.append("</fieldset> ");
		html.append("</form>");
		html.append("<div id='rename-result'></div>");
		html.append("</div>");
		
		return html.toString();
	}
	
	private StringBuffer printULTree() {
		//input
		//get an instance of factory
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		Document dom = null;
		try {
			//get an instance of builder
			DocumentBuilder db = dbf.newDocumentBuilder();
			//create an instance of DOM
			dom = db.newDocument();
		}catch(ParserConfigurationException pce) {
			//dump it
			System.out.println("Error while trying to instantiate DocumentBuilder " + pce);
		}
		//fill
		//TODO
		KnowWEEnvironment ke = KnowWEEnvironment.getInstance();
		List<KnowWEObjectType> typeList = ke.getRootTypes();
		//set div and ul
		Element div = dom.createElement("div");
		div.setAttribute("id", "typeTree");
		div.setAttribute("class", "ygtv-checkbox");
		dom.appendChild(div);
		//
		Element ul = dom.createElement("ul");
		Element li = dom.createElement("li");
		Text rootNode = dom.createTextNode("alle Bereiche");
		li.appendChild(rootNode);
		//li.setAttribute("class", "expanded");
		ul.appendChild(li);
		div.appendChild(ul);
		printULNodes(typeList, dom, li, new HashSet<String>());
		
		//output
		Transformer tr = null;
		try {
			tr = TransformerFactory.newInstance().newTransformer();
		} catch (TransformerConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransformerFactoryConfigurationError e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		tr.setOutputProperty(OutputKeys.INDENT, "yes");
		tr.setOutputProperty(OutputKeys.METHOD,"html");
		tr.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "3");
		//to send the output to StringBuffer
		StringWriter sw = new StringWriter();
		try {
			tr.transform( new DOMSource(dom), new StreamResult(sw));
		} catch (TransformerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return sw.getBuffer();
	}
	
	private void printULNodes(List<KnowWEObjectType> typeList, Document dom, Element father, 
			HashSet<String> usedTypeOrig) {
		if (typeList != null) {
			Element ul = dom.createElement("ul");
			boolean ulHasChildren = false;
			for (KnowWEObjectType koType : typeList) {
				HashSet<String> usedType = (HashSet<String>) usedTypeOrig.clone();
				if (koType != null) {
					String koTypeName = koType.getName();
					if (usedType.add(koTypeName)) {
						ulHasChildren = true;
						//füge im dom hinzu
						Element li = dom.createElement("li");
						Text koTypeNameText = dom.createTextNode(koTypeName);
						li.appendChild(koTypeNameText);
						ul.appendChild(li);
						//mach mit den Kindern weiter
						printULNodes(koType.getAllowedChildrenTypes(), dom, li,
								usedType);
					}
				}
			}
			if (ulHasChildren) {
				father.appendChild(ul);
			}
		}
	}
}
