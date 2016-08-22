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
package de.knowwe.visualization;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import com.denkbares.collections.DefaultMultiMap;
import com.denkbares.collections.MultiMap;
import com.denkbares.collections.MultiMaps;

/**
 * Contains a sub-set of a graph.
 *
 * @author Jochen Reutelshöfer
 * @created 27.05.2013
 */
public class SubGraphData {

	private final Map<String, ConceptNode> concepts = new LinkedHashMap<>();
	private final Map<ConceptNode, Set<Edge>> clusters = new HashMap<>();
	private MultiMap<String, String> subPropertiesMap = new DefaultMultiMap<>();

	/**
	 *
	 */
	public SubGraphData() {
		this(MultiMaps.emptyMultiMap());
	}

	public SubGraphData(MultiMap<String, String> subPropertiesMap) {
		clusters.put(ConceptNode.DEFAULT_CLUSTER_NODE, new HashSet<>());
		this.subPropertiesMap = subPropertiesMap;
	}

	public void createCluster(ConceptNode node) {
		clusters.put(node, new HashSet<>());
	}

	public Map<ConceptNode, Set<Edge>> getClusters() {
		return clusters;
	}

	public void addEdgeToCluster(ConceptNode node, Edge e) {
		if (clusters.get(node) == null) {
			createCluster(node);
		}
		final Set<Edge> set = clusters.get(node);
		set.add(e);
	}

	public Collection<ConceptNode> getConceptDeclarations() {
		return concepts.values();
	}

	/**
	 * removes all concepts from the concept set, which are not used within the edges of the default level.
	 */
	public void clearIsolatedNodesFromDefaultLevel() {
		Set<ConceptNode> usedNodes = new HashSet<>();
		final Set<Edge> defaultLevelEdges = clusters.get(ConceptNode.DEFAULT_CLUSTER_NODE);
		for (Edge edge : defaultLevelEdges) {
			usedNodes.add(edge.getSubject());
			usedNodes.add(edge.getObject());
		}
		Collection<String> keysToRemove = new HashSet<>();
		for (Map.Entry<String, ConceptNode> nodeEntry : concepts.entrySet()) {
			if (!usedNodes.contains(nodeEntry.getValue())) {
				keysToRemove.add(nodeEntry.getKey());
			}
		}
		for (String key : keysToRemove) {
			concepts.remove(key);
		}
	}

	public ConceptNode getConcept(String name) {
		return concepts.get(name);
	}

	public Set<Edge> getAllOutoingEdgesFor(ConceptNode node) {
		Set<Edge> result = new HashSet<>();
		final Set<Edge> defaultLevelEdges = clusters.get(ConceptNode.DEFAULT_CLUSTER_NODE);
		for (Edge edge : defaultLevelEdges) {
			if (edge.getSubject().equals(node)) {
				result.add(edge);
			}
		}
		final Set<Edge> cluster = clusters.get(node);
		if (cluster != null) {
			result.addAll(cluster);
		}
		return result;
	}

	public Set<Edge> getAllEdges() {
		Set<Edge> allEdges = new HashSet<>();
		for (ConceptNode conceptNode : clusters.keySet()) {
			allEdges.addAll(clusters.get(conceptNode));
		}
		return allEdges;
	}

	public void addEdge(Edge e) {
		clusters.get(ConceptNode.DEFAULT_CLUSTER_NODE).add(e);
	}

	public void removeEdge(Edge e) {
		final Set<Edge> defaultLevelEdges = clusters.get(ConceptNode.DEFAULT_CLUSTER_NODE);
		if (defaultLevelEdges.contains(e)) {
			defaultLevelEdges.remove(e);
		}
		final Set<Edge> cluster = clusters.get(e.getSubject());
		if (cluster != null) {
			if (cluster.contains(e)) {
				cluster.remove(e);
			}
		}
	}

	public void addConcept(ConceptNode n) {
		if (concepts.containsValue(n)) {
			// due to equals not regarding outer-flag
			concepts.remove(n.getName());
		}
		concepts.put(n.getName(), n);
	}

	public MultiMap<String, String> getSubPropertiesMap() {
		return subPropertiesMap;
	}
}
