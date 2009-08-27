package de.d3web.knowledgeExporter;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Set;

import de.d3web.kernel.domainModel.Diagnosis;
import de.d3web.kernel.domainModel.KnowledgeBase;
import de.d3web.kernel.domainModel.QASet;
import de.d3web.kernel.domainModel.RuleAction;
import de.d3web.kernel.domainModel.RuleComplex;
import de.d3web.kernel.domainModel.qasets.Question;
import de.d3web.kernel.domainModel.ruleCondition.AbstractCondition;
import de.d3web.kernel.domainModel.ruleCondition.CondDState;
import de.d3web.kernel.domainModel.ruleCondition.CondQuestion;
import de.d3web.kernel.domainModel.ruleCondition.NonTerminalCondition;
import de.d3web.kernel.domainModel.ruleCondition.TerminalCondition;
import de.d3web.kernel.psMethods.heuristic.ActionHeuristicPS;
import de.d3web.kernel.psMethods.nextQASet.ActionIndication;
import de.d3web.kernel.psMethods.nextQASet.ActionNextQASet;

/**
 * The class KnowledgeManager provides the KnowledgeWriters with
 * knowledge supposed to get exported. Therefore the actual 
 * KnowledgeBase is passed over to the Constructor and optionally
 * a list with diagnoses containig the diagnoses that are 
 * supposed to get exported. If a list is given, the KnowledgeManager
 * automatically calculates the necessary knowledge so that only 
 * complete and connected threes and only the needed rules etc.
 * get exported. Otherwise the complete KnowledgeBase gets exported.
 * 
 * 
 */
public class KnowledgeManager {


	

	private static Locale locale = Locale.ENGLISH;
	private static ResourceBundle rb;

	private Set allRules = new HashSet<RuleComplex>();
	private Set<RuleComplex> doneRules = new HashSet<RuleComplex>();
	private List<Diagnosis> diagnosisList = new LinkedList<Diagnosis>();
	private Set<Question> questions = new HashSet<Question>();
	private Set<QASet> qClasses = new HashSet<QASet>();
	private boolean filterOn = false;
	private KnowledgeBase kb;
	private Set indicationRules = new HashSet();
	
	/**
	 * If you dont want to export the complete KnowledgeBase, you
	 * have the possibility to set a List of specific Diagnoses.
	 * Set diagnosesToExport to null if you want do export the 
	 * complete KnowledgeBase or use the other Constructor.
	 * 
	 * @param diagnosesToExport is the List of Diagnoses you want
	 * 	to export.
	 * @param kb is the used KnowledgeBase
	 */
	public KnowledgeManager(KnowledgeBase kb, List<Diagnosis> diagnosesToExport) {
		this.kb = kb;
		Collection knowledge = kb.getAllKnowledgeSlices();

		if (diagnosesToExport == null) {
			diagnosisList = new LinkedList();
			diagnosisList.addAll(kb.getDiagnoses());
		} else {
			filterOn = true;
			diagnosisList = calcDiagnosisSet(diagnosesToExport);
		}

		for (Iterator iter = knowledge.iterator(); iter.hasNext();) {
			Object element = (Object) iter.next();
			if (element instanceof RuleComplex) {
				if (matchesFilter((RuleComplex) element) || !filterOn) {

					this.allRules.add(element);
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
	 * With this Contructor you export the complete KnowledgeBase, except
	 * you set a filter with the method "setFilter(List diagnosesToExport)"
	 * 
	 * @param kb is the used KnowledgeBase
	 */
	public KnowledgeManager(KnowledgeBase kb) {
		this.kb = kb;
		diagnosisList = new LinkedList();
		diagnosisList.addAll(kb.getDiagnoses());
		
		Collection knowledge = kb.getAllKnowledgeSlices();
		for (Iterator iter = knowledge.iterator(); iter.hasNext();) {
			Object element = (Object) iter.next();
			if (element instanceof RuleComplex) {
				this.allRules.add(element);
			}
		}
	}
	
	/**
	 * If you dont want to export the complete KnowledgeBase, you
	 * have the possibility to set a List of specific Diagnoses.
	 * 
	 * @param diagnosesToExport is the List of Diagnoses you want
	 * 	to export.
	 */
	public void setFilter(List<Diagnosis> diagnosesToExport) {
		
		if (diagnosesToExport != null) {
			filterOn = true;
			diagnosisList = calcDiagnosisSet(diagnosesToExport);
		} else {
			return;
		}

		Collection knowledge = kb.getAllKnowledgeSlices();
		
		this.allRules = new HashSet();
		for (Iterator iter = knowledge.iterator(); iter.hasNext();) {
			Object element = (Object) iter.next();
			if (element instanceof RuleComplex) {
				if (matchesFilter((RuleComplex) element)) {
					this.allRules.add(element);
				}
			}
		}
		if (filterOn) {
			calcRelevantQuestions();
			calcRelevantQASets();
			checkIndicationRules();
		}
	}
	
	public void ruleDone(RuleComplex rule) {
		doneRules.add(rule);
	}
	
	public boolean isDone(RuleComplex rule) {
		return doneRules.contains(rule);
	}

	
	public Collection<RuleComplex> getAllRules() {
		return allRules;
	}

	public KnowledgeBase getKB() {
		return kb;
	}

	public List<Diagnosis> getDiagnosisList() {
	
		return diagnosisList;
	}

	public Set<Question> getQuestions() {
		
		if (!filterOn) {
			Set s = new HashSet();
			s.addAll(kb.getQuestions());
			return s;
		}
		return questions;
	}

	public Set<QASet> getQClasses() {
		
		if (!filterOn) {
			Set s = new HashSet();
			Iterator iter = kb.getQASetIterator();
			for (Iterator iterator = iter; iterator.hasNext();) {
				QASet element = (QASet) iterator.next();
				if (element != null && !(element instanceof Question)) {
					s.add(element);
				}
			}
			return s;
	
		}
		return qClasses;
	}

	public boolean isFilterOn() {
		return filterOn;
	}
	
	public static ResourceBundle getResourceBundle() {
		if (rb == null) {
			rb = ResourceBundle.getBundle("properties.KnowledgeExporter", 
					locale, KnowledgeManager.class.getClassLoader());
		}		
		return rb;
	}
	
	public static void setLocale(Locale l) {
		locale = l;
		rb = ResourceBundle.getBundle("properties.KnowledgeExporter",
				locale, KnowledgeManager.class.getClassLoader());
	}
	
	public static Locale getLocale() {
		return locale;
	}
	
	private void checkIndicationRules() {

		for (Iterator iter = indicationRules.iterator(); iter.hasNext();) {
			RuleComplex element = (RuleComplex) iter.next();
			AbstractCondition cond = element.getCondition();
			Set questions = new HashSet();
			addAllAppearingQuestions(cond, questions);
			ActionIndication ai = (ActionIndication) element.getAction();
			questions.addAll(ai.getQASets());
			boolean ok = true;
			for (Iterator iterator = questions.iterator(); iterator.hasNext();) {
				QASet q = (QASet) iterator.next();
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

		Set s = new HashSet();
		for (Iterator iter = qClasses.iterator(); iter.hasNext();) {
			QASet element = (QASet) iter.next();
			addQASetPath(element, s);
		}
		qClasses = s;
	}

	private void addQASetPath(QASet q, Set l) {
		List fathers = q.getParents();
		l.add(q);
		for (Iterator iter = fathers.iterator(); iter.hasNext();) {
			QASet element = (QASet) iter.next();
			addQASetPath(element, l);
		}

	}

	private void addQuestionPath(Question q, Set s) {
		List fathers = q.getParents();
		s.add(q);
		for (Iterator iter = fathers.iterator(); iter.hasNext();) {
			QASet element = (QASet) iter.next();
			if (element instanceof Question) {
				addQASetPath(element, s);
			}
		}
	}

	private void searchRelQASets(QASet q) {

		List children = q.getChildren();
		for (Iterator iter = children.iterator(); iter.hasNext();) {
			QASet element = (QASet) iter.next();
			if (element instanceof Question) {
				if (questions.contains(element)) {
					qClasses.add(q);
				}
			} else {
				searchRelQASets(element);
			}
		}
	}

	private void calcRelevantQuestions() {

		for (Iterator iter = allRules.iterator(); iter.hasNext();) {
			RuleComplex element = (RuleComplex) iter.next();
			questions.addAll(getAllQuestions(element));
		}
		Set s = new HashSet();
		for (Iterator iter = questions.iterator(); iter.hasNext();) {
			Question element = (Question) iter.next();
			addQuestionPath(element, s);
		}
		Set<Question> q = new HashSet<Question>();
		for (Iterator iter = s.iterator(); iter.hasNext();) {
			Object element = iter.next();
			if (element instanceof Question) {
				q.add((Question) element);
			}
		}
		questions = q;

	}

	private Set getAllQuestions(RuleComplex r) {
		Set result = new HashSet();
		RuleAction a = r.getAction();
		if (a instanceof ActionHeuristicPS) {
			AbstractCondition cond = r.getCondition();
			addAllAppearingQuestions(cond, result);

		} else if (a instanceof ActionIndication) {
			List l = ((ActionIndication) a).getQASets();
			for (Iterator iter = l.iterator(); iter.hasNext();) {
				QASet element = (QASet) iter.next();
				if (element instanceof Question) {
					result.add(element);
				}
			}

		}
		return result;
	}

	private void addAllAppearingQuestions(AbstractCondition cond, Set s) {
		if (cond instanceof TerminalCondition) {
			if (cond instanceof CondQuestion) {
				s.add(((CondQuestion) cond).getQuestion());
			}
		} else {
			List terms = ((NonTerminalCondition) cond).getTerms();
			for (Iterator iter = terms.iterator(); iter.hasNext();) {
				AbstractCondition element = (AbstractCondition) iter.next();
				addAllAppearingQuestions(element, s);

			}
		}
	}

	private List calcDiagnosisSet(List l) {
		List result = new LinkedList();
		for (Iterator iter = l.iterator(); iter.hasNext();) {
			Diagnosis element = (Diagnosis) iter.next();
			addPathToList(element, result);
		}

		return result;
	}

	private void addPathToList(Diagnosis d, List l) {
		List parents = d.getParents();
		if (!l.contains(d)) {
			l.add(d);
		} else {
			return;
		}
		for (Iterator iter = parents.iterator(); iter.hasNext();) {
			Diagnosis element = (Diagnosis) iter.next();
			if (!l.contains(element)) {
				l.add(element);
				addPathToList(element, l);
			}

		}

	}
	
	private boolean matchesFilter(RuleComplex r) {
		List l = diagnosisList;
		if (l == null) {
			return true;
		}
		for (Iterator iter = l.iterator(); iter.hasNext();) {
			Object element = (Object) iter.next();
			if (element instanceof Diagnosis) {
				if (r.getAction() instanceof ActionHeuristicPS) {
					ActionHeuristicPS action = ((ActionHeuristicPS) r
							.getAction());
					if (action == null) {
						return false;
					}
					Diagnosis d = action.getDiagnosis();
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
					} else {
						this.indicationRules.add(r);
					}

				}
			}
		}

		return false;

	}
	
	


}
