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

package de.d3web.we.alignment.aligner;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import de.d3web.kernel.domainModel.Answer;
import de.d3web.kernel.domainModel.Diagnosis;
import de.d3web.kernel.domainModel.IDObject;
import de.d3web.kernel.domainModel.NamedObject;
import de.d3web.kernel.domainModel.qasets.QContainer;
import de.d3web.kernel.domainModel.qasets.Question;
import de.d3web.kernel.domainModel.qasets.QuestionChoice;
import de.d3web.kernel.domainModel.qasets.QuestionNum;
import de.d3web.kernel.supportknowledge.Property;
import de.d3web.we.alignment.AlignmentUtilRepository;
import de.d3web.we.alignment.D3webAlignUtils;
import de.d3web.we.alignment.GlobalAlignment;
import de.d3web.we.alignment.NumericalIdentity;
import de.d3web.we.alignment.SolutionIdentity;
import de.d3web.we.alignment.method.AlignMethod;
import de.d3web.we.alignment.type.AbstractAlignType;
import de.d3web.we.alignment.type.NoAlignType;
import de.d3web.we.alignment.type.NumericalIdentityAlignType;
import de.d3web.we.alignment.type.SolutionIdentityAlignType;
import de.d3web.we.basic.IdentifiableInstance;
import de.d3web.we.basic.TerminologyType;
import de.d3web.we.terminology.local.LocalTerminologyHandler;
import de.d3web.we.terminology.term.Term;
import de.d3web.we.terminology.term.TermInfoType;

public class CompleteGlobalAligner implements GlobalAligner<NamedObject>{

	public List<GlobalAlignment> align(Term term, NamedObject object, String idString) {
		List<GlobalAlignment> result = new ArrayList<GlobalAlignment>();
		
		if(term.getInfo(TermInfoType.TERM_VALUE) != null) return result;
		
		Collection<AlignMethod> methods = AlignmentUtilRepository.getInstance().getMethods(String.class);
		for (AlignMethod method : methods) {
			AbstractAlignType type = method.align(term.getInfo(TermInfoType.TERM_NAME), D3webAlignUtils.getText(object));
			if(!(type instanceof NoAlignType)) {
				GlobalAlignment globalAlignment = new GlobalAlignment(term, new IdentifiableInstance(idString, object.getId(), null), type);
				Object obj = object.getProperties().getProperty(Property.FOREIGN);
				if(obj != null && obj instanceof Boolean && ((Boolean)obj).booleanValue()){
					globalAlignment.setProperty("visible", Boolean.FALSE);
				}
				result.add(globalAlignment);
				// "values":
				if(object instanceof Diagnosis) {
					result.add(new GlobalAlignment(term, new IdentifiableInstance(idString, object.getId(), new SolutionIdentity()), SolutionIdentityAlignType.getInstance()));
					result.add(new GlobalAlignment(term, new IdentifiableInstance(idString, object.getId(), new NumericalIdentity()), NumericalIdentityAlignType.getInstance()));
				} else if(object instanceof QContainer) {
					//nothing else
				} else if(object instanceof Question) {
					result.add(alignQuestionValueUnknown(term, (Question) object, idString, type));
					//[FIXME] type of term!!!!!!1
					if(object instanceof QuestionNum) {
						result.addAll(alignValues(term, (QuestionNum)object, idString, type));
					} else if(object instanceof QuestionChoice) {
						result.addAll(alignValues(term, (QuestionChoice)object, idString, type));
					}
				}
			}
		}
		Collections.sort(result);
		return result;
	}

	private Collection<? extends GlobalAlignment> alignValues(Term term, QuestionChoice choice, String idString, AbstractAlignType type) {
		List<GlobalAlignment> result = new ArrayList<GlobalAlignment>();
		LocalTerminologyHandler<IDObject, IDObject> answer1Handler = AlignmentUtilRepository.getInstance().getLocalTerminogyHandler(IDObject.class);
		answer1Handler.setTerminology(choice);		
		for (IDObject a1 : answer1Handler) {
			if(a1 instanceof Answer) {
				Term valueTerm = new Term(TerminologyType.symptom);
				valueTerm.setInfo(TermInfoType.TERM_NAME, term.getInfo(TermInfoType.TERM_NAME));
				valueTerm.setInfo(TermInfoType.TERM_VALUE, D3webAlignUtils.getText(a1));
				GlobalAlignment newGA = new GlobalAlignment(valueTerm, getII(idString, a1), type);
				result.add(newGA);
			}
		}
		return result;
	}

	private Collection<? extends GlobalAlignment> alignValues(Term term, QuestionNum num, String idString, AbstractAlignType type) {
		List<GlobalAlignment> result = new ArrayList<GlobalAlignment>();
		Term valueTerm = new Term(TerminologyType.symptom);
		valueTerm.setInfo(TermInfoType.TERM_NAME, term.getInfo(TermInfoType.TERM_NAME));
		valueTerm.setInfo(TermInfoType.TERM_VALUE, new NumericalIdentity());
		GlobalAlignment newGA = new GlobalAlignment(valueTerm, getII(idString, num, new NumericalIdentity()), NumericalIdentityAlignType.getInstance());
		result.add(newGA);
		return result;
	}

	private GlobalAlignment alignQuestionValueUnknown(Term term, Question question, String idString, AbstractAlignType type) {
		Term valueTerm = new Term(TerminologyType.symptom);
		valueTerm.setInfo(TermInfoType.TERM_NAME, term.getInfo(TermInfoType.TERM_NAME));
		valueTerm.setInfo(TermInfoType.TERM_VALUE, D3webAlignUtils.getText(question.getUnknownAlternative()));
		return new GlobalAlignment(valueTerm, getII(idString, question.getUnknownAlternative()), type);
	}
	
	private IdentifiableInstance getII(String idString, IDObject object) {
		if(object instanceof NamedObject) {
			return new IdentifiableInstance(idString, object.getId(), null);
		} else if(object instanceof Answer) {
			Answer answer = (Answer) object;
			return new IdentifiableInstance(idString, answer.getQuestion().getId(), answer.getId());
		}
		return null;
	}
	
	private IdentifiableInstance getII(String idString, QuestionNum object, NumericalIdentity ni) {
		return new IdentifiableInstance(idString, object.getId(), ni);
	}
	
}
