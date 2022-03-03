/*
 * Copyright (C) 2013 denkbares GmbH
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
package de.knowwe.diaflux;

import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.denkbares.strings.Strings;
import de.knowwe.core.Environment;
import de.knowwe.core.action.UserActionContext;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.objects.Term;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.tools.GetToolMenuAction;
import de.knowwe.diaflux.type.DiaFluxType;
import de.knowwe.diaflux.type.FlowchartType;
import de.knowwe.diaflux.type.NodeType;
import de.knowwe.kdom.xml.AbstractXMLType;

/**
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 10.10.2013
 */
public class FlowchartToolMenuAction extends GetToolMenuAction {
	private static final Logger LOGGER = LoggerFactory.getLogger(FlowchartToolMenuAction.class);

	@Override
	protected Section<? extends Type> getSection(UserActionContext context, String identifier) {
		return getSection(identifier);
	}

	public static Section<? extends Type> getSection(String identifier) {
		try {
			JSONObject json = new JSONObject(identifier);
			String title = (String) json.get("pagename");
			String flowname = (String) json.get("flowname");
			String nodeID = (String) json.get("nodeID");
			if (title == null || flowname == null || nodeID == null) return null;
			title = Strings.decodeURL(title);
			Article article = Environment.getInstance().getArticle(Environment.DEFAULT_WEB, title);
			List<Section<DiaFluxType>> diafluxSections = Sections.children(
					article.getRootSection(), DiaFluxType.class);
			Section<FlowchartType> correctFlow = null;
			for (Section<DiaFluxType> section : diafluxSections) {
				Section<FlowchartType> flowchart = Sections.successor(section,
						FlowchartType.class);
				if (flowchart == null) continue;
				String name = DiaFluxType.getFlowchartName(section);
				if (name.equals(flowname)) {
					correctFlow = flowchart;
					break;
				}
			}
			if (correctFlow == null) return null;
			List<Section<NodeType>> nodeSections = Sections.successors(correctFlow,
					NodeType.class);
			for (Section<NodeType> section : nodeSections) {
				String fcid = AbstractXMLType.getAttributes(section).get("fcid");
				if (fcid.equals(nodeID)) {
					return Sections.successor(section, Term.class);
				}
			}

		}
		catch (JSONException e) {
			LOGGER.error("Recieved faulty JSON string", e);
		}
		return null;
	}

}
