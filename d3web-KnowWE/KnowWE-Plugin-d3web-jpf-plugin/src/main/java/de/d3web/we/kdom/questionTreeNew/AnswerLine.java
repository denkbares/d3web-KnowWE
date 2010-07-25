/*
 * Copyright (C) 2009 Chair of Artificial Intelligence and Applied Informatics
 *                    Computer Science VI, University of Wuerzburg
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package de.d3web.we.kdom.questionTreeNew;


import java.util.Arrays;
import java.util.Collection;

import de.d3web.core.knowledge.terminology.Question;
import de.d3web.core.knowledge.terminology.info.Property;
import de.d3web.we.d3webModule.D3webModule;
import de.d3web.we.kdom.DefaultAbstractKnowWEObjectType;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.dashTree.DashTreeElement;
import de.d3web.we.kdom.dashTree.DashTreeUtils;
import de.d3web.we.kdom.objects.AnswerDefinition;
import de.d3web.we.kdom.objects.QuestionDefinition;
import de.d3web.we.kdom.renderer.FontColorRenderer;
import de.d3web.we.kdom.report.KDOMReportMessage;
import de.d3web.we.kdom.report.message.ObjectCreatedMessage;
import de.d3web.we.kdom.report.message.ObjectCreationError;
import de.d3web.we.kdom.sectionFinder.AllTextFinderTrimmed;
import de.d3web.we.kdom.sectionFinder.ConditionalAllTextFinder;
import de.d3web.we.kdom.sectionFinder.OneOfStringEnumFinder;
import de.d3web.we.kdom.subtreeHandler.SubtreeHandler;

/**
 * Answerline of the questionTree; a dashTreeElement is an AnswerLine if its
 * DashTree father is a Question (and it hasn't been allocated as question also
 * before)
 * 
 * @author Jochen
 *
 */
public class AnswerLine extends DefaultAbstractKnowWEObjectType {

	@Override
	protected void init() {
		this.sectionFinder = new ConditionalAllTextFinder() {

			@Override
			protected boolean condition(String text, Section father) {

				Section dashTreeElement = father.getFather();
				if (dashTreeElement.getObjectType() instanceof DashTreeElement) {
					Section<? extends DashTreeElement> dashFather = DashTreeUtils
							.getFatherDashTreeElement(dashTreeElement);
					if (dashFather != null
							&& dashFather.findSuccessor(QuestionLine.class) != null) {
						return true;
					}
				}

				return false;
			}
		};

		QuestionTreeAnswerDefinition aid = new QuestionTreeAnswerDefinition();
		aid.setSectionFinder(new AllTextFinderTrimmed());
		this.childrenTypes.add(aid);
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
	static class InitFlag extends DefaultAbstractKnowWEObjectType {

		public InitFlag() {
			this.sectionFinder = new OneOfStringEnumFinder(new String[] {
					"<init>" });
			this.setCustomRenderer(new FontColorRenderer(FontColorRenderer.COLOR7));

			this.addSubtreeHandler(new SubtreeHandler<InitFlag>() {

				@Override
				public Collection<KDOMReportMessage> create(KnowWEArticle article, Section<InitFlag> s) {

					Section<AnswerDefinition> aDef = s.getFather().findSuccessor(
							AnswerDefinition.class);

					Section<? extends QuestionDefinition> qdef = aDef.get().getQuestionSection(
							aDef);
						
					if (qdef != null) {

						
						Question question = qdef.get().getTermObject(article, qdef);
						
						String answerName = aDef.get().getTermObject(article, aDef).getName();
						
						Object p = question.getProperties().getProperty(Property.INIT);
						
						if(p == null) {
							question.getProperties().setProperty(Property.INIT, answerName);
						} else {
							if(p instanceof String) {
								String newValue = ((String)p).concat(";"+answerName);
								question.getProperties().setProperty(Property.INIT, newValue);
							}
					
						}
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

}
