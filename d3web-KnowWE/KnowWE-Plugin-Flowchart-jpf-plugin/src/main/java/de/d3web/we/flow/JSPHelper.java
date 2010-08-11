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

package de.d3web.we.flow;

import java.util.List;

import de.d3web.we.core.KnowWEAttributes;
import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.core.KnowWEParameterMap;
import de.d3web.we.flow.kbinfo.GetInfoObjects;
import de.d3web.we.flow.kbinfo.SearchInfoObjects;
import de.d3web.we.flow.type.FlowchartType;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.xml.AbstractXMLObjectType;

public class JSPHelper {

	private final KnowWEParameterMap parameterMap;

	public JSPHelper(KnowWEParameterMap parameterMap) {
		this.parameterMap = parameterMap;
		if (this.parameterMap.getWeb() == null) {
			this.parameterMap.put(KnowWEAttributes.WEB, KnowWEEnvironment.DEFAULT_WEB);
		}
	}

	private List<String> getAllMatches(String className) {
		return SearchInfoObjects.searchObjects(
				de.d3web.we.core.KnowWEEnvironment.getInstance(),
				this.parameterMap.getWeb(),
				null, className, 65535);
	}

	public String getArticleIDsAsArray() {
		List<String> matches = getAllMatches("Article");
		StringBuffer buffer = new StringBuffer();
		buffer.append("[");
		boolean first = true;
		for (String id : matches) {
			if (first) {
				first = false;
			}
			else {
				buffer.append(", ");
			}
			buffer.append("'").append(id).append("'");
		}
		buffer.append("]");
		return buffer.toString();
	}

	public String getArticleInfoObjectsAsXML() {
		// search for matches
		List<String> matches = getAllMatches("Article");

		// fill the response buffer
		StringBuffer buffer = new StringBuffer();
		GetInfoObjects.appendHeader(this.parameterMap, buffer);
		for (String id : matches) {
			GetInfoObjects.appendInfoObject(this.parameterMap.getWeb(), id, buffer);
		}
		GetInfoObjects.appendFooter(this.parameterMap, buffer);

		// and done
		return buffer.toString();
	}

	public String getReferredInfoObjectsAsXML() {
		// TODO: extract used object ids from flowchart as a list
		// for now we simply use all existing objects
		List<String> matches = getAllMatches(null);

		// fill the response buffer
		StringBuffer buffer = new StringBuffer();
		GetInfoObjects.appendHeader(this.parameterMap, buffer);
		for (String id : matches) {
			GetInfoObjects.appendInfoObject(this.parameterMap.getWeb(), id, buffer);
		}
		GetInfoObjects.appendFooter(this.parameterMap, buffer);

		// and done
		return buffer.toString();
	}

	public String getKDOMNodeContent(String kdomID) {
		return KnowWEEnvironment.getInstance().getNodeData(parameterMap.getWeb(),
				parameterMap.getTopic(), kdomID);
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
		Section<FlowchartType> section = (Section<FlowchartType>) KnowWEEnvironment.getInstance().getArticle(parameterMap.getWeb(), parameterMap.getTopic()).findSection(parameterMap.get("kdomID"));
		
		return AbstractXMLObjectType.getAttributeMapFor(section).get(attributeName);
	}
}
