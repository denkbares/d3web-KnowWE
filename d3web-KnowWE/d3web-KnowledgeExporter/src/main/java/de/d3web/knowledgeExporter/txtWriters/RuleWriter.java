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

package de.d3web.knowledgeExporter.txtWriters;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import de.d3web.abstraction.ActionSetValue;
import de.d3web.abstraction.formula.FormulaElement;
import de.d3web.core.inference.PSAction;
import de.d3web.core.inference.Rule;
import de.d3web.core.inference.condition.Condition;
import de.d3web.core.inference.condition.TerminalCondition;
import de.d3web.core.knowledge.terminology.Choice;
import de.d3web.core.knowledge.terminology.QASet;
import de.d3web.core.knowledge.terminology.Question;
import de.d3web.core.knowledge.terminology.Solution;
import de.d3web.core.session.Value;
import de.d3web.core.session.values.MultipleChoiceValue;
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

	private final String string_if;
	private final String string_then;
	private final String string_not;
	private final String string_except;
	private final String string_kontext;
	private final String string_instant;
	private final String string_hide;

	public RuleWriter(KnowledgeManager manager) {
		super(manager);
		string_if = KnowledgeManager.getResourceBundle().getString("ruleWriter.if");
		string_then = KnowledgeManager.getResourceBundle().getString("ruleWriter.then");
		string_not = KnowledgeManager.getResourceBundle().getString("ruleWriter.not");
		string_except = KnowledgeManager.getResourceBundle().getString("ruleWriter.except");
		string_kontext = KnowledgeManager.getResourceBundle().getString("ruleWriter.kontext");
		string_instant = KnowledgeManager.getResourceBundle().getString("ruleWriter.instant");
		string_hide = KnowledgeManager.getResourceBundle().getString("ruleWriter.hide");

		// string_known =
		// de.d3web.textParser.complexRule.ComplexRuleConfiguration.BEKANNT;

	}

	@Override
	public String writeText() {
		StringBuffer s = new StringBuffer();
		Collection<Rule> rules = manager.getAllRules();

		for (Iterator<Rule> iter = rules.iterator(); iter.hasNext();) {
			Rule rule = iter.next();
			if (!manager.isDone(rule)) {
				if (isValidRule(rule)) {
					appendRule(rule, s);
				}
			}
		}

		return s.toString();
	}

	private void appendRule(Rule r, StringBuffer s) {
		StringBuffer ruleBuffer = new StringBuffer();
		ruleBuffer.append("\n" + string_if + " ");
		Condition cond = r.getCondition();
		ruleBuffer.append((cond instanceof TerminalCondition ? "" : "(")
				+ verbalizer.verbalize(cond, RenderingFormat.PLAIN_TEXT, null)
				+ (cond instanceof TerminalCondition ? "" : ")"));
		appendException(r, ruleBuffer);
		appendKontext(r, ruleBuffer);
		ruleBuffer.append("\n" + string_then + " ");
		boolean actionOK = appendAction(r.getAction(), ruleBuffer);
		ruleBuffer.append("\n");

		if (actionOK) {
			s.append(ruleBuffer);
		}
	}

	private void appendException(Rule r, StringBuffer buffy) {
		Condition cond = r.getException();
		if (cond != null) {

			buffy.append(" " + string_except + " ");
			buffy.append(verbalizer.verbalize(cond, RenderingFormat.PLAIN_TEXT, null));

		}
	}

	private void appendKontext(Rule r, StringBuffer buffy) {
		Condition cond = r.getContext();
		if (cond != null) {

			buffy.append(" " + string_kontext + " ");
			buffy.append(verbalizer.verbalize(cond, RenderingFormat.PLAIN_TEXT, null));

		}
	}

	private boolean appendAction(PSAction a, StringBuffer s) {
		if (a instanceof ActionHeuristicPS) {
			ActionHeuristicPS action = (ActionHeuristicPS) a;
			Solution d = action.getSolution();
			s.append(quote(d.toString()));
			s.append(" = ");
			s.append(action.getScore().toString());
		}
		else if (a instanceof ActionSetValue) {

			ActionSetValue action = (ActionSetValue) a;
			Question q = action.getQuestion();
			if (q == null) {
				// Ganze Regel wird dann nicht rausgeschrieben
				return false;
			}
			s.append(quote(q.toString()));

			// if (action instanceof ActionSetValue)
			s.append(" = ");
			// else if (action instanceof ActionAddValue) s.append(" += ");
			// append
			Object value = action.getValue();
			if (value instanceof Value) {
				if (value instanceof MultipleChoiceValue) {
					MultipleChoiceValue mcv = (MultipleChoiceValue) value;
					List<?> choices = (List<?>) mcv.getValue();
					for (int i = 0; i < choices.size(); i++) {
						s.append(((Choice) choices.get(i)).getName());
						if (i < choices.size() - 1) {
							s.append(" ; ");
						}
					}
				}
				else {
					s.append("" + quote(((Value) value).getValue().toString()));
				}
			}
			else if (value instanceof FormulaElement) {
				s.append("" + value.toString());
			}
		}
		else if (a instanceof ActionNextQASet) {
			// [TODO] muss nochgeändert werden
			if (a instanceof ActionInstantIndication) {
				s.append(string_instant + " [");
			}

			ActionNextQASet nQASet = (ActionNextQASet) a;
			List<QASet> l = nQASet.getQASets();
			for (Iterator<QASet> iter = l.iterator(); iter.hasNext();) {
				QASet element = iter.next();
				s.append(quote(element.toString()) + " ; ");
			}
			s.replace(s.length() - 2, s.length() - 1, "");
			if (a instanceof ActionInstantIndication) {
				s.append("]");
			}
			// } else if (a instanceof ActionIndication) {
			// } else if (a instanceof ActionClarify) {
			// } else if (a instanceof ActionRefine) {
		}
		else if (a instanceof ActionSuppressAnswer) {
			ActionSuppressAnswer asa = (ActionSuppressAnswer) a;
			s.append(string_hide);
			s.append(" " + "\"" + asa.getQuestion().getName() + "\"");
			s.append(" =");
			s.append(" [");
			boolean first = true;
			for (Choice choice : asa.getSuppress()) {
				if (!first) s.append(";");
				first = false;
				Choice answer = choice;
				s.append("\"" + answer.toString() + "\"");
			}
			s.append("]");
		}
		else if (a instanceof ActionContraIndication) {
			ActionContraIndication aci = (ActionContraIndication) a;
			s.append(string_not);
			s.append(" [");
			boolean first = true;
			for (Object o : aci.getQASets()) {
				if (!first) s.append(";");
				first = false;
				QASet qaset = (QASet) o;
				s.append("\"" + qaset.toString() + "\"");
			}
			s.append("]");
		}
		else {

			s.append(quote(a.toString()));
		}

		return true;
	}

	private String quote(String s) {
		return VerbalizationManager.quoteIfNecessary(s);
	}
}
