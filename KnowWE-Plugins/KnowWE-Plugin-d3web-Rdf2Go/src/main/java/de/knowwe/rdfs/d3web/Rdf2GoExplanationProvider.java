/*
 * Copyright (C) 2014 denkbares GmbH, Germany
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

package de.knowwe.rdfs.d3web;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.eclipse.rdf4j.query.BindingSet;

import com.denkbares.semanticcore.TupleQueryResult;
import de.d3web.core.knowledge.terminology.QuestionDate;
import de.d3web.core.knowledge.terminology.QuestionMC;
import de.d3web.core.knowledge.terminology.QuestionNum;
import de.d3web.core.knowledge.terminology.QuestionOC;
import de.d3web.core.knowledge.terminology.QuestionText;
import de.d3web.core.knowledge.terminology.QuestionYN;
import de.d3web.core.knowledge.terminology.Solution;
import de.d3web.core.session.Value;
import de.d3web.core.session.ValueUtils;
import de.d3web.core.session.values.ChoiceID;
import de.d3web.core.session.values.ChoiceValue;
import de.d3web.core.session.values.MultipleChoiceValue;
import de.d3web.core.session.values.NumValue;
import de.d3web.core.session.values.TextValue;
import de.d3web.scoring.HeuristicRating;
import com.denkbares.strings.Strings;
import de.knowwe.rdf2go.Rdf2GoCore;
import de.knowwe.rdf2go.sparql.utils.SparqlQuery;
import de.knowwe.rdf2go.utils.Rdf2GoUtils;

/**
 * Provides explanation utility methods.
 * <p>
 *
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 22.07.2014
 */
public class Rdf2GoExplanationProvider {

	private final Rdf2GoCore core;
	private final String sessionId;

	public Rdf2GoExplanationProvider(Rdf2GoCore core, String sessionId) {
		this.core = core;
		this.sessionId = sessionId;
	}

	public Collection<Fact> getPredecessorFacts(String objectName) {
		Collection<Fact> facts = new ArrayList<>();
		SparqlQuery query = new SparqlQuery().SELECT("?SourceObjectName ?SourceValue ?Agent ?AgentType ?SourceObjectType")
				.WHERE("<lns:" + sessionId + "> lns:hasFact ?Fact")
				.AND_WHERE("?Fact lns:hasTerminologyObject <lns:" + Strings.encodeURL(objectName) + ">")
				.AND_WHERE("?Fact prov:wasDerivedFrom ?OtherFact")
				.AND_WHERE("?OtherFact lns:hasTerminologyObject ?SourceObjectUri")
				.AND_WHERE("?SourceObjectUri rdf:type ?SourceObjectType")
				.AND_WHERE("?SourceObjectUri rdfs:label ?SourceObjectName")
				.AND_WHERE("?OtherFact lns:hasValue ?SourceValue")
				.AND_WHERE("?OtherFact prov:wasAttributedTo ?Agent")
				.AND_WHERE("?Agent rdf:type ?AgentType");
		TupleQueryResult queryRows = core.sparqlSelect(query.toSparql(core), new Rdf2GoCore.Options(false));
		for (BindingSet queryRow : queryRows) {
			Fact fact = new Fact();
			fact.terminologyObject = queryRow.getValue("SourceObjectName").toString();
			fact.value = toValue(clean(queryRow.getValue("SourceObjectType")
					.toString()), clean(queryRow.getValue("SourceValue").toString()));
			fact.agent = clean(queryRow.getValue("Agent").toString());
			fact.agentType = clean(queryRow.getValue("AgentType").toString());
			facts.add(fact);
		}
		return facts;
	}

	private Value toValue(String objectType, String value) {
		if (objectType.equals(QuestionYN.class.getSimpleName())
				|| objectType.equals(QuestionOC.class.getSimpleName())) {
			return new ChoiceValue(value);
		}
		else if (objectType.equals(QuestionMC.class.getSimpleName())) {
			try {
				JSONArray jsonArray = new JSONArray(value);
				ChoiceID[] choiceArray = new ChoiceID[jsonArray.length()];
				for (int i = 0; i < jsonArray.length(); i++) {
					choiceArray[i] = new ChoiceID(jsonArray.getString(i));
				}
				return new MultipleChoiceValue(choiceArray);
			}
			catch (JSONException e) {
				// should never happen
				throw new IllegalArgumentException("'" + value + "' is not a valid value");
			}
		}
		else if (objectType.equals(QuestionNum.class.getSimpleName())) {
			return new NumValue(Double.parseDouble(value));
		}
		else if (objectType.equals(QuestionText.class.getSimpleName())) {
			return new TextValue(value);
		}
		else if (objectType.equals(QuestionDate.class.getSimpleName())) {
			return ValueUtils.createDateValue(value);
		}
		else if (objectType.equals(Solution.class.getSimpleName())) {
			return new HeuristicRating(Double.parseDouble(value));
		}
		else {
			throw new IllegalArgumentException("'" + value + "' is not a valid value");
		}
	}

	private String clean(String text) {
		text = Rdf2GoUtils.trimNamespace(core, text);
		text = Rdf2GoUtils.trimDataType(core, text);
		return text;
	}

	public Fact getFact(String objectName) {
		SparqlQuery query = new SparqlQuery().SELECT("?Value ?Agent ?AgentType ?ObjectType")
				.WHERE("<lns:" + sessionId + "> lns:hasFact ?Fact")
				.AND_WHERE("?Fact lns:hasTerminologyObject <lns:" + Strings.encodeURL(objectName) + ">")
				.AND_WHERE("<lns:" + Strings.encodeURL(objectName) + "> rdf:type ?ObjectType")
				.AND_WHERE("?Fact lns:hasValue ?Value")
				.AND_WHERE("?Fact prov:wasAttributedTo ?Agent")
				.AND_WHERE("?Agent rdf:type ?AgentType");
		TupleQueryResult queryRows = core.sparqlSelect(query.toSparql(core), new Rdf2GoCore.Options(false));
		Iterator<BindingSet> iterator = queryRows.iterator();
		if (!iterator.hasNext()) {
			throw new IllegalArgumentException("No Fact found for object name '" + objectName + "'");
		}
		Fact fact = new Fact();
		fact.terminologyObject = objectName;
		BindingSet queryRow = iterator.next();
		fact.value = toValue(clean(queryRow.getValue("ObjectType").toString()), clean(queryRow.getValue("Value")
				.toString()));
		fact.agent = clean(queryRow.getValue("Agent").toString());
		fact.agentType = clean(queryRow.getValue("AgentType").toString());
		if (iterator.hasNext()) {
//			System.out.println(fact.value + ", " + fact.agent + ", " + fact.agentType);
//			BindingSet next = iterator.next();
//			System.out.println(clean(next.getValue("Value").toString()) + ", " + clean(next.getValue("Agent")
//					.toString()) + ", " + clean(next.getValue("AgentType").toString()));
			//throw new IllegalArgumentException("Multiple Facts found for object name '" + objectName + "'");
		}
		return fact;
	}

	public Collection<Fact> getSourceFacts(String objectName) {
		HashSet<Fact> sources = new HashSet<>();
		getSourceFacts(getFact(objectName), sources);
		return sources;
	}

	private void getSourceFacts(Fact currentFact, Set<Fact> sources) {
		Collection<Fact> predecessors = getPredecessorFacts(currentFact.terminologyObject);
		if (predecessors.isEmpty()) {
			sources.add(currentFact);
			return;
		}
		for (Fact predecessor : predecessors) {
			getSourceFacts(predecessor, sources);
		}
	}

	public static class Fact {

		public String terminologyObject;
		public Value value;
		public String agent;
		public String agentType;

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;

			Fact fact = (Fact) o;

			if (agent != null ? !agent.equals(fact.agent) : fact.agent != null) return false;
			if (agentType != null ? !agentType.equals(fact.agentType) : fact.agentType != null) return false;
			if (terminologyObject != null ? !terminologyObject.equals(fact.terminologyObject) : fact.terminologyObject != null) {
				return false;
			}
			return value != null ? value.equals(fact.value) : fact.value == null;
		}

		@Override
		public int hashCode() {
			int result = terminologyObject != null ? terminologyObject.hashCode() : 0;
			result = 31 * result + (value != null ? value.hashCode() : 0);
			result = 31 * result + (agent != null ? agent.hashCode() : 0);
			result = 31 * result + (agentType != null ? agentType.hashCode() : 0);
			return result;
		}
	}
}
