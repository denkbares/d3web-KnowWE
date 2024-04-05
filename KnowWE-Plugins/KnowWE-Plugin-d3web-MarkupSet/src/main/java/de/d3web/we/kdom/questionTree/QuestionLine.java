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

import com.denkbares.strings.Identifier;
import com.denkbares.strings.Strings;
import de.d3web.core.knowledge.KnowledgeBase;
import de.d3web.core.knowledge.terminology.Question;
import de.d3web.core.knowledge.terminology.QuestionDate;
import de.d3web.core.knowledge.terminology.QuestionMC;
import de.d3web.core.knowledge.terminology.QuestionNum;
import de.d3web.core.knowledge.terminology.QuestionOC;
import de.d3web.core.knowledge.terminology.QuestionYN;
import de.d3web.core.knowledge.terminology.QuestionZC;
import de.d3web.core.knowledge.terminology.info.BasicProperties;
import de.d3web.core.knowledge.terminology.info.MMInfo;
import de.d3web.core.knowledge.terminology.info.NumericalInterval;
import de.d3web.core.knowledge.terminology.info.NumericalInterval.IntervalException;
import de.d3web.we.kdom.questionTree.indication.IndicationHandler;
import de.d3web.we.knowledgebase.D3webCompileScript;
import de.d3web.we.knowledgebase.D3webCompiler;
import de.d3web.we.object.AbortCheck;
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
import de.knowwe.core.kdom.sectionFinder.ConditionalSectionFinder;
import de.knowwe.core.report.CompilerMessage;
import de.knowwe.core.report.Message;
import de.knowwe.core.report.Messages;
import de.knowwe.d3web.property.PropertyDeclarationHandler;
import de.knowwe.kdom.constraint.ConstraintSectionFinder;
import de.knowwe.kdom.constraint.SingleChildConstraint;
import de.knowwe.kdom.renderer.StyleRenderer;
import de.knowwe.kdom.renderer.StyleRenderer.MaskMode;
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
		// numerical unit - height [num] {cm}
		this.addChildType(new NumUnit());
		// numerical interval/bounds - height [num] (100 220)
		this.addChildType(new NumBounds());
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
			this.addCompileScript(Priority.HIGHER, new CreateQuestionHandler());
			this.addCompileScript(IndicationHandler.getInstance());
			this.addCompileScript(Priority.ABOVE_DEFAULT, new QuestionTreeQuestionRelationScript());
		}

		@Override
		public QuestionDefinition.QuestionType getQuestionType(Section<QuestionDefinition> s) {
			return QuestionTypeDeclaration.getQuestionType(Sections.successor(s.getParent(), QuestionTypeDeclaration.class));
		}
	}

	static class CreateQuestionHandler implements D3webHandler<QuestionDefinition> {

		@Override
		public Collection<Message> create(D3webCompiler compiler,
										  Section<QuestionDefinition> section) {

			Identifier identifier = section.get().getTermIdentifier(compiler, section);
			Class<?> termObjectClass = section.get().getTermObjectClass(compiler, section);
			TerminologyManager terminologyHandler = compiler.getTerminologyManager();
			terminologyHandler.registerTermDefinition(compiler, section, termObjectClass,
					identifier);

			AbortCheck<Question> abortCheck = section.get().canAbortTermObjectCreation(compiler, section);
			if (abortCheck.hasErrors()) {
				return abortCheck.getErrors();
			}

			if (!abortCheck.termExist()) {
				KnowledgeBase kb = getKnowledgeBase(compiler);

				String name = section.get().getTermName(section);

				QuestionDefinition.QuestionType questionType = section.get().getQuestionType(section);
				if (questionType == null) {
					return Messages.asList(Messages.objectCreationError(
							"No type found for question '" + name + "'"));
				}

				if (questionType == QuestionDefinition.QuestionType.OC) {
					new QuestionOC(kb, name);
				}
				else if (questionType == QuestionDefinition.QuestionType.MC) {
					new QuestionMC(kb, name);
				}
				else if (questionType == QuestionDefinition.QuestionType.NUM) {
					new QuestionNum(kb, name);
				}
				else if (questionType == QuestionDefinition.QuestionType.YN) {
					new QuestionYN(kb, name);
				}
				else if (questionType == QuestionDefinition.QuestionType.DATE) {
					new QuestionDate(kb, name);
				}
				else if (questionType == QuestionDefinition.QuestionType.INFO) {
					new QuestionZC(kb, name);
				}
				else if (questionType == QuestionDefinition.QuestionType.TEXT) {
					new de.d3web.core.knowledge.terminology.QuestionText(kb, name);
				}
				else {
					return Messages.asList(Messages.error(
							"No valid question type found for question '" + identifier + "'"));
				}
			}

			return Messages.noMessage();
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

			this.addCompileScript(Priority.HIGH, (D3webHandler<NumBounds>) (article, s) -> {

				Double lower = s.get().getLowerBound(s);
				Double upper = s.get().getUpperBound(s);
				if (lower == null || upper == null) {
					// if the numbers cannot be found throw error
					return Messages.asList(Messages.objectCreationError(
							D3webUtils.getD3webBundle()
									.getString("KnowWE.questiontree.incorrectinterval")));
				}

				Section<QuestionDefinition> qDef = Sections.successor(
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
						return Messages.noMessage();
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
			});
		}

		/**
		 * Returns the lower bound of the interval as Double if correctly defined
		 */
		public Double getLowerBound(Section<NumBounds> s) {
			String originalText = s.getText();
			String content = originalText.substring(1, originalText.length() - 1).trim();

			String[] numbers = content.split(" ");
			if (numbers.length == 2) {
				try {
					return Double.parseDouble(numbers[0]);
				}
				catch (Exception ignored) {
				}
			}

			return null;
		}

		/**
		 * Returns the upper bound of the interval as Double if correctly defined
		 */
		public Double getUpperBound(Section<NumBounds> s) {
			String originalText = s.getText();
			String content = originalText.substring(1, originalText.length() - 1).trim();

			String[] numbers = content.split(" ");
			if (numbers.length == 2) {
				try {
					return Double.parseDouble(numbers[1]);
				}
				catch (Exception ignored) {
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

			this.addCompileScript(Priority.HIGH, (D3webHandler<NumUnit>) (article, s) -> {
				Section<QuestionDefinition> qDef = Sections.successor(
						s.getParent(), QuestionDefinition.class);

				if (qDef != null) {

					Question question = qDef.get().getTermObject(article, qDef);
					if (!(question instanceof QuestionNum || question instanceof QuestionDate)) {
						return Messages.asList(Messages.error("Units are only allowed for numerical and date questions"));
					}
					String unit = s.get().getUnit(s);
					try {
						PropertyDeclarationHandler.validateProperty(question, MMInfo.UNIT, unit);
					}
					catch (IllegalArgumentException e) {
						return Messages.asList(Messages.error(e.getMessage()));
					}
					question.getInfoStore().addValue(MMInfo.UNIT, unit);
					return Messages.noMessage();
				}
				return Messages.asList(Messages.objectCreationError(
						D3webUtils.getD3webBundle()
								.getString("KnowWE.questiontree.unit")));
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
			this.setSectionFinder(new OneOfStringFinder("<abstract>", "<abstrakt>"));
			this.setRenderer(StyleRenderer.KEYWORDS.withMaskMode(MaskMode.htmlEntities));

			this.addCompileScript(Priority.HIGH, (D3webCompileScript<AbstractFlag>) (compiler, s) -> {

				Section<QuestionDefinition> qDef = Sections.successor(
						s.getParent(), QuestionDefinition.class);

				if (qDef != null) {
					Question question = qDef.get().getTermObject(compiler, qDef);
					if (question != null) {
						question.getInfoStore().addValue(BasicProperties.ABSTRACTION_QUESTION, Boolean.TRUE);
						return;
					}
				}
				throw new CompilerMessage(Messages.objectCreationError(
						D3webUtils.getD3webBundle().getString("KnowWE.questiontree.abstractflag")));
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
			this.setRenderer((section, user, result) -> StyleRenderer.PROMPT.renderText(QTEXT_START_SYMBOL + section.getText(), user, result));
			this.addCompileScript(Priority.HIGH, (D3webHandler<QuestionText>) (compiler, sec) -> {

				Section<QuestionDefinition> qDef = Sections.successor(
						sec.getParent(), QuestionDefinition.class);

				if (qDef != null) {

					Question question = qDef.get().getTermObject(compiler, qDef);

					if (question != null) {
						question.getInfoStore().addValue(MMInfo.PROMPT,
								QuestionText.getQuestionText(sec));
						return Messages.noMessage();
					}
				}
				return Messages.asList(Messages.objectCreationError(
						D3webUtils.getD3webBundle()
								.getString("KnowWE.questiontree.questiontext")));
			});
		}

		public static String getQuestionText(Section<QuestionText> s) {
			String text = s.getText();
			if (text.startsWith(QTEXT_START_SYMBOL)) {
				text = Strings.trim(text.substring(1));
			}
			return Strings.unquote(text);
		}
	}

	static class QuestionTypeChecker implements D3webHandler<QuestionTypeDeclaration> {

		@Override
		public Collection<Message> create(D3webCompiler compiler, Section<QuestionTypeDeclaration> section) {
			QuestionDefinition.QuestionType thisQuestionType = QuestionTypeDeclaration.getQuestionType(section);
			if (thisQuestionType == null) return Messages.asList();
			TerminologyManager terminologyHandler = compiler.getTerminologyManager();
			Section<QuestionDefinition> thisQuestionDef = section.get().getQuestionDefinition(
					section);
			Identifier termIdentifier = thisQuestionDef.get().getTermIdentifier(compiler, thisQuestionDef);
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
				String thisTypeString = thisQuestionType.toString().toLowerCase();
				if (actualQuestionType != thisQuestionType) {
					String questionText = actualQuestionDef.get().getTermIdentifier(
							compiler, actualQuestionDef).toPrettyPrint();
					questionText = Strings.trimQuotes(questionText);
					String warningText = "The question '" + questionText + "' is already defined with the type '"
							+ actualTypeString + "'. This type definition '" + thisTypeString + "' will be ignored.";
					return Messages.asList(Messages.error(warningText));
				}
			}
			return Messages.asList();
		}

		@Override
		public boolean isIncrementalCompilationSupported(Section<QuestionTypeDeclaration> section) {
			return true;
		}
	}
}
