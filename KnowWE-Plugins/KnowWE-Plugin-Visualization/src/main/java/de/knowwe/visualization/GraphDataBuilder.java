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
package de.knowwe.visualization;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import de.knowwe.core.Environment;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.utils.LinkToTermDefinitionProvider;
import de.knowwe.visualization.dot.DOTVisualizationRenderer;

/**
 * @author Johanna Latt
 * @created 11.10.2013
 */
public abstract class GraphDataBuilder {

	// stores the actual subset of the rdf-graph to rendered
	public SubGraphData data;
	protected Config config;
	protected Section<?> section;
	protected LinkToTermDefinitionProvider uriProvider = null;
	protected GraphVisualizationRenderer graphRenderer = null;
	protected boolean isTimeOut;

	public static String createBaseURL() {
		if (Environment.getInstance() != null
				&& Environment.getInstance().getWikiConnector() != null) {
			return Environment.getInstance().getWikiConnector().getBaseUrl() + "Wiki.jsp";
		}
		else {
			// for tests only
			return "http://localhost:8080/KnowWE/Wiki.jsp";
		}
	}

	public Config getParameterMap() {
		return config;
	}


	public void initialiseData(Section<?> section, Config config, LinkToTermDefinitionProvider uriProvider) {
		this.uriProvider = uriProvider;
		this.config = config;
		this.section = section;

		data = new SubGraphData();
		graphRenderer = new DOTVisualizationRenderer(data, config);
	}

	public void createData(long timeOutMillis) {
		if (Thread.currentThread().isInterrupted()) return;

		// select the relevant sub-graph from the overall rdf-graph
		selectGraphData(timeOutMillis);

		if (isTimeOut) return;

		if (Thread.currentThread().isInterrupted()) return;

		// create the source representation using the configured source-renderer
		this.graphRenderer.generateSource();

	}

	/**
	 * Starts the actual rendering process. Generates doc file and images files. Adds corresponding html source to the
	 * passed StringBuilder.
	 *
	 * @created 29.11.2012
	 */
	public void render(RenderResult result) {
		result.appendHtml(graphRenderer.getHTMLIncludeSnipplet());
	}

	/**
	 * Adds all requested concepts and information to the dotSources (the maps).
	 *
	 * @created 20.08.2012
	 */
	public abstract void selectGraphData(long timeOutMillis);

	public String getSource() {
		return this.graphRenderer.getSource();
	}


	protected List<String> getExcludedRelations() {
		return decorateStatements(config.getExcludeRelations());
	}

	private List<String> decorateStatements(Collection<String> statements) {
		List<String> statementsList = new ArrayList<>(statements);
		for (int i = 0; i < statementsList.size(); i++) {
			String statement = statementsList.get(i);
			if (!statement.contains(":")) {
				statementsList.set(i, "lns:" + statement);
			}
		}
		return statementsList;
	}

	protected List<String> getExcludedNodes() {
		return decorateStatements(config.getExcludeNodes());
	}

	protected List<String> getMainConcepts() {
		return decorateStatements(config.getConcepts());
	}


	public List<String> getFilteredRelations() {
		return decorateStatements(config.getFilterRelations());
	}


	public String createConceptURL(String to) {
		return createBaseURL() + "?page=" + section.getTitle() + "&concept=" + to;
	}

	/**
	 * Tests if the given node x is being excluded in the annotations.
	 *
	 * @created 20.08.2012
	 */
	public boolean excludedNode(String x) {
		// TODO: improve handling of node 'name' with respect to namespace prefix...
		return getExcludedNodes().contains(x) || getExcludedNodes().contains("lns:" + x);
	}

	/**
	 * Test if the given relation y is being excluded in the annotations.
	 *
	 * @created 20.08.2012
	 */
	public boolean excludedRelation(String y) {
		// TODO: improve handling of node 'name' with respect to namespace prefix...
		return getExcludedRelations().contains(y) || getExcludedNodes().contains("lns:" + y);
	}

	/**
	 * Tests if the given relation y is set as a filtered relation in the annotations.
	 *
	 * @created 24.11.2013
	 */
	public boolean filteredRelation(String y) {
		// TODO: improve handling of node 'name' with respect to namespace prefix...
		return getFilteredRelations().contains(y) || getFilteredRelations().contains("lns:" + y);
	}

	/**
	 * Tests if input is a valid int for the depth/height of the graph.
	 *
	 * @created 20.08.2012
	 */
	private boolean isValidInt(String input) {
		try {
			Integer value = Integer.parseInt(input);
			// final maximum depth/height for graph
			return !(value > 5 || value < 0);
		}
		catch (Exception e) {
			return false;
		}
	}

	public GraphVisualizationRenderer getGraphRenderer() {
		return graphRenderer;
	}

	public enum NODE_TYPE {
		CLASS, PROPERTY, INSTANCE, UNDEFINED, LITERAL, BLANKNODE,
	}

}
