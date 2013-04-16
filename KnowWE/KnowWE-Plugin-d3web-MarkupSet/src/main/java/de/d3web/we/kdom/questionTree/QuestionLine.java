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

import de.d3web.core.knowledge.terminology.Question;
import de.d3web.core.knowledge.terminology.QuestionNum;
import de.d3web.core.knowledge.terminology.info.BasicProperties;
import de.d3web.core.knowledge.terminology.info.MMInfo;
import de.d3web.core.knowledge.terminology.info.NumericalInterval;
import de.d3web.core.knowledge.terminology.info.NumericalInterval.IntervalException;
import de.d3web.strings.Identifier;
import de.d3web.strings.Strings;
import de.d3web.we.kdom.questionTree.indication.IndicationHandler;
import de.d3web.we.object.QASetDefinition;
import de.d3web.we.object.QuestionDefinition;
import de.d3web.we.object.QuestionDefinition.QuestionType;
import de.d3web.we.object.QuestionnaireDefinition;
import de.d3web.we.reviseHandler.D3webSubtreeHandler;
import de.d3web.we.utils.D3webUtils;
import de.knowwe.core.compile.Priority;
import de.knowwe.core.compile.terminology.TerminologyManager;
import de.knowwe.core.kdom.AbstractType;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.kdom.sectionFinder.AllTextFinderTrimmed;
import de.knowwe.core.kdom.sectionFinder.AllTextSectionFinder;
import de.knowwe.core.report.Message;
import de.knowwe.core.report.Messages;
import de.knowwe.core.utils.KnowWEUtils;
import de.knowwe.kdom.constraint.ConstraintSectionFinder;
import de.knowwe.kdom.constraint.SingleChildConstraint;
import de.knowwe.kdom.dashtree.DashTreeElementContent;
import de.knowwe.kdom.dashtree.DashTreeUtils;
import de.knowwe.kdom.renderer.StyleRenderer;
import de.knowwe.kdom.sectionFinder.ConditionalSectionFinder;
import de.knowwe.kdom.sectionFinder.EmbracedContentFinder;
import de.knowwe.kdom.sectionFinder.MatchUntilEndFinder;
import de.knowwe.kdom.sectionFinder.OneOfStringEnumFinder;
import de.knowwe.kdom.sectionFinder.StringSectionFinderUnquoted;

/**
 * QuestionLine of the QuestionTree, here Questions can be defined
 * 
 * @see QuestionTypeDeclaration
 * 
 * @author Jochen
 * 
 */
public class QuestionLine extends AbstractType {

	public QuestionLine() {

		// every line containing [...] (unquoted) is recognized as QuestionLine
		this.sectionFinder = new ConditionalSectionFinder(new AllTextSectionFinder()) {

			@Override
			protected boolean condition(String text, Section<?> father) {
				return Strings.containsUnquoted(text, "[")
						&& Strings.containsUnquoted(text, "]")
						&& !text.startsWith("[");
			}
		};

		// type of the question '[oc]'
		this.childrenTypes.add(new QuestionTypeDeclaration());
		// abstract flag: '<abstract>'
		this.childrenTypes.add(new AbstractFlag());
		// inline defined choice answers flag: '<low, average, high>'
		this.childrenTypes.add(new InlineChoiceAnswerDefinition());
		// numerical interval/bounds - height [num] (100 220)
		this.childrenTypes.add(new NumBounds());
		// numerical unit - height [num] {cm}
		this.childrenTypes.add(new NumUnit());
		// questiontext - startet by '~'
		this.childrenTypes.add(new QuestionText());

		// finally the name of the question
		this.childrenTypes
				.add(new QuestionTreeQuestionDefinition());
	}

	/**
	 * A QuestionDef type to define questions in the questiontree
	 * 
	 * @author Jochen
	 * 
	 */
	static class QuestionTreeQuestionDefinition extends QuestionDefinition {

		public QuestionTreeQuestionDefinition() {
			ConstraintSectionFinder f = new ConstraintSectionFinder(new AllTextFinderTrimmed());
			f.addConstraint(SingleChildConstraint.getInstance());
			this.setSectionFinder(f);
			// this.addSubtreeHandler(new CreateIndicationHandler());
			this.addSubtreeHandler(IndicationHandler.getInstance());
		}

		@Override
		public QuestionDefinition.QuestionType getQuestionType(Section<QuestionDefinition> s) {
			return QuestionTypeDeclaration
					.getQuestionType(Sections.findSuccessor(
							s.getFather(), QuestionTypeDeclaration.class));
		}

		@Override
		public int getPosition(Section<QuestionDefinition> s) {
			return DashTreeUtils.getPositionInFatherDashSubtree(s);
		}

		@SuppressWarnings("rawtypes")
		@Override
		public Section<? extends QASetDefinition> getParentQASetSection(Section<? extends QuestionDefinition> qdef) {
			Section<? extends DashTreeElementContent> fdtec = DashTreeUtils.getFatherDashTreeElementContent(qdef);
			if (fdtec != null) {
				Section<? extends QASetDefinition> qasetDef = Sections.findSuccessor(fdtec,
						QASetDefinition.class);
				if (qasetDef == null) {
					fdtec = DashTreeUtils.getFatherDashTreeElementContent(fdtec);
					qasetDef = Sections.findSuccessor(fdtec, QASetDefinition.class);
				}
				if (qasetDef != null) {
					if (qasetDef.get() instanceof QuestionnaireDefinition
							|| qasetDef.get() instanceof QuestionDefinition) {
						return qasetDef;
					}
				}
			}
			return null;
		}

	}

	/**
	 * A type allowing for the definition of numerical ranges/boundaries for
	 * numerical questions
	 * 
	 * example:
	 * 
	 * - height [num] (100 220)
	 * 
	 * @author Jochen
	 * 
	 */
	static class NumBounds extends AbstractType {

		public static final char BOUNDS_OPEN = '(';
		public static final char BOUNDS_CLOSE = ')';

		public NumBounds() {
			this.setRenderer(StyleRenderer.NUMBER);
			this.setSectionFinder(new EmbracedContentFinder(BOUNDS_OPEN, BOUNDS_CLOSE));

			this.addSubtreeHandler(Priority.HIGH, new D3webSubtreeHandler<NumBounds>() {

				/**
				 * creates the bound-property for a bound-definition
				 * 
				 * @param article
				 * @param s
				 * @return
				 */
				@Override
				public Collection<Message> create(Article article, Section<NumBounds> s) {

					Double lower = s.get().getLowerBound(s);
					Double upper = s.get().getUpperBound(s);
					if (lower == null || upper == null) {
						// if the numbers cannot be found throw error
						return Messages.asList(Messages.objectCreationError(
								D3webUtils.getD3webBundle()
										.getString("KnowWE.questiontree.incorrectinterval")));
					}

					Section<QuestionDefinition> qDef = Sections.findSuccessor(
							s.getFather(), QuestionDefinition.class);

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
				public void destroy(Article article, Section<NumBounds> sec) {
					// bounds are destroyed together with question
				}
			});
		}

		/**
		 * returns the lower bound of the interval as Double if correctly
		 * defined
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
		 * returns the upper bound of the interval as Double if correctly
		 * defined
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
	 * A type that allows for the definition of units for numerical questions by
	 * embracing it with '{' and '}'
	 * 
	 * The subtreehandler creates the corresponding property for the
	 * question-object in the knowledge base
	 * 
	 * @author Jochen
	 * 
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

			this.addSubtreeHandler(Priority.HIGH, new D3webSubtreeHandler<NumUnit>() {

				/**
				 * creates the unit-property for a unit-definition
				 * 
				 * @param article
				 * @param s
				 * @return
				 */
				@Override
				public Collection<Message> create(Article article, Section<NumUnit> s) {
					Section<QuestionDefinition> qDef = Sections.findSuccessor(
							s.getFather(), QuestionDefinition.class);

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
				public void destroy(Article article, Section<NumUnit> sec) {
					// unit is destroyed together with question
				}
			});
		}

	}

	/**
	 * Allows for the definition of abstract-flagged questions Syntax is:
	 * "<abstract>" or "<abstrakt>"
	 * 
	 * The subtreehandler creates the corresponding
	 * ABSTRACTION_QUESTION-property in the knoweldge base
	 * 
	 * 
	 * @author Jochen
	 * 
	 */
	static class AbstractFlag extends AbstractType {

		public AbstractFlag() {
			this.sectionFinder = new OneOfStringEnumFinder(new String[] {
					"<abstract>", "<abstrakt>" });
			this.setRenderer(StyleRenderer.KEYWORDS);

			this.addSubtreeHandler(Priority.HIGH, new D3webSubtreeHandler<AbstractFlag>() {

				@Override
				public Collection<Message> create(Article article, Section<AbstractFlag> s) {

					Section<QuestionDefinition> qDef = Sections.findSuccessor(
							s.getFather(), QuestionDefinition.class);

					if (qDef != null) {
						Question question = qDef.get().getTermObject(article, qDef);
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
				public void destroy(Article article, Section<AbstractFlag> sec) {
					// flag is destroyed together with question
				}
			});
		}
	}

	/**
	 * A type to allow for the definition of (extended) question-text for a
	 * question leaded by '~'
	 * 
	 * the subtreehandler creates the corresponding DCMarkup using
	 * MMInfoSubject.PROMPT for the question object
	 * 
	 * @author Jochen
	 * 
	 */
	static class QuestionText extends AbstractType {

		private static final String QTEXT_START_SYMBOL = "~";

		public QuestionText() {
			this.sectionFinder = new MatchUntilEndFinder(new StringSectionFinderUnquoted(
					QTEXT_START_SYMBOL));

			this.setRenderer(StyleRenderer.PROMPT);
			this.addSubtreeHandler(Priority.HIGH, new D3webSubtreeHandler<QuestionText>() {

				@Override
				public Collection<Message> create(Article article, Section<QuestionText> sec) {

					Section<QuestionDefinition> qDef = Sections.findSuccessor(
							sec.getFather(), QuestionDefinition.class);

					if (qDef != null) {

						Question question = qDef.get().getTermObject(article, qDef);

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
				public void destroy(Article article, Section<QuestionText> sec) {
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

	static class QuestionTypeChecker extends D3webSubtreeHandler<QuestionTypeDeclaration> {

		@Override
		public Collection<Message> create(Article article, Section<QuestionTypeDeclaration> section) {
			QuestionType thisQuestionType = QuestionTypeDeclaration.getQuestionType(section);
			if (thisQuestionType == null) return Messages.asList();
			TerminologyManager terminologyHandler = KnowWEUtils.getTerminologyManager(article);
			Section<QuestionDefinition> thisQuestionDef = section.get().getQuestionDefinition(
					section);
			Identifier termIdentifier = thisQuestionDef.get().getTermIdentifier(thisQuestionDef);
			Section<?> termDefiningSection =
					terminologyHandler.getTermDefiningSection(termIdentifier);
			if (termDefiningSection != null
					&& termDefiningSection.get() instanceof QuestionDefinition) {
				@SuppressWarnings("unchecked")
				Section<QuestionDefinition> actualQuestionDef = (Section<QuestionDefinition>) termDefiningSection;
				QuestionType actualQuestionType = actualQuestionDef.get().getQuestionType(
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
					String warningText = "The question '"
							+ questionText
							+ "' is already defined with the type '"
							+ actualTypeString
							+ "'. This type definition '"
							+ thisTypeString
							+ "' will be ignored.";
					return Messages.asList(Messages.error(warningText));
				}
			}
			return Messages.asList();
		}
	}
}
