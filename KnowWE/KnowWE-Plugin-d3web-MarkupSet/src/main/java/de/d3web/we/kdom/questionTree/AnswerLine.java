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

import de.d3web.core.knowledge.InfoStore;
import de.d3web.core.knowledge.terminology.Choice;
import de.d3web.core.knowledge.terminology.Question;
import de.d3web.core.knowledge.terminology.info.BasicProperties;
import de.d3web.core.knowledge.terminology.info.MMInfo;
import de.d3web.we.object.AnswerDefinition;
import de.d3web.we.object.QuestionDefinition;
import de.d3web.we.reviseHandler.D3webSubtreeHandler;
import de.d3web.we.utils.D3webUtils;
import de.knowwe.core.kdom.AbstractType;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.kdom.sectionFinder.AllTextFinderTrimmed;
import de.knowwe.core.kdom.sectionFinder.AllTextSectionFinder;
import de.knowwe.core.kdom.subtreeHandler.SubtreeHandler;
import de.knowwe.core.report.Message;
import de.knowwe.core.report.Messages;
import de.knowwe.core.utils.Strings;
import de.knowwe.kdom.dashtree.DashTreeElement;
import de.knowwe.kdom.dashtree.DashTreeUtils;
import de.knowwe.kdom.renderer.StyleRenderer;
import de.knowwe.kdom.sectionFinder.ConditionalSectionFinder;
import de.knowwe.kdom.sectionFinder.MatchUntilEndFinder;
import de.knowwe.kdom.sectionFinder.OneOfStringEnumFinder;
import de.knowwe.kdom.sectionFinder.StringSectionFinderUnquoted;

/**
 * Answerline of the questionTree; a dashTreeElement is an AnswerLine if its
 * DashTree father is a Question (and it hasn't been allocated as question also
 * before)
 * 
 * @author Jochen
 * 
 */
public class AnswerLine extends AbstractType {

	public AnswerLine() {
		this.sectionFinder = new ConditionalSectionFinder(new AllTextSectionFinder()) {

			@Override
			protected boolean condition(String text, Section<?> father) {

				Section<?> dashTreeElement = father.getFather();
				if (dashTreeElement.get() instanceof DashTreeElement) {
					Section<? extends DashTreeElement> dashFather = DashTreeUtils
							.getFatherDashTreeElement(dashTreeElement);
					if (dashFather != null
							&& Sections.findSuccessor(dashFather, QuestionLine.class) != null) {
						return true;
					}
				}

				return false;
			}
		};

		// description text - startet by '~'
		this.childrenTypes.add(new AnswerText());

		QuestionTreeAnswerDefinition aid = new QuestionTreeAnswerDefinition();
		aid.setSectionFinder(new AllTextFinderTrimmed());
		this.childrenTypes.add(aid);

	}

	/**
	 * Allows for the definition of abstract-flagged questions Syntax is:
	 * "<init>"
	 * 
	 * The subtreehandler creates the corresponding BasicProperties.INIT in the
	 * knoweldge base
	 * 
	 * 
	 * @author Jochen
	 * 
	 */
	static class InitFlag extends AbstractType {

		public InitFlag() {
			this.sectionFinder = new OneOfStringEnumFinder(new String[] {
					"<init>" });
			this.setRenderer(StyleRenderer.KEYWORDS);

			this.addSubtreeHandler(new SubtreeHandler<InitFlag>() {

				@Override
				public Collection<Message> create(Article article, Section<InitFlag> s) {

					Section<AnswerDefinition> aDef = Sections.findSuccessor(
							s.getFather(), AnswerDefinition.class);

					Section<? extends QuestionDefinition> qdef = aDef.get().getQuestionSection(
							aDef);

					if (qdef != null) {

						Question question = qdef.get().getTermObject(article, qdef);

						String answerName = aDef.get().getTermObject(article, aDef).getName();

						InfoStore infoStore = question.getInfoStore();
						Object p = infoStore.getValue(BasicProperties.INIT);

						if (p == null) {
							infoStore.addValue(BasicProperties.INIT, answerName);
						}
						else {
							if (p instanceof String) {
								String newValue = ((String) p).concat(";" + answerName);
								infoStore.addValue(BasicProperties.INIT, newValue);
							}

						}
						return Messages.asList(Messages.objectCreatedNotice(
								D3webUtils.getD3webBundle()
										.getString("KnowWE.questiontree.abstractquestion")));

					}
					return Messages.asList(Messages.objectCreationError(
							D3webUtils.getD3webBundle()
									.getString("KnowWE.questiontree.abstractflag")));
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
	static class AnswerText extends AbstractType {

		private static final String QTEXT_START_SYMBOL = "~";

		public AnswerText() {
			this.sectionFinder = new MatchUntilEndFinder(new StringSectionFinderUnquoted(
					QTEXT_START_SYMBOL));

			this.setRenderer(StyleRenderer.PROMPT);
			this.addSubtreeHandler(new D3webSubtreeHandler<AnswerText>() {

				@Override
				public Collection<Message> create(Article article, Section<AnswerText> sec) {

					Section<AnswerDefinition> aDef = Sections.findSuccessor(
							sec.getFather(), AnswerDefinition.class);

					Section<? extends QuestionDefinition> qSec = aDef.get().getQuestionSection(aDef);

					if (aDef != null && qSec != null) {

						Question question = qSec.get().getTermObject(article, qSec);
						Choice choice = aDef.get().getTermObject(article, aDef);

						if (question != null && choice != null) {
							choice.getInfoStore().addValue(MMInfo.PROMPT,
									AnswerText.getAnswerText(sec));
							return Messages.asList(Messages.objectCreatedNotice(
									"Answer text set"));
						}
					}
					return Messages.asList(Messages.objectCreationError(
							D3webUtils.getD3webBundle()
									.getString("KnowWE.questiontree.questiontext")));
				}

				@Override
				public void destroy(Article article, Section<AnswerText> sec) {
					// text is destroyed together with object
				}
			});
		}

		public static String getAnswerText(Section<AnswerText> s) {
			String text = s.getText();
			if (text.startsWith(QTEXT_START_SYMBOL)) {
				text = text.substring(1).trim();
			}

			return Strings.unquote(text);
		}
	}

}
