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
package de.knowwe.visualization.dot;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import com.denkbares.collections.MultiMap;
import com.denkbares.strings.Strings;
import com.denkbares.utils.Log;
import de.knowwe.visualization.ConceptNode;
import de.knowwe.visualization.Config;
import de.knowwe.visualization.Edge;
import de.knowwe.visualization.GraphDataBuilder.NODE_TYPE;
import de.knowwe.visualization.SubGraphData;
import de.knowwe.visualization.util.FileUtils;
import de.knowwe.visualization.util.SAXBuilderSingleton;
import de.knowwe.visualization.util.Utils;

/**
 * @author Jochen Reutelshöfer
 * @created 29.04.2013
 */
public class DOTRenderer {

	// appearance of outer node
	private static final String outerLabel = "[ shape=\"none\" fontsize=\"0\" fontcolor=\"white\" ];\n";
	private static final int TIMEOUT = 50000;

	private static final Set<String> cleanedCacheDirectories = new HashSet<>();

	public static String getFilePath(Config config) {
		return getDirectoryPath(config) + config.getCacheFileID();
	}

	private static String getDirectoryPath(Config config) {
		String path = config.getCacheDirectoryPath() + FileUtils.FILE_SEPARATOR + FileUtils.KNOWWEEXTENSION_FOLDER + FileUtils.FILE_SEPARATOR
				+ FileUtils.TMP_FOLDER
				+ FileUtils.FILE_SEPARATOR;
		handleCleanup(path);
		return path;
	}

	/**
	 * Cleans up all cache directories once on startup.
	 */
	private static void handleCleanup(String path) {
		if (cleanedCacheDirectories.add(path)) {
			File dir = new File(path);
			if (dir.exists() && dir.isDirectory()) {
				//noinspection ConstantConditions
				for (File file1 : dir.listFiles()) {
					//noinspection ResultOfMethodCallIgnored
					file1.delete();
				}
			}
		}
	}

	private static String buildLabel(RenderingStyle style) {
		StringBuilder result = new StringBuilder();
		result.append(" shape=\"").append(style.getShape()).append("\" ");
		if (!Strings.isBlank(style.getStyle())) {
			result.append(" style=\"").append(style.getStyle()).append("\" ");
		}
		if (!Strings.isBlank(style.getFillcolor())) {
			result.append(" fillcolor=\"").append(style.getFillcolor()).append("\" ");
		}
		return result.toString();
	}

	private static String buildRelation(String arrowtail, String color) {
		return " arrowtail=\"" + arrowtail + "\" " + " color=\"" + color + "\" ";
	}

	/**
	 * Given the label of the inner relation, the method returns the String of the appearance of the relation.
	 *
	 * @created 06.09.2012
	 */
	private static String innerRelation(String label, String color, boolean isBiDirectional) {
		// Basic Relation Attributes
		String arrowtail = "normal";
		String dir = "[ ";

		if (color == null) {
			// black is default
			color = "black";
		}

		if (isBiDirectional) {
			dir += "dir=\"both\" ";
		}

		return dir
				+ "label = \"" + label
				+ "\"" + buildRelation(arrowtail, color) + " ];\n";

	}

	/**
	 * The sources from the maps are being written into the String-dotSource.
	 *
	 * @created 18.08.2012
	 */
	static String createDotSources(SubGraphData data, Config config) {

		// we clean up the graph before rendering (e. g. undesired cycles etc)
//		if (config.getShowInverse() == Config.ShowInverse.FALSE_DOT_BASED || config.getShowInverse() == Config.ShowInverse.FALSE) {
//			simplifyGraph(data);
//		}
		// we clean up the graph before rendering (e. g. undesired cycles etc)
		simplifyGraph(data, config);

		String dotSource = "digraph {\n";

		// printing title of graph top left of visualization
		if (config.getTitle() != null) {
			dotSource += "graph [label = \"" + config.getTitle() + " (" + new Date() + ")\", labelloc = \"t\", labeljust = \"left\", fontsize = 10];\n";
		}

		//only useful for neato, ignored for dot
		dotSource += "sep=\"+25,25\";\n";
		dotSource += "splines = true;\n";
		if (Strings.isBlank(config.getOverlap())) {
			dotSource += "overlap=false;\n";
		}
		else {
			dotSource += "overlap=" + config.getOverlap() + ";\n";
		}

		// using rankSame constraints for custom layouting
		String rankSameValue = config.getRankSame();
		if (!Strings.isBlank(rankSameValue) && rankSameValue.contains(",")) {
			StringBuilder sb = new StringBuilder();
			String[] values = rankSameValue.split(",");

			for (String value : values) {
				sb.append("\"").append(value.trim()).append("\" ");
			}

			//{rank=same; q4 q3}
			dotSource += "{rank=same; " + sb + "}\n";
		}

		// set graph size and rankDir
		dotSource += DOTRenderer.setSizeAndRankDir(config.getRankDir(),
				config.getWidth(), config.getHeight(),
				config.getSize(), data.getConceptDeclarations().size());

		dotSource += generateGraphSource(data, config);

		dotSource += "}";

		return dotSource;
	}

	@SuppressWarnings("Duplicates")
	private static Set<Edge> getDoubleEdges(Set<Edge> edges) {
		Set<Edge> doubleEdges = new HashSet<>();
		ConceptNode o, s;
		Edge e1, e2;

		//look up every Edge in my SubGraphData
		for (int i = 0; i < edges.size() - 1; i++) {
			//for (Edge e1 : myEdges) {
			//memorize Object & Subject
			e1 = (Edge) edges.toArray()[i];
			o = e1.getObject();
			s = e1.getSubject();

			//look up every OTHER Edge
			for (int j = i + 1; j < edges.size(); j++) {
				//for (Edge e2 : myEdges) {
				// If subject == object (both directions) AND predicates are the same
				{
					e2 = (Edge) edges.toArray()[j];

					if (o.equals(e2.getSubject())
							&& s.equals(e2.getObject())
							&& e1.getPredicate().equals(e2.getPredicate())) {
						//mark first edge as bidirectional and add second Edge to redundant Set
						e1.setBidirectionalEdge(true);
						doubleEdges.add(e2);
//						data.removeEdge(e2);
					}
				}
			}
		}

		return doubleEdges;
	}

	private static Set<Edge> getRecursiveEdges(Set<Edge> edges) {
		Set<Edge> recursiveEdges = new HashSet<>();
		ConceptNode s, o;

		for (Edge e : edges) {
			s = e.getSubject();
			o = e.getObject();

			if (s.equals(o)) {
				recursiveEdges.add(e);
			}
		}

		return recursiveEdges;
	}

	private static void simplifyGraph(SubGraphData data, Config config) {
		Set<Edge> redundantEdges;

		if (!config.isShowRedundant()) {
			//mark all the superProperties (sets superProperty attributes)
			data = defineSuperProperties(data, data.getSubPropertiesMap());

			//receive all double edges
			redundantEdges = getDoubleEdges(data.getAllEdges());

			//receive and add all recursive edges
			redundantEdges.addAll(getRecursiveEdges(data.getAllEdges()));

			//receive and add all edges which are SuperProperties
			redundantEdges.addAll(getRedundantSuperPropertyEdges(data.getAllEdges()));

			// loop through redundant Set to remove all redundant Edges
			redundantEdges.forEach(data::removeEdge);
		}

		if (!config.isShowInverse()) {
			// change predicates of inverse Edges and receive now redundant Edges
			redundantEdges = defineInverseProperties(data, data.getInversePropertiesMap());

			// loop through redundant Set to remove all redundant Edges, once again
			redundantEdges.forEach(data::removeEdge);
		}
	}

	private static Set<Edge> defineInverseProperties(SubGraphData data, MultiMap<String, String> inversePropertiesMap) {
		Set<Edge> edges = data.getAllEdges();
		String relationURI, inverseURI, p1, p2 = null;
		Set<Edge> redundantEdges = new HashSet<>();
		Set<String> inverseProperties;

		ConceptNode o1, o2, s1, s2;

		// Check every Edge for inverseProperties
		for (Edge e1 : edges) {
			relationURI = e1.getRelationURI();
			s1 = e1.getSubject();
			o1 = e1.getObject();
			p1 = e1.getPredicate();
			inverseProperties = inversePropertiesMap.getValues(relationURI);

			if (inversePropertiesMap.getValues(relationURI).size() != 1) {
				continue;
			}

			inverseURI = (String) inversePropertiesMap.getValues(relationURI).toArray()[0];

			// Check if this there's a inverseProperty to this Edge's relationURI
			// and it's not an reflexive inverse
			// and isn't already redundant
			if (inverseProperties.size() == 1 && !inverseURI.equals(relationURI) && !redundantEdges.contains(e1)) {
				for (Edge e2 : edges) {
					s2 = e2.getSubject();
					o2 = e2.getObject();

					// Check if this edge is the inverse we are looking for
					if (e2.getRelationURI().equals(inverseURI) && s1.equals(o2) && o1.equals(s2)) {
						// if so: change Predicate of first edge, change to bidirectional and add second (inverse) edge to redundant edges
						p2 = e2.getPredicate();
						e1.setPredicate(p1 + " | " + p2);
						e1.setBidirectionalEdge(true);
						redundantEdges.add(e2);
					}
				}
			}
		}
		return redundantEdges;
	}

	private static SubGraphData defineSuperProperties(SubGraphData data, MultiMap<String, String> subPropertiesMap) {
		Set<Edge> edges = data.getAllEdges();
		String relationURI, subURI;
		ConceptNode o1, o2, s1, s2;

		Set<String> subProperties;

		// Check every Edge for SubProperties
		for (Edge e1 : edges) {
			relationURI = e1.getRelationURI();
			s1 = e1.getSubject();
			o1 = e1.getObject();

			// Check if there is SubProperties to this Edge's relationURI
			if (!subPropertiesMap.getValues(relationURI).isEmpty()) {
				subProperties = subPropertiesMap.getValues(relationURI);

				// Check edges once again for SubProperties, skip if it's the same edge
				for (Edge e2 : edges
						) {
					// skip if same edge
					if (!e1.equals(e2)) {
						s2 = e2.getSubject();
						o2 = e2.getObject();
						subURI = e2.getRelationURI();

						// check for same Source, Destination (swapped aswell!) and for being SubProperty
						// mark e1 as SuperProperty if true, continue outer loop (break inner loop)
						if ((s1.equals(s2) && o1.equals(o2)) || (s1.equals(o2) && o1.equals(s2)) && subProperties.contains(subURI)) {
							e1.setSuperProperty(true);
						}
					}
				}

			}
		}
		return data;
	}

	private static Set<Edge> getRedundantSuperPropertyEdges(Set<Edge> edges) {
		Set<Edge> redundantSuperPropertyEdges = new HashSet<>();

		// Checks if isSuperProperty and adds if true
		for (Edge e : edges
				) {
			if (e.isSuperProperty()) {
				redundantSuperPropertyEdges.add(e);
			}
		}

		return redundantSuperPropertyEdges;
	}

	private static String generateGraphSource(SubGraphData data, Config config) {
		Collection<ConceptNode> dotSourceLabel = data.getConceptDeclarations();
		StringBuilder dotSource = new StringBuilder();

		// iterate over the labels and add them to the dotSource
		Map<ConceptNode, Set<Edge>> clusters = data.getClusters();
		for (ConceptNode node : dotSourceLabel) {

			appendNodeDefinitionLineToSource(data, config, dotSource, node);
			// if the literal mode is 'TABLE', the literals are contained within the node
			// if the literal mode is 'NODES' we need to define literal nodes from the clusters
			if (Config.LiteralMode.NODES == config.getLiteralMode()) {
				Set<Edge> clusterEdges = clusters.get(node);
				if (clusterEdges != null) {
					for (Edge clusterEdge : clusterEdges) {
						ConceptNode object = clusterEdge.getObject();
						if (object != null) {
							appendNodeDefinitionLineToSource(data, config, dotSource, object);
						}
					}
				}
			}
		}

		/*
		iterate over the relations and add them to the dotSource
		 */

		// add level zero "default cluster"
		for (Edge key : clusters.get(ConceptNode.DEFAULT_CLUSTER_NODE)) {
			appendEdgeSource(config, dotSource, key);
		}

		// add literals contained in the clusters if the literal mode is 'NODES'
		if (Config.LiteralMode.NODES == config.getLiteralMode()) {
			for (ConceptNode node : dotSourceLabel) {
				if ((node.getType() != NODE_TYPE.LITERAL)) {
					Set<Edge> edges = clusters.get(node);
					if (edges != null) {
						for (Edge key : edges) {
							appendEdgeSource(config, dotSource, key);
						}
					}
				}
			}
		}

		return dotSource.toString();
	}

	private static void appendNodeDefinitionLineToSource(SubGraphData data, Config config, StringBuilder dotSource, ConceptNode node) {
		RenderingStyle style = node.getStyle();

		// root is rendered highlighted
		if (node.isRoot()) {
			style.addStyle("bold");
			style.setFontstyle(RenderingStyle.Fontstyle.UNDERLINING);
		}

		String label;

		if (node.isOuter()) {
			label = DOTRenderer.outerLabel;
		}
		else {
			String nodeLabel = clearLabel(node.getConceptLabel());
			if ((node.getType() != NODE_TYPE.LITERAL) &&
					Config.LiteralMode.TABLE == config.getLiteralMode()) {
				// we render the node as a table, showing the selected literals
				nodeLabel = createHTMLTable(node, data, style.getFontstyle(), config);
			}
			else {
				nodeLabel = createNodeLabel(nodeLabel, style.getFontstyle());
			}
			label = DOTRenderer.createDotConceptLabel(style, node.getConceptUrl(), nodeLabel, true);
		}
		dotSource.append("\"").append(node.getName()).append("\"").append(label);
	}

	private static String createHTMLTable(ConceptNode node, SubGraphData data, RenderingStyle.Fontstyle fontStyle, Config config) {
		final Map<ConceptNode, Set<Edge>> clusters = data.getClusters();
		final Set<Edge> edges = clusters.get(node);
		String label = node.getName();
		if (!"false".equalsIgnoreCase(config.getShowLabels())) {
			label = node.getConceptLabel();
		}
		String nodeLabel = createNodeLabel(escapeDot(label), fontStyle);

		if (edges == null || edges.isEmpty()) {
			return nodeLabel;
		}
		else {
			// if necessary, remove now unnecessary starttag for html again
			if (nodeLabel.startsWith("<")) {
				nodeLabel = nodeLabel.substring(1, nodeLabel.length() - 1);
			}

			// create table with cluster edges
			StringBuilder buffy = new StringBuilder();
			buffy.append("<");  // start html label
			buffy.append("<TABLE BORDER=\"0\">");

			// header row with actual node clazz and name
			buffy.append("<TR>");
			buffy.append("<TD BORDER=\"2\">");
			if (!Strings.isBlank(node.getClazz())) {
				buffy.append("<I>");
				buffy.append(node.getClazz());
				buffy.append("</I>");
			}
			buffy.append("</TD>");

			String conceptName = nodeLabel.replace("\\n", "<BR ALIGN=\"CENTER\"/>");

			buffy.append("<TD BORDER=\"2\">");
			buffy.append("<B>");
			buffy.append(Strings.unquote(conceptName));
			buffy.append("</B>");
			buffy.append("</TD>");
			buffy.append("</TR>");

			for (Edge edge : edges) {
				buffy.append("<TR>");
				buffy.append("<TD BORDER=\"1\">");
				buffy.append(escapeDot(edge.getPredicate()));
				buffy.append("</TD>");

				buffy.append("<TD BORDER=\"1\">");
				buffy.append(escapeDot(edge.getObject().getConceptLabel()));
				buffy.append("</TD>");
				buffy.append("</TR>");
			}
			buffy.append("</TABLE>");
			buffy.append(">");  // start html label
			return buffy.toString();
		}
	}

	private static String escapeDot(String text) {
		text = text.replace("&", "&amp;");
		text = text.replace("<", "&lt;");
		text = text.replace(">", "&gt;");
		return text;
	}

	private static String createNodeLabel(String name, RenderingStyle.Fontstyle f) {
		// prepareLabel adds linebreaks where necessary and possible
		// however that is not required as we generate only svg
		//String nodeLabel = Utils.prepareLabel(name);
		String nodeLabel = Utils.clean(name, Utils.LINE_BREAK);
		if (f == RenderingStyle.Fontstyle.NORMAL) {
			nodeLabel = "\"" + nodeLabel + "\"";
		}
		else {
			nodeLabel = styleLabel(nodeLabel, f);
		}

		Log.info("dwfsd");
		//System.out.println("dwfsd");
		return nodeLabel;
	}

	private static String styleLabel(String targetLabel, RenderingStyle.Fontstyle f) {
		String label = targetLabel.replace("\\n", "<BR ALIGN=\"CENTER\"/>");
		switch (f) {
			case BOLD:
				return "<<B>" + label + "</B>>";
			case ITALIC:
				return "<<I>" + label + "</I>>";
			case UNDERLINING:
				return "<<U>" + label + "</U>>";
			default:
				return targetLabel;
		}
	}

	private static void appendEdgeSource(Config config, StringBuilder dotSource, Edge key) {
//		private static void appendEdgeSource(Config config, StringBuilder dotSource, Edge key, boolean isBiDirectional) {
		String label = DOTRenderer.innerRelation(key.getPredicate(),
				config.getRelationColors().get(key.getPredicate()),
				key.isBidirectionalEdge());
		if (key.isOuter()) {
			boolean arrowHead = key.getSubject().isOuter();
			label = DOTRenderer.getOuterEdgeLabel(key.getPredicate(), arrowHead);
		}
		dotSource.append("\"")
				.append(key.getSubject().getName())
				.append("\"")
				.append(" -> ")
				.append("\"")
				.append(key.getObject().getName())
				.append("\" ")
				.append(label);
	}

	private static String clearLabel(String label) {
		String xsdStringAnnotation = "^^http://www.w3.org/2001/XMLSchema#string";
		if (label.endsWith(xsdStringAnnotation)) {
			label = label.substring(0, label.length() - xsdStringAnnotation.length());
		}
		return label;
	}

	private static String getOuterEdgeLabel(String relation, boolean showArrowHead) {
		// Relation Attributes
		String arrowhead = "arrowhead=\"none\" ";
		String arrowtail = "";
		if (showArrowHead) {
			arrowhead = "";
			arrowtail = "arrowtail = \"normal\" ";
		}
		String color = "#8b8989";
		String style = "dashed";

		return "[ label=\"" + relation
				+ "\" fontcolor=\"#8b8989\" " + arrowhead + arrowtail + " color=\"" + color
				+ "\" style=\"" + style + "\" ];\n";
	}

	private static String insertPrefixed(String dotSource, Config config) {
		String added = config.getDotAddLine();
		if (added != null) dotSource += added;

		return dotSource;
	}

	/**
	 * @created 30.10.2012
	 */
	private static String setSizeAndRankDir(Config.RankDir rankDirSetting, String width, String height, String graphSize, int numberOfConcepts) {
		String rankDir = rankDirSetting.name();
		String size = "";
		String ratio = "";

		if (width != null || height != null) {
			width = getSizeProperties(width);
			height = getSizeProperties(height);

			if (height != null && width != null) {
				// ratio = " ratio=\"" + Double.valueOf(height)/Double.valueOf(width) + "\" ";
				ratio = "ratio=\"fill\" ";
				size = width + "," + height + "!";
			}
			else if (height != null) {
				size = "10000000," + height + "!";
			}
			else {
				size = width + ",10000000";
			}
		}
		else if (graphSize != null) {
			if (graphSize.matches("\\d+px")) {
				graphSize = graphSize.substring(0, graphSize.length() - 2);
			}
			if (graphSize.matches("\\d+")) {
				size = String.valueOf(Double.valueOf(graphSize) * 0.010415597);
			}
			else {
				size = calculateAutomaticGraphSize(numberOfConcepts);
			}
		}
		else {
			if (numberOfConcepts == 1 || numberOfConcepts == 2) {
				size = calculateAutomaticGraphSize(numberOfConcepts);
			}
		}

		String source = "graph [ ";
		source += "rankdir=\"" + rankDir + "\" ";
		if (!Strings.isBlank(size)) {
			source += "size=\"" + size + "\" ";
		}
		if (!Strings.isBlank(ratio)) {
			source += ratio;
		}
		source += "]\n";

		return source;
	}

	private static String getSizeProperties(String property) {
		if (property != null) {
			if (property.matches("\\d+px")) {
				property = property.substring(0, property.length() - 2);
			}
			if (property.matches("\\d+")) {
				property = String.valueOf(Double.valueOf(property) * 0.010415597);
			}
		}
		return property;
	}

	private static String calculateAutomaticGraphSize(int numberOfConcepts) {
		if (numberOfConcepts == 1) return "1";
		if (numberOfConcepts == 2) return "3";
		return null;
	}

	/**
	 * The dot, svg and png files are created and written and returned (for easy cleanup).
	 *
	 * @created 20.08.2012
	 */
	static File[] createAndWriteFiles(Config config, String dotSource) {
		File dotFile = createFile("dot", getFilePath(config));
		File svgFile = createFile("svg", getFilePath(config));
		// File pngFile = createFile("png", filePath);

		boolean dotFileDeleted = dotFile.delete();
		boolean svgFileDeleted = svgFile.delete();
		if (!svgFileDeleted) {
			Log.warning("Could not delete file." + svgFile.getName());
		}
		// pngFile.delete();

		FileUtils.writeFile(dotFile, dotSource);

		// create svg

		try {
			convertDot(svgFile, dotFile, getSVGCommand(config, dotFile, svgFile));

			// convertDot(png, dot, createPngCommand(dotApp, dot, png));

			augmentSVG(svgFile);
		}
		catch (IOException e) {
			Log.warning("Exception while generating visualization", e);
		}
		return new File[] { dotFile, svgFile };
	}

	private static String[] getSVGCommand(Config config, File dot, File svg) {
		String dotApp = config.getDotApp();
		String layout = config.getLayout();
		if (layout != null) {
			return new String[] { dotApp, dot.getAbsolutePath(), "-Tsvg", "-o", svg.getAbsolutePath(), "-K" + layout.toLowerCase() };
		}
		else {
			return new String[] { dotApp, dot.getAbsolutePath(), "-Tsvg", "-o", svg.getAbsolutePath() };
		}
	}

	private static String createDotConceptLabel(RenderingStyle style, String targetURL, String targetLabel, boolean prepareLabel) {
		String newLineLabelValue;
		String url = "";
		if (targetURL != null) {
			// url = "URL=\"" + Strings.encodeHtml(targetURL) + "\" ";
		}
		if (prepareLabel) {
			// prevents HTML rendering !

		}

		newLineLabelValue = "[ " + url
				+ DOTRenderer.buildLabel(style) + "label="
				+ targetLabel + " ];\n";
		return newLineLabelValue;
	}

	/**
	 * Returns true, if a valid dot installation can be found.
	 */
	static boolean checkDotInstallation(Config config) {
		String dotApp = config.getDotApp();
		try {
			ProcessBuilder builder = new ProcessBuilder(dotApp, "-V");
			Process process = builder.start();
			process.waitFor(1, TimeUnit.SECONDS);
			int exitValue = process.exitValue();
			if (exitValue != 0) {
				return false;
			}
		}
		catch (IOException | InterruptedException e) {
			return false;
		}
		return true;
	}

	/**
	 * Adds the target-tag to every URL in the svg-file
	 *
	 * @created 01.08.2012
	 */
	private static void augmentSVG(File svg) throws IOException {
		Log.finest("Starting to augment SVG: " + svg.getAbsolutePath());
		try {
			// check if svg file is closed, otherwise wait timeout second
			long start = System.currentTimeMillis();
			while (!Utils.isFileClosed(svg)) {
				if ((System.currentTimeMillis() - start) > TIMEOUT) {
					Log.warning("Exceded timeout while waiting for SVG file to be closed.");
					return;
				}
			}

			Document doc = SAXBuilderSingleton.getInstance().build(svg);
			Element root = doc.getRootElement();
			if (root == null) return;

			findAndAugmentElements(root);

			XMLOutputter xmlOutputter = new XMLOutputter(Format.getPrettyFormat());
			xmlOutputter.output(doc, new FileWriter(svg));
			Log.finest("Finished augmenting SVG: " + svg.getAbsolutePath());
		}
		catch (JDOMException e) {
			Log.warning("Exception while augmenting SVG " + svg.getAbsolutePath(), e);
		}
	}

	/**
	 * Iterates through all the children of root to find all a-tag elements.
	 *
	 * @created 21.12.2013
	 */
	private static void findAndAugmentElements(Element root) {
		List<?> children = root.getChildren();
		Iterator<?> iter = children.iterator();
		//noinspection WhileLoopReplaceableByForEach
		while (iter.hasNext()) {
			Element childElement = (Element) iter.next();
			if (childElement.getName().equals("a")) {
				addTargetAttribute(childElement);
			}
			else {
				findAndAugmentElements(childElement);
			}
		}
	}

	/**
	 * Adds the target-attribute to the element.
	 *
	 * @created 21.12.2013
	 */
	private static void addTargetAttribute(Element element) {
		Attribute target = new Attribute("target", "_top");
		element.setAttribute(target);
	}

	private static void convertDot(File file, File dot, String... command) throws IOException {
		FileUtils.checkWriteable(file);
		FileUtils.checkReadable(dot);
		try {
			ProcessBuilder processBuilder = new ProcessBuilder(command);
			Process process = processBuilder.start();
			process.waitFor();
			int exitValue = process.exitValue();
			if (exitValue != 0) {
				FileUtils.printStream(process.getErrorStream());
				throw new IOException("Command could not successfully be executed: " + Strings.concat(" ", command));
			}

		}
		catch (InterruptedException e) {
			//Thread was interrupted by GraphReRenderer
			//Log.warning(e.getMessage(), e);
		}
	}

	private static File createFile(String type, String path) {
		String filename = path + "." + type;
		return new File(filename);
	}

}
