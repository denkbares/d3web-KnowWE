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

import java.util.Collection;
import java.util.logging.Logger;

import de.d3web.core.knowledge.KnowledgeBase;
import de.d3web.core.knowledge.terminology.QContainer;
import de.d3web.core.knowledge.terminology.info.MMInfo;
import de.d3web.we.object.QuestionnaireDefinition;
import de.d3web.we.reviseHandler.D3webSubtreeHandler;
import de.knowwe.core.kdom.AbstractType;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.kdom.sectionFinder.AllTextFinderTrimmed;
import de.knowwe.core.kdom.sectionFinder.AllTextSectionFinder;
import de.knowwe.core.kdom.sectionFinder.RegexSectionFinder;
import de.knowwe.core.report.Message;
import de.knowwe.core.report.Messages;
import de.knowwe.kdom.constraint.ConstraintSectionFinder;
import de.knowwe.kdom.constraint.SingleChildConstraint;
import de.knowwe.kdom.constraint.UnquotedConstraint;
import de.knowwe.kdom.dashtree.DashTreeElement;
import de.knowwe.kdom.dashtree.DashTreeElementContent;
import de.knowwe.kdom.dashtree.DashTreeUtils;
import de.knowwe.kdom.sectionFinder.ConditionalSectionFinder;

public class QClassLine extends AbstractType {

	public QClassLine() {

		initSectionFinder();
		// at first the init-number
		this.addChildType(new InitNumber());
		// add description-type via '~'
		this.addChildType(new ObjectDescription(MMInfo.DESCRIPTION));

		// finally the rest is QuestionniareDefinition
		this.addChildType(new QuestionTreeQuestionnaireDefinition());
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
		public void destroy(Article article, Section<QClassLine> s) {
			// will be destroyed by QuestionniareDefinition#destroy()

		}

		@Override
		public Collection<Message> create(Article article, Section<QClassLine> s) {
			Section<? extends DashTreeElementContent> fatherContent = DashTreeUtils.getFatherDashTreeElementContent(
					s);

			Section<QuestionnaireDefinition> localQuestionniareDef = Sections.findSuccessor(s,
					QuestionnaireDefinition.class);
			QContainer localQuestionnaire = localQuestionniareDef.get().getTermObject(
					article,
					localQuestionniareDef);

			if (fatherContent != null && localQuestionnaire != null) {

				Section<QuestionnaireDefinition> superQuestionnaireDef = Sections.findSuccessor(
						fatherContent, QuestionnaireDefinition.class);
				if (superQuestionnaireDef != null) {
					QContainer superQuestionnaire = superQuestionnaireDef.get().getTermObject(
							article,
							superQuestionnaireDef);

					KnowledgeBase kb = getKB(article);
					if (superQuestionnaire == null) {
						superQuestionnaire = kb.getManager().searchQContainer(
								superQuestionnaireDef.get().getTermIdentifier(superQuestionnaireDef).toString());
					}

					if (superQuestionnaire != null) {
						int position = localQuestionniareDef.get().getPosition(
								localQuestionniareDef);
						int childrenCount = superQuestionnaire.getChildren().length;
						if (position <= childrenCount) {
							// in case it was connected to the root, remove this
							// connection
							kb.getRootQASet().removeChild(localQuestionnaire);

							// here the actual taxonomic relation is established
							superQuestionnaire.addChild(localQuestionnaire,
									position);
						}
						else {
							String msg = "Unable to add sub-questionnaire at desired position.\nDesired position: "
									+ position + ", children count in parent: "
									+ childrenCount + ", questionnaire: '"
									+ localQuestionnaire + "', parent: '"
									+ superQuestionnaire + "'.\n This is likely because one of the"
									+ " sibling questionnaires could not be added due to an error.";
							Logger.getLogger(this.getClass().getName()).warning(msg);
						}
					}
				}
			}

			return Messages.asList();
		}
	}

	private void initSectionFinder() {
		this.setSectionFinder(new ConditionalSectionFinder(AllTextSectionFinder.getInstance()) {

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
		});
	}

	static class InitNumber extends AbstractType {

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
				public Collection<Message> create(Article article, Section<InitNumber> s) {

					Double originalnumber = s.get().getNumber(s);
					if (originalnumber == null) {
						// if the numbers cannot be found throw error
						return Messages.asList(Messages.objectCreationError(
								"Invalid number"));
					}
					Integer number = new Integer((originalnumber.intValue()));

					Section<QuestionnaireDefinition> qDef = Sections.findSuccessor(
							s.getParent(), QuestionnaireDefinition.class);

					if (qDef != null) {

						QContainer questionnaire = qDef.get().getTermObject(article, qDef);

						if (questionnaire != null) {
							boolean alreadyInitDefined = getKB(article).removeInitQuestion(
									questionnaire);
							// check whether there is already some init-number
							// registered for this QASet
							if (alreadyInitDefined) {
								// do nothing and throw error iff
								return Messages.asList(Messages.objectAlreadyDefinedError(
										"Init priority"));
							}
							else {
								// else register init value
								getKB(article).addInitQuestion(
										questionnaire,
										number);

								return Messages.asList(Messages.objectCreatedNotice(
										"Init property"));
							}
						}

					}
					return Messages.asList(Messages.objectCreationError(
							"Init priority"));
				}

				@Override
				public void destroy(Article article, Section<InitNumber> s) {
					Section<QuestionnaireDefinition> qDef = Sections.findSuccessor(
							s.getParent(), QuestionnaireDefinition.class);

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
			String originalText = s.getText();
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
