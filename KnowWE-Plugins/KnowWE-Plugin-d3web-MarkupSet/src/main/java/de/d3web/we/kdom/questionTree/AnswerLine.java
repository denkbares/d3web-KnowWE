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

import de.d3web.core.knowledge.InfoStore;
import de.d3web.core.knowledge.terminology.Choice;
import de.d3web.core.knowledge.terminology.Question;
import de.d3web.core.knowledge.terminology.info.BasicProperties;
import de.d3web.core.knowledge.terminology.info.MMInfo;
import com.denkbares.strings.Strings;
import de.d3web.we.knowledgebase.D3webCompileScript;
import de.d3web.we.object.AnswerDefinition;
import de.d3web.we.object.QuestionDefinition;
import de.d3web.we.reviseHandler.D3webHandler;
import de.d3web.we.utils.D3webUtils;
import de.knowwe.core.kdom.AbstractType;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.kdom.sectionFinder.AllTextFinder;
import de.knowwe.core.kdom.sectionFinder.AllTextFinderTrimmed;
import de.knowwe.core.report.Messages;
import de.knowwe.kdom.dashtree.DashTreeElement;
import de.knowwe.kdom.dashtree.DashTreeUtils;
import de.knowwe.kdom.renderer.StyleRenderer;
import de.knowwe.core.kdom.sectionFinder.ConditionalSectionFinder;
import de.knowwe.kdom.sectionFinder.MatchUntilEndFinder;
import de.knowwe.kdom.sectionFinder.OneOfStringFinder;
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
		this.setSectionFinder(new ConditionalSectionFinder(AllTextFinder.getInstance()) {

			@Override
			protected boolean condition(String text, Section<?> father) {

				Section<?> dashTreeElement = father.getParent();
				if (dashTreeElement.get() instanceof DashTreeElement) {
					Section<? extends DashTreeElement> dashFather = DashTreeUtils
							.getParentDashTreeElement(dashTreeElement);
					return dashFather != null
							&& Sections.successor(dashFather, QuestionLine.class) != null;
				}

				return false;
			}
		});

		// description text - startet by '~'
		this.addChildType(new AnswerText());

		QuestionTreeAnswerDefinition aid = new QuestionTreeAnswerDefinition();
		aid.setSectionFinder(new AllTextFinderTrimmed());
		this.addChildType(aid);

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
			this.setSectionFinder(new OneOfStringFinder("<init>"));
			this.setRenderer(StyleRenderer.KEYWORDS);

			this.addCompileScript((D3webCompileScript<InitFlag>) (compiler, section) -> {
				Section<AnswerDefinition> aDef = Sections.successor(
						section.getParent(), AnswerDefinition.class);

				Section<? extends QuestionDefinition> qdef = aDef.get().getQuestionSection(aDef);

				if (qdef != null) {
					Question question = qdef.get().getTermObject(compiler, qdef);

					String answerName = aDef.get().getTermObject(compiler, aDef).getName();

					InfoStore infoStore = question.getInfoStore();
					String p = infoStore.getValue(BasicProperties.INIT);

					if (p == null) {
						infoStore.addValue(BasicProperties.INIT, answerName);
					}
					else {
						String newValue = p.concat(";" + answerName);
						infoStore.addValue(BasicProperties.INIT, newValue);
					}

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
			this.setSectionFinder(new MatchUntilEndFinder(new StringSectionFinderUnquoted(
					QTEXT_START_SYMBOL)));

			this.setRenderer(StyleRenderer.PROMPT);
			this.addCompileScript((D3webHandler<AnswerText>) (compiler, sec) -> {

				Section<AnswerDefinition> aDef = Sections.successor(
						sec.getParent(), AnswerDefinition.class);

				Section<? extends QuestionDefinition> qSec = aDef.get().getQuestionSection(aDef);

				if (qSec != null) {

					Question question = qSec.get().getTermObject(compiler, qSec);
					Choice choice = aDef.get().getTermObject(compiler, aDef);

					if (question != null && choice != null) {
						choice.getInfoStore().addValue(MMInfo.PROMPT,
								AnswerText.getAnswerText(sec));
						return Messages.noMessage();
					}
				}
				return Messages.asList(Messages.objectCreationError(
						D3webUtils.getD3webBundle()
								.getString("KnowWE.questiontree.questiontext")));
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
