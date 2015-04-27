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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import de.d3web.strings.Identifier;
import de.d3web.strings.Strings;
import de.knowwe.core.Environment;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.utils.LinkToTermDefinitionProvider;
import de.knowwe.visualization.d3.D3VisualizationRenderer;
import de.knowwe.visualization.dot.DOTVisualizationRenderer;
import de.knowwe.visualization.util.FileUtils;

/**
 * @param <T> The type of data the graph is supposed to visualize
 * @author Johanna Latt
 * @created 11.10.2013
 */
public abstract class GraphDataBuilder<T extends Object> {

	public enum NODE_TYPE {
		CLASS, PROPERTY, INSTANCE, UNDEFINED, LITERAL, BLANKNODE,
	}

	public enum Renderer {
		dot, d3
	}

	public static final String RENDERER = "renderer";

	public static final String VISUALIZATION = "visualization";

	public static final String FORMAT = "format";
	public static final String CONCEPT = "concept";
	public static final String MASTER = "master";
	public static final String LANGUAGE = "language";
	public static final String REQUESTED_HEIGHT = "requested_height";
	public static final String REQUESTED_DEPTH = "requested_depth";

	public static final String EXCLUDED_NODES = "excluded_nodes";
	public static final String EXCLUDED_RELATIONS = "excluded_relations";

	public static final String FILTERED_CLASSES = "filtered_classes";
	public static final String FILTERED_RELATIONS = "filtered_relations";

	public static final String SHOW_CLASSES = "show_classes";
	public static final String SHOW_PROPERTIES = "show_properties";
	public static final String SHOW_SCROLLBAR = "show_scrollbar";
	public static final String USE_LABELS = "use_labels";

	public static final String GRAPH_SIZE = "graph_size";
	public static final String GRAPH_WIDTH = "graph_width";
	public static final String GRAPH_HEIGHT = "graph height";
	public static final String RANK_DIRECTION = "rank_direction";
	public static final String LINK_MODE = "LINK_MODE";
	public static final String LINK_MODE_JUMP = "jump";
	public static final String LINK_MODE_BROWSE = "browse";

	public static final String DOT_APP = "dot_app";
	public static final String ADD_TO_DOT = "add_to_dot";
	public static final String TITLE = "title";
	public static final String SECTION_ID = "section-id";
	public static final String REAL_PATH = "realpath";

	public static final String D3_FORCE_VISUALISATION_STYLE = "d3_force_visualisation_style";

	public static final String RELATION_COLOR_CODES = "relation_color_codes";
	public static final String CLASS_COLOR_CODES = "class_color_codes";

	public static final String SHOW_OUTGOING_EDGES = "SHOW_OUTGOING_EDGES";
	public static final String SHOW_INVERSE = "SHOW_INVERSE";

	public static final String FILE_ID = "file-id";

	public int requestedDepth = 1;
	public int requestedHeight = 1;

	private boolean showClasses;
	private boolean showProperties;
	private boolean showOutgoingEdges = true;

	// stores the actual subset of the rdf-graph to rendered
	public SubGraphData data;

	// concept and relation names which are black-listed
	//private List<String> excludedNodes;
	//private List<String> excludedRelations;

	//private List<String> filteredClasses;
	//private List<String> filteredRelations;

	protected Section<?> section;

	private Map<String, String> parameters;

	protected LinkToTermDefinitionProvider uriProvider = null;

	private GraphVisualizationRenderer sourceRenderer = null;

	public Map<String, String> getParameterMap() {
		return parameters;
	}

	public boolean showProperties() {
		return showProperties;
	}

	public boolean showOutgoingEdges() {
		return showOutgoingEdges;
	}

	public boolean showClasses() {
		return showClasses;
	}

	public void initialiseData(String realPath, Section<?> section, Map<String, String> parameters, LinkToTermDefinitionProvider uriProvider) {
		this.uriProvider = uriProvider;

		String requestedHeightString = parameters.get(REQUESTED_HEIGHT);
		if (requestedHeightString != null) {
			try {
				this.requestedHeight = Integer.parseInt(requestedHeightString);
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}

		String requestedDepthString = parameters.get(REQUESTED_DEPTH);
		if (requestedDepthString != null) {
			try {
				this.requestedDepth = Integer.parseInt(requestedDepthString);
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}

		if (parameters.get(SHOW_OUTGOING_EDGES) != null) {
			if (parameters.get(SHOW_OUTGOING_EDGES).equals("false")) {
				showOutgoingEdges = false;
			}
		}

		this.parameters = parameters;
		this.section = section;

		parameters.put(REAL_PATH, realPath);
		parameters.put(TITLE, getSectionTitle(section));
		parameters.put(SECTION_ID, getSectionID(section));

		data = new SubGraphData();

		// current default source renderer is DOT
		String renderer = parameters.get(GraphDataBuilder.RENDERER);
		if (renderer != null && renderer.equals(Renderer.d3.name())) {
			sourceRenderer = new D3VisualizationRenderer(data, parameters);
		}
		else {
			sourceRenderer = new DOTVisualizationRenderer(data, parameters);
		}

		// set config values
		setConfigurationParameters();
	}

	public static String getSectionTitle(Section<?> section) {
		if (section != null) {
			return section.getTitle();
		}
		return "NO_ID";
	}

	public static String getSectionID(Section<?> section) {
		if (section != null) {
			return section.getID();
		}
		return "NO_ID";
	}

	public void createData() {
		if (Thread.currentThread().isInterrupted()) return;

		// select the relevant sub-graph from the overall rdf-graph
		selectGraphData();

		if (Thread.currentThread().isInterrupted()) return;

		// create the source representation using the configured source-renderer
		this.sourceRenderer.generateSource();

	}

	/**
	 * Starts the actual rendering process. Generates doc file and images files. Adds corresponding html source to the
	 * passed StringBuilder.
	 *
	 * @param builder html source showing the generated images is added to this builder
	 * @created 29.11.2012
	 */
	public void render(RenderResult builder) {
		// if the graph already exists it doesn't have to be re-created
		if (!FileUtils.filesAlreadyRendered(sourceRenderer.getGraphFilePath())) {
			// System.out.println("Creating data for: " + parameters.get(FILE_ID));
			createData();
		} else {
			// System.out.println("Could cache data for: " + parameters.get(FILE_ID));
		}

		if (builder != null && !Thread.currentThread().isInterrupted()) {
			builder.appendHtml(sourceRenderer.getHTMLIncludeSnipplet());
		}
	}

	/**
	 * Adds all requested concepts and information to the dotSources (the maps).
	 *
	 * @created 20.08.2012
	 */
	public abstract void selectGraphData();

	public String getEncodedConceptName() {
		String concept = parameters.get(CONCEPT);
		String conceptNameEncoded = null;
		try {
			conceptNameEncoded = URLEncoder.encode(concept, "UTF-8");
		}
		catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return conceptNameEncoded;
	}

	public String getSource() {
		return this.sourceRenderer.getSource();
	}

	/**
	 * @created 20.08.2012
	 */
	private void setConfigurationParameters() {
		setSuccessors();
		setPredecessors();
		setShowAnnotations();
	}

	/**
	 * @created 18.08.2012
	 */
	private void setPredecessors() {
		if (isValidInt(parameters.get(REQUESTED_HEIGHT))) {
			requestedHeight = Integer.parseInt(parameters.get(REQUESTED_HEIGHT));
		}
	}

	/**
	 * @created 18.08.2012
	 */
	private void setSuccessors() {
		if (isValidInt(parameters.get(REQUESTED_DEPTH))) {
			requestedDepth = Integer.parseInt(parameters.get(REQUESTED_DEPTH));
		}
	}

	/**
	 * @created 20.08.2012
	 */
	protected List<String> getExcludedRelations() {
		String exclude = parameters.get(EXCLUDED_RELATIONS);
		return getList(exclude);
	}

	private List<String> getList(String input) {
		List<String> excludedRelations = new ArrayList<String>();
		if (!Strings.isBlank(input)) {
			String[] array = input.split(",");
			for (String item : array) {
				String trimmedItem = item.trim();
				if (trimmedItem.contains(":")) {
					excludedRelations.add(trimmedItem);
				}
				else {
					excludedRelations.add("lns:" + trimmedItem);
				}
			}
		}
		return excludedRelations;
	}

	/**
	 * @created 20.08.2012
	 */
	protected List<String> getExcludedNodes() {
		String exclude = parameters.get(EXCLUDED_NODES);
		return getList(exclude);
	}

	/**
	 * @created 20.08.2012
	 */
	protected List<String> getMainConcepts() {
		String value = parameters.get(CONCEPT);
		return getList(value);
	}

	/**
	 * @created 24.11.2013
	 */
	public List<String> getFilteredClasses() {
		String filters = parameters.get(FILTERED_CLASSES);
		return getList(filters);
	}

	/**
	 * @created 24.11.2013
	 */
	public List<String> getFilteredRelations() {
		String filters = parameters.get(FILTERED_RELATIONS);
		return getList(filters);
	}

	/**
	 * @created 13.09.2012
	 */
	private void setShowAnnotations() {

		String classes = parameters.get(SHOW_CLASSES);
		showClasses = !(classes != null && classes.equals("false"));

		String properties = parameters.get(SHOW_PROPERTIES);
		showProperties = !(properties != null && properties.equals("false"));
	}

	public String createConceptURL(String to) {
		if (parameters.get(LINK_MODE) != null) {
			if (parameters.get(LINK_MODE).equals(LINK_MODE_BROWSE)) {
				return uriProvider.getLinkToTermDefinition(new Identifier(to),
						parameters.get(MASTER));
			}
		}
		return createBaseURL() + "?page=" + getSectionTitle(section)
				+ "&concept=" + to;
	}

	/**
	 * @return
	 * @created 29.11.2012
	 */
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

	/**
	 * Tests if the given node x is being excluded in the annotations.
	 *
	 * @param x
	 * @created 20.08.2012
	 */
	public boolean excludedNode(String x) {
		// TODO: improve handling of node 'name' with respect to namespace prefix...
		return getExcludedNodes().contains(x) || getExcludedNodes().contains("lns:" + x);
	}

	/**
	 * Test if the given relation y is being excluded in the annotations.
	 *
	 * @param y
	 * @created 20.08.2012
	 */
	public boolean excludedRelation(String y) {
		// TODO: improve handling of node 'name' with respect to namespace prefix...
		return getExcludedRelations().contains(y) || getExcludedNodes().contains("lns:" + y);
	}

	/**
	 * Tests if the given class x is set as a filtered class in the annotations.
	 *
	 * @param x
	 * @return
	 * @created 24.11.2013
	 */
	public boolean filteredClass(String x) {
		// TODO: improve handling of node 'name' with respect to namespace prefix...
		return getFilteredClasses().contains(x) || getFilteredClasses().contains("lns:" + x);
	}

	/**
	 * Tests if the given relation y is set as a filtered relation in the annotations.
	 *
	 * @param y
	 * @return
	 * @created 24.11.2013
	 */
	public boolean filteredRelation(String y) {
		// TODO: improve handling of node 'name' with respect to namespace prefix...
		return getFilteredRelations().contains(y) || getFilteredRelations().contains("lns:" + y);
	}

	/**
	 * Tests if input is a valid int for the depth/height of the graph.
	 *
	 * @param input
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

	public GraphVisualizationRenderer getSourceRenderer() {
		return sourceRenderer;
	}

}
