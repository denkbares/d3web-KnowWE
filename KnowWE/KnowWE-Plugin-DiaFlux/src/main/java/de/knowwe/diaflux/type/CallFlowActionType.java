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

import de.knowwe.core.kdom.AbstractType;
import de.knowwe.core.kdom.basicType.KeywordType;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.kdom.sectionFinder.RegexSectionFinder;
import de.knowwe.core.utils.Patterns;

/**
 * 
 * @author Reinhard Hatko
 * @created 15.11.2010
 */
public class CallFlowActionType extends AbstractType {

	private static final int STARTNODE_GROUP = 1;
	private static final String REGEX_STARTNDOE = "\\(([^()\"]*|" + Patterns.QUOTED + ")\\)";
	private static final Pattern PATTERN_STARTNDOE = Pattern.compile(REGEX_STARTNDOE);

	private static final int ACTION_GROUP = 1;
	private static final int FLOWCHART_GROUP = 2;
	private static final String REGEX = "^\\s*(CALL\\[(.*)" + REGEX_STARTNDOE + "\\])\\s*$";
	private static final Pattern PATTERN = Pattern.compile(REGEX, Pattern.CASE_INSENSITIVE);

	public CallFlowActionType() {
		setSectionFinder(new RegexSectionFinder(PATTERN, ACTION_GROUP));

		FlowchartReference flowchartReference = new FlowchartReference();
		flowchartReference.setSectionFinder(new RegexSectionFinder(PATTERN, FLOWCHART_GROUP));
		addChildType(flowchartReference);

		StartNodeReference startNodeReference = new StartNodeReference();
		startNodeReference.setSectionFinder(new RegexSectionFinder(PATTERN_STARTNDOE,
				STARTNODE_GROUP));
		addChildType(startNodeReference);

		addChildType(new KeywordType("["));
		addChildType(new KeywordType("]"));
		addChildType(new KeywordType("CALL"));
	}

	public static String getStartNodeName(Section<CallFlowActionType> action) {
		Section<StartNodeReference> nodeSection = Sections.child(action,
				StartNodeReference.class);
		return nodeSection.get().getTermName(nodeSection);
	}

	public static String getFlowName(Section<CallFlowActionType> action) {
		Section<FlowchartReference> flowRefSection = Sections.child(action,
				FlowchartReference.class);
		return flowRefSection.get().getTermName(flowRefSection);
	}

}