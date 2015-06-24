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
import java.util.regex.PatternSyntaxException;

import de.d3web.core.inference.condition.CondRegex;
import de.d3web.core.inference.condition.Condition;
import de.d3web.core.knowledge.terminology.Question;
import de.d3web.we.knowledgebase.D3webCompiler;
import de.d3web.we.object.QuestionReference;
import de.knowwe.core.kdom.AbstractType;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.kdom.sectionFinder.AllTextFinderTrimmed;
import de.knowwe.core.kdom.sectionFinder.RegexSectionFinder;
import de.knowwe.core.report.Message;
import de.knowwe.core.report.Messages;
import de.knowwe.kdom.AnonymousType;
import de.knowwe.kdom.constraint.ConstraintSectionFinder;
import de.knowwe.kdom.constraint.SingleChildConstraint;
import de.knowwe.kdom.renderer.StyleRenderer;
import de.knowwe.kdom.sectionFinder.StringSectionFinderUnquoted;

/**
 *
 *
 * @author volker_belli
 * @created 06.03.2013
 */
public class CondRegularExpression extends D3webCondition<CondRegularExpression> {

	private static final Pattern PATTERN = Pattern.compile(
			"^.+?=\\s*/(.*)/\\s*$",
			Pattern.CASE_INSENSITIVE);

	public CondRegularExpression() {
		setSectionFinder(new RegexSectionFinder(PATTERN, 0));

		// comparator
		AnonymousType comparator = new AnonymousType("equals");
		comparator.setSectionFinder(new StringSectionFinderUnquoted("="));
		this.addChildType(comparator);

		// question
		QuestionReference questionRef = new QuestionReference();
		ConstraintSectionFinder questionFinder = new ConstraintSectionFinder(
				new AllTextFinderTrimmed());
		questionFinder.addConstraint(SingleChildConstraint.getInstance());
		questionRef.setSectionFinder(questionFinder);
		this.addChildType(questionRef);

		// regex to be checked
		this.addChildType(new RegexType());

	}

	@Override
	protected Condition createCondition(D3webCompiler compiler, Section<CondRegularExpression> section) {
		Section<QuestionReference> qRef = Sections.successor(section, QuestionReference.class);
		Section<RegexType> valueSec = Sections.successor(section, RegexType.class);

		if (valueSec == null || qRef == null) {
			// should not happen due to our regexp
			return null;
		}

		Question question = qRef.get().getTermObject(compiler, qRef);
		String regex = valueSec.getText();
		regex = regex.substring(1, regex.length() - 1);

		if (question == null) {
			Message msg = Messages.noSuchObjectError(Question.class.getSimpleName(), qRef.getText());
			Messages.storeMessage(compiler, qRef, getClass(), msg);
			return null;
		}

		try {
			return new CondRegex(question, regex);
		}
		catch (PatternSyntaxException e) {
			Message msg = Messages.syntaxError(
					"'" + regex + "' is not valid regular expression");
			Messages.storeMessage(compiler, valueSec, getClass(), msg);
			return null;
		}
	}

	private static class RegexType extends AbstractType {

		public RegexType() {
			this.setSectionFinder(new AllTextFinderTrimmed());
			this.setRenderer(StyleRenderer.CHOICE);
		}
	}

}
