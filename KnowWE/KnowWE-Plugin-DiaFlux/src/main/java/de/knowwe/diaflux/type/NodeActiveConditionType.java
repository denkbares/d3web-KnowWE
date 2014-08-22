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

import de.d3web.core.inference.condition.Condition;
import de.d3web.diaFlux.inference.NodeActiveCondition;
import de.d3web.we.kdom.condition.D3webCondition;
import de.d3web.we.knowledgebase.D3webCompiler;
import de.knowwe.core.kdom.basicType.KeywordType;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.kdom.sectionFinder.RegexSectionFinder;
import de.knowwe.core.utils.Patterns;

/**
 * @author Reinhard Hatko
 * @created 15.11.2010
 */
public class NodeActiveConditionType extends D3webCondition<NodeActiveConditionType> {

	private static final int EXITNODE_GROUP = 1;
	private static final String REGEX_EXITNODE = "\\(([^()\"]*|" + Patterns.QUOTED + ")\\)";
	private static final Pattern PATTERN_EXITNODE = Pattern.compile(REGEX_EXITNODE);

	private static final int CONDITION_GROUP = 1;
	private static final int FLOWCHART_GROUP = 2;
	private static final String REGEX = "^\\s*(IS_ACTIVE\\[(.*)" + REGEX_EXITNODE + "\\])\\s*$";
	private static final Pattern PATTERN = Pattern.compile(REGEX, Pattern.CASE_INSENSITIVE);

	public NodeActiveConditionType() {
		setSectionFinder(new RegexSectionFinder(PATTERN, CONDITION_GROUP));

		FlowchartReference flowchartReference = new FlowchartReference();
		flowchartReference.setSectionFinder(new RegexSectionFinder(PATTERN, FLOWCHART_GROUP));
		addChildType(flowchartReference);

		ExitNodeReference exitNodeReference = new ExitNodeReference();
		exitNodeReference.setSectionFinder(new RegexSectionFinder(PATTERN_EXITNODE, EXITNODE_GROUP));
		addChildType(exitNodeReference);

		addChildType(new KeywordType("["));
		addChildType(new KeywordType("]"));
		addChildType(new KeywordType("IS_ACTIVE"));
	}

	@Override
	protected Condition createCondition(D3webCompiler compiler, Section<NodeActiveConditionType> section) {
		Section<FlowchartReference> flowRef =
				Sections.successor(section, FlowchartReference.class);
		Section<ExitNodeReference> nodeRef =
				Sections.successor(section, ExitNodeReference.class);

		if (flowRef == null || nodeRef == null) return null;

		String flowName = flowRef.get().getTermName(flowRef);
		String nodeName = nodeRef.get().getTermName(nodeRef);

		return new NodeActiveCondition(flowName, nodeName);
	}

}
