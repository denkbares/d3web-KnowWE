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

package de.d3web.knowledgeExporter.txtWriters;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import de.d3web.abstraction.ActionAddValue;
import de.d3web.abstraction.ActionQuestionSetter;
import de.d3web.abstraction.ActionSetValue;
import de.d3web.abstraction.formula.FormulaExpression;
import de.d3web.core.inference.Rule;
import de.d3web.core.inference.RuleAction;
import de.d3web.core.inference.condition.AbstractCondition;
import de.d3web.core.inference.condition.TerminalCondition;
import de.d3web.core.knowledge.terminology.Answer;
import de.d3web.core.knowledge.terminology.Diagnosis;
import de.d3web.core.knowledge.terminology.QASet;
import de.d3web.core.knowledge.terminology.Question;
import de.d3web.indication.ActionContraIndication;
import de.d3web.indication.ActionInstantIndication;
import de.d3web.indication.ActionNextQASet;
import de.d3web.indication.ActionSuppressAnswer;
import de.d3web.kernel.verbalizer.VerbalizationManager;
import de.d3web.kernel.verbalizer.VerbalizationManager.RenderingFormat;
import de.d3web.knowledgeExporter.KnowledgeManager;
import de.d3web.scoring.ActionHeuristicPS;


/**
 * @author reutelshoefer
 *
 */
public class RuleWriter extends TxtKnowledgeWriter {
	
	private String string_if;
	private String string_then;
	private String string_or;
	private String string_and;
	private String string_not;
	private String string_except;
	private String string_kontext;
	private String string_minmax;
	private String string_instant;
	private String string_hide;
	private String string_known;
	private String string_unknown;

	
	public RuleWriter(KnowledgeManager manager) {
		super(manager);
		string_if = KnowledgeManager.getResourceBundle().getString("ruleWriter.if");
		string_then = KnowledgeManager.getResourceBundle().getString("ruleWriter.then");
		string_or = KnowledgeManager.getResourceBundle().getString("ruleWriter.or");
		string_and = KnowledgeManager.getResourceBundle().getString("ruleWriter.and");
		string_not = KnowledgeManager.getResourceBundle().getString("ruleWriter.not");
		string_except = KnowledgeManager.getResourceBundle().getString("ruleWriter.except");
		string_kontext = KnowledgeManager.getResourceBundle().getString("ruleWriter.kontext");
		string_minmax = KnowledgeManager.getResourceBundle().getString("ruleWriter.minmax");
		string_instant = KnowledgeManager.getResourceBundle().getString("ruleWriter.instant");
		string_hide = KnowledgeManager.getResourceBundle().getString("ruleWriter.hide");
		string_known = KnowledgeManager.getResourceBundle().getString("ruleWriter.known");
		string_unknown = KnowledgeManager.getResourceBundle().getString("ruleWriter.unknown");
		
		//string_known = de.d3web.textParser.complexRule.ComplexRuleConfiguration.BEKANNT;

	}

	@Override
	public String writeText() {
		StringBuffer s = new StringBuffer();
		Collection rules = manager.getAllRules();
		
		for (Iterator iter = rules.iterator(); iter.hasNext();) {
			Object element = iter.next();
			if (element instanceof Rule) {
				Rule rule = (Rule) element;
				if (!manager.isDone(rule)) {
					if(isValidRule(rule)) {
						appendRule(rule, s);
					}

				}
			}

		}

		return s.toString();
	}
	
	

	private void appendRule(Rule r, StringBuffer s) {
		StringBuffer ruleBuffer = new StringBuffer();
		ruleBuffer.append("\n" + string_if + " ");
		AbstractCondition cond = r.getCondition();
		ruleBuffer.append((cond instanceof TerminalCondition ? "" : "(") 
				+ verbalizer.verbalize(cond, RenderingFormat.PLAIN_TEXT, null) 
				+ (cond instanceof TerminalCondition ? "" : ")"));
		appendException(r,ruleBuffer);
		appendKontext(r,ruleBuffer);
		ruleBuffer.append("\n" + string_then + " ");
		boolean actionOK = appendAction(r.getAction(), ruleBuffer);
		ruleBuffer.append("\n");
		
		if(actionOK) {
			s.append(ruleBuffer);
		}
	}
	
	private void appendException(Rule r, StringBuffer buffy) {
		AbstractCondition cond = r.getException();
		if(cond != null) {
			
			buffy.append(" "+string_except+" ");
			buffy.append(verbalizer.verbalize(cond, RenderingFormat.PLAIN_TEXT, null));
			
		}
	}
	
	private void appendKontext(Rule r, StringBuffer buffy) {
		AbstractCondition cond = r.getContext();
		if(cond != null) {
			
			buffy.append(" "+string_kontext+" ");
			buffy.append(verbalizer.verbalize(cond, RenderingFormat.PLAIN_TEXT, null));
			
		}
	}

	private boolean appendAction(RuleAction a, StringBuffer s) {
		if (a instanceof ActionHeuristicPS) {
			ActionHeuristicPS action = (ActionHeuristicPS) a;
			Diagnosis d = action.getDiagnosis();
			s.append(quote(d.toString()));
			s.append(" = ");
			s.append(action.getScore().toString());
		} else if (a instanceof ActionQuestionSetter) {
			
			ActionQuestionSetter action = (ActionQuestionSetter) a;
			Question q = action.getQuestion();
			if(q == null) {
				//Ganze Regel wird dann nicht rausgeschrieben
				return false;
			}
			s.append(quote(q.toString()));
			
			if (action instanceof ActionSetValue)
				s.append(" = ");
			else if (action instanceof ActionAddValue)
				s.append(" += ");
			// append
			Object[] values = action.getValues();
			for (int i = 0; i < values.length; i++) {

				if (values[i] instanceof Answer) {

					Answer answer = (Answer) values[i];
					s.append("" + quote(answer.toString())+ " ; ");
				
				} else if (values[i] instanceof FormulaExpression) {
				
					FormulaExpression exp = (FormulaExpression) values[i];

					s.append("" + exp.getFormulaElement().toString()+ " ; ");
				}
			}
			s.replace(s.length()-2,s.length()-1,"");
			
		} else if (a instanceof ActionNextQASet) {
			//[TODO] muss nochgeändert werden
			if (a instanceof ActionInstantIndication) {
				s.append(string_instant + " [");
			}
			
			ActionNextQASet nQASet = (ActionNextQASet)a;
			List l = nQASet.getQASets();
			for (Iterator iter = l.iterator(); iter.hasNext();) {
				QASet element = (QASet) iter.next();
				s.append(quote(element.toString())+" ; ");
			}
			s.replace(s.length()-2,s.length()-1,"");
			if (a instanceof ActionInstantIndication) {
				s.append("]");
			}
//		} else if (a instanceof ActionIndication) {
//		} else if (a instanceof ActionClarify) {
//		} else if (a instanceof ActionRefine) {
		} else if (a instanceof ActionSuppressAnswer) {
			ActionSuppressAnswer asa = (ActionSuppressAnswer)a;
			s.append(string_hide);
			s.append(" " + "\"" + asa.getQuestion().getText() + "\"");
			s.append(" =");
			s.append(" [");
			boolean first = true;
			for(Object o: asa.getSuppress()){
				if(!first) s.append(";");
				first = false;
				Answer answer = (Answer)o;
				s.append("\"" + answer.toString() + "\"");
			}
			s.append("]");
		} else if (a instanceof ActionContraIndication) {
			ActionContraIndication aci = (ActionContraIndication)a;
			s.append(string_not);
			s.append(" [");
			boolean first = true;
			for(Object o: aci.getQASets()){
				if(!first) s.append(";");
				first = false;
				QASet qaset = (QASet)o;
				s.append("\"" + qaset.toString() + "\"");
			}
			s.append("]");
		} else {

			s.append(quote(a.toString()));
		}
		
		return true;
	}

	
	private String quote(String s) {
		return VerbalizationManager.quoteIfNecessary(s);
	}

	
	private boolean stringContainsAny(String s, String [] words) {
		for (int i = 0; i < words.length; i++) {
			if(s.contains(words[i]) || s.contains(words[i].toUpperCase()) || s.contains(words[i].toLowerCase())) {
				return true;		
			}
		}
		return false;
	}
	

}
