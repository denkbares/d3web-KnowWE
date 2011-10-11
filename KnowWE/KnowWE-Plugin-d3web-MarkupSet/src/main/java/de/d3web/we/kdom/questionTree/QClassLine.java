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
package de.d3web.we.kdom.questionTree;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import de.d3web.core.knowledge.terminology.QASet;
import de.d3web.core.knowledge.terminology.QContainer;
import de.d3web.core.knowledge.terminology.info.MMInfo;
import de.d3web.we.object.QASetDefinition;
import de.d3web.we.object.QuestionnaireDefinition;
import de.d3web.we.reviseHandler.D3webSubtreeHandler;
import de.knowwe.core.KnowWEEnvironment;
import de.knowwe.core.compile.IncrementalMarker;
import de.knowwe.core.kdom.AbstractType;
import de.knowwe.core.kdom.KnowWEArticle;
import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.kdom.sectionFinder.AllTextFinderTrimmed;
import de.knowwe.core.kdom.sectionFinder.AllTextSectionFinder;
import de.knowwe.core.kdom.sectionFinder.RegexSectionFinder;
import de.knowwe.core.report.KDOMReportMessage;
import de.knowwe.kdom.constraint.ConstraintSectionFinder;
import de.knowwe.kdom.constraint.SingleChildConstraint;
import de.knowwe.kdom.constraint.UnquotedConstraint;
import de.knowwe.kdom.dashtree.DashTreeElement;
import de.knowwe.kdom.dashtree.DashTreeElementContent;
import de.knowwe.kdom.dashtree.DashTreeUtils;
import de.knowwe.kdom.sectionFinder.ConditionalSectionFinder;
import de.knowwe.plugin.Plugins;
import de.knowwe.report.message.ObjectAlreadyDefinedError;
import de.knowwe.report.message.ObjectCreatedMessage;
import de.knowwe.report.message.ObjectCreationError;
import de.knowwe.report.message.RelationCreatedMessage;

public class QClassLine extends AbstractType implements IncrementalMarker {

	public QClassLine() {

		initSectionFinder();

		for (Type childType : Plugins.getTypes(this.getClass().getName())) {
			addChildType(childType);
		}
		// at first the init-number
		this.childrenTypes.add(new InitNumber());
		// add description-type via '~'
		this.addChildType(new ObjectDescription(MMInfo.DESCRIPTION));

		// finally the rest is QuestionniareDefinition
		this.childrenTypes.add(new QuestionTreeQuestionnaireDefinition());
		this.addSubtreeHandler(new CreateSubQuestionnaireRelationHandler());

	}

	static class QuestionTreeQuestionnaireDefinition extends QuestionnaireDefinition {

		public QuestionTreeQuestionnaireDefinition() {
			ConstraintSectionFinder csf = new ConstraintSectionFinder(
					new AllTextFinderTrimmed());
			csf.addConstraint(SingleChildConstraint.getInstance());
			setSectionFinder(csf);
		}

		@Override
		public int getPosition(Section<QuestionnaireDefinition> s) {
			return DashTreeUtils.getPositionInFatherDashSubtree(s);
		}

		@Override
		public boolean violatedConstraints(KnowWEArticle article, Section<QASetDefinition<? extends QASet>> s) {
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
		public void destroy(KnowWEArticle article, Section<QClassLine> s) {
			// will be destroyed by QuestionniareDefinition#destroy()

		}

		@Override
		public Collection<KDOMReportMessage> create(KnowWEArticle article, Section<QClassLine> s) {
			Section<? extends DashTreeElementContent> fatherContent = DashTreeUtils.getFatherDashTreeElementContent(
					s);

			if (fatherContent != null) {
				List<Section<QuestionLine>> questionLine = Sections.findChildrenOfType(
						fatherContent, QuestionLine.class);
				if (questionLine != null && !questionLine.isEmpty()) {
					// this situation can only occur with incremental update
					// -> fullparse
					if (article != s.getArticle()) {
						KnowWEEnvironment.getInstance().getArticleManager(s.getWeb()).registerArticle(
								KnowWEArticle.createArticle(
										s.getArticle().getSection().getOriginalText(),
										s.getTitle(),
										KnowWEEnvironment.getInstance().getRootType(),
										s.getWeb(), true), false);
					}
					article.setFullParse(this.getClass());
				}
			}

			Section<QuestionnaireDefinition> localQuestionniareDef = Sections.findSuccessor(s,
					QuestionnaireDefinition.class);
			QContainer localQuestionnaire = localQuestionniareDef.get().getTermObject(
					article,
					localQuestionniareDef);

			if (fatherContent != null && localQuestionnaire != null) {

				Section<QuestionnaireDefinition> questionniareDef = Sections.findSuccessor(
						fatherContent, QuestionnaireDefinition.class);
				if (questionniareDef != null) {
					QContainer superQuasetionniare = questionniareDef.get().getTermObject(
							article,
							questionniareDef);
					// here the actual taxonomic relation is established
					superQuasetionniare.addChild(localQuestionnaire);

					return Arrays.asList((KDOMReportMessage) new RelationCreatedMessage(
							s.getClass().getSimpleName()
									+ " " + localQuestionnaire.getName()
									+ "sub-questionnaire of "
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

				Section<DashTreeElement> s = Sections.findAncestorOfType(father,
						DashTreeElement.class);
				if (DashTreeUtils.getDashLevel(s) == 0) {
					// is root level
					return true;
				}
				Section<? extends DashTreeElement> dashTreeFather = DashTreeUtils
						.getFatherDashTreeElement(s);
				if (dashTreeFather != null) {
					// is child of a QClass declaration => also declaration
					if (Sections.findSuccessor(dashTreeFather, QClassLine.class) != null) {
						return true;
					}
				}

				return false;
			}
		};
	}

	static class InitNumber extends AbstractType implements IncrementalMarker {

		public InitNumber() {

			ConstraintSectionFinder initNumberFinder = new ConstraintSectionFinder(
					new RegexSectionFinder("#\\d*"));
			initNumberFinder.addConstraint(UnquotedConstraint.getInstance());
			initNumberFinder.addConstraint(SingleChildConstraint.getInstance());
			this.setSectionFinder(initNumberFinder);

			this.addSubtreeHandler(new D3webSubtreeHandler<InitNumber>() {

				/**
				 * creates the bound-property for a bound-definition
				 * 
				 * @param article
				 * @param s
				 * @return
				 */
				@Override
				public Collection<KDOMReportMessage> create(KnowWEArticle article, Section<InitNumber> s) {

					Double originalnumber = s.get().getNumber(
							s);
					if (originalnumber == null) {
						// if the numbers cannot be found throw error
						return Arrays.asList((KDOMReportMessage) new ObjectCreationError(
								"invalid number",
								this.getClass()));
					}
					Integer number = new Integer((originalnumber.intValue()));

					Section<QuestionnaireDefinition> qDef = Sections.findSuccessor(
								s.getFather(), QuestionnaireDefinition.class);

					if (qDef != null) {

						QContainer questionnaire = qDef.get().getTermObject(article, qDef);

						boolean alreadyInitDefined = getKB(article).removeInitQuestion(
								questionnaire);
						// check whether there is already some init-number
						// registered for this QASet
						if (alreadyInitDefined) {
							// do nothing and throw error iff
							return Arrays.asList((KDOMReportMessage) new ObjectAlreadyDefinedError(
									"Init priority for object already defined"));
						}
						else {
							// else register init value
							getKB(article).addInitQuestion(
									questionnaire,
									number);

							return Arrays.asList((KDOMReportMessage) new ObjectCreatedMessage(
									"Init property set"));
						}

					}
					return Arrays.asList((KDOMReportMessage) new ObjectCreationError(
							"KnowWE.questiontree.numerical",
							this.getClass()));
				}

				@Override
				public void destroy(KnowWEArticle article, Section<InitNumber> s) {
					Section<QuestionnaireDefinition> qDef = Sections.findSuccessor(
							s.getFather(), QuestionnaireDefinition.class);

					if (qDef != null) {
						// remove init number value from registration in KB
						QContainer questionnaire = qDef.get().getTermObject(article,
								qDef);
						getKB(article).removeInitQuestion(
								questionnaire);
					}
				}
			});

		}

		public Double getNumber(Section<InitNumber> s) {
			String originalText = s.getOriginalText();
			String content = originalText.replace("#", "").trim();

			Double d = null;
			try {
				d = Double.parseDouble(content);
				return d;
			}
			catch (Exception e) {

			}

			return null;
		}

	}

}
