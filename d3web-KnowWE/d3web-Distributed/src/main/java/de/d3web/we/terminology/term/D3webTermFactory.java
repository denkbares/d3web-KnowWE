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

package de.d3web.we.terminology.term;

import java.util.Collection;
import java.util.HashSet;

import de.d3web.core.knowledge.TerminologyObject;
import de.d3web.core.knowledge.terminology.Choice;
import de.d3web.core.knowledge.terminology.NamedObject;
import de.d3web.core.knowledge.terminology.QContainer;
import de.d3web.core.knowledge.terminology.Question;
import de.d3web.core.knowledge.terminology.QuestionChoice;
import de.d3web.core.knowledge.terminology.QuestionNum;
import de.d3web.core.knowledge.terminology.Solution;
import de.d3web.core.session.values.Unknown;
import de.d3web.utilities.ISetMap;
import de.d3web.utilities.SetMap;
import de.d3web.we.alignment.D3webAlignUtils;
import de.d3web.we.alignment.NumericalIdentity;
import de.d3web.we.basic.TerminologyType;
import de.d3web.we.terminology.global.GlobalTerminology;
import de.d3web.we.terminology.local.LocalTerminologyAccess;

public class D3webTermFactory implements TermFactory<TerminologyObject, TerminologyObject> {

	public static final String DEFAULT_QUESTIONNAIRE_NAME = "Standardfragebogen";

	private Term getTerm(TerminologyObject object, TerminologyType type, GlobalTerminology gt) {
		Term term = gt.getTerm(object.getName(), null);
		if (term == null) {
			term = new Term();
			term.setInfo(TermInfoType.TERM_NAME, object.getName());
			gt.addTerm(term);
		}
		return term;
	}

	public ISetMap<TerminologyObject, Term> addTerminology(TerminologyObject to, String idString, GlobalTerminology globalTerminology, Term oldParentTerm, Collection<NamedObject> alreadyDone) {
		// TODO: remove this, if getInfoStore is active
		NamedObject no = null;
		if (to instanceof NamedObject) {
			no = (NamedObject) to;
		}
		else {
			return null;
		}
		Boolean privat = false;
		if (privat != null && privat) return new SetMap<TerminologyObject, Term>();
		if (alreadyDone.contains(no)) {
			return new SetMap<TerminologyObject, Term>();
		}
		alreadyDone.add(no);
		if (!isIntegrable(no)) {
			return new SetMap<TerminologyObject, Term>();
		}

		Term parentTerm = null;
		if ((!no.getName().equals("P000")) && (!no.getName().equals("Q000"))
					&& (!no.getName().equals(DEFAULT_QUESTIONNAIRE_NAME))) {
			if ((no instanceof QContainer || no instanceof Solution)) {
				// new Root -> complete subtree
				parentTerm = getTerm(no, globalTerminology.getType(), globalTerminology);
				// parentTerm.addAlignments(getAlignments(no, idString,
				// parentTerm));
			}
			else if (no instanceof Question) {
				getTerm(no, globalTerminology.getType(), globalTerminology);
				createAnswerTerms((Question) no, globalTerminology);
			}
		}
		for (TerminologyObject child : no.getChildren()) {
			addTerminology(child, idString, globalTerminology, parentTerm, alreadyDone);
		}

		ISetMap<TerminologyObject, Term> result = new SetMap<TerminologyObject, Term>();
		return result;
	}

	private boolean isIntegrable(NamedObject no) {
		Boolean b = false;
		return !b;
	}

	private void createAnswerTerms(Question question, GlobalTerminology globalTerminology) {
		Term valueTerm = null;
		valueTerm = new Term();
		valueTerm.setInfo(TermInfoType.TERM_NAME, D3webAlignUtils.getText(question));
		valueTerm.setInfo(TermInfoType.TERM_VALUE, D3webAlignUtils.getText(Unknown.getInstance()));
		globalTerminology.addTerm(valueTerm);
		if (question instanceof QuestionChoice) {
			for (Choice each : ((QuestionChoice) question).getAllAlternatives()) {
				valueTerm = new Term();
				valueTerm.setInfo(TermInfoType.TERM_NAME, D3webAlignUtils.getText(question));
				valueTerm.setInfo(TermInfoType.TERM_VALUE, D3webAlignUtils.getText(each));
				globalTerminology.addTerm(valueTerm);
			}
		}
		else if (question instanceof QuestionNum) {
			valueTerm = new Term();
			valueTerm.setInfo(TermInfoType.TERM_NAME, D3webAlignUtils.getText(question));
			valueTerm.setInfo(TermInfoType.TERM_VALUE, new NumericalIdentity());
			globalTerminology.addTerm(valueTerm);
		}
	}

	@Override
	public ISetMap<TerminologyObject, Term> addTerminology(LocalTerminologyAccess<TerminologyObject> localTerminology, String idString, GlobalTerminology globalTerminology) {
		TerminologyObject child = localTerminology.getHandler().iterator().next();
		return addTerminology(child, idString, globalTerminology, null, new HashSet<NamedObject>());
	}

}
