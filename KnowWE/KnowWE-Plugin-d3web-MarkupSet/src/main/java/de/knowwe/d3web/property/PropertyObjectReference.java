/*
 * Copyright (C) 2010 denkbares GmbH
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
package de.knowwe.d3web.property;

import java.util.Collection;
import java.util.regex.Pattern;

import de.d3web.core.knowledge.KnowledgeBase;
import de.d3web.core.knowledge.terminology.NamedObject;
import de.d3web.we.object.AnswerReference;
import de.d3web.we.object.D3webTerm;
import de.d3web.we.object.D3webTermReference;
import de.d3web.we.object.NamedObjectReference;
import de.d3web.we.object.QuestionReference;
import de.d3web.we.reviseHandler.D3webSubtreeHandler;
import de.knowwe.core.kdom.KnowWEArticle;
import de.knowwe.core.kdom.objects.SimpleTermReferenceRegistrationHandler;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.kdom.sectionFinder.AllTextFinderTrimmed;
import de.knowwe.core.kdom.sectionFinder.RegexSectionFinder;
import de.knowwe.core.report.Message;
import de.knowwe.core.report.Messages;
import de.knowwe.kdom.constraint.ConstraintSectionFinder;
import de.knowwe.kdom.constraint.NoChildrenOfOtherTypesConstraint;

/**
 * This is a type that represents a {@link NamedObject}. Depending on type of
 * the NamedObject, there are different renderings. getNamedObject(...) returns
 * the matching NamedObject from the {@link KnowledgeBase}.
 * 
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 02.08.2011
 */
public class PropertyObjectReference extends D3webTermReference<NamedObject> {

	public PropertyObjectReference() {

		String questionAnswerPattern =
				"^\\s*(" + PropertyDeclarationType.NAME + "\\s*(?:#\\s*"
						+ PropertyDeclarationType.NAME + ")?)\\s*";
		this.setSectionFinder(new RegexSectionFinder(Pattern.compile(questionAnswerPattern), 1));

		QuestionReference questionReference = new QuestionReference();
		questionReference.setSectionFinder(new RegexSectionFinder(
				Pattern.compile("^\\s*(" + PropertyDeclarationType.NAME + ")\\s*#"), 1));
		questionReference.addSubtreeHandler(new WildcardQuestionErrorRemover());
		this.addChildType(questionReference);

		PropertyAnswerReference answerReference = new PropertyAnswerReference();
		answerReference.setSectionFinder(new RegexSectionFinder(
				Pattern.compile("^#\\s*(" + PropertyDeclarationType.NAME + ")\\s*"), 1));
		this.addChildType(answerReference);
		NamedObjectReference namedObjectReference = new NamedObjectReference();
		namedObjectReference.setSectionFinder(new ConstraintSectionFinder(
				new AllTextFinderTrimmed(), NoChildrenOfOtherTypesConstraint.getInstance()));
		this.addChildType(namedObjectReference);
	}

	@Override
	public NamedObject getTermObject(KnowWEArticle article, Section<? extends D3webTerm<NamedObject>> s) {
		Section<PropertyAnswerReference> propertyAnswerReference = Sections.findChildOfType(s,
				PropertyAnswerReference.class);
		if (propertyAnswerReference != null) {
			return propertyAnswerReference.get().getTermObject(article, propertyAnswerReference);
		}
		Section<NamedObjectReference> namedObjectReference = Sections.findChildOfType(s,
				NamedObjectReference.class);
		if (namedObjectReference != null) {
			return namedObjectReference.get().getTermObject(article, namedObjectReference);
		}
		return null;
	}

	static class WildcardQuestionErrorRemover extends D3webSubtreeHandler<QuestionReference> {

		@Override
		public Collection<Message> create(KnowWEArticle article, Section<QuestionReference> section) {
			if (section.getText().isEmpty()) {
				Messages.clearMessages(article, section,
						SimpleTermReferenceRegistrationHandler.class);
			}
			return null;
		}
	}

	static class PropertyAnswerReference extends AnswerReference {

		public PropertyAnswerReference() {
			this.addSubtreeHandler(new WildcardAnswerErrorRemover());
		}

		@Override
		public Section<QuestionReference> getQuestionSection(Section<? extends AnswerReference> s) {
			return Sections.findChildOfType(s.getFather(), QuestionReference.class);
		}

	}

	static class WildcardAnswerErrorRemover extends D3webSubtreeHandler<PropertyAnswerReference> {

		@Override
		public Collection<Message> create(KnowWEArticle article, Section<PropertyAnswerReference> section) {
			Section<QuestionReference> questionSection = section.get().getQuestionSection(section);
			if (questionSection != null && questionSection.getText().isEmpty()) {
				Messages.clearMessages(article, section,
						SimpleTermReferenceRegistrationHandler.class);
			}
			return null;
		}
	}

	@Override
	public Class<?> getTermObjectClass() {
		return NamedObject.class;
	}

}
