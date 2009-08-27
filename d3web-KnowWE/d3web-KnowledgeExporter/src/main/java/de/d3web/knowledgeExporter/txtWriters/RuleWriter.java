package de.d3web.knowledgeExporter.txtWriters;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import de.d3web.kernel.domainModel.Answer;
import de.d3web.kernel.domainModel.Diagnosis;
import de.d3web.kernel.domainModel.QASet;
import de.d3web.kernel.domainModel.RuleAction;
import de.d3web.kernel.domainModel.RuleComplex;
import de.d3web.kernel.domainModel.formula.FormulaExpression;
import de.d3web.kernel.domainModel.qasets.Question;
import de.d3web.kernel.domainModel.ruleCondition.AbstractCondition;
import de.d3web.kernel.psMethods.contraIndication.ActionContraIndication;
import de.d3web.kernel.psMethods.heuristic.ActionHeuristicPS;
import de.d3web.kernel.psMethods.nextQASet.ActionInstantIndication;
import de.d3web.kernel.psMethods.nextQASet.ActionNextQASet;
import de.d3web.kernel.psMethods.questionSetter.ActionAddValue;
import de.d3web.kernel.psMethods.questionSetter.ActionQuestionSetter;
import de.d3web.kernel.psMethods.questionSetter.ActionSetValue;
import de.d3web.kernel.psMethods.suppressAnswer.ActionSuppressAnswer;
import de.d3web.kernel.verbalizer.VerbalizationManager;
import de.d3web.kernel.verbalizer.VerbalizationManager.RenderingFormat;
import de.d3web.knowledgeExporter.KnowledgeManager;


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

	public String writeText() {
		StringBuffer s = new StringBuffer();
		Collection rules = manager.getAllRules();
		
		for (Iterator iter = rules.iterator(); iter.hasNext();) {
			Object element = (Object) iter.next();
			if (element instanceof RuleComplex) {
				RuleComplex rule = (RuleComplex) element;
				if (!manager.isDone(rule)) {
					if(isValidRule(rule)) {
						appendRule(rule, s);
					}

				}
			}

		}

		return s.toString();
	}
	
	

	private void appendRule(RuleComplex r, StringBuffer s) {
		StringBuffer ruleBuffer = new StringBuffer();
		ruleBuffer.append("\n"+string_if+" ");
		AbstractCondition cond = r.getCondition();
		ruleBuffer.append("(" + verbalizer.verbalize(cond, RenderingFormat.PLAIN_TEXT, null) + ")");
		appendException(r,ruleBuffer);
		appendKontext(r,ruleBuffer);
		ruleBuffer.append("\n"+string_then+" ");
		boolean actionOK = appendAction(r.getAction(), ruleBuffer);
		ruleBuffer.append("\n");
		
		if(actionOK) {
			s.append(ruleBuffer);
		}
	}
	
	private void appendException(RuleComplex r, StringBuffer buffy) {
		AbstractCondition cond = r.getException();
		if(cond != null) {
			
			buffy.append(" "+string_except+" ");
			buffy.append(verbalizer.verbalize(cond, RenderingFormat.PLAIN_TEXT, null));
			
		}
	}
	
	private void appendKontext(RuleComplex r, StringBuffer buffy) {
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
