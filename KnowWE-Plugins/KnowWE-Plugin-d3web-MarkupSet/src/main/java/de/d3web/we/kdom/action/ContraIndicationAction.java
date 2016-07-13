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
package de.d3web.we.kdom.action;

import java.util.ArrayList;
import java.util.List;

import de.d3web.core.inference.PSAction;
import de.d3web.core.inference.PSMethod;
import de.d3web.core.knowledge.terminology.QASet;
import de.d3web.indication.ActionContraIndication;
import de.d3web.indication.inference.PSMethodStrategic;
import com.denkbares.strings.Strings;
import de.d3web.we.knowledgebase.D3webCompiler;
import de.d3web.we.object.QASetReference;
import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.objects.Term;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.kdom.sectionFinder.SectionFinder;
import de.knowwe.core.kdom.sectionFinder.SectionFinderResult;

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
		return new QASetReferenceInBrackets();
	}

	@Override
	public PSAction createAction(D3webCompiler compiler, Section<ContraIndicationAction> s) {
		Section<QASetReference> qSec = Sections.successor(s, QASetReference.class);
		QASet termObject = qSec.get().getTermObject(compiler, qSec);

		ActionContraIndication actionContraIndication = new ActionContraIndication();
		List<QASet> obs = new ArrayList<>();
		obs.add(termObject);
		actionContraIndication.setQASets(obs);
		return actionContraIndication;
	}

	static class QASetReferenceInBrackets extends QASetReference {

		public QASetReferenceInBrackets() {
			this.setSectionFinder(new SectionFinder() {

				@Override
				public List<SectionFinderResult> lookForSections(String text,
						Section<?> father, Type type) {

					return SectionFinderResult
							.singleItemList(new SectionFinderResult(
									Strings.indexOfUnquoted(text, OPEN),
									Strings.indexOfUnquoted(text, CLOSE) + 1));
				}
			});
		}

		@Override
		public String getTermName(Section<? extends Term> s) {
			String text = s.getText().trim();
			String questionName = "";
			if (text.indexOf(OPEN) == 0 && text.lastIndexOf(CLOSE) == text.length() - 1) {
				questionName = text.substring(1, text.length() - 1).trim();
			}

			return Strings.trimQuotes(questionName);
		}
	}

	@Override
	public Class<? extends PSMethod> getProblemSolverContext() {
		return PSMethodStrategic.class;
	}

}
