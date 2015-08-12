/*
 * Copyright (C) 2012 Chair of Artificial Intelligence and Applied Informatics
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
package de.knowwe.rdfs.vis;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.ontoware.aifbcommons.collection.ClosableIterator;
import org.ontoware.rdf2go.exception.ModelRuntimeException;
import org.ontoware.rdf2go.model.QueryResultTable;
import org.ontoware.rdf2go.model.QueryRow;
import org.ontoware.rdf2go.model.node.BlankNode;
import org.ontoware.rdf2go.model.node.Node;
import org.ontoware.rdf2go.model.node.URI;
import org.ontoware.rdf2go.model.node.impl.URIImpl;

import de.d3web.strings.Strings;
import de.d3web.utils.Log;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.utils.LinkToTermDefinitionProvider;
import de.knowwe.rdf2go.Rdf2GoCore;
import de.knowwe.rdf2go.utils.Rdf2GoUtils;
import de.knowwe.rdfs.vis.util.Utils;
import de.knowwe.visualization.ConceptNode;
import de.knowwe.visualization.Config;
import de.knowwe.visualization.Edge;
import de.knowwe.visualization.GraphDataBuilder;

/**
 * @author Johanna Latt
 * @created 11.10.2013
 */
public class OntoGraphDataBuilder extends GraphDataBuilder {

	private Rdf2GoCore rdf2GoCore = null;

	private int depth = 0;

	private int height = 0;
	private Set<Node> expandedPredecessors = new HashSet<>();

	private Set<Node> expandedSuccessors = new HashSet<>();
	private Set<Node> literalsExpanded = new HashSet<>();
	private Map<Integer, String> propertyExcludeSPARQLFilterCache = new HashMap<>();
	private Set<Node> fringeNodes = new HashSet<>();

	private String nodeFilterExpression = null;
	/*
	For Debugging/Optimization only
	 */
	private static final boolean DEBUG_MODE = false;
	private int addSuccessorsCalls = 0;
	private int addOutgoingSuccessorsCalls = 0;
	private int addPredecessorsCalls = 0;
	private int addOutgoingPredecessorsCalls = 0;
	private int addOuterConceptCalls = 0;
	private List<OuterConceptCheck> outerConceptCalls = new ArrayList<>();
	private Set<OuterConceptCheck> checkedOuterConcepts = new HashSet<>();
	private List<String> succQueries = new ArrayList<>();
	private List<String> predQueries = new ArrayList<>();

	/**
	 * Allows to create a new Ontology Rendering Core. For each rendering task a new one should be created.
	 *
	 * @param section a section that the graph is rendered for/at
	 * @param config  the configuration, consider the constants of this class
	 */
	public OntoGraphDataBuilder(Section<?> section, Config config, LinkToTermDefinitionProvider uriProvider, Rdf2GoCore rdf2GoCore) {
		if (rdf2GoCore == null) {
			throw new NullPointerException("The RDF repository can't be null!");
		}
		this.rdf2GoCore = rdf2GoCore;

		initialiseData(section, config, uriProvider);

		// filter inverseOf-relations if asked (default is no filter)
		if (!config.isShowInverse()) {
			addInverseRelationsToFilter();
		}

	}

	private void addInverseRelationsToFilter() {

		String exclude;

		// find all inverse Relations
		String query = "SELECT ?x ?z WHERE { ?x owl:inverseOf ?z }";
		ClosableIterator<QueryRow> result =
				rdf2GoCore.sparqlSelectIt(
						query);
		while (result.hasNext()) {
			QueryRow row = result.next();
			Node xURI = row.getValue("x");
			String x = getConceptName(xURI);

			Node zURI = row.getValue("z");
			String z = getConceptName(zURI);

			// find out which relation should be excluded
			boolean isXFiltered = getFilteredRelations().contains(x);
			boolean isZFiltered = getFilteredRelations().contains(z);

			boolean isXExcluded = getExcludedRelations().contains(x);
			boolean isZExcluded = getExcludedRelations().contains(z);

			if (isXFiltered || isZFiltered) {
				if (isXFiltered) {
					exclude = z;
				}
				else {
					exclude = x;
				}
			}
			else if (isXExcluded || isZExcluded) {
				if (isXExcluded) {
					exclude = x;
				}
				else {
					exclude = z;
				}
			}
			else {
				if (x.compareTo(z) < 0) {
					exclude = z;
				}
				else {
					exclude = x;
				}
			}
			config.addExcludeRelations(exclude);
		}

	}

	public String getConceptName(Node uri) {
		return Utils.getConceptName(uri, this.rdf2GoCore);
	}

	@Override
	public void selectGraphData() {

		long before = System.currentTimeMillis();

		List<URI> mainConceptURIs = new ArrayList<>();
		final List<String> mainConcepts = getMainConcepts();
		for (String name : mainConcepts) {
			String concept = name.trim();
			String conceptNameEncoded;

			String url;
			if (concept.contains(":")) {
				url = Rdf2GoUtils.expandNamespace(rdf2GoCore, concept);
			}
			else {
				conceptNameEncoded = Strings.encodeURL(concept);
				url = rdf2GoCore.getLocalNamespace() + conceptNameEncoded;
			}
			URI conceptURI = new URIImpl(url);
			mainConceptURIs.add(conceptURI);
		}

		for (URI conceptURI : mainConceptURIs) {
			// if requested, the predecessor are added to the source
			if (config.getPredecessors() > 0) {
				addPredecessors(conceptURI);
			}
			insertMainConcept(conceptURI);
			// if requested, the successors are added to the source
			if (config.getSuccessors() > 0) {
				addSuccessors(conceptURI, null, null);
			}
			addType(conceptURI);
		}

		//expand edges of fringe nodes
		for (Node fringeNode : fringeNodes) {
			addOutgoingEdgesPredecessors(fringeNode);
			addOutgoingEdgesSuccessors(fringeNode);

			//TODO find solution for blank node
			if (!Utils.isBlankNode(fringeNode)) {
				if (!literalsExpanded.contains(fringeNode)) {
					addLiterals(fringeNode);
				}
				addType(fringeNode);
			}
		}

		SubpropertyEliminator.eliminateSubproperties(data, rdf2GoCore);
		data.clearIsolatedNodesFromDefaultLevel();

		if (DEBUG_MODE) {

			long after = System.currentTimeMillis();

			Log.info("Visualization Stats for: " + Strings.concat(",", getMainConcepts()));
			Log.info("took " + (after - before) + "ms");
			Log.info("addSuccessorCalls: " + addSuccessorsCalls);
			Log.info("addPredecessorCalls: " + addPredecessorsCalls);
			Log.info("addOutgoingSuccessorCalls: " + addOutgoingSuccessorsCalls);
			Log.info("addOutgoingPredecessorCalls: " + addOutgoingPredecessorsCalls);
			Log.info("addOuterConceptCalls: " + addOuterConceptCalls);
			Set<OuterConceptCheck> outerSet = new HashSet<>();
			outerSet.addAll(checkedOuterConcepts);
			Log.info("different outer-concepts: " + outerSet.size());

			Set<String> predQuerySet = new HashSet<>();
			predQuerySet.addAll(predQueries);
			Log.info("number of pred-queries: " + predQueries.size());
			Log.info("number of different pred-queries: " + predQuerySet.size());

			Set<String> succQueriesSet = new HashSet<>();
			succQueriesSet.addAll(succQueries);
			Log.info("number of succ-queries: " + succQueries.size());
			Log.info("number of succ-different queries: " + succQueriesSet.size());

			Set<String> allQueriesSet = new HashSet<>();
			allQueriesSet.addAll(succQueries);
			allQueriesSet.addAll(predQueries);
			Log.info("number of total different queries: " + allQueriesSet.size());
		}
	}

	private void addType(Node node) {
		String query = "SELECT ?class ?pred WHERE { " + node.toSPARQL() + " ?pred ?class . FILTER regex(str(?pred),\"type\") }";
		ClosableIterator<QueryRow> result = null;
		try {
			result = rdf2GoCore.sparqlSelectIt(query);
		}
		catch (ModelRuntimeException exception) {
			Log.severe("invalid query: " + query + " /n" + exception.toString());
		}
		while (result != null && result.hasNext()) {
			QueryRow row = result.next();
			Node yURI = row.getValue("pred");
			Node zURI = row.getValue("class");
			addConcept(node, zURI, yURI);

			// currently we use the first type found
			// TODO: detect (one) most specific type from all types
			break;
		}
	}

	private void addLiterals(Node fringeNode) {
		String query = "SELECT ?literal ?pred WHERE { " + fringeNode.toSPARQL() + " ?pred ?literal . FILTER isLiteral(?literal) }";
		ClosableIterator<QueryRow> result = null;
		try {
			result = rdf2GoCore.sparqlSelectIt(query);
		}
		catch (ModelRuntimeException exception) {
			Log.severe("invalid query: " + query + " /n" + exception.toString());
		}
		while (result != null && result.hasNext()) {
			QueryRow row = result.next();
			Node yURI = row.getValue("pred");
			Node zURI = row.getValue("literal");
			addConcept(fringeNode, zURI, yURI);
		}
	}

	private void insertMainConcept(Node conceptURI) {
		String concept = getConceptName(conceptURI);

		String conceptLabel = Utils.getRDFSLabel(conceptURI.asURI(), rdf2GoCore,
				config.getLanguage());
		if (conceptLabel == null) {
			conceptLabel = concept;
		}
		// the main concept is inserted
		// TODO: use Utils.createNode() to obtain correct coloring of root
		/*ConceptNode conceptNode = new ConceptNode(concept,
				getConceptType(conceptURI.asURI()),
				conceptURI.toString(), conceptLabel, Utils.getStyle(getConceptType(conceptURI)));
		*/
		ConceptNode conceptNode = Utils.createNode(config, rdf2GoCore, uriProvider, section, data, conceptURI, true);
		conceptNode.setRoot(true);
		data.addConcept(conceptNode);

	}

	private void addSuccessors(Node conceptToBeExpanded, Node predecessor, Node predecessorPredicate) {
		addSuccessors(conceptToBeExpanded, predecessor, predecessorPredicate, ExpandMode.Normal, Direction.Forward);
	}

	final String previousBlankNodeSparqlVariableName = "previousBlankNode";

	private void addSuccessors(Node conceptToBeExpanded, Node previousNode, Node previousPredicate, ExpandMode mode, Direction direction) {

		if (Utils.isBlankNode(conceptToBeExpanded) && (previousNode == null || previousPredicate == null)) {
			throw new IllegalArgumentException("case not considered yet!");
		}

		// literals cannot have successors
		if (Utils.isLiteral(conceptToBeExpanded)) return;

		if (mode != ExpandMode.LiteralsOnly) {
			if (expandedSuccessors.contains(conceptToBeExpanded)) {
				// already expanded
				return;
			}
			expandedSuccessors.add(conceptToBeExpanded);
		}

		addSuccessorsCalls++;

		String query;

		if (Utils.isBlankNode(conceptToBeExpanded)) {
			// workaround as blank nodes are not allowed explicitly in sparql query
			if (!Utils.isBlankNode(previousNode)) {
				if (direction == Direction.Forward) {

					query = "SELECT ?y ?z WHERE { " +
							previousNode.toSPARQL() + " " + previousPredicate.toSPARQL() + "[ ?y ?z" + "]" +
							"}";
				}
				else {
					// case: direction == DirectionToBlankNode.Backward
					query = "SELECT ?y ?z WHERE { [ ?y ?z" + "] " + previousPredicate.toSPARQL() + " " + previousNode.toSPARQL() + "}";
				}
			}
			else {
				   /*
				TODO: damn it - how to solve this case?
                 */
				// this solution works but is quite inefficient
				if (direction == Direction.Forward) {
					query = "SELECT ?y ?z ?" + previousBlankNodeSparqlVariableName + " WHERE { ?" + previousBlankNodeSparqlVariableName + " " + previousPredicate
							.toSPARQL() + "[ ?y ?z" + "]" +
							"}";
				}
				else {
					// case: direction == DirectionToBlankNode.Backward
					query = "SELECT ?y ?z ?" + previousBlankNodeSparqlVariableName + " WHERE { [ ?y ?z" + "] " + previousPredicate
							.toSPARQL() + " ?" + previousBlankNodeSparqlVariableName + ". }";
				}
				// like this we only can show the first element of a list for instance
				//return;
			}
		}
		else {
			query = "SELECT ?y ?z WHERE { "
					+ conceptToBeExpanded.toSPARQL()
					+ " ?y ?z. " + predicateFilter(Direction.Forward, "z") + nodeFilter("?z", mode) + "}";
		}
		ClosableIterator<QueryRow> result = null;
		try {
			result = rdf2GoCore.sparqlSelectIt(query);
		}
		catch (ModelRuntimeException exception) {
			Log.severe("invalid query: " + query + " /n" + exception.toString());
		}
		int count = 0;
		while (result != null && result.hasNext()) {
			count++;
			QueryRow row = result.next();
			Node yURI = row.getValue("y");
			String y = getConceptName(yURI);

			Node zURI = row.getValue("z");
			String z = getConceptName(zURI);
			NODE_TYPE nodeType = Utils.getConceptType(zURI, rdf2GoCore);

			// check blank node sequence case
			final Node previousBlankNode = row.getValue(previousBlankNodeSparqlVariableName);
			// TODO what if there are multiple matches for ?previousBlankNodeSparqlVariableName - and not all are blanknodes !?
			if (previousBlankNode != null) {
				// here we check for the right blank node, quit all the others
				if (!Utils.isBlankNode(previousBlankNode)) {
					// is a completely undesired match
					continue;
				}
				if (!previousBlankNode.asBlankNode().toString().equals(previousNode.asBlankNode().toString())) {
					continue;
				}
			}

			if (checkTripleFilters(query, y, z, nodeType, mode)) continue;

			addConcept(conceptToBeExpanded, zURI, yURI);

			depth++;
			if (depth < config.getSuccessors() || nodeType.equals(NODE_TYPE.BLANKNODE)) {
				addSuccessors(zURI, conceptToBeExpanded, yURI);
			}

            /*
			TODO: this should be done _after_ the last concept node has been added to the graph
             */
			if (depth == config.getSuccessors()) {
				if (!nodeType.equals(NODE_TYPE.LITERAL)) {
					fringeNodes.add(zURI);
				}
				//addOutgoingEdgesSuccessors(zURI);
				//addOutgoingEdgesPredecessors(zURI);
				//if (!literalsExpanded.contains(zURI)) {
				// add literals
				//    addSuccessors(zURI, conceptToBeExpanded, yURI, ExpandMode.LiteralsOnly, Direction.Forward);
				//}
			}

			depth--;
		}
		if (DEBUG_MODE) {
			if (succQueries.contains(query)) {
				Log.warning("Query was already processed in succ:" + query);
			}
			succQueries.add(query);
			if (count > 20) {
				Log.warning("Large expansion query: " + query);
			}
		}
	}

	private void addPredecessors(Node conceptToBeExpanded) {
		addPredecessors(conceptToBeExpanded, null, null, null);
	}

	private void addPredecessors(Node conceptToBeExpanded, Node previousNode, Node previousPredicate, Direction direction) {
		if (Utils.isBlankNode(conceptToBeExpanded) && (previousNode == null || previousPredicate == null || direction == null)) {
			throw new IllegalArgumentException("case not considered yet!");
		}

		if (expandedPredecessors.contains(conceptToBeExpanded)) {
			// already expanded
			return;
		}

		addPredecessorsCalls++;

		expandedPredecessors.add(conceptToBeExpanded);

		String query;
		if (Utils.isBlankNode(conceptToBeExpanded)) {
			// workaround as blank nodes are not allowed explicitly in sparql query
			if (!Utils.isBlankNode(previousNode)) {
				// TODO: consider direction to blank node
				query = "SELECT ?x ?y WHERE { ?bNode " + previousPredicate.toSPARQL() + " " + previousNode.toSPARQL() + "." +
						"?x ?y ?bNode." +
						"}";
			}
			else {
				/*
				TODO: damn it - how to solve this case?
                 */

				// this works but is quite inefficient
				query = "SELECT ?x ?y ?" + previousBlankNodeSparqlVariableName + " WHERE { ?bNode " + previousPredicate.toSPARQL() + " ?" + previousBlankNodeSparqlVariableName + "." +
						"?x ?y ?bNode." +
						"}";

				// like this we only can show the first element of a list for instance
				//return;
			}
		}
		else {
			query = "SELECT ?x ?y WHERE { ?x ?y "
					+ conceptToBeExpanded.toSPARQL() + " . " + predicateFilter(Direction.Backward, null) + nodeFilter("?x", ExpandMode.Normal) + "}";
		}
		ClosableIterator<QueryRow> result = rdf2GoCore.sparqlSelectIt(query);
		int count = 0;
		while (result.hasNext()) {
			count++;
			QueryRow row = result.next();
			Node xURI = row.getValue("x");
			String x = getConceptName(xURI);
			NODE_TYPE nodeType = Utils.getConceptType(xURI, rdf2GoCore);

			Node yURI = row.getValue("y");
			String y = getConceptName(yURI);

			// check blank node sequence case
			final Node previousBlankNode = row.getValue(previousBlankNodeSparqlVariableName);
			if (previousBlankNode != null) {
				// here we check for the right blank node, quit all the others

				if ((!(previousBlankNode instanceof BlankNode)) || !previousBlankNode.asBlankNode()
						.toString()
						.equals(previousNode.asBlankNode().toString())) {
					continue;
				}
			}

			if (checkTripleFilters(query, y, x, nodeType)) continue;

			height++;
			if (height < config.getPredecessors() || nodeType.equals(NODE_TYPE.BLANKNODE)) {
				addPredecessors(xURI, conceptToBeExpanded, yURI, Direction.Backward);
				if (!literalsExpanded.contains(xURI)) {
					// add literals for x
					addSuccessors(xURI, conceptToBeExpanded, yURI, ExpandMode.LiteralsOnly, Direction.Backward);
				}

			}

            /*
            TODO: this should be done _after_ the last concept node has been added to the graph
             */
			if (height == config.getPredecessors()) {
				if (!nodeType.equals(NODE_TYPE.LITERAL)) {
					fringeNodes.add(xURI);
				}
				//addOutgoingEdgesPredecessors(xURI);
				//addOutgoingEdgesSuccessors(xURI);
				//if (!literalsExpanded.contains(xURI)) {
				//    addSuccessors(xURI, conceptToBeExpanded, yURI, ExpandMode.LiteralsOnly, Direction.Backward);
				//}
			}

			height--;

			addConcept(xURI, conceptToBeExpanded, yURI);
		}
		if (DEBUG_MODE) {
			predQueries.add(query);
			if (count > 20) {
				Log.warning("Large expansion query: " + query);
			}
		}
	}

	/**
	 * Expands a 'fringe' node (outgoing edges).
	 * - no recursion
	 * - no new nodes are added to visualization (except for indicating existence of outgoing edges)
	 * - adds all/new edges between this node and already existing nodes
	 */
	private void addOutgoingEdgesSuccessors(Node conceptURI) {
		if (Utils.isLiteral(conceptURI)) return;
        /*
        TODO: handle outgoing edges to blank nodes !
         */
		if (Utils.isBlankNode(conceptURI)) return;

		String conceptFilter = "Filter(true)";
		if (!config.isShowOutgoingEdges()) {
			// this filter brings considerable performance boost
			// but will dismiss outgoing edges
			conceptFilter = conceptFilter("?z", data.getConceptDeclarations());
		}

		addOutgoingSuccessorsCalls++;

		String query = "SELECT ?y ?z WHERE { "
				+ conceptURI.toSPARQL()
				+ " ?y ?z. " + predicateFilter(Direction.Forward, "z") + " " + conceptFilter + "}";
		ClosableIterator<QueryRow> result =
				rdf2GoCore.sparqlSelectIt(
						query);
		int count = 0;
		while (result.hasNext()) {
			count++;
			QueryRow row = result.next();
			Node yURI = row.getValue("y");
			String y = getConceptName(yURI);

			Node zURI = row.getValue("z");
			String z = getConceptName(zURI);
			NODE_TYPE nodeType = Utils.getConceptType(zURI, rdf2GoCore);

			final OuterConceptCheck check = new OuterConceptCheck(conceptURI, zURI, yURI, false);

			if (checkTripleFilters(query, y, z, nodeType)) continue;

			outerConceptCalls.add(check);
			if (!checkedOuterConcepts.contains(check)) {
				addOuterConcept(conceptURI, zURI, yURI, false);
			}
		}

		if (DEBUG_MODE) {
			if (count > 20) {
				Log.warning("Large expansion query: " + query);
			}
		}
	}

	/**
	 * Expands a 'fringe' node (ingoing edges).
	 * - no recursion
	 * - no new nodes are added to visualization (except for indicating existence of outgoing edges)
	 * - adds all/new edges between this node and already existing nodes
	 */
	private void addOutgoingEdgesPredecessors(Node conceptURI) {
		if (Utils.isLiteral(conceptURI)) return;
         /*
        TODO: handle outgoing edges to blank nodes !
         */
		if (Utils.isBlankNode(conceptURI)) return;

		addOutgoingPredecessorsCalls++;

		final Collection<ConceptNode> conceptDeclarations = data.getConceptDeclarations();

		String conceptFilter = "Filter(true)";
		if (!config.isShowOutgoingEdges()) {
			// this filter brings considerable performance boost
			// but will dismiss outgoing edges
			conceptFilter = conceptFilter("?x", conceptDeclarations);
		}

		String query = "SELECT ?x ?y WHERE { ?x ?y "
				+ conceptURI.toSPARQL()
				+ " . " + predicateFilter(Direction.Backward, null) + " " + conceptFilter + "}";
		QueryResultTable resultTable = rdf2GoCore.sparqlSelect(
				query);

		ClosableIterator<QueryRow> result = resultTable.iterator();
		int count = 0;
		while (result.hasNext()) {
			count++;
			QueryRow row = result.next();
			Node xURI = row.getValue("x");
			String x = getConceptName(xURI);
			NODE_TYPE nodeType = Utils.getConceptType(xURI, rdf2GoCore);

			Node yURI = row.getValue("y");
			String y = getConceptName(yURI);
			final OuterConceptCheck check = new OuterConceptCheck(xURI, conceptURI, yURI, true);

			if (checkTripleFilters(query, y, x, nodeType)) continue;

			outerConceptCalls.add(check);
			if (!checkedOuterConcepts.contains(check)) {
				addOuterConcept(xURI, conceptURI, yURI, true);
			}
		}
		if (DEBUG_MODE) {
			if (count > 20) {
				Log.warning("Large expansion query: " + query);
			}
		}
	}

	private String conceptFilter(String variable, Collection<ConceptNode> conceptDeclarations) {
		StringBuilder filter = new StringBuilder();
		filter.append("FILTER (");
		if (conceptDeclarations.size() == 0) {
			filter.append("true");
		}
		else {
			final Iterator<ConceptNode> iterator = conceptDeclarations.iterator();
			boolean firstIteration = true;
			while (iterator.hasNext()) {
				ConceptNode conceptDeclaration = iterator.next();
				if (conceptDeclaration.getType().equals(NODE_TYPE.LITERAL)) {
					continue;
				}
				if (conceptDeclaration.getType().equals(NODE_TYPE.BLANKNODE)) {
					// TODO: find solution for this case
					continue;
				}
				if (firstIteration) {
					firstIteration = false;
				}
				else {
					filter.append(" || ");
				}
				String concept = conceptDeclaration.getName();
				if (concept.matches("https?://.+")) {
					concept = "<" + concept + ">";
				}
				filter.append(variable).append(" = ").append(concept);
			}

		}

		filter.append(")");
		return filter.toString();
	}

	private boolean checkTripleFilters(String query, String y, String z, NODE_TYPE nodeType) {
		return checkTripleFilters(query, y, z, nodeType, ExpandMode.Normal);
	}

	private String predicateFilter(Direction dir, String objectVar) {
		// Filter expression is cached
		final int filterHashcode = dir.hashCode() + (objectVar == null ? 0 : objectVar.hashCode());
		if (propertyExcludeSPARQLFilterCache.get(filterHashcode) != null) {
			return propertyExcludeSPARQLFilterCache.get(filterHashcode);
		}

		if (getFilteredRelations().size() > 0) {
			// we are in white list mode, i.e. show only "..."
			this.propertyExcludeSPARQLFilterCache.put(filterHashcode, createExclusiveFilter(dir, objectVar));
			return propertyExcludeSPARQLFilterCache.get(filterHashcode);
		}
		else {
			// we are in black list mode, i.e. show all but "..."
			if (getExcludedRelations().size() == 0) {
				this.propertyExcludeSPARQLFilterCache.put(filterHashcode, "FILTER (true)");
				return propertyExcludeSPARQLFilterCache.get(filterHashcode);
			}
			this.propertyExcludeSPARQLFilterCache.put(filterHashcode, createExcludeFilter());
			return propertyExcludeSPARQLFilterCache.get(filterHashcode);

		}

	}

	private String createExclusiveFilter(Direction dir, String objectVariable) {
		StringBuilder filterExp = new StringBuilder();

		filterExp.append("FILTER (");

		Iterator<String> iter = getFilteredRelations().iterator();
		List<String> filterRelations = new LinkedList<>();
		while (iter.hasNext()) {
			String relation = iter.next();
			String namespace = Rdf2GoUtils.parseKnownNamespacePrefix(rdf2GoCore, relation);
			if (namespace != null) {
				filterRelations.add(relation);
			}
		}

		iter = filterRelations.iterator();
		while (iter.hasNext()) {
			filterExp.append(" ?y = ").append(iter.next());
			if (iter.hasNext()) {
				filterExp.append(" || ");
			}
		}
		String insertDataTypeException = "";
		if (dir == Direction.Forward) {
			insertDataTypeException = "|| isLiteral(?" + objectVariable + ") ";
		}

		filterExp.append(" ").append(insertDataTypeException).append("  ). ");

		return filterExp.toString();
	}

	private String createExcludeFilter() {
		StringBuilder filterExp = new StringBuilder();

		filterExp.append("FILTER (");

		Iterator<String> iter = getExcludedRelations().iterator();
		List<String> excludesWithExistingNamespace = new LinkedList<>();
		while (iter.hasNext()) {
			String relation = iter.next();
			String namespace = Rdf2GoUtils.parseKnownNamespacePrefix(rdf2GoCore, relation);
			if (relation.startsWith("onto") || namespace != null) {
				excludesWithExistingNamespace.add(relation);
			}
		}

		iter = excludesWithExistingNamespace.iterator();
		while (iter.hasNext()) {
			filterExp.append(" ?y != ").append(iter.next());
			if (iter.hasNext()) {
				filterExp.append(" && ");
			}
		}
		filterExp.append("). ");

		return filterExp.toString();
	}

	private String nodeFilter(String variable, ExpandMode mode) {
		if (mode.equals(ExpandMode.LiteralsOnly)) {
			return " FILTER isLiteral(" + variable + ")";
		}
		return " FILTER (true).";


/*
        if(nodeFilterExpression != null) {
            return nodeFilterExpression;
        }
        if(getExcludedNodes().size() == 0) {
            nodeFilterExpression = " FILTER (true).";
            return nodeFilterExpression;
        }
        StringBuffer filterExp = new StringBuffer();

        filterExp.append("FILTER (");

        Iterator<String> iter = getExcludedNodes().iterator();
        while(iter.hasNext()) {
            filterExp.append(" "+variable+" != "+iter.next());
            if(iter.hasNext()) {
                filterExp.append( " && ");
            }
        }
        filterExp.append("). ");

        this.nodeFilterExpression = filterExp.toString();
        return nodeFilterExpression;
*/
	}

	private boolean checkTripleFilters(String query, String y, String z, NODE_TYPE nodeType, ExpandMode mode) {
		if (y == null) {
			Log.severe("Variable y of query was null: " + query);
			return true;
		}
		if (z == null) {
			Log.severe("Variable z of query was null: " + query);
			return true;
		}
		if (excludedRelation(y)) {
			// this filter is already contained in the sparql query
			return true;
		}
		if (excludedNode(z)) {
			return true;
		}

		if (nodeType == NODE_TYPE.CLASS && !config.isShowClasses()) {
			return true;
		}
		else if (nodeType == NODE_TYPE.PROPERTY && !config.isShowClasses()) {
			return true;
		}

		// literals only mode for expansion of fringe nodes
		if (mode == ExpandMode.LiteralsOnly) {
			return !(nodeType.equals(NODE_TYPE.LITERAL) || isTypeRelation(y));
		}

		if (nodeType.equals(NODE_TYPE.LITERAL) || isTypeRelation(y)) {
			// only literals and type assertions are not filtered out
			return false;
		}

		//noinspection SimplifiableIfStatement
		if (nodeType.equals(NODE_TYPE.LITERAL) || isTypeRelation(y)) {
			// only literals and type assertions are not filtered out
			return false;
		}

		return isWhiteListMode() && !(filteredRelation(y));

	}

	private boolean isWhiteListMode() {
		return getFilteredRelations().size() > 0;
	}

	private void addConcept(Node fromURI, Node toURI, Node relationURI) {
		String relation = getConceptName(relationURI);

        /*
        cluster change
        */
		String clazz = null;
		if (isTypeRelation(relation)) {
            /*
            no matter what class this type relation goes to, we look for a representative/meaningful class-uri to display
             */
			try {
				final URI uri = fromURI.asURI();
				final URI mostSpecificClass = Rdf2GoUtils.findMostSpecificClass(rdf2GoCore, uri);
				clazz = null;
				if (mostSpecificClass != null) {
					clazz = getConceptName(mostSpecificClass);
				}
			}
			catch (ClassCastException e) {
				// is not an URI but a BNode probably
			}
		}

		ConceptNode toNode;
		ConceptNode fromNode;

		toNode = Utils.createNode(this.getParameterMap(), this.rdf2GoCore, this.uriProvider, this.section, this.data, toURI, true);

		toNode.setOuter(false);

		fromNode = Utils.createNode(config, this.rdf2GoCore, this.uriProvider, this.section, this.data, fromURI, true, clazz);

		if (fromNode != null) fromNode.setOuter(false);

		// look for label for the property
		String relationLabel = null;

		if (!config.isShowLabels()) {
			relationLabel = Utils.getRDFSLabel(relationURI.asURI(), rdf2GoCore, config.getLanguage());
			if (relationLabel != null && relationLabel.charAt(relationLabel.length() - 3) == '@') {
				// do not show language tag of relation labels
				relationLabel = relationLabel.substring(0, relationLabel.length() - 3);
			}
		}

		if (relationLabel != null) {
			relation = relationLabel;
		}

		if (Strings.isBlank(clazz)) {
			// classes are rendered as cluster labels - so no extra edge is required
			Edge edge = new Edge(fromNode, relation, toNode);
			addEdge(edge);
		}

	}

	private boolean isTypeRelation(String relation) {
		return relation.length() > 4 && relation.substring(relation.length() - 4).equalsIgnoreCase("type");
	}

	class OuterConceptCheck {
		private Node fromURI;
		private Node toURI;
		private Node relationURI;
		private boolean predecessor;

		OuterConceptCheck(Node fromURI, Node toURI, Node relationURI, boolean predecessor) {
			this.fromURI = fromURI;
			this.toURI = toURI;
			this.relationURI = relationURI;
			this.predecessor = predecessor;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;

			OuterConceptCheck that = (OuterConceptCheck) o;

			if (predecessor != that.predecessor) return false;
			if (!fromURI.equals(that.fromURI)) return false;
			//noinspection SimplifiableIfStatement
			if (!relationURI.equals(that.relationURI)) return false;
			return toURI.equals(that.toURI);

		}

		@Override
		public int hashCode() {
			int result = fromURI.hashCode();
			result = 31 * result + toURI.hashCode();
			result = 31 * result + relationURI.hashCode();
			result = 31 * result + (predecessor ? 1 : 0);
			return result;
		}

		@Override
		public String toString() {
			return fromURI + " " + relationURI + " " + toURI.toString() + " (forward: " + predecessor + ")";
		}
	}

	/**
	 * Adds a nodes expanded by a fringe node.
	 * <p>
	 * - if the node is not part of the visualization yet it is not added (except as outer node for indicating the
	 * existence of further edges)
	 * - EXCEPT for datatype property edges which are always added to the visualization
	 * - if the node is already part of the visualization the respective edge is added
	 */
	private void addOuterConcept(Node fromURI, Node toURI, Node relationURI, boolean predecessor) {
		String to = getConceptName(toURI);
		String relation = getConceptName(relationURI);

		// TODO: implement rendering of literal nodes
		if (to == null) {
			return;
		}

		addOuterConceptCalls++;


		/*
        cluster change
		 */
		String clazz = null;
		if (isTypeRelation(relation)) {
			clazz = getConceptName(toURI);
		}

		ConceptNode toNode = Utils.createNode(this.getParameterMap(), this.rdf2GoCore, this.uriProvider, this.section, this.data, toURI, false);

		ConceptNode fromNode = Utils.createNode(this.getParameterMap(), this.rdf2GoCore, this.uriProvider, this.section, this.data, fromURI, false, clazz);

		ConceptNode current;
		if (predecessor) {
			// from is current new one
			current = fromNode;
		}
		else {
			// to is current new one
			current = toNode;
		}

		this.checkedOuterConcepts.add(new OuterConceptCheck(fromURI, toURI, relationURI, predecessor));

		boolean nodeIsNew = !data.getConceptDeclarations().contains(current);

		Edge edge = new Edge(fromNode, relation, toNode);

		boolean edgeIsNew = !data.getAllEdges().contains(edge);

		if (config.isShowOutgoingEdges()) {
            /*
            show outgoing-edges currently not working as for these cases this method is not called
            for efficiency reasons.
            Currently only missing "internal links" are created by this method.
             */
			if (nodeIsNew) {
				if (predecessor) {
					// from is current new one
					fromNode.setOuter(true);
					data.addConcept(fromNode);
				}
				else {
					// to is current new one
					toNode.setOuter(true);
					data.addConcept(toNode);
				}
			}
			if (edgeIsNew) {
				addEdge(edge);
			}
		}
		else {
			// do not show outgoing edges
			if (!nodeIsNew) {
				// but show if its node is internal one already, i.e. node would exist even without this edge
				if (!isTypeRelation(relation)) { // cluster change
					addEdge(edge);
				}

			}
			else {
				// exception for labels:
				// labels are shown for rim concepts even if out of scope in principle
				if (isLiteralEdge(edge)) {
					final ConceptNode label = edge.getObject();
					data.addConcept(label);
					addEdge(edge);
				}
			}
		}
	}

	/**
	 * adds an edge to the graph data object in the following way: if the edge is a label it will be added to a cluster
	 * for the subject node. The edge will be added at default level (no cluster) otherwise
	 */
	private void addEdge(Edge edge) {
		if (isLiteralEdge(edge)) {
			// this is a label edge
			data.addEdgeToCluster(edge.getSubject(), edge);
		}
		else {
			data.addEdge(edge);
		}
	}

	private boolean isLiteralEdge(Edge edge) {
		return edge.getObject().getType().equals(NODE_TYPE.LITERAL);
	}

	enum ExpandMode {Normal, LiteralsOnly}

	enum Direction {Forward, Backward}
}
