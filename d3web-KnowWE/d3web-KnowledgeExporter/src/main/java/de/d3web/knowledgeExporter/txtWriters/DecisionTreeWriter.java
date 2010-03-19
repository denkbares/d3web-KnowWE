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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.d3web.core.inference.KnowledgeSlice;
import de.d3web.core.inference.MethodKind;
import de.d3web.core.inference.PSMethod;
import de.d3web.core.inference.Rule;
import de.d3web.core.inference.RuleAction;
import de.d3web.core.inference.RuleSet;
import de.d3web.core.inference.condition.AbstractCondition;
import de.d3web.core.inference.condition.CondDState;
import de.d3web.core.inference.condition.CondNum;
import de.d3web.core.inference.condition.CondNumIn;
import de.d3web.core.inference.condition.NonTerminalCondition;
import de.d3web.core.knowledge.terminology.Answer;
import de.d3web.core.knowledge.terminology.Diagnosis;
import de.d3web.core.knowledge.terminology.NamedObject;
import de.d3web.core.knowledge.terminology.QASet;
import de.d3web.core.knowledge.terminology.QContainer;
import de.d3web.core.knowledge.terminology.Question;
import de.d3web.core.knowledge.terminology.QuestionChoice;
import de.d3web.core.knowledge.terminology.QuestionMC;
import de.d3web.core.knowledge.terminology.QuestionNum;
import de.d3web.core.knowledge.terminology.QuestionOC;
import de.d3web.core.knowledge.terminology.QuestionText;
import de.d3web.core.knowledge.terminology.QuestionYN;
import de.d3web.core.knowledge.terminology.info.NumericalInterval;
import de.d3web.core.knowledge.terminology.info.Property;
import de.d3web.core.session.values.AnswerChoice;
import de.d3web.indication.ActionIndication;
import de.d3web.indication.ActionNextQASet;
import de.d3web.indication.inference.PSMethodNextQASet;
import de.d3web.kernel.verbalizer.CondVerbalization;
import de.d3web.kernel.verbalizer.TerminalCondVerbalization;
import de.d3web.kernel.verbalizer.VerbalizationManager;
import de.d3web.knowledgeExporter.KnowledgeManager;
import de.d3web.scoring.ActionHeuristicPS;
import de.d3web.scoring.Score;
import de.d3web.scoring.inference.PSMethodHeuristic;

public class DecisionTreeWriter extends TxtKnowledgeWriter {
	
//	private KnowledgeBaseManagement kbm;

	private Set<QASet> qContainerSet;
	
	private Set<Question> processedQuestions = new HashSet<Question>();

	private int level = 0;

	private boolean exportDecisionTreeID;	

	public DecisionTreeWriter(KnowledgeManager manager) {
		super(manager);
		qContainerSet = manager.getQClasses();
	}
	
	@Override
	public String writeText() {

		StringBuffer s = new StringBuffer();

		QASet qaset = manager.getKB().getRootQASet();
		processSubQASets(qaset, s);
		
		if (s.length() > 0 && s.substring(0, 1).equals("\n")) {
			return s.substring(1);
		} else {
			return s.toString();
		}
	}
	
	private String quote(String s) {
		return VerbalizationManager.quoteIfNecessary(s);
	}

	private String getQuestionIntervall(Question q) {
		String result = "";
		if (q instanceof QuestionNum) {
			Object o = q.getProperties().getProperty(
					Property.QUESTION_NUM_RANGE);
			if (o instanceof Collection) {
				Collection coll = (Collection) o;
				if (coll.size() > 1) {
					result += "{";
				}
				for (Object co : coll) {
					if (co instanceof NumericalInterval) {
						NumericalInterval range = (NumericalInterval) co;

						result += makeString(range);
					}
				}
				if (coll.size() > 1) {
					result += "}";
				}
			}
			if (o instanceof NumericalInterval) {
				NumericalInterval range = (NumericalInterval) o;

				result += makeString(range);
			}
		}
		return result;
	}

	private String makeString(NumericalInterval range) {
		String result = "";
		String a = trimNum(new Double(range.getLeft()).toString());
		String b = trimNum(new Double(range.getRight()).toString());
		result += " (";
		result += a.toString();
		result += " ";
		result += b.toString();
		result += ") ";
		return result;
	}

	private void processSubQASets(QASet qaset, StringBuffer s) {
		boolean qasetIsWritten = false;
		List<? extends NamedObject> children = qaset.getChildren();
		List<QContainer> remainingQContainers = new LinkedList<QContainer>();
		for (Object element: children) {
			if (element instanceof Question) {
				if (!qasetIsWritten) {
					s.append(quote(qaset.toString()));
					s.append("\n");
					qasetIsWritten = true;
				}
				Question q = (Question) element;
				processQuestion(q, s);
			} else if (element instanceof QContainer && qContainerSet.contains(element)) {
				remainingQContainers.add((QContainer)element);
				qContainerSet.remove(element);
			}
		}
		for (QContainer qc: remainingQContainers) {
			s.append("\n");
			processSubQASets(qc, s);
		}
	}

	private String dashes(int k) {
		String s = "";
		for (int i = 0; i < k; i++) {
			s += "-";
		}
		s += " ";
		return s;
	}

	private String getQuestionUnitString(Question q) {
		if (q instanceof QuestionNum) {
			QuestionNum numQ = ((QuestionNum) q);
			Object o = numQ.getProperties().getProperty(Property.UNIT);
			if (o != null) {
				return " {" + o.toString() + "}";
			}
		}
		return "";
	}

	private void processQuestion(Question q, StringBuffer s) {
		processedQuestions.add(q);
		level++;
		s.append(dashes(level)
				+ quote(q.toString())
				+ " "
				+ getQuestionTypeString(q)
				+ getQuestionUnitString(q)
				+ getQuestionIntervall(q));
		if (isAbstractionQuestion(q)) {
			s.append("<"
					+ KnowledgeManager.getResourceBundle().getString("writer.abstract")
					+ ">");
		}
		if (exportDecisionTreeID) {
			s.append(" #" + q.getId());
		}
		s.append("\n");
		writeAnswers(q, s);
		for (Object o: q.getChildren()) {
			if (o instanceof Question && !processedQuestions.contains(o)) {
				processQuestion((Question)o, s);
			}
		}
		
		level--;
	}

	private void addChildren(NamedObject nob, Class<? extends PSMethod> psMethod, Map<AbstractCondition, List<Rule>> mergedRules) {
		KnowledgeSlice slices = nob.getKnowledge(
				psMethod, MethodKind.FORWARD);
		if (slices != null) {
			if (slices instanceof RuleSet) {
				for (Rule rule: ((RuleSet) slices).getRules()) {
					AbstractCondition condition = rule.getCondition();
					if(mergedRules.containsKey(condition)) {
						List<Rule> rules = mergedRules.get(condition);
						rules.add(rule);
					} else {
						List<Rule> rules = new LinkedList<Rule>();
						rules.add(rule);
						mergedRules.put(condition, rules);
					}
				}
			}
		}
	}
	
	private void writeAnswers(Question q, StringBuffer s) {


		level++;
		Map<AbstractCondition, List<Rule>> mergedRules = new HashMap<AbstractCondition, List<Rule>>();
		addChildren(q, PSMethodNextQASet.class, mergedRules);
		addChildren(q, PSMethodHeuristic.class, mergedRules);
		
		Set<AbstractCondition> conditions = mergedRules.keySet();
		Map<String, AbstractCondition> answerConds = new HashMap<String, AbstractCondition>();
		Map<AbstractCondition, CondVerbalization> condCondVerbs = new HashMap<AbstractCondition, 
			CondVerbalization>();
		List<AbstractCondition> sortedConditions = new ArrayList<AbstractCondition>();
		
		for (AbstractCondition cond:conditions) {
			if (cond instanceof NonTerminalCondition) {
				continue;
			}
			TerminalCondVerbalization tCondVerb = (TerminalCondVerbalization) verbalizer.createConditionVerbalization(cond);
			answerConds.put(tCondVerb.getAnswer(), cond);
			condCondVerbs.put(cond, tCondVerb);
		}
		
	
		AnswerList answerList = new AnswerList();
		if (q instanceof QuestionChoice) {
			answerList.addAll(((QuestionChoice) q).getAllAlternatives());
		}
		
		for (AnswerChoice ac:answerList) {
			if (answerConds.containsKey(ac.toString())) {
				sortedConditions.add(answerConds.remove(ac.toString()));
			}
		}
		List<AbstractCondition> condNums = new ArrayList<AbstractCondition>();
		condNums.addAll(answerConds.values());
		Collections.sort(condNums, new CondNumComparator());
		sortedConditions.addAll(condNums);
		
		String dashes = dashes(level);
		boolean foundYesNo = false;
		for (AbstractCondition cond:sortedConditions) {
			if (cond instanceof NonTerminalCondition) {
				continue;
			}
			
			TerminalCondVerbalization tCondVerb = (TerminalCondVerbalization) condCondVerbs.get(cond);
			
			String operator = "";
			if (!tCondVerb.getOperator().equals("=") && !tCondVerb.getOriginalClass()
					.equals(CondNumIn.class.getSimpleName())) {
				operator = tCondVerb.getOperator() + " ";
			}
			
			String answer = tCondVerb.getAnswer();
			if (isYes(answer) || isNo(answer)) {
				foundYesNo = true;
			}
			if (q instanceof QuestionChoice) {
				if (exportDecisionTreeID) {
					answer += " #" + answerList.get(answer).getId();
				}
				answerList.remove(tCondVerb.getAnswer());
			}
			if (!(cond instanceof CondNumIn)) {
				answer = quote(answer);
			}
			s.append(dashes + operator + answer + "\n");
			processActions(mergedRules, cond, s);
		}
		
		if (!foundYesNo) {
			for (Answer remainingAnswer:answerList) {
				String sTemp = "";
				if (isYes(remainingAnswer.toString())) {
					sTemp = KnowledgeManager.getResourceBundle().getString("datamanager.answerYes");
				} else if (isNo(remainingAnswer.toString())) {
					sTemp = KnowledgeManager.getResourceBundle().getString("datamanager.answerNo");
				} else {
					sTemp = remainingAnswer.toString();
				}
				s.append(dashes + quote(sTemp));
				if (exportDecisionTreeID) {
					s.append(" #" + remainingAnswer.getId());
				}
				s.append("\n");
			}
		}
		
		level--;
	}
	
	private boolean isYes(String a) {
		if (a.compareToIgnoreCase("yes") == 0 
			|| a.compareToIgnoreCase("ja") == 0) {
			return true;
		} else {
			return false;
		}
	}
	
	private boolean isNo(String a) {
		if (a.compareToIgnoreCase("no") == 0 
			|| a.compareToIgnoreCase("nein") == 0) {
			return true;
		} else {
			return false;
		}
	}
	
	
	private void processActions(Map<AbstractCondition, List<Rule>> mergedRules, AbstractCondition condition, StringBuffer s) {
		for (Rule rule: mergedRules.get(condition)) {
			RuleAction action = rule.getAction();
			if (action instanceof ActionIndication) {
				ActionIndication ai = (ActionIndication) action;
				List<QASet> qasets = ai.getQASets();
				for (QASet qaset: qasets) {
					if (qaset instanceof Question && !manager.isDone(rule)) {
						processQuestion((Question) qaset, s);
						manager.ruleDone(rule);
					} else if (qaset instanceof QContainer && !manager.isDone(rule)) {
						s.append(dashes(level + 1)
								+ quote(qaset.toString()) + "\n");
						manager.ruleDone(rule);
					}
				}
			} else if (action instanceof ActionHeuristicPS && !manager.isDone(rule)) {
				ActionHeuristicPS ah = (ActionHeuristicPS) action;
				Diagnosis diagnosis = ah.getDiagnosis();
				String dt = diagnosis.getText();
				Score score = ah.getScore();
				s.append(dashes(level + 1) + quote(dt) + " ("
						+ score.getSymbol() + ")\n");
				processDiagnosis(diagnosis, s);
				manager.ruleDone(rule);
			}
		}
	}

	private void processDiagnosis(Diagnosis diagnosis, StringBuffer s) {
		Map<AbstractCondition, List<Rule>> mergedRules = new HashMap<AbstractCondition, List<Rule>>();
		addChildren(diagnosis, PSMethodNextQASet.class, mergedRules);
		for (AbstractCondition diagCondition: mergedRules.keySet()) {
			if (diagCondition instanceof CondDState) {
				for (Rule diagRule: mergedRules.get(diagCondition)) {
					RuleAction diagRuleAction = diagRule.getAction();
					if (diagRuleAction instanceof ActionNextQASet && !manager.isDone(diagRule)) {
						List<QASet> nextQASets = ((ActionNextQASet) diagRuleAction).getQASets();
						for (QASet next : nextQASets) {
							if (next instanceof Question) {
								processQuestion((Question) next, s);
							} else if (next instanceof QContainer) {
								String suff = dashes(level + 1)
								+ quote(next.getText())
								+ "\n";
								if (!s.toString().endsWith(suff))
								s.append(suff);
								// dürfte gar nicht vorkommen
								// laut Konvention:
							}
						}
						manager.ruleDone(diagRule);
					}
				}
			}
		}
	}

//	private void addNumAnswer(CondNum cn, StringBuffer s) {
//		if (cn instanceof CondNumEqual) {
//			s.append(dashes(level) + "= " + trimNum(cn.getAnswerValue().toString()) + "\n");
//		} else if (cn instanceof CondNumGreater) {
//			s.append(dashes(level) + "> " + trimNum(cn.getAnswerValue().toString()) + "\n");
//		} else if (cn instanceof CondNumGreaterEqual) {
//			s.append(dashes(level) + ">= " + trimNum(cn.getAnswerValue().toString()) + "\n");
//		} else if (cn instanceof CondNumIn) {
//			CondNumIn cni = (CondNumIn) cn;
//			s.append(dashes(level) + "[" + trimNum(cni.getMinValue().toString()) + " "
//					+ trimNum(cni.getMaxValue().toString()) + "]\n");
//		} else if (cn instanceof CondNumLess) {
//			s.append(dashes(level) + "< " + trimNum(cn.getAnswerValue().toString()) + "\n");
//		} else if (cn instanceof CondNumLessEqual) {
//			s.append(dashes(level) + "<= " + trimNum(cn.getAnswerValue().toString()) + "\n");
//		}
//	}

	private boolean isAbstractionQuestion(Question q) {
		Boolean b =  (Boolean) q.getProperties().getProperty(
				Property.ABSTRACTION_QUESTION);
		return (b != null) ? b : false;
	}

	private String getQuestionTypeString(Question q) {
		String s = "";
		if (q instanceof QuestionYN) {
			s = KnowledgeManager.getResourceBundle().getString("decisionTreeWriter.yn");
		} else if (q instanceof QuestionOC) {
			s = "[oc]";
		} else if (q instanceof QuestionMC) {
			s = "[mc]";
		} else if (q instanceof QuestionNum) {
			s = "[num]";
		} else if (q instanceof QuestionText) {
			s = "[text]";
		}

		return s;
	}
	



	public boolean isExportDecisionTreeID() {
		return exportDecisionTreeID;
	}

	public void setExportDecisionTreeID(boolean exportDecisionTreeID) {
		this.exportDecisionTreeID = exportDecisionTreeID;
	}
	
	private class AnswerList extends ArrayList<AnswerChoice> {

		public void remove(String answer) {
	    	for (int i = 0; i < this.size(); i++) {
	    		if (this.get(i).toString().equals(answer)) {
	    			this.remove(i);
	    		}
	    	}
	    }
	    
	    public AnswerChoice get(String answer) {
	    	for (int i = 0; i < this.size(); i++) {
	    		if (this.get(i).toString().equals(answer)) {
	    			return this.get(i);
	    		}
	    	}
	    	return null;
	    }
	}
	
	private class CondNumComparator implements Comparator<AbstractCondition> {

		@Override
		public int compare(AbstractCondition o1, AbstractCondition o2) {
			if (o1 instanceof CondNum && o2 instanceof CondNum) {
				CondNum n1 = (CondNum) o1;
				CondNum n2 = (CondNum) o2;
				if (n1 instanceof CondNumIn || n2 instanceof CondNumIn) {
					if (n1 instanceof CondNumIn && n2 instanceof CondNumIn) {
						if (Double.compare(((CondNumIn) n1).getMaxValue(), ((CondNumIn) n2).getMaxValue()) != 0) {
							return Double.compare(((CondNumIn) n1).getMaxValue(), ((CondNumIn) n2).getMaxValue());
						} else {
							return Double.compare(((CondNumIn) n1).getMinValue(), ((CondNumIn) n2).getMinValue());
						}
					} else if (n1 instanceof CondNumIn) {
						return Double.compare(((CondNumIn) n1).getMaxValue(), n2.getAnswerValue());
					} else {
						return Double.compare(n1.getAnswerValue(), ((CondNumIn) n2).getMinValue());
					}
				} else {
					return Double.compare(n1.getAnswerValue(), n2.getAnswerValue());
					
				}
			}
			return 0;
		}
		
	}

}
