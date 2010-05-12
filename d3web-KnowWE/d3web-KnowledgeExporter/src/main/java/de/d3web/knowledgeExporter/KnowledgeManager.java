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

package de.d3web.knowledgeExporter;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Set;

import de.d3web.core.inference.KnowledgeSlice;
import de.d3web.core.inference.PSAction;
import de.d3web.core.inference.Rule;
import de.d3web.core.inference.RuleSet;
import de.d3web.core.inference.condition.CondDState;
import de.d3web.core.inference.condition.CondQuestion;
import de.d3web.core.inference.condition.Condition;
import de.d3web.core.inference.condition.NonTerminalCondition;
import de.d3web.core.inference.condition.TerminalCondition;
import de.d3web.core.knowledge.KnowledgeBase;
import de.d3web.core.knowledge.TerminologyObject;
import de.d3web.core.knowledge.terminology.QASet;
import de.d3web.core.knowledge.terminology.Question;
import de.d3web.core.knowledge.terminology.Solution;
import de.d3web.indication.ActionIndication;
import de.d3web.indication.ActionNextQASet;
import de.d3web.scoring.ActionHeuristicPS;

/**
 * The class KnowledgeManager provides the KnowledgeWriters with knowledge
 * supposed to get exported. Therefore the actual KnowledgeBase is passed over
 * to the Constructor and optionally a list with diagnoses containig the
 * diagnoses that are supposed to get exported. If a list is given, the
 * KnowledgeManager automatically calculates the necessary knowledge so that
 * only complete and connected threes and only the needed rules etc. get
 * exported. Otherwise the complete KnowledgeBase gets exported.
 * 
 * 
 */
public class KnowledgeManager {

	private static Locale locale = Locale.ENGLISH;
	private static ResourceBundle rb;

	private Set<Rule> allRules = new HashSet<Rule>();
	private Set<Rule> doneRules = new HashSet<Rule>();
	private List<Solution> diagnosisList = new LinkedList<Solution>();
	private Set<Question> questions = new HashSet<Question>();
	private Set<QASet> qClasses = new HashSet<QASet>();
	private boolean filterOn = false;
	private KnowledgeBase kb;
	private Set<Rule> indicationRules = new HashSet<Rule>();

	/**
	 * If you dont want to export the complete KnowledgeBase, you have the
	 * possibility to set a List of specific Diagnoses. Set diagnosesToExport to
	 * null if you want do export the complete KnowledgeBase or use the other
	 * Constructor.
	 * 
	 * @param diagnosesToExport is the List of Diagnoses you want to export.
	 * @param kb is the used KnowledgeBase
	 */
	public KnowledgeManager(KnowledgeBase kb, List<Solution> diagnosesToExport) {
		this.kb = kb;
		Collection<KnowledgeSlice> knowledge = kb.getAllKnowledgeSlices();

		if (diagnosesToExport == null) {
			diagnosisList = new LinkedList<Solution>();
			diagnosisList.addAll(kb.getSolutions());
		}
		else {
			filterOn = true;
			diagnosisList = calcDiagnosisSet(diagnosesToExport);
		}

		for (Iterator<KnowledgeSlice> iter = knowledge.iterator(); iter.hasNext();) {
			KnowledgeSlice element = iter.next();
			if (element instanceof RuleSet) {
				for (Rule r : ((RuleSet) element).getRules()) {
					if (matchesFilter(r) || !filterOn) {
						this.allRules.add(r);
					}
				}
			}
		}
		if (filterOn) {

			calcRelevantQuestions();
			calcRelevantQASets();
			checkIndicationRules();
		}
	}

	/**
	 * With this Contructor you export the complete KnowledgeBase, except you
	 * set a filter with the method "setFilter(List diagnosesToExport)"
	 * 
	 * @param kb is the used KnowledgeBase
	 */
	public KnowledgeManager(KnowledgeBase kb) {
		this.kb = kb;
		diagnosisList = new LinkedList<Solution>();
		diagnosisList.addAll(kb.getSolutions());

		Collection<KnowledgeSlice> knowledge = kb.getAllKnowledgeSlices();
		for (Iterator<KnowledgeSlice> iter = knowledge.iterator(); iter.hasNext();) {
			KnowledgeSlice element = iter.next();
			if (element instanceof RuleSet) {
				this.allRules.addAll(((RuleSet) element).getRules());
			}
		}
	}

	/**
	 * If you dont want to export the complete KnowledgeBase, you have the
	 * possibility to set a List of specific Diagnoses.
	 * 
	 * @param diagnosesToExport is the List of Diagnoses you want to export.
	 */
	public void setFilter(List<Solution> diagnosesToExport) {

		if (diagnosesToExport != null) {
			filterOn = true;
			diagnosisList = calcDiagnosisSet(diagnosesToExport);
		}
		else {
			return;
		}

		Collection<KnowledgeSlice> knowledge = kb.getAllKnowledgeSlices();

		this.allRules = new HashSet<Rule>();
		for (Iterator<KnowledgeSlice> iter = knowledge.iterator(); iter.hasNext();) {
			KnowledgeSlice element = iter.next();
			if (element instanceof RuleSet) {
				RuleSet rs = (RuleSet) element;
				for (Rule r : rs.getRules()) {
					if (matchesFilter(r)) {
						this.allRules.add(r);
					}
				}

			}
		}
		if (filterOn) {
			calcRelevantQuestions();
			calcRelevantQASets();
			checkIndicationRules();
		}
	}

	public void ruleDone(Rule rule) {
		doneRules.add(rule);
	}

	public boolean isDone(Rule rule) {
		return doneRules.contains(rule);
	}

	public Collection<Rule> getAllRules() {
		return allRules;
	}

	public KnowledgeBase getKB() {
		return kb;
	}

	public List<Solution> getDiagnosisList() {

		return diagnosisList;
	}

	public Set<Question> getQuestions() {

		if (!filterOn) {
			Set<Question> s = new HashSet<Question>();
			s.addAll(kb.getQuestions());
			return s;
		}
		return questions;
	}

	public Set<QASet> getQClasses() {

		if (!filterOn) {
			Set<QASet> s = new HashSet<QASet>();
			s.addAll(kb.getQContainers());
			// Iterator<QASet> iter = kb.getQASetIterator();
			// for (Iterator<QASet> iterator = iter; iterator.hasNext();) {
			// QASet element = iterator.next();
			// if (element != null && !(element instanceof Question)) {
			// s.add(element);
			// }
			// }
			return s;

		}
		return qClasses;
	}

	public boolean isFilterOn() {
		return filterOn;
	}

	public static ResourceBundle getResourceBundle() {
		if (rb == null) {
			rb = ResourceBundle.getBundle("properties.KnowledgeExporter", locale);
			// rb = ResourceBundle.getBundle("config_DT");
		}
		return rb;
	}

	public static void setLocale(Locale l) {
		locale = l;
		rb = ResourceBundle.getBundle("properties.KnowledgeExporter", locale);
		// rb = ResourceBundle.getBundle("config_DT");
	}

	public static Locale getLocale() {
		return locale;
	}

	private void checkIndicationRules() {

		for (Iterator<Rule> iter = indicationRules.iterator(); iter.hasNext();) {
			Rule element = iter.next();
			Condition cond = element.getCondition();
			Set<Question> questions = new HashSet<Question>();
			addAllAppearingQuestions(cond, questions);
			ActionIndication ai = (ActionIndication) element.getAction();
			for (QASet q : ai.getQASets()) {
				if (q instanceof Question) questions.add((Question) q);
			}
			boolean ok = true;
			for (Iterator<Question> iterator = questions.iterator(); iterator.hasNext();) {
				QASet q = iterator.next();
				if (!qClasses.contains(q) && !questions.contains(q)) {
					ok = false;
				}
			}
			if (ok) {
				this.allRules.add(element);
			}
		}
	}

	private void calcRelevantQASets() {
		QASet root = kb.getRootQASet();
		searchRelQASets(root);

		Set<QASet> s = new HashSet<QASet>();
		for (Iterator<QASet> iter = qClasses.iterator(); iter.hasNext();) {
			QASet element = iter.next();
			addQASetPath(element, s);
		}
		qClasses = s;
	}

	private void addQASetPath(QASet q, Set<QASet> l) {
		l.add(q);
		for (TerminologyObject to : q.getParents()) {
			QASet element = (QASet) to;
			addQASetPath(element, l);
		}

	}

	private void addQuestionPath(Question q, Set<QASet> s) {
		s.add(q);
		for (TerminologyObject to : q.getParents()) {
			QASet element = (QASet) to;
			if (element instanceof Question) {
				addQASetPath(element, s);
			}
		}
	}

	private void searchRelQASets(QASet q) {
		for (TerminologyObject to : q.getChildren()) {
			QASet element = (QASet) to;
			if (element instanceof Question) {
				if (questions.contains(element)) {
					qClasses.add(q);
				}
			}
			else {
				searchRelQASets(element);
			}
		}
	}

	private void calcRelevantQuestions() {

		for (Iterator<Rule> iter = allRules.iterator(); iter.hasNext();) {
			Rule element = iter.next();
			questions.addAll(getAllQuestions(element));
		}
		Set<QASet> s = new HashSet<QASet>();
		for (Iterator<Question> iter = questions.iterator(); iter.hasNext();) {
			Question element = iter.next();
			addQuestionPath(element, s);
		}
		Set<Question> q = new HashSet<Question>();
		for (Iterator<QASet> iter = s.iterator(); iter.hasNext();) {
			Object element = iter.next();
			if (element instanceof Question) {
				q.add((Question) element);
			}
		}
		questions = q;

	}

	private Set<Question> getAllQuestions(Rule r) {
		Set<Question> result = new HashSet<Question>();
		PSAction a = r.getAction();
		if (a instanceof ActionHeuristicPS) {
			Condition cond = r.getCondition();
			addAllAppearingQuestions(cond, result);

		}
		else if (a instanceof ActionIndication) {
			List<QASet> l = ((ActionIndication) a).getQASets();
			for (Iterator<QASet> iter = l.iterator(); iter.hasNext();) {
				QASet element = iter.next();
				if (element instanceof Question) {
					result.add((Question) element);
				}
			}

		}
		return result;
	}

	private void addAllAppearingQuestions(Condition cond, Set<Question> s) {
		if (cond instanceof TerminalCondition) {
			if (cond instanceof CondQuestion) {
				s.add(((CondQuestion) cond).getQuestion());
			}
		}
		else {
			List<Condition> terms = ((NonTerminalCondition) cond).getTerms();
			for (Iterator<Condition> iter = terms.iterator(); iter.hasNext();) {
				Condition element = iter.next();
				addAllAppearingQuestions(element, s);

			}
		}
	}

	private List<Solution> calcDiagnosisSet(List<Solution> l) {
		List<Solution> result = new LinkedList<Solution>();
		for (Iterator<Solution> iter = l.iterator(); iter.hasNext();) {
			Solution element = iter.next();
			addPathToList(element, result);
		}

		return result;
	}

	private void addPathToList(Solution d, List<Solution> l) {
		List<TerminologyObject> parents = Arrays.asList(d.getParents());
		if (!l.contains(d)) {
			l.add(d);
		}
		else {
			return;
		}
		for (Iterator<TerminologyObject> iter = parents.iterator(); iter.hasNext();) {
			Solution element = (Solution) iter.next();
			if (!l.contains(element)) {
				l.add(element);
				addPathToList(element, l);
			}

		}

	}

	private boolean matchesFilter(Rule r) {
		List<Solution> l = diagnosisList;
		if (l == null) {
			return true;
		}
		for (Iterator<Solution> iter = l.iterator(); iter.hasNext();) {
			Object element = iter.next();
			if (element instanceof Solution) {
				if (r.getAction() instanceof ActionHeuristicPS) {
					ActionHeuristicPS action = ((ActionHeuristicPS) r
							.getAction());
					if (action == null) {
						return false;
					}
					Solution d = action.getDiagnosis();
					if (d == null) {
						return false;
					}
					if (d.equals(element)) {
						return true;
					}

				}
				if (r.getAction() instanceof ActionNextQASet) {

					if (r.getCondition() instanceof CondDState) {

						if (((CondDState) r.getCondition()).getDiagnosis()
								.equals(element)) {
							return true;
						}
					}
					else {
						this.indicationRules.add(r);
					}

				}
			}
		}

		return false;

	}

}
