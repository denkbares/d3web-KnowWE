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

package de.knowwe.rdfs.vis;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import de.d3web.collections.PartialHierarchy;
import de.d3web.collections.PartialHierarchyTree;
import de.d3web.utils.Pair;
import de.knowwe.rdf2go.Rdf2GoCore;
import de.knowwe.rdf2go.utils.Rdf2GoUtils;
import de.knowwe.visualization.ConceptNode;
import de.knowwe.visualization.Edge;
import de.knowwe.visualization.SubGraphData;

/**
 * @author Jochen Reutelshöfer
 * @created 15.05.2014
 */
public class SubpropertyEliminator {

	/**
	 * For a give rdf-based SubGraphData set the redundant edges between two nodes are eliminated. Example: Edges: A p1
	 * B; A p2 B If p1 is a subproperty of p2, then the p2 edge gets eliminated as it is redundant.
	 * <p/>
	 * If no subproperty relations of this kind exist, the data set stays unmodified.
	 *
	 * @param data
	 * @param core
	 */
	public static void eliminateSubproperties(SubGraphData data, Rdf2GoCore core) {
		PropertyHierarchy hierarchy = new PropertyHierarchy(core);
		for (ConceptNode conceptNode : data.getConceptDeclarations()) {
			Set<Edge> outgoingEdges = data.getAllOutoingEdgesFor(conceptNode);
			Map<Pair<ConceptNode, ConceptNode>, Set<Edge>> edgeBundles = new HashMap<>();

			// at first we need to create the bundles of edges which have the same source and target nodes
			for (Edge outgoingEdge : outgoingEdges) {
				Pair pair = new Pair(outgoingEdge.getSubject(), outgoingEdge.getObject());
				Set<Edge> bundle = edgeBundles.get(pair);
				if (bundle == null) {
					bundle = new HashSet<>();
					edgeBundles.put(pair, bundle);
				}
				bundle.add(outgoingEdge);
			}

			// for each bundle we create a hierarchy for the edge properties
			for (Set<Edge> edges : edgeBundles.values()) {
				PartialHierarchyTree tree = new PartialHierarchyTree(hierarchy);
				for (Edge edge : edges) {
					tree.insertNode(edge);
				}

				// the desired properties are the leafs of this hierarchy
				final Collection<Edge> leafProperties = tree.getLeafNodes();
				for (Edge edge : edges) {
					if (!leafProperties.contains(edge)) {
						// we remove all others
						data.removeEdge(edge);
					}
				}
			}

		}
	}

	static class PropertyHierarchy implements PartialHierarchy<Edge> {

		private Rdf2GoCore core;

		PropertyHierarchy(Rdf2GoCore core) {
			this.core = core;
		}

		@Override
		public boolean isSuccessorOf(Edge node1, Edge node2) {
			final String uri1 = Rdf2GoUtils.expandNamespace(core, node1.getPredicate());
			final String uri2 = Rdf2GoUtils.expandNamespace(core, node2.getPredicate());
			String sparql = "ASK { <" + uri1 + "> rdfs:subPropertyOf <" + uri2 + "> .}";
			try {
				return core.sparqlAsk(sparql);

			}
			catch (Exception e) {
				// uris not valid, so property hierarchy is not resolved
				// false will yield in a completely flat property hierarchy
				return false;
			}
		}
	}
}
