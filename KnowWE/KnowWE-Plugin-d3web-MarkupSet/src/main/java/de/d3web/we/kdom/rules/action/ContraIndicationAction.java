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
import de.d3web.we.object.QuestionReference;
import de.knowwe.core.kdom.KnowWEArticle;
import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.objects.SimpleTerm;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.kdom.sectionFinder.SectionFinder;
import de.knowwe.core.kdom.sectionFinder.SectionFinderResult;
import de.knowwe.core.utils.KnowWEUtils;
import de.knowwe.core.utils.SplitUtility;

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
	protected Type getObjectReference() {
		return new QuestionReferenceInBrackets();
	}

	@Override
	public PSAction createAction(KnowWEArticle article, Section<ContraIndicationAction> s) {
		Section<QuestionReference> qSec = Sections.findSuccessor(s, QuestionReference.class);
		Question termObject = qSec.get().getTermObject(article, qSec);

		ActionContraIndication actionContraIndication = new ActionContraIndication();
		List<QASet> obs = new ArrayList<QASet>();
		obs.add(termObject);
		actionContraIndication.setQASets(obs);
		return actionContraIndication;
	}

	static class QuestionReferenceInBrackets extends QuestionReference {

		public QuestionReferenceInBrackets() {
			this.sectionFinder = new SectionFinder() {

				@Override
				public List<SectionFinderResult> lookForSections(String text,
						Section<?> father, Type type) {

					return SectionFinderResult
							.createSingleItemList(new SectionFinderResult(
									SplitUtility.indexOfUnquoted(text, OPEN),
									SplitUtility.indexOfUnquoted(text, CLOSE) + 1));
				}
			};
		}

		@Override
		public String getTermIdentifier(Section<? extends SimpleTerm> s) {
			String text = s.getText().trim();
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
