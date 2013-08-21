/*
 * Copyright (C) 2010 University Wuerzburg, Computer Science VI
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

import java.util.regex.Pattern;

import de.d3web.core.inference.PSAction;
import de.d3web.core.inference.PSMethod;
import de.d3web.diaFlux.inference.CallFlowAction;
import de.d3web.indication.inference.PSMethodStrategic;
import de.d3web.we.kdom.rules.action.D3webRuleAction;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.kdom.sectionFinder.RegexSectionFinder;
import de.knowwe.kdom.renderer.StyleRenderer;
import de.knowwe.kdom.renderer.StyleRenderer.MaskMode;

/**
 * 
 * @author Reinhard Hatko
 * @created 15.11.2010
 */
public class CallFlowActionType extends D3webRuleAction<CallFlowActionType> {

	public static final int FLOWCHART_GROUP = 1;
	public static final int STARTNODE_GROUP = 2;
	public static final String REGEX = "CALL\\[(.*?)\\((.*?)\\)\\]";
	public static final Pattern PATTERN = Pattern.compile(REGEX, Pattern.CASE_INSENSITIVE);

	public CallFlowActionType() {
		setSectionFinder(new RegexSectionFinder(PATTERN));

		FlowchartReference flowchartReference = new FlowchartReference();
		flowchartReference.setSectionFinder(new RegexSectionFinder(PATTERN, FLOWCHART_GROUP));
		flowchartReference.setRenderer(new StyleRenderer(MaskMode.htmlEntities));
		addChildType(flowchartReference);

		StartNodeReference startNodeReference = new StartNodeReference();
		startNodeReference.setSectionFinder(new RegexSectionFinder(Pattern.compile("\\((.*)\\)"), 1));
		startNodeReference.setRenderer(new StyleRenderer(MaskMode.htmlEntities));
		addChildType(startNodeReference);
	}

	@Override
	protected PSAction createAction(Article article,
			Section<CallFlowActionType> section) {

		String flowName = getStartNodeName(section);
		String nodeName = getFlowName(section);

		return new CallFlowAction(flowName, nodeName);

	}

	public static String getStartNodeName(Section<CallFlowActionType> action) {
		Section<StartNodeReference> nodeSection = Sections.findChildOfType(action,
				StartNodeReference.class);
		return nodeSection.getText();
	}

	public static String getFlowName(Section<CallFlowActionType> action) {
		Section<FlowchartReference> nodeSection = Sections.findChildOfType(action,
				FlowchartReference.class);
		return nodeSection.getText();
	}

	@Override
	public Class<? extends PSMethod> getActionPSContext() {
		return PSMethodStrategic.class;
	}

}