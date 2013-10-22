/*
 * Copyright (C) 2013 denkbares GmbH
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
package de.knowwe.rdf2go.utils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.ontoware.aifbcommons.collection.ClosableIterator;
import org.ontoware.rdf2go.model.QueryResultTable;
import org.ontoware.rdf2go.model.QueryRow;
import org.ontoware.rdf2go.model.node.Node;
import org.ontoware.rdf2go.model.node.URI;
import org.ontoware.rdf2go.model.node.impl.URIImpl;

import de.knowwe.core.Environment;
import de.knowwe.rdf2go.Rdf2GoCore;

/**
 * 
 * @author jochenreutelshofer
 * @created 01.10.2013
 */
public class HierarchyUtils {

	public static boolean isDirectSubConceptOf(URI concept1, URI concept2, URI hierarchyProperty) {
		String sparql = "ASK { <" + concept1 + "> <" + hierarchyProperty.toString() + "> <"
				+ concept2 + ">.}";
		boolean result = Rdf2GoCore.getInstance().sparqlAsk(sparql);
		return result;
	}

	/**
	 * Returns all direct parent concepts of the passed concept.
	 * 
	 * @created 01.10.2013
	 * @param term
	 * @param hierarchyProperty
	 * @return
	 */
	public static List<URI> getParentConcepts(URI term, URI hierarchyProperty, String master) {
		return selectTripleObjects(term, hierarchyProperty, master);
	}

	/**
	 * Returns all direct parent concepts of the passed concept.
	 * 
	 * @created 01.10.2013
	 * @param term
	 * @param hierarchyProperty
	 * @return
	 */
	public static List<URI> getChildrenConcepts(URI term, URI hierarchyProperty, String master) {
		return selectTripleSubjects(hierarchyProperty, term, master);
	}

	private static List<URI> selectTripleObjects(URI subject, URI predicate, String master) {

		String sparql = "SELECT ?x WHERE { <" + subject.toString() + "> <"
					+ predicate.toString() + "> ?x.}";
		return executeSimpleSelectQuery(sparql, master);
	}

	private static List<URI> selectTripleSubjects(URI predicate, URI object, String master) {
		String sparql = "SELECT ?x WHERE { ?x <" + predicate.toString() + "> <"
					+ object.toString() + ">.}";
		return executeSimpleSelectQuery(sparql, master);
	}

	/**
	 * 
	 * @created 01.10.2013
	 * @param sparql
	 * @return
	 */
	private static List<URI> executeSimpleSelectQuery(String sparql, String master) {
		List<URI> result = new ArrayList<URI>();
		Rdf2GoCore core = null;
		if (master == null || master.trim().length() == 0) {
			core = Rdf2GoCore.getInstance();
		}
		else {
			core = Rdf2GoCore.getInstance(Environment.DEFAULT_WEB, master);
		}
		QueryResultTable resultTable = core.sparqlSelect(sparql);

		ClosableIterator<QueryRow> resultIterator = resultTable.iterator();
		while (resultIterator.hasNext()) {
			QueryRow parentConceptResult = resultIterator.next();
			Node value = parentConceptResult.getValue("x");
			String urlString = value.asURI().toString();
			result.add(new URIImpl(urlString));
		}
		return result;
	}

	/**
	 * Determines whether concept1 is a sub-concept of the second concept
	 * (recursively), i.e., the hierarchical relation is followed transitively.
	 * 
	 * @created 12.04.2013
	 * @param concept1
	 * @param target
	 * @param hierarchyProperty
	 * @return
	 */
	public static boolean isSubConceptOf(URI concept1, URI target, URI hierarchyProperty, String master) {
		return isSubConceptOfRecursive(concept1, target, new HashSet<URI>(), hierarchyProperty,
				master);
	}

	private static boolean isSubConceptOfRecursive(URI concept1, URI target, Set<URI> terms, URI hierarchyProperty, String master) {
		terms.add(concept1);
		String sparql = "SELECT ?x WHERE { <" + concept1 + "> <"
				+ hierarchyProperty.toString() + "> ?x.}";
		Rdf2GoCore core = null;
		if (master == null || master.trim().length() == 0) {
			core = Rdf2GoCore.getInstance();
		}
		else {
			core = Rdf2GoCore.getInstance(Environment.DEFAULT_WEB, master);
		}
		QueryResultTable resultTable = core.sparqlSelect(sparql);

		// direct ask (necessary ?)
		String sparqlAsk = "ASK { <" + concept1 + "> <"
				+ hierarchyProperty.toString() + "> <" + target + ">.}";
		boolean isChild = core.sparqlAsk(sparqlAsk);
		if (isChild) return true;

		ClosableIterator<QueryRow> resultIterator = resultTable.iterator();
		if (!resultIterator.hasNext()) {
			return false;
		}
		while (resultIterator.hasNext()) {
			QueryRow parentConceptResult = resultIterator.next();
			Node value = parentConceptResult.getValue("x");
			URI parent = value.asURI();
			if (parent.equals(target)) {
				// this case also could be (is?) handled by a directs ask query
				return true;
			}
			else {
				// beware of circles in the hierarchy network
				if (!terms.contains(parent)) {
					return isSubConceptOfRecursive(parent, target, terms, hierarchyProperty, master);
				}
			}
		}
		return false;
	}

}
