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

package de.d3web.we.kdom.condition;

import java.util.Collection;
import java.util.List;

import de.knowwe.core.kdom.AbstractType;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.sectionFinder.AllTextFinderTrimmed;
import de.knowwe.core.kdom.subtreeHandler.SubtreeHandler;
import de.knowwe.core.report.Message;
import de.knowwe.core.report.Messages;
import de.knowwe.kdom.AnonymousType;

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
public class TerminalCondition extends AbstractType {

	private static final String DEFAULT_typeName = "UnrecognizedTerminalCondition";

	private static final String DEFAULT_messageText = "no valid TerminalCondition: ";

	public TerminalCondition() {
		this(DEFAULT_typeName, DEFAULT_messageText);
	}

	public TerminalCondition(String typename, final String messageText) {
		this.sectionFinder = new AllTextFinderTrimmed();

		// last: Anything left is an UnrecognizedTC throwing an error
		AnonymousType unrecognizedCond = new AnonymousType(typename);
		unrecognizedCond.setSectionFinder(new AllTextFinderTrimmed());
		unrecognizedCond.addSubtreeHandler(new SubtreeHandler<TerminalCondition>() {

			@Override
			public Collection<Message> create(Article article, Section<TerminalCondition> s) {
				return Messages.asList(Messages.syntaxError(
						messageText + s.getText()));
			}
		});

		this.addChildType(unrecognizedCond);
	}

	public void setAllowedTerminalConditions(List<Type> types) {
		this.childrenTypes.addAll(3, types);
	}

}
