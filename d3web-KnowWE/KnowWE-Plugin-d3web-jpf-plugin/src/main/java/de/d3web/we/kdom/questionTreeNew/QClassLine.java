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
package de.d3web.we.kdom.questionTreeNew;

import java.util.Arrays;
import java.util.Collection;

import de.d3web.core.knowledge.terminology.QContainer;
import de.d3web.we.kdom.DefaultAbstractKnowWEObjectType;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.dashTree.DashTreeElement;
import de.d3web.we.kdom.dashTree.DashTreeElementContent;
import de.d3web.we.kdom.dashTree.DashTreeUtils;
import de.d3web.we.kdom.objects.KnowWETermMarker;
import de.d3web.we.kdom.objects.QuestionnaireDefinition;
import de.d3web.we.kdom.report.KDOMReportMessage;
import de.d3web.we.kdom.report.message.RelationCreatedMessage;
import de.d3web.we.kdom.sectionFinder.AllTextFinderTrimmed;
import de.d3web.we.kdom.sectionFinder.AllTextSectionFinder;
import de.d3web.we.kdom.sectionFinder.ConditionalSectionFinder;
import de.d3web.we.terminology.D3webSubtreeHandler;

public class QClassLine extends DefaultAbstractKnowWEObjectType implements KnowWETermMarker {

	@Override
	protected void init() {

		initSectionFinder();

		this.childrenTypes.add(new QuestionTreeQuestionnaireDefinition());
		this.addSubtreeHandler(new CreateSubQuestionnaireRelationHandler());

	}

	static class QuestionTreeQuestionnaireDefinition extends QuestionnaireDefinition {

		public QuestionTreeQuestionnaireDefinition() {
			setSectionFinder(new AllTextFinderTrimmed());
		}

		@Override
		public int getPosition(Section<QuestionnaireDefinition> s) {
			return DashTreeUtils.getPositionInFatherDashSubtree(s);
		}

		@Override
		public boolean hasViolatedConstraints(KnowWEArticle article, Section<?> s) {
			return QuestionDashTreeUtils.isChangeInRootQuestionSubtree(article, s);
		}

	}

	/**
	 * @author Jochen
	 * 
	 *         This handler establishes sub-questionnaire-relations defined by
	 *         the questionTree in the knowledge base i.e., if a questionnaire
	 *         is a dashTree-child of another questionnaire we add it as child
	 *         in the knowledge base
	 * 
	 */
	static class CreateSubQuestionnaireRelationHandler extends D3webSubtreeHandler<QClassLine> {

		@Override
		public boolean needsToCreate(KnowWEArticle article, Section<QClassLine> s) {
			return super.needsToCreate(article, s)
					|| QuestionDashTreeUtils.isChangeInRootQuestionSubtree(article, s);
		}

		@Override
		public void destroy(KnowWEArticle article, Section<QClassLine> s) {
			// will be destroyed by QuestionniareDefinition#destroy()

		}

		@Override
		public Collection<KDOMReportMessage> create(KnowWEArticle article, Section<QClassLine> s) {
			Section<? extends DashTreeElementContent> fatherContent = DashTreeUtils.getFatherDashTreeElementContent(
					s);
			Section<QuestionnaireDefinition> localQuestionniareDef = s.findSuccessor(QuestionnaireDefinition.class);
			QContainer localQuestionnaire = localQuestionniareDef.get().getTermObject(article,
					localQuestionniareDef);

			if (fatherContent != null && localQuestionnaire != null) {

				Section<QuestionnaireDefinition> questionniareDef = fatherContent.findSuccessor(QuestionnaireDefinition.class);
				if (questionniareDef != null) {
					QContainer superQuasetionniare = questionniareDef.get().getTermObject(article,
							questionniareDef);
					// here the actual taxonomic relation is established
					superQuasetionniare.addChild(localQuestionnaire);
					return Arrays.asList((KDOMReportMessage) new RelationCreatedMessage(
							s.getClass().getSimpleName()
									+ " " + localQuestionnaire.getName() + "sub-questionnaire of "
									+ superQuasetionniare.getName()));
				}
			}

			return null;
		}

	}

	private void initSectionFinder() {
		this.sectionFinder = new ConditionalSectionFinder(new AllTextSectionFinder()) {

			@Override
			protected boolean condition(String text, Section<?> father) {

				Section<DashTreeElement> s = father
						.findAncestorOfType(DashTreeElement.class);
				if (DashTreeUtils.getDashLevel(s) == 0) {
					// is root level
					return true;
				}
				Section<? extends DashTreeElement> dashTreeFather = DashTreeUtils
						.getFatherDashTreeElement(s);
				if (dashTreeFather != null) {
					// is child of a QClass declaration => also declaration
					if (dashTreeFather.findSuccessor(QClassLine.class) != null) {
						return true;
					}
				}

				return false;
			}
		};
	}

}
