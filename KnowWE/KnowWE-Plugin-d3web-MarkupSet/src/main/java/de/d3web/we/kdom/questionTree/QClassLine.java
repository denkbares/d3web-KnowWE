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
import java.util.List;

import de.d3web.core.knowledge.TerminologyObject;
import de.d3web.core.knowledge.terminology.NamedObject;
import de.d3web.core.knowledge.terminology.QASet;
import de.d3web.core.knowledge.terminology.QContainer;
import de.d3web.core.knowledge.terminology.info.MMInfo;
import de.d3web.strings.Identifier;
import de.d3web.we.knowledgebase.D3webCompiler;
import de.d3web.we.object.QuestionnaireDefinition;
import de.d3web.we.reviseHandler.D3webHandler;
import de.d3web.we.utils.D3webUtils;
import de.knowwe.core.compile.Priority;
import de.knowwe.core.kdom.AbstractType;
import de.knowwe.core.kdom.objects.TermDefinition;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.kdom.sectionFinder.AllTextFinder;
import de.knowwe.core.kdom.sectionFinder.AllTextFinderTrimmed;
import de.knowwe.core.kdom.sectionFinder.RegexSectionFinder;
import de.knowwe.core.report.Message;
import de.knowwe.core.report.Messages;
import de.knowwe.kdom.constraint.ConstraintSectionFinder;
import de.knowwe.kdom.constraint.SingleChildConstraint;
import de.knowwe.kdom.constraint.UnquotedConstraint;
import de.knowwe.kdom.dashtree.DashTreeElement;
import de.knowwe.kdom.dashtree.DashTreeTermRelationScript;
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
//		this.addCompileScript(new CreateSubQuestionnaireRelationHandler());

	}

	static class QuestionTreeQuestionnaireDefinition extends QuestionnaireDefinition {

		public QuestionTreeQuestionnaireDefinition() {
			ConstraintSectionFinder csf = new ConstraintSectionFinder(
					new AllTextFinderTrimmed());
			csf.addConstraint(SingleChildConstraint.getInstance());
			setSectionFinder(csf);
			this.addCompileScript(Priority.ABOVE_DEFAULT, new DashTreeTermRelationScript<D3webCompiler>() {
				@Override
				protected void createObjectRelations(Section<TermDefinition> parentSection, D3webCompiler compiler, Identifier parentIdentifier, List<Identifier> childrenIdentifier) {
					QASet parentQASet = (QASet) D3webUtils.getTermObject(compiler, parentIdentifier);
					if (parentQASet == null) return;
					TerminologyObject[] parents = parentQASet.getParents();
					if (parents.length == 0) {
						parentQASet.getKnowledgeBase().getRootQASet().addChild(parentQASet);
					}
					for (Identifier childIdentifier : childrenIdentifier) {
						NamedObject namendObject = D3webUtils.getTermObject(compiler, childIdentifier);
						if (namendObject == null || !(namendObject instanceof QASet)) continue;
						QASet childQASet = (QASet) namendObject;
						parentQASet.getKnowledgeBase().getRootQASet().removeChild(childQASet);
						parentQASet.addChild(childQASet);
					}
				}

				@Override
				public Class<D3webCompiler> getCompilerClass() {
					return D3webCompiler.class;
				}
			});
		}

	}

	private void initSectionFinder() {
		this.setSectionFinder(new ConditionalSectionFinder(AllTextFinder.getInstance()) {

			@Override
			protected boolean condition(String text, Section<?> father) {

				Section<DashTreeElement> s = Sections.ancestor(father,
						DashTreeElement.class);
				if (DashTreeUtils.getDashLevel(s) == 0) {
					// is root level
					return true;
				}
				Section<? extends DashTreeElement> dashTreeFather = DashTreeUtils
						.getParentDashTreeElement(s);
				if (dashTreeFather != null) {
					// is child of a QClass declaration => also declaration
					if (Sections.successor(dashTreeFather, QClassLine.class) != null) {
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

			this.addCompileScript(new D3webHandler<InitNumber>() {

				/**
				 * creates the bound-property for a bound-definition
				 */
				@Override
				public Collection<Message> create(D3webCompiler compiler, Section<InitNumber> s) {
					Double originalnumber = s.get().getNumber(s);
					if (originalnumber == null) {
						// if the numbers cannot be found throw error
						return Messages.asList(Messages.objectCreationError(
								"Invalid number"));
					}
					Integer number = (originalnumber.intValue());

					Section<QuestionnaireDefinition> qDef = Sections.successor(
							s.getParent(), QuestionnaireDefinition.class);

					if (qDef != null) {

						QContainer questionnaire = qDef.get().getTermObject(compiler, qDef);

						if (questionnaire != null) {
							boolean alreadyInitDefined = getKnowledgeBase(compiler).removeInitQuestion(
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
								getKnowledgeBase(compiler).addInitQuestion(
										questionnaire,
										number);

								return Messages.noMessage();
							}
						}

					}
					return Messages.asList(Messages.objectCreationError(
							"Init priority"));
				}

				@Override
				public void destroy(D3webCompiler article, Section<InitNumber> s) {
					Section<QuestionnaireDefinition> qDef = Sections.successor(
							s.getParent(), QuestionnaireDefinition.class);

					if (qDef != null) {
						// remove init number value from registration in KB
						QContainer questionnaire = qDef.get().getTermObject(article,
								qDef);
						getKnowledgeBase(article).removeInitQuestion(
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
			catch (Exception ignored) {
			}
			return null;
		}

	}

}
