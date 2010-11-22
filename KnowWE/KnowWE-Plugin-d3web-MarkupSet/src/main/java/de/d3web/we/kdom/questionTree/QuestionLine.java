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

import de.d3web.core.knowledge.terminology.Question;
import de.d3web.core.knowledge.terminology.QuestionNum;
import de.d3web.core.knowledge.terminology.info.BasicProperties;
import de.d3web.core.knowledge.terminology.info.MMInfo;
import de.d3web.core.knowledge.terminology.info.NumericalInterval;
import de.d3web.core.knowledge.terminology.info.NumericalInterval.IntervalException;
import de.d3web.we.basic.D3webModule;
import de.d3web.we.kdom.DefaultAbstractKnowWEObjectType;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.constraint.ConstraintSectionFinder;
import de.d3web.we.kdom.constraint.SingleChildConstraint;
import de.d3web.we.kdom.objects.KnowWETermMarker;
import de.d3web.we.kdom.questionTree.QuestionLine.QuestionTypeDeclaration;
import de.d3web.we.kdom.questionTree.indication.IndicationHandler;
import de.d3web.we.kdom.report.KDOMReportMessage;
import de.d3web.we.kdom.report.SimpleMessageError;
import de.d3web.we.kdom.report.message.ObjectCreatedMessage;
import de.d3web.we.kdom.report.message.ObjectCreationError;
import de.d3web.we.kdom.sectionFinder.AllTextFinderTrimmed;
import de.d3web.we.kdom.sectionFinder.AllTextSectionFinder;
import de.d3web.we.kdom.sectionFinder.ConditionalSectionFinder;
import de.d3web.we.kdom.sectionFinder.EmbracedContentFinder;
import de.d3web.we.kdom.sectionFinder.MatchUntilEndFinder;
import de.d3web.we.kdom.sectionFinder.OneOfStringEnumFinder;
import de.d3web.we.kdom.sectionFinder.SectionFinder;
import de.d3web.we.kdom.sectionFinder.SectionFinderResult;
import de.d3web.we.kdom.sectionFinder.StringEnumChecker;
import de.d3web.we.kdom.sectionFinder.StringSectionFinderUnquoted;
import de.d3web.we.kdom.subtreeHandler.SubtreeHandler;
import de.d3web.we.object.QASetDefinition;
import de.d3web.we.object.QuestionDefinition;
import de.d3web.we.object.QuestionDefinition.QuestionType;
import de.d3web.we.object.QuestionnaireDefinition;
import de.d3web.we.reviseHandler.D3webSubtreeHandler;
import de.d3web.we.utils.KnowWEUtils;
import de.d3web.we.utils.SplitUtility;
import de.knowwe.core.dashtree.DashTreeElementContent;
import de.knowwe.core.dashtree.DashTreeUtils;
import de.knowwe.core.renderer.FontColorRenderer;

/**
 * QuestionLine of the QuestionTree, here Questions can be defined
 * 
 * @see QuestionTypeDeclaration
 * 
 * @author Jochen
 * 
 */
public class QuestionLine extends DefaultAbstractKnowWEObjectType {

	public QuestionLine() {

		// every line containing [...] (unquoted) is recognized as QuestionLine
		this.sectionFinder = new ConditionalSectionFinder(new AllTextSectionFinder()) {

			@Override
			protected boolean condition(String text, Section<?> father) {
				return SplitUtility.containsUnquoted(text, "[")
						&& SplitUtility.containsUnquoted(text, "]")
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

		@Override
		protected void init() {
			ConstraintSectionFinder f = new ConstraintSectionFinder(new AllTextFinderTrimmed());
			f.addConstraint(SingleChildConstraint.getInstance());
			this.setSectionFinder(f);
			// this.addSubtreeHandler(new CreateIndicationHandler());
			this.addSubtreeHandler(IndicationHandler.getInstance());
		}

		@Override
		public QuestionDefinition.QuestionType getQuestionType(Section<QuestionDefinition> s) {
			return QuestionTypeDeclaration
					.getQuestionType(s.getFather().findSuccessor(
							QuestionTypeDeclaration.class));
		}

		@Override
		public int getPosition(Section<QuestionDefinition> s) {
			return DashTreeUtils.getPositionInFatherDashSubtree(s);
		}

		@Override
		public Section<? extends QASetDefinition> getParentQASetSection(Section<? extends QuestionDefinition> qdef) {
			Section<? extends DashTreeElementContent> fdtec = DashTreeUtils.getFatherDashTreeElementContent(qdef);
			if (fdtec != null) {
				Section<? extends QASetDefinition> qasetDef = fdtec.findSuccessor(QASetDefinition.class);
				if (qasetDef == null) {
					fdtec = DashTreeUtils.getFatherDashTreeElementContent(fdtec);
					qasetDef = fdtec.findSuccessor(QASetDefinition.class);
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

		@Override
		public boolean hasViolatedConstraints(KnowWEArticle article, Section<?> s) {
			return QuestionDashTreeUtils.isChangeInRootQuestionSubtree(article, s);
		}

	}

	// /**
	// * This handler creates an indication rule if a question if son of an
	// answer
	// * if a preceeding question
	// *
	// * @author Jochen
	// *
	// */
	// static class CreateIndicationHandler extends
	// QuestionTreeElementDefSubtreeHandler<QuestionTreeQuestionDef> {
	//
	// private final String ruleKey = "RULE_STORE_KEY";
	//
	// @Override
	// public Collection<KDOMReportMessage> create(KnowWEArticle article,
	// Section<QuestionTreeQuestionDef> qidSection) {
	//
	// // current DashTreeElement
	// Section<DashTreeElement> element = KnowWEObjectTypeUtils
	// .getAncestorOfType(qidSection, DashTreeElement.class);
	//
	// // get dashTree-father
	// Section<? extends DashTreeElement> dashTreeFather = DashTreeElement
	// .getDashTreeFather(element);
	//
	// Section<QuestionTreeAnswerDef> answerSec = dashTreeFather
	// .findSuccessor(QuestionTreeAnswerDef.class);
	// Section<NumericCondLine> numCondSec = dashTreeFather
	// .findSuccessor(NumericCondLine.class);
	//
	// if (answerSec != null || numCondSec != null) {
	//
	// KnowledgeBaseManagement mgn = getKBM(article);
	//
	// String newRuleID = mgn.createRuleID();
	//
	// Condition cond = Utils.createCondition(article,
	// DashTreeElement.getDashTreeAncestors(element));
	//
	// Rule r = RuleFactory.createIndicationRule(newRuleID, qidSection
	// .get().getObject(article, qidSection), cond);
	// if (r != null) {
	// KnowWEUtils.storeSectionInfo(article, qidSection, ruleKey, r);
	// return Arrays.asList((KDOMReportMessage) new ObjectCreatedMessage(
	// r.getClass() + " : "
	// + r.getId()));
	// }
	// else {
	// return Arrays.asList((KDOMReportMessage) new CreateRelationFailed(
	// Rule.class.getSimpleName()));
	// }
	// }
	//
	// return new ArrayList<KDOMReportMessage>(0);
	// }
	//
	// @Override
	// public void destroy(KnowWEArticle article,
	// Section<QuestionTreeQuestionDef> s) {
	// Rule kbr = (Rule) KnowWEUtils.getObjectFromLastVersion(article, s,
	// ruleKey);
	// if (kbr != null) kbr.remove();
	// }
	//
	// }

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
	static class NumBounds extends DefaultAbstractKnowWEObjectType {

		public static final char BOUNDS_OPEN = '(';
		public static final char BOUNDS_CLOSE = ')';

		public NumBounds() {
			this.setCustomRenderer(new FontColorRenderer(FontColorRenderer.COLOR7));
			this.setSectionFinder(new EmbracedContentFinder(BOUNDS_OPEN, BOUNDS_CLOSE));

			this.addSubtreeHandler(new SubtreeHandler<NumBounds>() {

				/**
				 * creates the bound-property for a bound-definition
				 * 
				 * @param article
				 * @param s
				 * @return
				 */
				@Override
				public Collection<KDOMReportMessage> create(KnowWEArticle article, Section<NumBounds> s) {

					Double lower = s.get().getLowerBound(s);
					Double upper = s.get().getUpperBound(s);
					if (lower == null || upper == null) {
						// if the numbers cannot be found throw error
						return Arrays.asList((KDOMReportMessage) new ObjectCreationError(
								D3webModule.getKwikiBundle_d3web()
										.getString("KnowWE.questiontree.incorrectinterval"),
								this.getClass()));
					}

					Section<QuestionDefinition> qDef = s.getFather().findSuccessor(
							QuestionDefinition.class);

					if (qDef != null) {

						Question question = qDef.get().getTermObject(article, qDef);
						if (!(question instanceof QuestionNum)) {
							// if not numerical question throw error
							return Arrays.asList((KDOMReportMessage) new ObjectCreationError(
									D3webModule.getKwikiBundle_d3web()
											.getString("KnowWE.questiontree.onlyfornumerical"),
									this.getClass()));
						}
						try {
							// trying to create interval
							// --> throws IntervalException if
							// for example lower > upper
							NumericalInterval interval = new NumericalInterval(lower,
									upper);
							question.getInfoStore().addValue(BasicProperties.QUESTION_NUM_RANGE,
									interval);
							return Arrays.asList((KDOMReportMessage) new ObjectCreatedMessage(
									D3webModule.getKwikiBundle_d3web()
											.getString("KnowWE.questiontree.setnumerical")));
						}
						catch (IntervalException e) {
							return Arrays.asList((KDOMReportMessage) new ObjectCreationError(
									D3webModule.getKwikiBundle_d3web()
											.getString("KnowWE.questiontree.invalidinterval"),
									this.getClass()));
						}

					}
					return Arrays.asList((KDOMReportMessage) new ObjectCreationError(
							D3webModule.getKwikiBundle_d3web()
									.getString("KnowWE.questiontree.numerical"),
							this.getClass()));
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
			String originalText = s.getOriginalText();
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
			String originalText = s.getOriginalText();
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
	static class NumUnit extends DefaultAbstractKnowWEObjectType {

		public static final char UNIT_OPEN = '{';
		public static final char UNIT_CLOSE = '}';

		public String getUnit(Section<NumUnit> s) {
			String originalText = s.getOriginalText();
			originalText = originalText.substring(1, originalText.length() - 1);
			originalText = KnowWEUtils.trimQuotes(originalText);
			return originalText;
		}

		public NumUnit() {
			this.setCustomRenderer(new FontColorRenderer(FontColorRenderer.COLOR7));

			this.setSectionFinder(new EmbracedContentFinder(UNIT_OPEN, UNIT_CLOSE));

			this.addSubtreeHandler(new SubtreeHandler<NumUnit>() {

				/**
				 * creates the unit-property for a unit-definition
				 * 
				 * @param article
				 * @param s
				 * @return
				 */
				@Override
				public Collection<KDOMReportMessage> create(KnowWEArticle article, Section<NumUnit> s) {
					Section<QuestionDefinition> qDef = s.getFather().findSuccessor(
							QuestionDefinition.class);

					if (qDef != null) {

						Question question = qDef.get().getTermObject(article, qDef);
						if (!(question instanceof QuestionNum)) {
							return Arrays.asList((KDOMReportMessage) new ObjectCreationError(
									D3webModule.getKwikiBundle_d3web()
											.getString("KnowWE.questiontree.onlyfornumerical"),
									this.getClass()));
						}
						question.getInfoStore().addValue(MMInfo.UNIT, s.get().getUnit(s));
						return Arrays.asList((KDOMReportMessage) new ObjectCreatedMessage(
								D3webModule.getKwikiBundle_d3web()
										.getString("KnowWE.questiontree.setunit")));

					}
					return Arrays.asList((KDOMReportMessage) new ObjectCreationError(
							D3webModule.getKwikiBundle_d3web()
									.getString("KnowWE.questiontree.unit"),
							this.getClass()));
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
	static class AbstractFlag extends DefaultAbstractKnowWEObjectType implements KnowWETermMarker {

		public AbstractFlag() {
			this.sectionFinder = new OneOfStringEnumFinder(new String[] {
					"<abstract>", "<abstrakt>" });
			this.setCustomRenderer(new FontColorRenderer(FontColorRenderer.COLOR7));

			this.addSubtreeHandler(new D3webSubtreeHandler<AbstractFlag>() {

				@Override
				public Collection<KDOMReportMessage> create(KnowWEArticle article, Section<AbstractFlag> s) {

					Section<QuestionDefinition> qDef = s.getFather().findSuccessor(
							QuestionDefinition.class);

					if (qDef != null) {

						Question question = qDef.get().getTermObject(article, qDef);
						question.getInfoStore().addValue(BasicProperties.ABSTRACTION_QUESTION,
								Boolean.TRUE);
						return Arrays.asList((KDOMReportMessage) new ObjectCreatedMessage(
								D3webModule.getKwikiBundle_d3web()
										.getString("KnowWE.questiontree.abstractquestion")));

					}
					return Arrays.asList((KDOMReportMessage) new ObjectCreationError(
							D3webModule.getKwikiBundle_d3web()
									.getString("KnowWE.questiontree.abstractflag"),
							this.getClass()));
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
	static class QuestionText extends DefaultAbstractKnowWEObjectType {

		private static final String QTEXT_START_SYMBOL = "~";

		@Override
		protected void init() {
			this.sectionFinder = new MatchUntilEndFinder(new StringSectionFinderUnquoted(
					QTEXT_START_SYMBOL));

			this.setCustomRenderer(new FontColorRenderer(FontColorRenderer.COLOR8));
			this.addSubtreeHandler(new SubtreeHandler<QuestionText>() {

				@Override
				public Collection<KDOMReportMessage> create(KnowWEArticle article, Section<QuestionText> sec) {

					Section<QuestionDefinition> qDef = sec.getFather().findSuccessor(
							QuestionDefinition.class);

					if (qDef != null) {

						Question question = qDef.get().getTermObject(article, qDef);

						if (question != null) {
							question.getInfoStore().addValue(MMInfo.PROMPT,
									QuestionText.getQuestionText(sec));
							return Arrays.asList((KDOMReportMessage) new ObjectCreatedMessage(
									D3webModule.getKwikiBundle_d3web()
											.getString("KnowWE.questiontree.questiontextcreated")));
						}
					}
					return Arrays.asList((KDOMReportMessage) new ObjectCreationError(
							D3webModule.getKwikiBundle_d3web()
									.getString("KnowWE.questiontree.questiontext"),
							this.getClass()));
				}
			});
		}

		public static String getQuestionText(Section<QuestionText> s) {
			String text = s.getOriginalText();
			if (text.startsWith(QTEXT_START_SYMBOL)) {
				text = text.substring(1).trim();
			}

			return SplitUtility.unquote(text);
		}
	}

	/**
	 * @author Jochen
	 * 
	 *         A KnowWEType for the question-type declaration keys
	 *         "[oc],[mc],[num],..."
	 * 
	 */
	static class QuestionTypeDeclaration extends
			DefaultAbstractKnowWEObjectType {

		public static QuestionType getQuestionType(Section<QuestionTypeDeclaration> typeSection) {

			if (typeSection == null) return null;
			String embracedContent = typeSection.getOriginalText();
			if (embracedContent.contains("oc")) {
				return QuestionType.OC;
			}
			else if (embracedContent.contains("mc")) {
				return QuestionType.MC;
			}
			else if (embracedContent.contains("num")) {
				return QuestionType.NUM;
			}
			else if (embracedContent.contains("jn")
					|| embracedContent.contains("yn")) {
				return QuestionType.YN;
			}
			else if (embracedContent.contains("date")) {
				return QuestionType.DATE;
			}
			else if (embracedContent.contains("info")) {
				return QuestionType.INFO;
			}
			else if (embracedContent.contains("text")) {
				return QuestionType.TEXT;
			}
			else {
				return null;
			}

		}

		public static final String[] QUESTION_DECLARATIONS = {
				"oc", "mc",
				"yn", "jn", "num", "date", "text", "info" };

		@Override
		protected void init() {
			SectionFinder typeFinder = new SectionFinder() {

				@Override
				public List<SectionFinderResult> lookForSections(String text,
						Section<?> father, KnowWEObjectType type) {

					return SectionFinderResult
							.createSingleItemList(new SectionFinderResult(
									SplitUtility.indexOfUnquoted(text, "["),
									SplitUtility.indexOfUnquoted(text, "]") + 1));
				}
			};
			this.setSectionFinder(typeFinder);
			this.setCustomRenderer(new FontColorRenderer(FontColorRenderer.COLOR7));
			this.addSubtreeHandler(new StringEnumChecker<QuestionTypeDeclaration>(
					QUESTION_DECLARATIONS, new SimpleMessageError(
							D3webModule.getKwikiBundle_d3web()
									.getString("KnowWE.questiontree.allowingonly")
									+ QUESTION_DECLARATIONS.toString())));
		}
	}

}
