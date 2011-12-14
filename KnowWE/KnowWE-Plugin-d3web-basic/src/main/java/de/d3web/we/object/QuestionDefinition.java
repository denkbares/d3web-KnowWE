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
import java.util.LinkedList;
import java.util.List;

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
import de.d3web.we.utils.D3webUtils;
import de.knowwe.core.compile.Priority;
import de.knowwe.core.compile.TerminologyHandler;
import de.knowwe.core.kdom.KnowWEArticle;
import de.knowwe.core.kdom.parsing.Section;
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
		super(Question.class);
		this.addSubtreeHandler(Priority.HIGHER, new CreateQuestionHandler());
		this.setCustomRenderer(StyleRenderer.Question);
		this.setOrderSensitive(true);
	}

	public abstract QuestionType getQuestionType(Section<QuestionDefinition> s);

	@SuppressWarnings("rawtypes")
	public abstract Section<? extends QASetDefinition> getParentQASetSection(Section<? extends QuestionDefinition> qdef);

	public abstract int getPosition(Section<QuestionDefinition> s);

	static class CreateQuestionHandler extends D3webSubtreeHandler<QuestionDefinition> {

		@Override
		@SuppressWarnings("unchecked")
		public Collection<Message> create(KnowWEArticle article,
				Section<QuestionDefinition> s) {

			Section<QuestionDefinition> qidSection = (s);
			String name = qidSection.get().getTermIdentifier(qidSection);

			TerminologyHandler terminologyHandler = KnowWEUtils.getTerminologyHandler(article.getWeb());
			terminologyHandler.registerTermDefinition(article, s);

			KnowledgeBase kb = getKB(article);

			@SuppressWarnings("rawtypes")
			Section<? extends QASetDefinition> parentQASetSection =
					s.get().getParentQASetSection(s);

			QASet parent = null;
			if (parentQASetSection != null) {
				parent = (QASet) parentQASetSection.get().getTermObject(article, parentQASetSection);
			}
			if (parent == null) {
				parent = kb.getRootQASet();
			}
			else {
				if (terminologyHandler.getTermDefiningSection(article, s) != s) {
					return s.get().handleRedundantDefinition(article, s);
				}
			}

			QuestionType questionType = qidSection.get().getQuestionType(
					qidSection);
			if (questionType == null) {
				return Messages.asList(Messages.objectCreationError(
						"No question type found: " + name));
			}

			Question q = null;
			if (questionType.equals(QuestionType.OC)) {
				q = new QuestionOC(parent, name);
			}
			else if (questionType.equals(QuestionType.MC)) {
				q = new QuestionMC(parent, name);
			}
			else if (questionType.equals(QuestionType.NUM)) {
				q = new QuestionNum(parent, name);
			}
			else if (questionType.equals(QuestionType.YN)) {
				q = new QuestionYN(parent, name);
				if (q != null) {
					handleYNChoices(article, s);
				}
			}
			else if (questionType.equals(QuestionType.DATE)) {
				q = new QuestionDate(parent, name);
			}
			else if (questionType.equals(QuestionType.INFO)) {
				q = new QuestionZC(parent, name);
			}
			else if (questionType.equals(QuestionType.TEXT)) {
				q = new QuestionText(parent, name);
			}
			else {
				return Messages.asList(Messages.error(
						"No valid question type found for question '" + name + "'"));
			}

			// ok, everything went well
			// set position right in case this is an incremental update
			if (!article.isFullParse()) {
				parent.addChild(q, s.get().getPosition(s));
			}

			// store object in section
			qidSection.get().storeTermObject(article, qidSection, q);

			// return success message
			return Messages.asList(Messages.objectCreatedNotice(
					q.getClass().getSimpleName() + " " + q.getName()));

		}

		@Override
		public void destroy(KnowWEArticle article,
				Section<QuestionDefinition> s) {

			Question q = s.get().getTermObject(article, s);

			if (q != null) {
				D3webUtils.removeRecursively(q);
				KnowWEUtils.getTerminologyHandler(article.getWeb()).unregisterTermDefinition(
						article, s);
				if (q instanceof QuestionYN) {
					handleYNChoices(article, s);
				}
			}

		}

		/**
		 * Special treatment for QuestionYN, since there may be no actual terms
		 * definitions for the answers.
		 */
		private void handleYNChoices(KnowWEArticle article, Section<QuestionDefinition> s) {
			List<Section<?>> refs = new LinkedList<Section<?>>();
			refs.addAll(KnowWEUtils.getTerminologyHandler(article.getWeb()).getTermReferenceSections(
					article, s.get().getTermIdentifier(s) + " Yes", s.get().getTermScope()));
			refs.addAll(KnowWEUtils.getTerminologyHandler(article.getWeb()).getTermReferenceSections(
					article, s.get().getTermIdentifier(s) + " No", s.get().getTermScope()));
			for (Section<?> ref : refs) {
				ref.clearReusedBySet();
			}
		}

	}

}
