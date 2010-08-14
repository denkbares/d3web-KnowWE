package de.d3web.we.wisec.event;

import java.util.LinkedList;
import java.util.List;

import org.openrdf.query.BindingSet;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.Query;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

import common.Logger;

import de.d3web.core.knowledge.terminology.QContainer;
import de.d3web.core.knowledge.terminology.Question;
import de.d3web.core.manage.KnowledgeBaseManagement;
import de.d3web.core.session.Value;
import de.d3web.core.session.values.NumValue;
import de.d3web.we.basic.Information;
import de.d3web.we.basic.InformationType;
import de.d3web.we.basic.TerminologyType;
import de.d3web.we.core.DPSEnvironment;
import de.d3web.we.core.broker.Broker;
import de.d3web.we.core.knowledgeService.D3webKnowledgeServiceSession;
import de.d3web.we.core.semantic.SemanticCoreDelegator;
import de.d3web.we.d3webModule.DPSEnvironmentManager;
import de.d3web.we.event.Event;
import de.d3web.we.event.EventListener;
import de.d3web.we.event.FindingSetEvent;
import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.kdom.Section;
import de.d3web.we.wisec.util.Criteria;

public class WISECFindingSetEventListener implements EventListener<FindingSetEvent> {

	private static final String LIST = "list";
	private List<String> activeLists = new LinkedList<String>();

	@Override
	public Class<? extends Event> getEvent() {
		return FindingSetEvent.class;
	}

	@Override
	public void notify(FindingSetEvent event, String web, String username,
			Section<? extends KnowWEObjectType> s) {

		if (!checkQuestion(event.getQuestion())) return;

		// Specifies whether vales are added (true) or subtracted (false)
		boolean add = true;
		if (event.getValue().toString().equals("excluded")) add = false;

		// Stores the currently computed lists
		List<String> computedLists = new LinkedList<String>();

		for (Criteria criteria : Criteria.values()) {

			try {
				Query query = createQuery(event.getQuestion(), event.getValue(), criteria);
				TupleQueryResult result = evaluateQuery(query);
				double accumulatedValue = computeNumValue(criteria, result, computedLists, add);
				setCounterValue(criteria, accumulatedValue, web, username, event.getNamespace());
				System.out.println(criteria + ": " + accumulatedValue);
			}
			catch (RepositoryException e) {
				Logger.getLogger(this.getClass()).warn(
						"Unable to create SPARQL-Query, no values set! " + e.getMessage());
			}
			catch (MalformedQueryException e) {
				Logger.getLogger(this.getClass()).warn(
						"Unable to create SPARQL-Query, no values set! " + e.getMessage());
			}
			catch (QueryEvaluationException e) {
				Logger.getLogger(this.getClass()).warn(
						"Unable to evaluate SPARQL-Query, no values set! " + e.getMessage());
			}

		}

		if (add) // Set all computed Lists as active Lists
		activeLists.addAll(computedLists);
		else // Or remove them in case of substraction
		activeLists.removeAll(computedLists);
	}

	/**
	 * Checks if the question which triggered this event is a question of the
	 * "Substances"-Questionnaire.
	 */
	private boolean checkQuestion(Question question) {
		KnowledgeBaseManagement kbm =
				KnowledgeBaseManagement.createInstance(question.getKnowledgeBase());
		QContainer qc = kbm.findQContainer("Substances");
		if (qc == null) return false;
		return qc.hasChild(question);
	}

	/**
	 * Creates a SPARQL-Query with the appropriate substance and criteria.
	 */
	private Query createQuery(Question question, Value value, Criteria criteria) throws RepositoryException, MalformedQueryException {

		String queryString = SemanticCoreDelegator.getInstance().getSparqlNamespaceShorts() +
								"SELECT ?" + LIST + " ?" + criteria + " " +
								"WHERE { " +
								"<http://ki.informatik.uni-wuerzburg.de/d3web/we/knowwe.owl#" +
								question.getName() + "> w:onListRelation ?substancelistrelation . "
				+
								"?" + LIST + " w:hasSubstanceRelation ?substancelistrelation . " +
								"?" + LIST + " w:" + criteria + " ?" + criteria + " ." +
								"}";

		RepositoryConnection con = SemanticCoreDelegator.getInstance().getUpper().getConnection();
		return con.prepareQuery(QueryLanguage.SPARQL, queryString);
	}

	/**
	 * Evaluates a SPARQL-Query and returns a TupleQueryResult containing the
	 * required numerical values.
	 */
	private TupleQueryResult evaluateQuery(Query query) throws QueryEvaluationException {
		if (!(query instanceof TupleQuery)) throw new IllegalStateException(
				"Query needs to be an instance of TupleQuery.");

		return ((TupleQuery) query).evaluate();
	}

	/**
	 * Computes the numerical value which will be added to the counter. The
	 * value is based on the committed TupleQueryResult.
	 */
	private double computeNumValue(Criteria criteria, TupleQueryResult result,
			List<String> computedLists, boolean add) throws QueryEvaluationException {
		double accumulatedValue = 0;
		while (result.hasNext()) {
			BindingSet binding = result.next();
			String currentList = binding.getValue(LIST).stringValue();
			// TODO: Check this!
			if ((activeLists.contains(currentList) && add) // Don't add if list
															// was already
															// computed
					|| (!activeLists.contains(currentList) && !add)) // Don't
																		// subtract
																		// if
																		// list
																		// hasn't
																		// been
																		// computed
																		// yet
			continue;
			accumulatedValue += Double.parseDouble(binding.getValue(criteria.name()).stringValue());
			computedLists.add(currentList);
		}
		return add ? accumulatedValue : accumulatedValue * (-1);
	}

	/**
	 * Adds the computed value to the counter of the current criteria.
	 */
	private void setCounterValue(Criteria criteria, double accumulatedValue,
			String web, String user, String namespace) {

		if (accumulatedValue == 0) return;

		// Get the KnowledgeServiceSession
		DPSEnvironment env = DPSEnvironmentManager.getInstance().getEnvironments(web);
		Broker broker = env.getBroker(user);
		D3webKnowledgeServiceSession kss =
				(D3webKnowledgeServiceSession) broker.getSession().getServiceSession(namespace);
		if (kss == null) {
			Logger.getLogger(this.getClass()).error(
					"Unable to get KnowledgeServiceSession for namespace: " + namespace);
			return;
		}

		// Search the Counter-Question (P, B, Aqua_Tox etc.)
		Question counterQuestion = kss.getBaseManagement().findQuestion(criteria.name());
		if (counterQuestion == null) {
			Logger.getLogger(this.getClass()).error(
					"Counter-Question: " + criteria + " was not found! No value set!");
			return;
		}

		// Get the old value
		Value oldValue = kss.getSession().getBlackboard().getValue(counterQuestion);
		double oldNumValue = oldValue instanceof NumValue ? (Double) oldValue.getValue() : 0;
		NumValue newValue = new NumValue(oldNumValue + accumulatedValue);

		// Set the new value
		List<Object> newValueList = new LinkedList<Object>();
		newValueList.add(newValue);
		Information info = new Information(namespace, counterQuestion.getId(), newValueList,
				TerminologyType.symptom, InformationType.ExternalInformation);
		kss.inform(info);
		broker.update(info);
	}

}
