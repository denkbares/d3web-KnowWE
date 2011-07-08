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

import java.util.ArrayList;
import java.util.Arrays;
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
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Priority;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.rendering.StyleRenderer;
import de.d3web.we.kdom.report.KDOMReportMessage;
import de.d3web.we.kdom.report.SimpleMessageError;
import de.d3web.we.kdom.report.message.ObjectCreationError;
import de.d3web.we.reviseHandler.D3webSubtreeHandler;
import de.d3web.we.utils.D3webUtils;
import de.d3web.we.utils.KnowWEUtils;
import de.d3web.we.utils.MessageUtils;

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
		public Collection<KDOMReportMessage> create(KnowWEArticle article,
				Section<QuestionDefinition> s) {

			Section<QuestionDefinition> qidSection = (s);
			String name = qidSection.get().getTermIdentifier(qidSection);

			boolean alreadyRegistered = false;
			if (!KnowWEUtils.getTerminologyHandler(article.getWeb())
					.registerTermDefinition(article, s)) {
				alreadyRegistered = true;
			}

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
				if (alreadyRegistered) {
					parent.addChild(s.get().getTermObject(article, s));
					return new ArrayList<KDOMReportMessage>(0);
				}
			}

			QuestionType questionType = qidSection.get().getQuestionType(
					qidSection);

			if (questionType == null) {
				return Arrays.asList((KDOMReportMessage) new ObjectCreationError(
						"no question type found: " + name,
						this.getClass()));
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
				return MessageUtils.asList(new SimpleMessageError(
						"No valid question type found for question '" + name + "'"));

			}

			// ok everything went well
			// set position right in case this is an incremental update
			if (!article.isFullParse()) {
				parent.addChild(q, s.get().getPosition(s));
			}

			// store object in section
			qidSection.get().storeTermObject(article, qidSection, q);

			// return success message
			return MessageUtils.objectCreatedAsList(q);

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
