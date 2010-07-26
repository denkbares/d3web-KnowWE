/*
 * Copyright (C) 2010 University Wuerzburg, Computer Science VI
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
package de.d3web.we.kdom.questionTreeNew.setValue;

import java.util.List;

import de.d3web.core.knowledge.terminology.Choice;
import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.objects.AnswerReference;
import de.d3web.we.kdom.objects.KnowWETerm;
import de.d3web.we.kdom.objects.QuestionReference;
import de.d3web.we.kdom.sectionFinder.ISectionFinder;
import de.d3web.we.kdom.sectionFinder.SectionFinderResult;
import de.d3web.we.utils.KnowWEUtils;
import de.d3web.we.utils.SplitUtility;

/**
 * 
 * @author Jochen
 * @created 23.07.2010 
 */
public class AnswerReferenceInBrackets extends AnswerReference {
	
	public AnswerReferenceInBrackets() {
		this.sectionFinder = new ISectionFinder() {
			@Override
			public List<SectionFinderResult> lookForSections(String text,
					Section father, KnowWEObjectType type) {

				return SectionFinderResult
						.createSingleItemList(new SectionFinderResult(
								SplitUtility.indexOfUnquoted(text, "("),
								SplitUtility.indexOfUnquoted(text, ")") + 1));
			}
		};
	}

	@Override
	public Section<QuestionReference> getQuestionSection(Section<? extends AnswerReference> s) {
		return s.getFather().findSuccessor(QuestionReference.class);
	}

	@Override
	public String getTermName(Section<? extends KnowWETerm<Choice>> s) {
		String text = s.getOriginalText().trim();
		String answer = "";
		if (text.indexOf('(') == 0 && text.lastIndexOf(')') == text.length() - 1) {
			answer = text.substring(1, text.length() - 1).trim();
		}
		
		return KnowWEUtils.trimQuotes(answer);
	}

}
