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

import de.d3web.core.knowledge.terminology.Choice;
import de.d3web.we.kdom.DefaultAbstractKnowWEObjectType;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.basic.AnonymousType;
import de.d3web.we.kdom.objects.AnswerDef;
import de.d3web.we.kdom.objects.QuestionDef;
import de.d3web.we.kdom.sectionFinder.AllTextFinderTrimmed;
import de.d3web.we.kdom.sectionFinder.EmbracedContentFinder;
import de.d3web.we.kdom.sectionFinder.UnquotedExpressionFinder;

/**
 * This type is a child-type of QuestionLine and allows for the definition of
 * choice alternatives inline with the question-definition
 *
 *
 * @author Jochen
 *
 */
public class InlineChoiceAnswerDefinition extends DefaultAbstractKnowWEObjectType {

	public static final char ANSWERS_OPEN = '<';
	public static final char ANSWERS_CLOSE = '>';

	public InlineChoiceAnswerDefinition() {
		this.setSectionFinder(new EmbracedContentFinder(ANSWERS_OPEN, ANSWERS_CLOSE));

		// TODO find better way to crop open and closing signs
		AnonymousType open = new AnonymousType(Character.toString(ANSWERS_OPEN));
		open.setSectionFinder(new UnquotedExpressionFinder(
				Character.toString(ANSWERS_OPEN)));
		this.addChildType(open);

		AnonymousType close = new AnonymousType(Character.toString(ANSWERS_CLOSE));
		close.setSectionFinder(new UnquotedExpressionFinder(
				Character.toString(ANSWERS_CLOSE)));
		this.addChildType(close);

		// split by search for komas
		AnonymousType koma = new AnonymousType("koma");
		koma.setSectionFinder(new UnquotedExpressionFinder(","));
		this.addChildType(koma);

		// the rest is definitions of answers
		InlineAnswerDef answerDef = new InlineAnswerDef();
		answerDef.setSectionFinder(new AllTextFinderTrimmed());
		this.addChildType(answerDef);

	}

	/**
	 * Delivers the QuestionDef for an AnswerDef which is in the same
	 * (Qusetion-)Line
	 *
	 * @author Jochen
	 * 
	 */
	class InlineAnswerDef extends AnswerDef {


		@Override
		public int getPosition(Section<? extends QuestionTreeElementDef<Choice>> s) {
			// TODO Auto-generated method stub
			return 0;
		}


		@Override
		public Section<? extends QuestionDef> retrieveAndStoreParentQASetSection(
				Section<? extends QuestionTreeElementDef<?>> s) {
			// TODO Auto-generated method stub
			return null;
		}
	}

}
