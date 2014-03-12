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
import java.util.LinkedList;
import java.util.List;

import de.d3web.core.knowledge.TerminologyObject;
import de.d3web.core.knowledge.terminology.Choice;
import de.d3web.core.knowledge.terminology.NamedObject;
import de.d3web.core.knowledge.terminology.Question;
import de.d3web.core.knowledge.terminology.QuestionChoice;
import de.d3web.core.knowledge.terminology.QuestionNum;
import de.d3web.core.knowledge.terminology.QuestionYN;
import de.d3web.core.knowledge.terminology.info.BasicProperties;
import de.d3web.core.knowledge.terminology.info.MMInfo;
import de.d3web.core.knowledge.terminology.info.NumericalInterval;
import de.d3web.core.knowledge.terminology.info.NumericalInterval.IntervalException;
import de.d3web.strings.Identifier;
import de.d3web.strings.Strings;
import de.d3web.we.kdom.questionTree.indication.IndicationHandler;
import de.d3web.we.knowledgebase.D3webCompiler;
import de.d3web.we.object.AnswerDefinition;
import de.d3web.we.object.QuestionDefinition;
import de.d3web.we.reviseHandler.D3webHandler;
import de.d3web.we.utils.D3webUtils;
import de.knowwe.core.compile.Priority;
import de.knowwe.core.compile.terminology.TerminologyManager;
import de.knowwe.core.kdom.AbstractType;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.kdom.sectionFinder.AllTextFinder;
import de.knowwe.core.kdom.sectionFinder.AllTextFinderTrimmed;
import de.knowwe.core.report.Message;
import de.knowwe.core.report.Messages;
import de.knowwe.kdom.dashtree.DashTreeTermRelationScript;
import de.knowwe.kdom.constraint.ConstraintSectionFinder;
import de.knowwe.kdom.constraint.SingleChildConstraint;
import de.knowwe.kdom.dashtree.DashTreeElement;
import de.knowwe.kdom.dashtree.DashTreeUtils;
import de.knowwe.kdom.renderer.StyleRenderer;
import de.knowwe.kdom.renderer.StyleRenderer.MaskMode;
import de.knowwe.kdom.sectionFinder.ConditionalSectionFinder;
import de.knowwe.kdom.sectionFinder.EmbracedContentFinder;
import de.knowwe.kdom.sectionFinder.MatchUntilEndFinder;
import de.knowwe.kdom.sectionFinder.OneOfStringFinder;
import de.knowwe.kdom.sectionFinder.StringSectionFinderUnquoted;

/**
 * QuestionLine of the QuestionTree, here Questions can be defined
 *
 * @author Jochen
 * @see QuestionTypeDeclaration
 */
public class QuestionLine extends AbstractType {

	public QuestionLine() {

		// every line containing [...] (unquoted) is recognized as QuestionLine
		this.setSectionFinder(new ConditionalSectionFinder(AllTextFinder.getInstance()) {

			@Override
			protected boolean condition(String text, Section<?> father) {
				return Strings.containsUnquoted(text, "[")
						&& Strings.containsUnquoted(text, "]")
						&& !text.startsWith("[");
			}
		});

		// type of the question '[oc]'
		this.addChildType(new QuestionTypeDeclaration());
		// abstract flag: '<abstract>'
		this.addChildType(new AbstractFlag());
		// inline defined choice answers flag: '<low, average, high>'
		this.addChildType(new InlineChoiceAnswerDefinition());
		// numerical interval/bounds - height [num] (100 220)
		this.addChildType(new NumBounds());
		// numerical unit - height [num] {cm}
		this.addChildType(new NumUnit());
		// questiontext - startet by '~'
		this.addChildType(new QuestionText());

		// finally the name of the question
		this.addChildType(new QuestionTreeQuestionDefinition());
	}

	/**
	 * A QuestionDef type to define questions in the questiontree
	 *
	 * @author Jochen
	 */
	static class QuestionTreeQuestionDefinition extends QuestionDefinition {

		public QuestionTreeQuestionDefinition() {
			ConstraintSectionFinder f = new ConstraintSectionFinder(new AllTextFinderTrimmed());
			f.addConstraint(SingleChildConstraint.getInstance());
			this.setSectionFinder(f);
			this.addCompileScript(IndicationHandler.getInstance());
			this.addCompileScript(Priority.ABOVE_DEFAULT, new DashTreeTermRelationScript<D3webCompiler>() {

				@Override
				protected void createObjectRelations(D3webCompiler compiler, Identifier parentIdentifier, List<Identifier> childrenIdentifier) {
					Question parentQuestion = (Question) D3webUtils.getTermObject(compiler, parentIdentifier);
					if (parentQuestion == null) return;
					TerminologyObject[] parents = parentQuestion.getParents();
					if (parents.length == 0) {
						parentQuestion.getKnowledgeBase().getRootQASet().addChild(parentQuestion);
					}
					for (Identifier childIdentifier : childrenIdentifier) {
						NamedObject childObject = D3webUtils.getTermObject(compiler, childIdentifier);
						if (childObject == null) continue;
						if (childObject instanceof Question) {
							Question childQuestion = (Question) childObject;
							parentQuestion.getKnowledgeBase().getRootQASet().removeChild(childQuestion);
							parentQuestion.addChild(childQuestion);
						}
						else if (parentQuestion instanceof QuestionChoice && childObject instanceof Choice) {
							// nothing to to for QuestionYN, answers are already there and immutable
							if (parentQuestion instanceof QuestionYN) continue;
							((QuestionChoice) parentQuestion).addAlternative((Choice) childObject);
						}
					}
				}

				/**
				 * In QuestionTreeQuestionDefinitions, we have followup questions not only directly as children of the
				 * current question, but also as children of the answers of the question.
				 */
				@Override
				protected List<Section<DashTreeElement>> getChildrenDashtreeElements(Section<?> termDefiningSection) {
					List<Section<DashTreeElement>> childrenList = super.getChildrenDashtreeElements(termDefiningSection);
					LinkedList<Section<DashTreeElement>> augmentedChildrenList = new LinkedList();
					for (Section<DashTreeElement> child : childrenList) {
						augmentedChildrenList.add(child);
						Section<AnswerDefinition> answerDef = Sections.findSuccessor(child, AnswerDefinition.class);
						Section<NumericCondLine> numCondLine = Sections.findSuccessor(child, NumericCondLine.class);
						if (answerDef == null && numCondLine == null) continue;
						// if we have a AnswerDefinition, look for Questions below
						List<Section<DashTreeElement>> followUpQuestions = DashTreeUtils.findChildrenDashtreeElements(child);
						for (Section<DashTreeElement> followUpQuestion : followUpQuestions) {
							// we ignore &REF sections
							if (Sections.findSuccessor(child, QuestionDefinition.class) != null) continue;
							augmentedChildrenList.addAll(followUpQuestions);
						}
					}
					return augmentedChildrenList;
				}

				@Override
				public Class<D3webCompiler> getCompilerClass() {
					return D3webCompiler.class;
				}
			});
		}

		@Override
		public QuestionDefinition.QuestionType getQuestionType(Section<QuestionDefinition> s) {
			return QuestionTypeDeclaration
					.getQuestionType(Sections.findSuccessor(
							s.getParent(), QuestionTypeDeclaration.class));
		}

	}

	/**
	 * A type allowing for the definition of numerical ranges/boundaries for numerical questions
	 * <p/>
	 * example:
	 * <p/>
	 * - height [num] (100 220)
	 *
	 * @author Jochen
	 */
	static class NumBounds extends AbstractType {

		public static final char BOUNDS_OPEN = '(';
		public static final char BOUNDS_CLOSE = ')';

		public NumBounds() {
			this.setRenderer(StyleRenderer.NUMBER);
			this.setSectionFinder(new EmbracedContentFinder(BOUNDS_OPEN, BOUNDS_CLOSE));

			this.addCompileScript(Priority.HIGH, new D3webHandler<NumBounds>() {

				/**
				 * creates the bound-property for a bound-definition
				 *
				 * @param article
				 * @param s
				 * @return
				 */
				@Override
				public Collection<Message> create(D3webCompiler article, Section<NumBounds> s) {

					Double lower = s.get().getLowerBound(s);
					Double upper = s.get().getUpperBound(s);
					if (lower == null || upper == null) {
						// if the numbers cannot be found throw error
						return Messages.asList(Messages.objectCreationError(
								D3webUtils.getD3webBundle()
										.getString("KnowWE.questiontree.incorrectinterval")));
					}

					Section<QuestionDefinition> qDef = Sections.findSuccessor(
							s.getParent(), QuestionDefinition.class);

					if (qDef != null) {

						Question question = qDef.get().getTermObject(article, qDef);
						if (!(question instanceof QuestionNum)) {
							// if not numerical question throw error
							return Messages.asList(Messages.objectCreationError(
									D3webUtils.getD3webBundle()
											.getString("KnowWE.questiontree.onlyfornumerical")));
						}
						try {
							// trying to create interval
							// --> throws IntervalException if
							// for example lower > upper
							NumericalInterval interval = new NumericalInterval(lower,
									upper);
							interval.checkValidity();
							question.getInfoStore().addValue(BasicProperties.QUESTION_NUM_RANGE,
									interval);
							return Messages.asList(Messages.objectCreatedNotice(
									D3webUtils.getD3webBundle()
											.getString("KnowWE.questiontree.setnumerical")));
						}
						catch (IntervalException e) {
							return Messages.asList(Messages.objectCreationError(
									D3webUtils.getD3webBundle()
											.getString("KnowWE.questiontree.invalidinterval")));
						}

					}
					return Messages.asList(Messages.objectCreationError(
							D3webUtils.getD3webBundle()
									.getString("KnowWE.questiontree.numerical")));
				}

				@Override
				public void destroy(D3webCompiler article, Section<NumBounds> sec) {
					// bounds are destroyed together with question
				}
			});
		}

		/**
		 * returns the lower bound of the interval as Double if correctly defined
		 *
		 * @param s
		 * @return
		 */
		public Double getLowerBound(Section<NumBounds> s) {
			String originalText = s.getText();
			String content = originalText.substring(1, originalText.length() - 1).trim();

			String[] numbers = content.split(" ");
			if (numbers.length == 2) {
				Double d = null;
				try {
					d = Double.parseDouble(numbers[0]);
					return d;
				}
				catch (Exception e) {

				}

			}

			return null;
		}

		/**
		 * returns the upper bound of the interval as Double if correctly defined
		 *
		 * @param s
		 * @return
		 */
		public Double getUpperBound(Section<NumBounds> s) {
			String originalText = s.getText();
			String content = originalText.substring(1, originalText.length() - 1).trim();

			String[] numbers = content.split(" ");
			if (numbers.length == 2) {
				Double d = null;
				try {
					d = Double.parseDouble(numbers[1]);
					return d;
				}
				catch (Exception e) {

				}

			}

			return null;
		}
	}

	/**
	 * A type that allows for the definition of units for numerical questions by embracing it with '{' and '}'
	 * <p/>
	 * The subtreehandler creates the corresponding property for the question-object in the knowledge base
	 *
	 * @author Jochen
	 */
	static class NumUnit extends AbstractType {

		public static final char UNIT_OPEN = '{';
		public static final char UNIT_CLOSE = '}';

		public String getUnit(Section<NumUnit> s) {
			String originalText = s.getText();
			originalText = originalText.substring(1, originalText.length() - 1);
			originalText = Strings.trimQuotes(originalText);
			return originalText;
		}

		public NumUnit() {
			this.setRenderer(StyleRenderer.NUMBER);

			this.setSectionFinder(new EmbracedContentFinder(UNIT_OPEN, UNIT_CLOSE));

			this.addCompileScript(Priority.HIGH, new D3webHandler<NumUnit>() {

				/**
				 * creates the unit-property for a unit-definition
				 *
				 * @param article
				 * @param s
				 * @return
				 */
				@Override
				public Collection<Message> create(D3webCompiler article, Section<NumUnit> s) {
					Section<QuestionDefinition> qDef = Sections.findSuccessor(
							s.getParent(), QuestionDefinition.class);

					if (qDef != null) {

						Question question = qDef.get().getTermObject(article, qDef);
						if (!(question instanceof QuestionNum)) {
							return Messages.asList(Messages.objectCreationError(
									D3webUtils.getD3webBundle()
											.getString("KnowWE.questiontree.onlyfornumerical")));
						}
						question.getInfoStore().addValue(MMInfo.UNIT, s.get().getUnit(s));
						return Messages.asList(Messages.objectCreatedNotice(
								D3webUtils.getD3webBundle()
										.getString("KnowWE.questiontree.setunit")));

					}
					return Messages.asList(Messages.objectCreationError(
							D3webUtils.getD3webBundle()
									.getString("KnowWE.questiontree.unit")));
				}

				@Override
				public void destroy(D3webCompiler article, Section<NumUnit> sec) {
					// unit is destroyed together with question
				}
			});
		}

	}

	/**
	 * Allows for the definition of abstract-flagged questions Syntax is: "<abstract>" or "<abstrakt>"
	 * <p/>
	 * The subtreehandler creates the corresponding ABSTRACTION_QUESTION-property in the knoweldge base
	 *
	 * @author Jochen
	 */
	static class AbstractFlag extends AbstractType {

		public AbstractFlag() {
			this.setSectionFinder(new OneOfStringFinder(new String[] {
					"<abstract>", "<abstrakt>" }));
			this.setRenderer(new StyleRenderer(StyleRenderer.KEYWORDS, MaskMode.htmlEntities));

			this.addCompileScript(Priority.HIGH, new D3webHandler<AbstractFlag>() {

				@Override
				public Collection<Message> create(D3webCompiler compiler, Section<AbstractFlag> s) {

					Section<QuestionDefinition> qDef = Sections.findSuccessor(
							s.getParent(), QuestionDefinition.class);

					if (qDef != null) {
						Question question = qDef.get().getTermObject(compiler, qDef);
						if (question != null) {
							question.getInfoStore().addValue(BasicProperties.ABSTRACTION_QUESTION,
									Boolean.TRUE);
							return Messages.asList(Messages.objectCreatedNotice(
									D3webUtils.getD3webBundle()
											.getString("KnowWE.questiontree.abstractquestion")));
						}

					}
					return Messages.asList(Messages.objectCreationError(
							D3webUtils.getD3webBundle()
									.getString("KnowWE.questiontree.abstractflag")));
				}

				@Override
				public void destroy(D3webCompiler article, Section<AbstractFlag> sec) {
					// flag is destroyed together with question
				}
			});
		}
	}

	/**
	 * A type to allow for the definition of (extended) question-text for a question leaded by '~'
	 * <p/>
	 * the subtreehandler creates the corresponding DCMarkup using MMInfoSubject.PROMPT for the question object
	 *
	 * @author Jochen
	 */
	static class QuestionText extends AbstractType {

		private static final String QTEXT_START_SYMBOL = "~";

		public QuestionText() {
			this.setSectionFinder(new MatchUntilEndFinder(new StringSectionFinderUnquoted(
					QTEXT_START_SYMBOL)));

			this.setRenderer(StyleRenderer.PROMPT);
			this.addCompileScript(Priority.HIGH, new D3webHandler<QuestionText>() {

				@Override
				public Collection<Message> create(D3webCompiler compiler, Section<QuestionText> sec) {

					Section<QuestionDefinition> qDef = Sections.findSuccessor(
							sec.getParent(), QuestionDefinition.class);

					if (qDef != null) {

						Question question = qDef.get().getTermObject(compiler, qDef);

						if (question != null) {
							question.getInfoStore().addValue(MMInfo.PROMPT,
									QuestionText.getQuestionText(sec));
							return Messages.asList(Messages.objectCreatedNotice(
									D3webUtils.getD3webBundle()
											.getString("KnowWE.questiontree.questiontextcreated")));
						}
					}
					return Messages.asList(Messages.objectCreationError(
							D3webUtils.getD3webBundle()
									.getString("KnowWE.questiontree.questiontext")));
				}

				@Override
				public void destroy(D3webCompiler article, Section<QuestionText> sec) {
					// text is destroyed together with question
				}
			});
		}

		public static String getQuestionText(Section<QuestionText> s) {
			String text = s.getText();
			if (text.startsWith(QTEXT_START_SYMBOL)) {
				text = text.substring(1).trim();
			}

			return Strings.unquote(text);
		}
	}

	static class QuestionTypeChecker extends D3webHandler<QuestionTypeDeclaration> {

		@Override
		public Collection<Message> create(D3webCompiler compiler, Section<QuestionTypeDeclaration> section) {
			QuestionDefinition.QuestionType thisQuestionType = QuestionTypeDeclaration.getQuestionType(section);
			if (thisQuestionType == null) return Messages.asList();
			TerminologyManager terminologyHandler = compiler.getTerminologyManager();
			Section<QuestionDefinition> thisQuestionDef = section.get().getQuestionDefinition(
					section);
			Identifier termIdentifier = thisQuestionDef.get().getTermIdentifier(thisQuestionDef);
			Section<?> termDefiningSection =
					terminologyHandler.getTermDefiningSection(termIdentifier);
			if (termDefiningSection != null
					&& termDefiningSection.get() instanceof QuestionDefinition) {
				@SuppressWarnings("unchecked")
				Section<QuestionDefinition> actualQuestionDef = (Section<QuestionDefinition>) termDefiningSection;
				QuestionDefinition.QuestionType actualQuestionType = actualQuestionDef.get().getQuestionType(
						actualQuestionDef);
				String actualTypeString = actualQuestionType == null
						? "undefined"
						: actualQuestionType.toString().toLowerCase();
				String thisTypeString = thisQuestionType == null
						? "undefined"
						: thisQuestionType.toString().toLowerCase();
				if (actualQuestionType != thisQuestionType) {
					String questionText = actualQuestionDef.get().getTermIdentifier(
							actualQuestionDef).toString();
					questionText = Strings.trimQuotes(questionText);
					String warningText = "The question '" + questionText + "' is already defined with the type '"
							+ actualTypeString + "'. This type definition '" + thisTypeString + "' will be ignored.";
					return Messages.asList(Messages.error(warningText));
				}
			}
			return Messages.asList();
		}
	}
}
