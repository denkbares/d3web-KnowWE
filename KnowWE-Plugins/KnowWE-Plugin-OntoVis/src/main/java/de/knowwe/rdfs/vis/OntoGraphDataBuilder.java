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
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.rdf4j.model.BNode;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.query.BindingSet;

import com.denkbares.collections.DefaultMultiMap;
import com.denkbares.collections.MultiMap;
import com.denkbares.semanticcore.TupleQueryResult;
import com.denkbares.semanticcore.utils.Sparqls;
import com.denkbares.semanticcore.utils.Text;
import com.denkbares.strings.Locales;
import com.denkbares.strings.Strings;
import com.denkbares.utils.Log;
import com.denkbares.utils.Stopwatch;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.utils.LinkToTermDefinitionProvider;
import de.knowwe.rdf2go.Rdf2GoCore;
import de.knowwe.rdf2go.utils.Rdf2GoUtils;
import de.knowwe.rdfs.vis.util.Utils;
import de.knowwe.visualization.ConceptNode;
import de.knowwe.visualization.Config;
import de.knowwe.visualization.Edge;
import de.knowwe.visualization.GraphDataBuilder;
import de.knowwe.visualization.SubGraphData;
import de.knowwe.visualization.dot.DOTVisualizationRenderer;

/**
 * @author Johanna Latt
 * @created 11.10.2013
 */
public class OntoGraphDataBuilder extends GraphDataBuilder {

	/*
	For Debugging/Optimization only
	 */
	private static final boolean DEBUG_MODE = false;
	final String previousBlankValueSparqlVariableName = "previousBlankValue";
	private final Set<Value> expandedPredecessors = new HashSet<>();
	private final Set<Value> expandedSuccessors = new HashSet<>();
	private final Set<Value> literalsExpanded = new HashSet<>();
	private final Map<Integer, String> propertyExcludeSPARQLFilterCache = new HashMap<>();
	private final Set<Value> fringeValues = new HashSet<>();
	private final List<OuterConceptCheck> outerConceptCalls = new ArrayList<>();
	private final Set<OuterConceptCheck> checkedOuterConcepts = new HashSet<>();
	private final List<String> succQueries = new ArrayList<>();
	private final List<String> predQueries = new ArrayList<>();
	private final Rdf2GoCore rdf2GoCore;
	private int depth = 0;
	private int height = 0;
	private int addSuccessorsCalls = 0;
	private int addOutgoingSuccessorsCalls = 0;
	private int addPredecessorsCalls = 0;
	private int addOutgoingPredecessorsCalls = 0;
	private int addOuterConceptCalls = 0;

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
	}

	@Override
	public void initialiseData(Section<?> section, Config config, LinkToTermDefinitionProvider uriProvider) {
		this.uriProvider = uriProvider;
		this.config = config;
		this.section = section;

		MultiMap<String, String> subPropertiesMap = Utils.getSubPropertyMap(rdf2GoCore);
		MultiMap<String, String> inverseRelationsMap = Utils.getInverseRelationsMap(rdf2GoCore);

		data = new SubGraphData(subPropertiesMap, inverseRelationsMap);
		graphRenderer = new DOTVisualizationRenderer(data, config);
	}

	public String getConceptName(Value uri) {
		return Utils.getConceptName(uri, this.rdf2GoCore);
	}

	@Override
	public void selectGraphData(long timeOutMillis) {

		Stopwatch stopwatch = new Stopwatch();

		List<IRI> mainConceptIRIs = new ArrayList<>();
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
			IRI conceptIRI = Rdf2GoUtils.getValueFactory().createIRI(url);
			mainConceptIRIs.add(conceptIRI);

			if (isTimeOut(stopwatch, timeOutMillis)) return;
		}

		for (IRI conceptIRI : mainConceptIRIs) {
			// if requested, the predecessor are added to the source
			if (config.getPredecessors() > 0) {
				addPredecessors(conceptIRI);
			}
			insertMainConcept(conceptIRI);
			// if requested, the successors are added to the source
			if (config.getSuccessors() > 0) {
				addSuccessors(conceptIRI, null, null);
			}
			addType(conceptIRI);

			if (isTimeOut(stopwatch, timeOutMillis)) return;
		}

		//expand edges of fringe nodes
		for (Value fringeValue : fringeValues) {
			addOutgoingEdgesPredecessors(fringeValue);
			addOutgoingEdgesSuccessors(fringeValue);

			//TODO find solution for blank node
			if (!Utils.isBlankNode(fringeValue)) {
				if (!literalsExpanded.contains(fringeValue)) {
					if (config.getLiteralMode() != Config.LiteralMode.OFF) {
						addLiterals(fringeValue);
					}
				}
				addType(fringeValue);
			}

			if (isTimeOut(stopwatch, timeOutMillis)) return;
		}

		data.clearIsolatedNodesFromDefaultLevel();

		if (DEBUG_MODE) {

			long after = System.currentTimeMillis();

			Log.info("Visualization Stats for: " + Strings.concat(",", getMainConcepts()));
			Log.info("took " + stopwatch.getDisplay());
			Log.info("addSuccessorCalls: " + addSuccessorsCalls);
			Log.info("addPredecessorCalls: " + addPredecessorsCalls);
			Log.info("addOutgoingSuccessorCalls: " + addOutgoingSuccessorsCalls);
			Log.info("addOutgoingPredecessorCalls: " + addOutgoingPredecessorsCalls);
			Log.info("addOuterConceptCalls: " + addOuterConceptCalls);
			Set<OuterConceptCheck> outerSet = new HashSet<>(checkedOuterConcepts);
			Log.info("different outer-concepts: " + outerSet.size());

			Set<String> predQuerySet = new HashSet<>(predQueries);
			Log.info("number of pred-queries: " + predQueries.size());
			Log.info("number of different pred-queries: " + predQuerySet.size());

			Set<String> succQueriesSet = new HashSet<>(succQueries);
			Log.info("number of succ-queries: " + succQueries.size());
			Log.info("number of succ-different queries: " + succQueriesSet.size());

			Set<String> allQueriesSet = new HashSet<>();
			allQueriesSet.addAll(succQueries);
			allQueriesSet.addAll(predQueries);
			Log.info("number of total different queries: " + allQueriesSet.size());
		}
	}

	private boolean isTimeOut(Stopwatch stopwatch, long timeOutMillis) {
		boolean timedOut = stopwatch.getTime() > timeOutMillis;
		if (timedOut) {
			this.isTimeOut = true;
		}
		return timedOut;
	}

	public boolean isTimeOut() {
		return isTimeOut;
	}

	private void addType(Value node) {
		String query = "SELECT ?class ?pred WHERE { <" + node.stringValue() + "> ?pred ?class . FILTER regex(str(?pred),\"type\") }";
		for (BindingSet row : rdf2GoCore.sparqlSelect(query).getBindingSets()) {
			Value yIRI = row.getValue("pred");
			Value zIRI = row.getValue("class");
			addConcept(node, zIRI, yIRI);

			// currently we use the first type found
			// TODO: detect (one) most specific type from all types
			break;
		}
	}

	private void addLiterals(Value fringeNode) {
		if (fringeNode instanceof BNode) return;
		String propertyFilter = predicateFilter(Direction.Forward, "literal");
		String query = "SELECT ?literal ?y WHERE { <" + fringeNode.stringValue() + "> ?y ?literal . FILTER isLiteral(?literal) . " + propertyFilter + " }";
		Iterator<BindingSet> result = rdf2GoCore.sparqlSelect(query).getBindingSets().iterator();

		Map<Value, Map<Locale, Value>> literalsMap = new HashMap<>();

		while (result.hasNext()) {
			BindingSet row = result.next();
			Value predIRI = row.getValue("y");
			Value value = row.getValue("literal");
			Locale locale = Locale.ROOT;
			if (value instanceof Literal) {
				locale = ((Literal) value).getLanguage().map(Strings::parseLocale).orElse(Locale.ROOT);
			}
			literalsMap.computeIfAbsent(predIRI, k -> new HashMap<>()).put(locale, value);
		}
		List<Locale> preferred = Arrays.asList(getConfig().getLanguages());
		for (Map.Entry<Value, Map<Locale, Value>> entry : literalsMap.entrySet()) {
			Locale bestLocale = Locales.findBestLocale(preferred, entry.getValue().keySet());
			addConcept(fringeNode, entry.getValue().get(bestLocale), entry.getKey());
		}

	}

	private void insertMainConcept(Value conceptIRI) {
		ConceptNode conceptValue = Utils.createValue(config, rdf2GoCore, uriProvider, section, data, conceptIRI, true);
		conceptValue.setRoot(true);
		data.addConcept(conceptValue);
	}

	private void addSuccessors(Value conceptToBeExpanded, Value predecessor, Value predecessorPredicate) {
		addSuccessors(conceptToBeExpanded, predecessor, predecessorPredicate, ExpandMode.Normal, Direction.Forward);
	}

	private void addSuccessors(Value conceptToBeExpanded, Value previousValue, Value previousPredicate, ExpandMode mode, Direction direction) {

		if (Utils.isBlankNode(conceptToBeExpanded) && (previousValue == null || previousPredicate == null)) {
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
			if (!Utils.isBlankNode(previousValue)) {
				if (direction == Direction.Forward) {

					query = "SELECT ?y ?z WHERE { <" +
							previousValue.stringValue() + "> <" + previousPredicate.stringValue() + ">[ ?y ?z" + "]" +
							"}";
				}
				else {
					// case: direction == DirectionToBlankValue.Backward
					query = "SELECT ?y ?z WHERE { [ ?y ?z" + "] <" + previousPredicate.stringValue() + "> <" + previousValue
							.stringValue() + ">}";
				}
			}
			else {
				   /*
				TODO: damn it - how to solve this case?
                 */
				// this solution works but is quite inefficient
				if (direction == Direction.Forward) {
					query = "SELECT ?y ?z ?" + previousBlankValueSparqlVariableName
							+ " WHERE { ?" + previousBlankValueSparqlVariableName + " <" + previousPredicate.stringValue() + ">[ ?y ?z" + "]" +
							"}";
				}
				else {
					// case: direction == DirectionToBlankValue.Backward
					query = "SELECT ?y ?z ?" + previousBlankValueSparqlVariableName + " WHERE { [ ?y ?z" + "] <"
							+ previousPredicate.stringValue() + "> ?" + previousBlankValueSparqlVariableName + ". }";
				}
				// like this we only can show the first element of a list for instance
				//return;
			}
		}
		else {
			query = "SELECT ?y ?z WHERE { <"
					+ conceptToBeExpanded.stringValue()
					+ "> ?y ?z. " + predicateFilter(Direction.Forward, "z") + nodeFilter("?z", mode) + "}";
		}
		Iterator<BindingSet> result = rdf2GoCore.sparqlSelect(query).getBindingSets().iterator();
		int count = 0;
		while (result != null && result.hasNext()) {
			count++;
			BindingSet row = result.next();
			Value yIRI = row.getValue("y");
			String y = getConceptName(yIRI);

			Value zIRI = row.getValue("z");
			String z = getConceptName(zIRI);
			NODE_TYPE nodeType = Utils.getConceptType(zIRI, rdf2GoCore);

			// check blank node sequence case
			final Value previousBlankValue = row.getValue(previousBlankValueSparqlVariableName);
			// TODO what if there are multiple matches for ?previousBlankValueSparqlVariableName - and not all are blanknodes !?
			if (previousBlankValue != null) {
				// here we check for the right blank node, quit all the others
				if (!Utils.isBlankNode(previousBlankValue)) {
					// is a completely undesired match
					continue;
				}
				if (!previousBlankValue.stringValue().equals(previousValue.stringValue())) {
					continue;
				}
			}

			if (nodeType == NODE_TYPE.LITERAL) {
				// literals are handled separately after this loop
				continue;
			}

			if (checkTripleFilters(query, y, z, nodeType, mode)) continue;

			addConcept(conceptToBeExpanded, zIRI, yIRI);

			depth++;
			if (depth < config.getSuccessors() || nodeType == NODE_TYPE.BLANKNODE) {
				addSuccessors(zIRI, conceptToBeExpanded, yIRI);
			}

            /*
			TODO: this should be done _after_ the last concept node has been added to the graph
             */
			if (depth == config.getSuccessors()) {
				fringeValues.add(zIRI);
				//addOutgoingEdgesSuccessors(zIRI);
				//addOutgoingEdgesPredecessors(zIRI);
				//if (!literalsExpanded.contains(zIRI)) {
				// add literals
				//    addSuccessors(zIRI, conceptToBeExpanded, yIRI, ExpandMode.LiteralsOnly, Direction.Forward);
				//}
			}

			depth--;
		}

		// finally add the literals
		addLiterals(conceptToBeExpanded);

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

	private void addPredecessors(Value conceptToBeExpanded) {
		addPredecessors(conceptToBeExpanded, null, null, null);
	}

	private void addPredecessors(Value conceptToBeExpanded, Value previousValue, Value previousPredicate, Direction direction) {
		if (Utils.isBlankNode(conceptToBeExpanded) && (previousValue == null || previousPredicate == null || direction == null)) {
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
			if (!Utils.isBlankNode(previousValue)) {
				// TODO: consider direction to blank node
				query = "SELECT ?x ?y WHERE { ?bValue <" + previousPredicate.stringValue() + "> <" + previousValue.stringValue() + ">." +
						"?x ?y ?bValue." +
						"}";
			}
			else {
				/*
				TODO: damn it - how to solve this case?
                 */

				// this works but is quite inefficient
				query = "SELECT ?x ?y ?" + previousBlankValueSparqlVariableName + " WHERE { ?bValue <"
						+ previousPredicate.stringValue() + "> ?" + previousBlankValueSparqlVariableName + "." +
						"?x ?y ?bValue." +
						"}";

				// like this we only can show the first element of a list for instance
				//return;
			}
		}
		else {
			query = "SELECT ?x ?y WHERE { ?x ?y <"
					+ conceptToBeExpanded.stringValue() + "> . " + predicateFilter(Direction.Backward, null) + nodeFilter("?x", ExpandMode.Normal) + "}";
		}
		Iterator<BindingSet> result = rdf2GoCore.sparqlSelect(query).getBindingSets().iterator();
		int count = 0;
		while (result.hasNext()) {
			count++;
			BindingSet row = result.next();
			Value xIRI = row.getValue("x");
			String x = getConceptName(xIRI);
			NODE_TYPE nodeType = Utils.getConceptType(xIRI, rdf2GoCore);

			Value yIRI = row.getValue("y");
			String y = getConceptName(yIRI);

			// check blank node sequence case
			final Value previousBlankValue = row.getValue(previousBlankValueSparqlVariableName);
			if (previousBlankValue != null) {
				// here we check for the right blank node, quit all the others

				if ((!(previousBlankValue instanceof BNode)) || !previousBlankValue.stringValue()
						.equals(previousValue.stringValue())) {
					continue;
				}
			}

			if (checkTripleFilters(query, y, x, nodeType)) continue;

			height++;
			if (height < config.getPredecessors() || nodeType == NODE_TYPE.BLANKNODE) {
				addPredecessors(xIRI, conceptToBeExpanded, yIRI, Direction.Backward);
				if (!literalsExpanded.contains(xIRI) &&
						Config.LiteralMode.OFF != config.getLiteralMode()) {
					// add literals for x
					addSuccessors(xIRI, conceptToBeExpanded, yIRI, ExpandMode.LiteralsOnly, Direction.Backward);
				}
			}

            /*
			TODO: this should be done _after_ the last concept node has been added to the graph
             */
			if (height == config.getPredecessors()) {
				if (nodeType != NODE_TYPE.LITERAL) {
					fringeValues.add(xIRI);
				}
				//addOutgoingEdgesPredecessors(xIRI);
				//addOutgoingEdgesSuccessors(xIRI);
				//if (!literalsExpanded.contains(xIRI)) {
				//    addSuccessors(xIRI, conceptToBeExpanded, yIRI, ExpandMode.LiteralsOnly, Direction.Backward);
				//}
			}

			height--;

			addConcept(xIRI, conceptToBeExpanded, yIRI);
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
	private void addOutgoingEdgesSuccessors(Value conceptIRI) {
		if (Utils.isLiteral(conceptIRI)) return;
		/*
		TODO: handle outgoing edges to blank nodes !
         */
		if (Utils.isBlankNode(conceptIRI)) return;

		String conceptFilter = "Filter(true)";
		if (!config.isShowOutgoingEdges()) {
			// this filter brings considerable performance boost
			// but will dismiss outgoing edges
			conceptFilter = conceptFilter("?z", data.getConceptDeclarations());
		}

		addOutgoingSuccessorsCalls++;

		String query = "SELECT ?y ?z WHERE { <"
				+ conceptIRI.stringValue()
				+ "> ?y ?z. " + predicateFilter(Direction.Forward, "z") + " " + conceptFilter + "}";
		Iterator<BindingSet> result =
				rdf2GoCore.sparqlSelect(query).getBindingSets().iterator();
		int count = 0;
		while (result.hasNext()) {
			count++;
			BindingSet row = result.next();
			Value yIRI = row.getValue("y");
			String y = getConceptName(yIRI);

			Value zIRI = row.getValue("z");
			String z = getConceptName(zIRI);
			NODE_TYPE nodeType = Utils.getConceptType(zIRI, rdf2GoCore);

			final OuterConceptCheck check = new OuterConceptCheck(conceptIRI, zIRI, yIRI, false);

			if (checkTripleFilters(query, y, z, nodeType)) continue;

			outerConceptCalls.add(check);
			if (!checkedOuterConcepts.contains(check)) {
				addOuterConcept(conceptIRI, zIRI, yIRI, false);
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
	private void addOutgoingEdgesPredecessors(Value conceptIRI) {
		if (Utils.isLiteral(conceptIRI)) return;
		 /*
		TODO: handle outgoing edges to blank nodes !
         */
		if (Utils.isBlankNode(conceptIRI)) return;

		addOutgoingPredecessorsCalls++;

		final Collection<ConceptNode> conceptDeclarations = data.getConceptDeclarations();

		String conceptFilter = "Filter(true)";
		if (!config.isShowOutgoingEdges()) {
			// this filter brings considerable performance boost
			// but will dismiss outgoing edges
			conceptFilter = conceptFilter("?x", conceptDeclarations);
		}

		String query = "SELECT ?x ?y WHERE { ?x ?y <"
				+ conceptIRI.stringValue()
				+ "> . " + predicateFilter(Direction.Backward, null) + " " + conceptFilter + "}";
		TupleQueryResult resultTable = rdf2GoCore.sparqlSelect(
				query);

		Iterator<BindingSet> result = resultTable.iterator();
		int count = 0;
		while (result.hasNext()) {
			count++;
			BindingSet row = result.next();
			Value xIRI = row.getValue("x");
			String x = getConceptName(xIRI);
			NODE_TYPE nodeType = Utils.getConceptType(xIRI, rdf2GoCore);

			Value yIRI = row.getValue("y");
			String y = getConceptName(yIRI);
			final OuterConceptCheck check = new OuterConceptCheck(xIRI, conceptIRI, yIRI, true);

			if (checkTripleFilters(query, y, x, nodeType)) continue;

			outerConceptCalls.add(check);
			if (!checkedOuterConcepts.contains(check)) {
				addOuterConcept(xIRI, conceptIRI, yIRI, true);
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

		final Iterator<ConceptNode> iterator = conceptDeclarations.iterator();
		boolean firstIteration = true;
		while (iterator.hasNext()) {
			ConceptNode conceptDeclaration = iterator.next();
			if (conceptDeclaration.getType() == NODE_TYPE.LITERAL) {
				continue;
			}
			if (conceptDeclaration.getType() == NODE_TYPE.UNDEFINED) {
				continue;
			}
			if (conceptDeclaration.getType() == NODE_TYPE.BLANKNODE) {
				// TODO: find solution for this case
				continue;
			}
			String concept = conceptDeclaration.getName();
			if (concept.matches("^https?://.+")) {
				concept = "<" + concept + ">";
			}
			else if (!concept.contains(":")) {
				continue;
			}

			if (firstIteration) {
				firstIteration = false;
			}
			else {
				filter.append(" || ");
			}
			filter.append(variable).append(" = ").append(concept);
		}
		if (firstIteration) {
			filter.append("true");
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

		if (!getFilteredRelations().isEmpty()) {
			// we are in white list mode, i.e. show only "..."
			this.propertyExcludeSPARQLFilterCache.put(filterHashcode, createExclusiveFilter(dir, objectVar));
			return propertyExcludeSPARQLFilterCache.get(filterHashcode);
		}
		else {
			// we are in black list mode, i.e. show all but "..."
			if (getExcludedRelations().isEmpty()) {
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
		if (mode == ExpandMode.LiteralsOnly) {
			return " FILTER isLiteral(" + variable + ")";
		}
		return " FILTER (true).";


/*
		if(nodeFilterExpression != null) {
            return nodeFilterExpression;
        }
        if(getExcludedValues().size() == 0) {
            nodeFilterExpression = " FILTER (true).";
            return nodeFilterExpression;
        }
        StringBuffer filterExp = new StringBuffer();

        filterExp.append("FILTER (");

        Iterator<String> iter = getExcludedValues().iterator();
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
			return !(nodeType == NODE_TYPE.LITERAL || isTypeRelation(y));
		}

		if (nodeType == NODE_TYPE.LITERAL || isTypeRelation(y)) {
			// only literals and type assertions are not filtered out
			return false;
		}

		return isWhiteListMode() && !(filteredRelation(y));
	}

	private boolean isWhiteListMode() {
		return !getFilteredRelations().isEmpty();
	}

	private void addConcept(Value fromIRI, Value toIRI, Value relationIRI) {
		String relation = getConceptName(relationIRI);

        /*
		cluster change
        */
		String clazz = null;
		if (isTypeRelation(relation)) {
            /*
            no matter what class this type relation goes to, we look for a representative/meaningful class-uri to display
             */
			try {
				final IRI uri = (IRI) fromIRI;
				final IRI mostSpecificClass = Rdf2GoUtils.findMostSpecificClass(rdf2GoCore, uri);
				clazz = null;
				if (mostSpecificClass != null) {
					clazz = getConceptName(mostSpecificClass);
				}
			}
			catch (ClassCastException ignore) {
				// is not an IRI but a BValue probably
			}
		}

		ConceptNode toValue = Utils.createValue(this.getParameterMap(), this.rdf2GoCore, this.uriProvider, this.section, this.data, toIRI, true);
		ConceptNode fromValue = Utils.createValue(config, this.rdf2GoCore, this.uriProvider, this.section, this.data, fromIRI, true, clazz);
		if (toValue == null || fromValue == null) return;
		toValue.setOuter(false);
		fromValue.setOuter(false);

		// look for label for the property
		String relationLabel = Utils.fetchLabel(config, relationIRI, rdf2GoCore);
		if (relationLabel != null) {
			relation = relationLabel;
		}

		if (Strings.isBlank(clazz)) {
			// classes are rendered as cluster labels - so no extra edge is required
			Edge edge = new Edge(fromValue, relation, relationIRI.stringValue(), toValue);

			addEdge(edge);
		}
	}

	private boolean isTypeRelation(String relation) {
		return relation.length() > 4 && relation.substring(relation.length() - 4).equalsIgnoreCase("type");
	}

	/**
	 * Adds a nodes expanded by a fringe node.
	 * <p>
	 * - if the node is not part of the visualization yet it is not added (except as outer node for indicating the
	 * existence of further edges)
	 * - EXCEPT for datatype property edges which are always added to the visualization
	 * - if the node is already part of the visualization the respective edge is added
	 */
	private void addOuterConcept(Value fromIRI, Value toIRI, Value relationIRI, boolean predecessor) {
		String to = getConceptName(toIRI);
		String relation = getConceptName(relationIRI);

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
			clazz = getConceptName(toIRI);
		}

		ConceptNode toValue = Utils.createValue(this.getParameterMap(), this.rdf2GoCore, this.uriProvider, this.section, this.data, toIRI, false);

		ConceptNode fromValue = Utils.createValue(this.getParameterMap(), this.rdf2GoCore, this.uriProvider, this.section, this.data, fromIRI, false, clazz);

		ConceptNode current;
		if (predecessor) {
			// from is current new one
			current = fromValue;
		}
		else {
			// to is current new one
			current = toValue;
		}

		this.checkedOuterConcepts.add(new OuterConceptCheck(fromIRI, toIRI, relationIRI, predecessor));

		boolean nodeIsNew = !data.getConceptDeclarations().contains(current);

		Edge edge = new Edge(fromValue, relation, relationIRI.stringValue(), toValue);

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
					fromValue.setOuter(true);
					data.addConcept(fromValue);
				}
				else {
					// to is current new one
					toValue.setOuter(true);
					data.addConcept(toValue);
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
		return edge.getObject().getType() == NODE_TYPE.LITERAL;
	}

	public Config getConfig() {
		return config;
	}

	enum ExpandMode {Normal, LiteralsOnly}

	enum Direction {Forward, Backward}

	static class OuterConceptCheck {
		private final Value fromIRI;
		private final Value toIRI;
		private final Value relationIRI;
		private final boolean predecessor;

		OuterConceptCheck(Value fromIRI, Value toIRI, Value relationIRI, boolean predecessor) {
			this.fromIRI = fromIRI;
			this.toIRI = toIRI;
			this.relationIRI = relationIRI;
			this.predecessor = predecessor;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;

			OuterConceptCheck that = (OuterConceptCheck) o;

			if (predecessor != that.predecessor) return false;
			if (!fromIRI.equals(that.fromIRI)) return false;
			//noinspection SimplifiableIfStatement
			if (!relationIRI.equals(that.relationIRI)) return false;
			return toIRI.equals(that.toIRI);
		}

		@Override
		public int hashCode() {
			int result = fromIRI.hashCode();
			result = 31 * result + toIRI.hashCode();
			result = 31 * result + relationIRI.hashCode();
			result = 31 * result + (predecessor ? 1 : 0);
			return result;
		}

		@Override
		public String toString() {
			return fromIRI + " " + relationIRI + " " + toIRI + " (forward: " + predecessor + ")";
		}
	}
}
