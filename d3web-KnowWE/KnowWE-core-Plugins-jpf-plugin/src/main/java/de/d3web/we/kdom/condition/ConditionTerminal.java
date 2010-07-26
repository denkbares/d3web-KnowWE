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

package de.d3web.we.kdom.condition;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import de.d3web.we.kdom.DefaultAbstractKnowWEObjectType;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.basic.AnonymousType;
import de.d3web.we.kdom.report.KDOMReportMessage;
import de.d3web.we.kdom.report.message.UnexpectedSequence;
import de.d3web.we.kdom.sectionFinder.AllTextFinderTrimmed;
import de.d3web.we.kdom.subtreeHandler.SubtreeHandler;

/**
 * The TerminalCondition type of the CompositeCondition
 * {@link CompositeCondition}
 * 
 * A section of this type is instantiated for each leaf of the
 * CompositeCondition tree. Various allowed concrete terminal-conditions can be
 * registered as child-types. Section which are not accepted by one of the
 * registered terminal-conditions are automatically labeled with an error as
 * "unrecognized-terminal-condition".
 * 
 * @author Jochen
 * 
 */
public class ConditionTerminal extends DefaultAbstractKnowWEObjectType {
	@Override
	protected void init() {
		this.sectionFinder = new AllTextFinderTrimmed();

		// last: Anything left is an UnrecognizedTC throwing an error
		AnonymousType unrecognizedCond = new AnonymousType(
				"UnrecognizedTerminalCondition");
		unrecognizedCond.setSectionFinder(new AllTextFinderTrimmed());
		unrecognizedCond.addSubtreeHandler(new SubtreeHandler<ConditionTerminal>() {
			@Override
			public Collection<KDOMReportMessage> create(KnowWEArticle article, Section s) {
				return Arrays.asList((KDOMReportMessage) new UnexpectedSequence(
						"no valid TerminalCondition: "
								+ s.getOriginalText()));
			}
		});

		this.addChildType(unrecognizedCond);
	}

	/**
	 * 
	 * Via this method all valid TerminalConditions are registered. The
	 * KDOM-types of these allowed TerminalConditions are registered as children
	 * types of this type
	 * 
	 * @created 26.07.2010
	 * @param types
	 */
	public void setAllowedTerminalConditions(List<? extends ConditionTerminal> types) {
		this.childrenTypes.addAll(0, types);
	}

}
