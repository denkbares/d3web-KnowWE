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
package de.d3web.we.object;

import java.util.Collection;

import de.d3web.core.knowledge.KnowledgeBase;
import de.d3web.core.knowledge.terminology.QASet;
import de.d3web.core.knowledge.terminology.Question;
import de.d3web.core.knowledge.terminology.QuestionDate;
import de.d3web.core.knowledge.terminology.QuestionMC;
import de.d3web.core.knowledge.terminology.QuestionNum;
import de.d3web.core.knowledge.terminology.QuestionOC;
import de.d3web.core.knowledge.terminology.QuestionText;
import de.d3web.core.knowledge.terminology.QuestionYN;
import de.d3web.core.knowledge.terminology.QuestionZC;
import de.d3web.we.reviseHandler.D3webSubtreeHandler;
import de.knowwe.core.compile.Priority;
import de.knowwe.core.compile.terminology.TermIdentifier;
import de.knowwe.core.compile.terminology.TerminologyManager;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.objects.SimpleTerm;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.report.Message;
import de.knowwe.core.report.Messages;
import de.knowwe.core.utils.KnowWEUtils;
import de.knowwe.kdom.renderer.StyleRenderer;

/**
 * 
 * Abstract Type for the definition of questions
 * 
 * @author Jochen/Albrecht
 * @created 26.07.2010
 */
public abstract class QuestionDefinition extends QASetDefinition<Question> {

	public static enum QuestionType {
		OC, MC, YN, NUM, DATE, TEXT, INFO;
	}

	public QuestionDefinition() {
		this.addSubtreeHandler(Priority.HIGHER, new CreateQuestionHandler());
		this.setRenderer(StyleRenderer.Question);
		this.setOrderSensitive(true);
	}

	@Override
	public final Class<?> getTermObjectClass(Section<? extends SimpleTerm> section) {
		QuestionType questionType = getQuestionType(Sections.cast(section, QuestionDefinition.class));
		if (questionType == null) return Question.class;
		switch (questionType) {
		case DATE:
			return QuestionDate.class;
		case INFO:
			return QuestionZC.class;
		case MC:
			return QuestionMC.class;
		case NUM:
			return QuestionNum.class;
		case OC:
			return QuestionOC.class;
		case TEXT:
			return QuestionText.class;
		case YN:
			return QuestionYN.class;
		}
		return Question.class;
	}

	public abstract QuestionType getQuestionType(Section<QuestionDefinition> s);

	@SuppressWarnings("rawtypes")
	public abstract Section<? extends QASetDefinition> getParentQASetSection(Section<? extends QuestionDefinition> qdef);

	public abstract int getPosition(Section<QuestionDefinition> s);

	static class CreateQuestionHandler extends D3webSubtreeHandler<QuestionDefinition> {

		@Override
		@SuppressWarnings("unchecked")
		public Collection<Message> create(Article article,
				Section<QuestionDefinition> section) {

			TermIdentifier identifier = section.get().getTermIdentifier(section);
			Class<?> termObjectClass = section.get().getTermObjectClass(section);
			TerminologyManager terminologyHandler = KnowWEUtils.getTerminologyManager(article);
			terminologyHandler.registerTermDefinition(section, termObjectClass, identifier);

			Collection<Message> msgs = section.get().canAbortTermObjectCreation(article, section);
			if (msgs != null) return msgs;

			KnowledgeBase kb = getKB(article);

			@SuppressWarnings("rawtypes")
			Section<? extends QASetDefinition> parentQASetSection =
					section.get().getParentQASetSection(section);

			QASet parent = null;
			if (parentQASetSection != null) {
				parent = (QASet) parentQASetSection.get().getTermObject(article, parentQASetSection);
			}
			if (parent == null) {
				parent = kb.getRootQASet();
			}

			String name = section.get().getTermName(section);

			QuestionType questionType = section.get().getQuestionType(section);
			if (questionType == null) {
				return Messages.asList(Messages.objectCreationError(
						"No type found for question '" + name + "'"));
			}

			if (questionType.equals(QuestionType.OC)) {
				new QuestionOC(parent, name);
			}
			else if (questionType.equals(QuestionType.MC)) {
				new QuestionMC(parent, name);
			}
			else if (questionType.equals(QuestionType.NUM)) {
				new QuestionNum(parent, name);
			}
			else if (questionType.equals(QuestionType.YN)) {
				new QuestionYN(parent, name);
			}
			else if (questionType.equals(QuestionType.DATE)) {
				new QuestionDate(parent, name);
			}
			else if (questionType.equals(QuestionType.INFO)) {
				new QuestionZC(parent, name);
			}
			else if (questionType.equals(QuestionType.TEXT)) {
				new QuestionText(parent, name);
			}
			else {
				return Messages.asList(Messages.error(
						"No valid question type found for question '" + identifier + "'"));
			}

			// return success message
			return Messages.asList(Messages.objectCreatedNotice(
					termObjectClass.getSimpleName() + " " + identifier));

		}
	}

}
