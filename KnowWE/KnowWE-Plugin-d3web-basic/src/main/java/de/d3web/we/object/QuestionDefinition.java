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

import de.d3web.core.knowledge.terminology.QASet;
import de.d3web.core.knowledge.terminology.Question;
import de.d3web.core.manage.KnowledgeBaseManagement;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Priority;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.objects.TermDefinition;
import de.d3web.we.kdom.rendering.StyleRenderer;
import de.d3web.we.kdom.report.KDOMReportMessage;
import de.d3web.we.kdom.report.message.ObjectCreationError;
import de.d3web.we.kdom.report.message.TermNameCaseWarning;
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

	public abstract Section<? extends QASetDefinition> getParentQASetSection(Section<? extends QuestionDefinition> qdef);

	public abstract int getPosition(Section<QuestionDefinition> s);

	static class CreateQuestionHandler extends D3webSubtreeHandler<QuestionDefinition> {

		@Override
		@SuppressWarnings("unchecked")
		public Collection<KDOMReportMessage> create(KnowWEArticle article,
				Section<QuestionDefinition> sec) {

			Section<QuestionDefinition> qidSection = (sec);
			String name = qidSection.get().getTermName(qidSection);

			if (KnowWEUtils.getTerminologyHandler(article.getWeb()).isDefinedTerm(article, sec)) {
				KnowWEUtils.getTerminologyHandler(article.getWeb()).registerTermDefinition(article,
						sec);

				Section<? extends TermDefinition<Question>> termDef = KnowWEUtils.getTerminologyHandler(
						article.getWeb()).getTermDefiningSection(article, sec);

				String termDefName = termDef.get().getTermName(termDef);

				if (!name.equals(termDefName)) {
					return Arrays.asList((KDOMReportMessage) new TermNameCaseWarning(termDefName));
				}
				return new ArrayList<KDOMReportMessage>(0);
			}

			KnowledgeBaseManagement mgn = getKBM(article);

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
					parent.moveChildToPosition(q,
							sec.get().getPosition(sec));
				}
				// register term
				KnowWEUtils.getTerminologyHandler(article.getWeb()).registerTermDefinition(
						article, sec);

				// store object in section
				qidSection.get().storeTermObject(article, qidSection, q);

				// return success message
				return MessageUtils.objectCreatedAsList(q);
			}
			else {
				return Arrays.asList((KDOMReportMessage) new ObjectCreationError(name,
						this.getClass()));
			}

		}

		@Override
		public void destroy(KnowWEArticle article,
				Section<QuestionDefinition> question) {

			Question q = question.get().getTermObjectFromLastVersion(article,
					question);

			if (q != null) {
				D3webUtils.removeRecursively(q);
				KnowWEUtils.getTerminologyHandler(article.getWeb()).unregisterTermDefinition(
						article, question);
			}

		}

	}

}
