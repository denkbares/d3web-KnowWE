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
package de.d3web.we.kdom.objects;

import java.util.Arrays;
import java.util.Collection;

import de.d3web.core.knowledge.terminology.QASet;
import de.d3web.core.knowledge.terminology.Question;
import de.d3web.core.manage.KnowledgeBaseManagement;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Priority;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.renderer.FontColorRenderer;
import de.d3web.we.kdom.report.KDOMReportMessage;
import de.d3web.we.kdom.report.message.NewObjectCreated;
import de.d3web.we.kdom.report.message.ObjectAlreadyDefinedError;
import de.d3web.we.kdom.report.message.ObjectCreationError;
import de.d3web.we.terminology.D3webSubtreeHandler;
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
		this.setCustomRenderer(new FontColorRenderer(FontColorRenderer.COLOR3));
		this.setOrderSensitive(true);
	}

	public abstract QuestionType getQuestionType(Section<QuestionDefinition> s);

	@SuppressWarnings("unchecked")
	public abstract Section<? extends QASetDefinition> getParentQASetSection(Section<? extends QuestionDefinition> qdef);

	public abstract int getPosition(Section<QuestionDefinition> s);

	static class CreateQuestionHandler extends D3webSubtreeHandler<QuestionDefinition> {

		@Override
		@SuppressWarnings("unchecked")
		public Collection<KDOMReportMessage> create(KnowWEArticle article,
				Section<QuestionDefinition> sec) {

			Section<QuestionDefinition> qidSection = (sec);

			String name = qidSection.get().getTermName(qidSection);

			KnowledgeBaseManagement mgn = getKBM(article);

			if (KnowWEUtils.getTerminologyHandler(article.getWeb()).isDefinedTerm(article, sec)) {
				return Arrays.asList((KDOMReportMessage) new ObjectAlreadyDefinedError(
						sec.get().getTermName(sec)));
			}

			Section<? extends QASetDefinition> parentQASetSection =
					sec.get().getParentQASetSection(sec);

			QASet parent = null;
			if (parentQASetSection != null) {
				parent = (QASet) parentQASetSection.get().getTermObject(article, parentQASetSection);
			}
			if (parent == null) parent = mgn.getKnowledgeBase().getRootQASet();

			QuestionType questionType = qidSection.get().getQuestionType(
					qidSection);

			if (questionType == null) {
				return Arrays.asList((KDOMReportMessage) new ObjectCreationError(
						"no question type found: " + name,
						this.getClass()));
			}

			Question q = null;
			if (questionType.equals(QuestionType.OC)) {
				q = mgn.createQuestionOC(name, parent, new String[] {});
			}
			else if (questionType.equals(QuestionType.MC)) {
				q = mgn.createQuestionMC(name, parent, new String[] {});
			}
			else if (questionType.equals(QuestionType.NUM)) {
				q = mgn.createQuestionNum(name, parent);
			}
			else if (questionType.equals(QuestionType.YN)) {
				q = mgn.createQuestionYN(name, parent);
			}
			else if (questionType.equals(QuestionType.DATE)) {
				q = mgn.createQuestionDate(name, parent);
			}
			else if (questionType.equals(QuestionType.INFO)) {
				q = mgn.createQuestionZC(name, parent);
			}
			else if (questionType.equals(QuestionType.TEXT)) {
				q = mgn.createQuestionText(name, parent);
			}
			else {
				// no valid type...
			}

			if (q != null) {
				// ok everything went well
				// set position right in case this is an incremental update
				if (!article.isFullParse()) {
					parent.moveChildToPosition(q, sec.get().getPosition(sec));
				}
				// register term
				KnowWEUtils.getTerminologyHandler(article.getWeb()).registerTermDefinition(
						article, sec);

				// store object in section
				qidSection.get().storeTermObject(article, qidSection, q);

				// return success message
				return MessageUtils.createdMessageAsList(q);
			}
			else {
				return Arrays.asList((KDOMReportMessage) new ObjectCreationError(name,
						this.getClass()));
			}

		}

		@Override
		public void destroy(KnowWEArticle article, Section<QuestionDefinition> question) {

			Question q = question.get().getTermObjectFromLastVersion(article, question);

			if (q != null) {
				D3webUtils.removeRecursively(q);
				KnowWEUtils.getTerminologyHandler(article.getWeb()).unregisterTermDefinition(
						article, question);
			}

		}

	}

}
