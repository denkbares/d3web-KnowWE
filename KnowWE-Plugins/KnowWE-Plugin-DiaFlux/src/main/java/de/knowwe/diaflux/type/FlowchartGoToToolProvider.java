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
package de.knowwe.diaflux.type;

import java.util.Collection;
import java.util.TreeSet;

import de.d3web.we.knowledgebase.D3webCompiler;
import de.d3web.we.utils.D3webUtils;
import de.knowwe.core.compile.terminology.TerminologyManager;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.user.UserContext;
import de.knowwe.core.utils.KnowWEUtils;
import de.knowwe.diaflux.type.ExitType.ExitNodeDef;
import de.knowwe.diaflux.type.FlowchartXMLHeadType.FlowchartTermDef;
import de.knowwe.diaflux.type.StartType.StartNodeDef;
import de.knowwe.tools.DefaultTool;
import de.knowwe.tools.Tool;
import de.knowwe.tools.ToolProvider;
import de.knowwe.tools.ToolUtils;
import de.knowwe.util.Icon;

/**
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 16.10.2013
 */
public class FlowchartGoToToolProvider implements ToolProvider {

	@Override
	public Tool[] getTools(Section<?> section, UserContext userContext) {

		TreeSet<Tool> tools = new TreeSet<>();
		D3webCompiler compiler = D3webUtils.getCompiler(userContext, section);
		if (compiler == null) return ToolUtils.emptyToolArray();
		TerminologyManager terminologyManager = compiler.getTerminologyManager();
		if (section.get() instanceof StartNodeDef || section.get() instanceof ExitNodeDef) {
			Collection<Section<?>> termRefSections = terminologyManager.getTermReferenceSections(KnowWEUtils.getTermIdentifier(section));
			for (Section<?> termSection : termRefSections) {
				tools.add(createGoToTool(termSection));
			}
			if (section.get() instanceof ExitNodeDef) {
				// if the outgoing edge of the calling node uses processed
				// instead of an exit node, we also want to show the parent flow
				Section<FlowchartType> flowchartSection = Sections.ancestor(
						section, FlowchartType.class);
				Section<FlowchartTermDef> flowTermDef = Sections.successor(flowchartSection,
						FlowchartTermDef.class);
				// we get all references to the owner flow of this exit node
				Collection<Section<?>> flowReferenceSections = terminologyManager.getTermReferenceSections(KnowWEUtils.getTermIdentifier(flowTermDef));
				for (Section<?> flowReference : flowReferenceSections) {
					// now we look for the references that are processed
					// conditions
					Section<FlowchartProcessedConditionType> processed = Sections.ancestor(
							flowReference, FlowchartProcessedConditionType.class);
					if (processed != null) {
						tools.add(createGoToTool(flowReference));
					}

				}
			}
		}
		if (section.get() instanceof FlowchartReference) {
			Section<?> termDefSection = terminologyManager.getTermDefiningSection(KnowWEUtils.getTermIdentifier(section));
			tools.add(createGoToTool(termDefSection));
		}

		return tools.toArray(new Tool[tools.size()]);
	}

	@Override
	public boolean hasTools(Section<?> section, UserContext userContext) {
		return getTools(section, userContext).length > 0;
	}

	private OpenFlowTool createGoToTool(Section<?> termSection) {
		Section<FlowchartType> flowchartSection = Sections.ancestor(
				termSection, FlowchartType.class);
		String link = KnowWEUtils.getURLLink(flowchartSection);
		String title = "Open '" + FlowchartType.getFlowchartName(flowchartSection) + "'";
		return new OpenFlowTool(Icon.ARTICLE,
				title, title, link, Tool.ActionType.HREF);
	}

	private class OpenFlowTool extends DefaultTool implements Comparable<OpenFlowTool> {

		private final String jsAction;

		public OpenFlowTool(Icon icon, String title, String description, String jsAction, ActionType actionType) {
			super(icon, title, description, jsAction, actionType, null);
			this.jsAction = jsAction;
		}

		@Override
		public int hashCode() {
			return this.jsAction.hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			return this.jsAction.equals(((OpenFlowTool) obj).jsAction);
		}

		@Override
		public int compareTo(OpenFlowTool o) {
			return this.jsAction.compareTo(o.jsAction);
		}

		@Override
		public String getCategory() {
			return this.getClass().getName();
		}

	}
}
