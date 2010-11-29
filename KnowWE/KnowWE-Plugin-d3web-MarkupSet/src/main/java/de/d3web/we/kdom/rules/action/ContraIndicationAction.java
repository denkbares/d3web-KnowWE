/*
 * Copyright (C) 2010 University Wuerzburg, Computer Science VI
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
package de.d3web.we.kdom.rules.action;

import java.util.ArrayList;
import java.util.List;

import de.d3web.core.inference.PSAction;
import de.d3web.core.inference.PSMethod;
import de.d3web.core.knowledge.terminology.QASet;
import de.d3web.core.knowledge.terminology.Question;
import de.d3web.indication.ActionContraIndication;
import de.d3web.indication.inference.PSMethodStrategic;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.objects.KnowWETerm;
import de.d3web.we.kdom.sectionFinder.ISectionFinder;
import de.d3web.we.kdom.sectionFinder.SectionFinderResult;
import de.d3web.we.object.QuestionReference;
import de.d3web.we.utils.KnowWEUtils;
import de.d3web.we.utils.SplitUtility;

/**
 * 
 * @author Jochen
 * @created 26.07.2010
 */
public class ContraIndicationAction extends BracketsAction<ContraIndicationAction> {

	public ContraIndicationAction() {
		super(new String[] {
				"NICHT", "NOT" });

	}

	@Override
	protected KnowWEObjectType getObjectReference() {
		return new QuestionReferenceInBrackets();
	}

	@Override
	public PSAction createAction(KnowWEArticle article, Section<ContraIndicationAction> s) {
		Section<QuestionReference> qSec = s.findSuccessor(QuestionReference.class);
		Question termObject = qSec.get().getTermObject(article, qSec);

		ActionContraIndication actionContraIndication = new ActionContraIndication();
		List<QASet> obs = new ArrayList<QASet>();
		obs.add(termObject);
		actionContraIndication.setQASets(obs);
		return actionContraIndication;
	}

	static class QuestionReferenceInBrackets extends QuestionReference {

		public QuestionReferenceInBrackets() {
			this.sectionFinder = new ISectionFinder() {

				@Override
				public List<SectionFinderResult> lookForSections(String text,
						Section father, KnowWEObjectType type) {

					return SectionFinderResult
							.createSingleItemList(new SectionFinderResult(
									SplitUtility.indexOfUnquoted(text, OPEN),
									SplitUtility.indexOfUnquoted(text, CLOSE) + 1));
				}
			};
		}

		@Override
		public String getTermName(Section<? extends KnowWETerm<Question>> s) {
			String text = s.getOriginalText().trim();
			String questionName = "";
			if (text.indexOf(OPEN) == 0 && text.lastIndexOf(CLOSE) == text.length() - 1) {
				questionName = text.substring(1, text.length() - 1).trim();
			}

			return KnowWEUtils.trimQuotes(questionName);
		}
	}

	@Override
	public Class<? extends PSMethod> getActionPSContext() {
		return PSMethodStrategic.class;
	}

}
