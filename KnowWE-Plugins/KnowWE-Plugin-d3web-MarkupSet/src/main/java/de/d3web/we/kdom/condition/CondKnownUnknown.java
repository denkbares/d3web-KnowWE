/*
 * Copyright (C) 2010 denkbares GmbH, Germany
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

import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import de.d3web.core.inference.condition.CondKnown;
import de.d3web.core.inference.condition.CondUnknown;
import de.d3web.core.inference.condition.Condition;
import de.d3web.core.knowledge.terminology.Question;
import de.d3web.we.knowledgebase.D3webCompiler;
import de.d3web.we.object.QuestionReference;
import de.knowwe.core.kdom.AbstractType;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.kdom.sectionFinder.AllTextFinderTrimmed;
import de.knowwe.core.kdom.sectionFinder.RegexSectionFinder;
import de.knowwe.core.report.Messages;
import de.knowwe.kdom.AnonymousType;
import de.knowwe.kdom.constraint.ConstraintSectionFinder;
import de.knowwe.kdom.constraint.SingleChildConstraint;
import de.knowwe.kdom.renderer.StyleRenderer;
import de.knowwe.kdom.sectionFinder.StringSectionFinderUnquoted;

/**
 * @author volker_belli
 * @created 07.12.2010
 */
public class CondKnownUnknown extends D3webCondition<CondKnownUnknown> {
	private static final Logger LOGGER = LoggerFactory.getLogger(CondKnownUnknown.class);

	private enum Assignment {
		KNOWN, UNKNOWN
	}

	private static final Pattern PATTERN = Pattern.compile("^\\s*\\S.*\\s*=\\s*(?i:KNOWN|UNKNOWN)\\s*$");

	public CondKnownUnknown() {
		setSectionFinder(new RegexSectionFinder(PATTERN, 0));

		// comparator
		AnonymousType comparator = new AnonymousType("equals");
		comparator.setSectionFinder(new StringSectionFinderUnquoted("="));
		this.addChildType(comparator);

		// question
		QuestionReference questionRef = new QuestionReference();
		questionRef.setSectionFinder(new ConstraintSectionFinder(
				new AllTextFinderTrimmed(),
				SingleChildConstraint.getInstance()));
		this.addChildType(questionRef);

		// value to be checked
		this.addChildType(new AssignmentType());
	}

	@Override
	protected Condition createCondition(D3webCompiler compiler, Section<CondKnownUnknown> section) {
		Section<QuestionReference> qRef = Sections.successor(section, QuestionReference.class);
		Section<AssignmentType> valueSec = Sections.successor(section, AssignmentType.class);
		if (valueSec == null || qRef == null) {
			// should not happen due to our regexp
			return null;
		}

		Question question = qRef.get().getTermObject(compiler, qRef);
		if (question == null) {
			// we need no error here, cause QuestionRef has its own error
			return null;
		}

		try {
			switch (Assignment.valueOf(valueSec.getText().toUpperCase())) {
				case KNOWN:
					return new CondKnown(question);
				case UNKNOWN:
					return new CondUnknown(question);
			}
			throw new IllegalArgumentException();
		}
		catch (IllegalArgumentException e) {
			// should not happen due to our regexp
			LOGGER.error("unexpected internal error", e);
			Messages.storeMessage(compiler, section, getClass(), Messages.error(e));
			return null;
		}
	}

	private static class AssignmentType extends AbstractType {

		public AssignmentType() {
			this.setSectionFinder(new AllTextFinderTrimmed());
			this.setRenderer(StyleRenderer.OPERATOR);
		}
	}
}
