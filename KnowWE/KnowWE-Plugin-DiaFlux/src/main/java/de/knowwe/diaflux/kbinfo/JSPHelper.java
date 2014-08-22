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

package de.knowwe.diaflux.kbinfo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import de.d3web.strings.Identifier;
import de.d3web.strings.Strings;
import de.knowwe.core.Attributes;
import de.knowwe.core.Environment;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.user.UserContext;
import de.knowwe.diaflux.type.FlowchartType;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupType;
import de.knowwe.kdom.xml.AbstractXMLType;

public class JSPHelper {

	private final UserContext userContext;

	public JSPHelper(UserContext userContext) {
		this.userContext = userContext;
		if (this.userContext.getWeb() == null) {
			this.userContext.getParameters().put(Attributes.WEB,
					Environment.DEFAULT_WEB);
		}
	}

	private static Collection<Identifier> getAllMatches(String className, String web, String flowchartSectionID) {
		return SearchInfoObjects.searchObjects(
				Environment.getInstance(),
				web, null, className, 65535, flowchartSectionID);
	}

	public String getArticleIDsAsArray(String flowchartSectionID) {
		List<Identifier> matches = new ArrayList<Identifier>(
				getAllMatches("Article", this.userContext.getWeb(), flowchartSectionID));
		Collections.sort(matches);
		StringBuffer buffer = new StringBuffer();
		buffer.append("[");
		boolean first = true;
		for (Identifier identifier : matches) {
			// filters KnowWE-Doc from object tree
			if (identifier.getPathElementAt(0).startsWith("Doc ")) continue;

			if (first) {
				first = false;
			}
			else {
				buffer.append(", ");
			}
			String quotable = identifier.toExternalForm().replace("'", "\\'");
			buffer.append("'").append(quotable).append("'");
		}
		buffer.append("]");
		return buffer.toString();
	}

	public String getSectionText(String id) {
		Section<?> sec = Sections.get(id);
		String data = "Section not found: " + id;
		if (sec != null) {
			data = sec.getText();
		}
		return data;
	}

	public String getArticleInfoObjectsAsXML(String flowchartSectionID) {
		// search for matches
		Collection<Identifier> matches =
				getAllMatches("Article", this.userContext.getWeb(), flowchartSectionID);

		// fill the response buffer
		StringBuilder bob = new StringBuilder();
		GetInfoObjects.appendHeader(bob);
		for (Identifier id : matches) {
			GetInfoObjects.appendInfoObject(this.userContext.getWeb(), id, bob);
		}
		GetInfoObjects.appendFooter(bob);

		// and done
		return bob.toString();
	}

	public String getReferredInfoObjectsAsXML(String flowchartSectionID) {
		return getReferrdInfoObjectsAsXML(this.userContext.getWeb(), flowchartSectionID);
	}

	public static String getReferrdInfoObjectsAsXML(String web, String flowchartSectionID) {
		// TODO: extract used object ids from flowchart as a list
		// for now we simply use all existing objects
		Collection<Identifier> matches = getAllMatches(null, web, flowchartSectionID);

		// fill the response buffer
		StringBuilder bob = new StringBuilder();
		GetInfoObjects.appendHeader(bob);
		for (Identifier identifier : matches) {
			GetInfoObjects.appendInfoObject(web, identifier, bob);
		}
		GetInfoObjects.appendFooter(bob);

		// and done
		return bob.toString();

	}

	@SuppressWarnings("unchecked")
	public String loadFlowchart(String kdomID) {

		Section<DefaultMarkupType> diaFluxSection = (Section<DefaultMarkupType>) Sections.get(kdomID);

		if (diaFluxSection == null) {
			throw new IllegalArgumentException("Could not find flowchart at: "
					+ kdomID);
		}

		Section<FlowchartType> flowchart = Sections.successor(diaFluxSection,
				FlowchartType.class);

		if (flowchart == null) {
			return Strings.encodeHtml(getEmptyFlowchart());
		}

		return Strings.encodeHtml(flowchart.getText());
	}

	/**
	 * 
	 * @created 25.11.2010
	 * @return
	 */
	private String getEmptyFlowchart() {
		String id = createNewFlowchartID();
		return "<flowchart fcid=\"flow_"
				+ id
				+ "\" name=\"New Flowchart\" icon=\"sanduhr.gif\" width=\"750\" height=\"500\" idCounter=\"1\">"
				+ "</flowchart>";
	}

	/**
	 * 
	 * @created 23.02.2011
	 * @return
	 */
	public static String createNewFlowchartID() {
		return UUID.randomUUID().toString().substring(0, 8);
	}

	public String getFlowchartID() {
		return getFlowchartAttributeValue("fcid");
	}

	public String getFlowchartWidth() {
		return getFlowchartAttributeValue("width");
	}

	public String getFlowchartHeight() {
		return getFlowchartAttributeValue("height");
	}

	private String getFlowchartAttributeValue(String attributeName) {
		Section<FlowchartType> section = Sections.successor(
				Sections.get(userContext.getParameter("kdomID")), FlowchartType.class);

		return AbstractXMLType.getAttributes(section).get(attributeName);
	}
}
