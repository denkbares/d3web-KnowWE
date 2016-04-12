/*
 * Copyright (C) 2014 denkbares GmbH
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
package de.knowwe.ontology.ci;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.ontoware.aifbcommons.collection.ClosableIterator;
import org.ontoware.rdf2go.model.QueryResultTable;
import org.ontoware.rdf2go.model.QueryRow;
import org.ontoware.rdf2go.model.node.Node;

import de.d3web.testing.AbstractTest;
import de.d3web.testing.Message;
import de.d3web.testing.Message.Type;
import de.knowwe.ontology.compile.OntologyCompiler;
import de.knowwe.rdf2go.Rdf2GoCore;
import de.knowwe.rdf2go.sparql.utils.SparqlQuery;


/**
 * 
 * @author Jochen Reutelshöfer
 * @created 10.01.2014
 */
public class FunctionalPropertyTest extends AbstractTest<OntologyCompiler> {

	@Override
	public Message execute(OntologyCompiler testObject, String[] args, String[]... ignores) throws InterruptedException {
		Rdf2GoCore rdf2GoCore = testObject.getRdf2GoCore();

		String propVariableName = "?funcProp";
		SparqlQuery query = new SparqlQuery().SELECT(propVariableName).WHERE(
				propVariableName + " rdf:type owl:FunctionalProperty");


		List<Node> functionalProperties = new ArrayList<Node>();
		QueryResultTable sparqlSelect = rdf2GoCore.sparqlSelect(query);
		ClosableIterator<QueryRow> iterator = sparqlSelect.iterator();
		while (iterator.hasNext()) {
			QueryRow row = iterator.next();
			Node value = row.getValue(propVariableName.substring(1));
			functionalProperties.add(value);
		}

		Map<Node, Map<Node, Set<Node>>> conflicts = new HashMap<Node, Map<Node, Set<Node>>>();
		for (Node prop : functionalProperties) {
			Map<Node, Set<Node>> conflictsForProp = checkFunctionalProperty(prop, rdf2GoCore);
			if (conflictsForProp.size() > 0) {
				conflicts.put(prop, conflictsForProp);
			}
		}

		if (conflicts.size() > 0) {

			StringBuffer message = new StringBuffer();
			message.append("There are violations for functional properties:\n");
			Set<Node> propConflicts = conflicts.keySet();
			for (Node prop : propConflicts) {
				message.append("\n* " + prop.toString() + ":");
				Map<Node, Set<Node>> map = conflicts.get(prop);
				Set<Node> subjectConflictSet = map.keySet();
				for (Node subject : subjectConflictSet) {
					message.append("\n** Subject: " + subject.toString() + " Objects: ");
					Set<Node> objects = map.get(subject);
					for (Node object : objects) {
						message.append(object.toString() + ", ");
					}
					
					
				}
			
				
			}

			return new Message(Type.FAILURE, message.toString());
		}

		/*
		 * no conflicts, therefore test successful
		 */
		return Message.SUCCESS;
	}

	private Map<Node, Set<Node>> checkFunctionalProperty(Node prop, Rdf2GoCore core) {
		String subjectVariableName = "?subject";
		String objectVariableName = "?object";
		SparqlQuery queryFunctionalPropertyAssertions = new SparqlQuery().SELECT(
				subjectVariableName + " " + objectVariableName).WHERE(
				subjectVariableName + " " + prop.toSPARQL() + " " + objectVariableName);
		QueryResultTable assertions = core.sparqlSelect(queryFunctionalPropertyAssertions);
		ClosableIterator<QueryRow> iterator = assertions.iterator();
		Map<Node, Set<Node>> assertionMap = new HashMap<Node, Set<Node>>();
		while (iterator.hasNext()) {
			QueryRow row = iterator.next();
			Node subject = row.getValue(subjectVariableName.substring(1));
			Node object = row.getValue(objectVariableName.substring(1));
			if (assertionMap.containsKey(subject)) {
				assertionMap.get(subject).add(object);
			}
			else {
				Set<Node> objects = new HashSet<Node>();
				objects.add(object);
				assertionMap.put(subject, objects);
			}
		}
		Map<Node, Set<Node>> conflicts = new HashMap<Node, Set<Node>>();
		Set<Node> keySet = assertionMap.keySet();
		for (Node subject : keySet) {
			Set<Node> conflictSet = checkUnity(assertionMap.get(subject), core);
			if (conflictSet != null && conflictSet.size() > 1) {
				conflicts.put(subject, conflictSet);
			}
		}

		return conflicts;

	}

	/**
	 * Determine a set of object URIs where none is owl:sameAs any other
	 * 
	 * @created 09.01.2014
	 * @param set
	 * @param core
	 * @return
	 */
	private Set<Node> checkUnity(Set<Node> set,Rdf2GoCore core) {
		if (set.size() == 1) {
			// unity asserted ;-)
			return set;
		}
		Set<Node> conflictSet = new HashSet<Node>();
		for (Node newNode : set) {
			boolean isIn = false;
			for (Node unifiedNode : conflictSet) {
				boolean isSame = core.sparqlAsk("ASK {" + newNode.toSPARQL() + " owl:sameAs "
						+ unifiedNode.toSPARQL() + ". }");
				if (isSame) {
					isIn = true;
					break;
				}
				
			}
			if (!isIn) {
				conflictSet.add(newNode);
			}
		}

		return conflictSet;
	}

	@Override
	public Class<OntologyCompiler> getTestObjectClass() {
		return OntologyCompiler.class;
	}

	@Override
	public String getDescription() {
		return "Checks for all existing functional property whether they are indeed used as functional, i.e., for each subject node only one object exists.";
	}

}
