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
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import de.d3web.strings.Strings;
import de.d3web.utils.Log;
import de.knowwe.visualization.ConceptNode;
import de.knowwe.visualization.Edge;
import de.knowwe.visualization.GraphDataBuilder;
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
	public static final String outerLabel = "[ shape=\"none\" fontsize=\"0\" fontcolor=\"white\" ];\n";

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
	private static String innerRelation(String label, String relationColorCodes) {
		// Basic Relation Attributes
		String arrowtail = "normal";

		String color = Utils.getColorCode(label, relationColorCodes);
		if (color == null) {
			// black is default
			color = "black";
		}
		return "[ label = \"" + label
				+ "\"" + buildRelation(arrowtail, color) + " ];\n";
	}

	/**
	 * The sources from the maps are being written into the String-dotSource.
	 *
	 * @created 18.08.2012
	 */
	public static String createDotSources(SubGraphData data, Map<String, String> parameters) {
		String graphtitle = "Konzeptuebersicht";
		String dotSource = "digraph " + graphtitle + " {\n";
		dotSource = insertPraefixed(dotSource, parameters);
		dotSource += DOTRenderer.setSizeAndRankDir(parameters.get(GraphDataBuilder.RANK_DIRECTION),
				parameters.get(GraphDataBuilder.GRAPH_WIDTH), parameters.get(GraphDataBuilder.GRAPH_HEIGHT),
				parameters.get(GraphDataBuilder.GRAPH_SIZE), data.getConceptDeclarations().size());

		dotSource += generateGraphSource(data, parameters);

		dotSource += "}";

		return dotSource;
	}

	private static String generateGraphSource(SubGraphData data, Map<String, String> parameters) {
		Collection<ConceptNode> dotSourceLabel = data.getConceptDeclarations();
		final Map<ConceptNode, Set<Edge>> clusters = data.getClusters();
		StringBuilder dotSource = new StringBuilder();

		// iterate over the labels and add them to the dotSource
		for (ConceptNode node : dotSourceLabel) {

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
				//nodeLabel = "<<B>"+nodeLabel+"</B>>";

				if ((!node.getType().equals(NODE_TYPE.LITERAL)) &&
						parameters.get(GraphDataBuilder.USE_LABELS) != null && parameters.get(GraphDataBuilder.USE_LABELS)
						.equals("false")) {
					// use of labels suppressed by the user -> show concept name, i.e. uri
					nodeLabel = createHTMLTable(node, data, style.getFontstyle());
				}
				else {
					nodeLabel = createNodeLabel(nodeLabel, style.getFontstyle());
				}

				label = DOTRenderer.createDotConceptLabel(style, node.getConceptUrl(), nodeLabel, true);
			}
			dotSource.append("\"" + node.getName() + "\"" + label);

		}

		/*
		iterate over the relations and add them to the dotSource
		 */

		// add level zero "default cluster"
		for (Edge key : clusters.get(ConceptNode.DEFAULT_CLUSTER_NODE)) {
			appendEdgeSource(parameters, dotSource, key);
		}

		// add real clusters
		/*
		final Set<ConceptNode> clusterNodes = clusters.keySet();
		for (ConceptNode clusterNode : clusterNodes) {
			if(clusterNode != ConceptNode.DEFAULT_CLUSTER_NODE) {
				// create graph cluster
				final Set<Edge> clusterEdges = clusters.get(clusterNode);
				if(clusterEdges.size() > 0) {
					String clusterName = "subgraph cluster_"+Math.abs(clusterNode.getName().hashCode());
					dotSource.append(clusterName +" {\n");
					dotSource.append("color=black;\n" +
							"style = \"dotted,rounded\";\n");
					String clazz = clusterNode.getClazz();
					if(clazz != null && clazz.length() > 0) {
						dotSource.append("label =<<B>"+clazz+"</B>>\n");
					}
					for (Edge clusterEdge : clusterEdges) {
						appendEdgeSource(parameters, dotSource, clusterEdge);
					}
					dotSource.append("}\n");
				}
			}
		}
		*/
		return dotSource.toString();
	}

	private static String createHTMLTable(ConceptNode node, SubGraphData data, RenderingStyle.Fontstyle f) {
		final Map<ConceptNode, Set<Edge>> clusters = data.getClusters();
		final Set<Edge> edges = clusters.get(node);
		String nodeLabel = createNodeLabel(node.getName(), f);

		if (edges == null || edges.size() == 0) {
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

			String conceptName = nodeLabel.replace("\\n", "<BR ALIGN=\"CENTER\"/>").replace("&", "&amp;");

			buffy.append("<TD BORDER=\"2\">");
			buffy.append("<B>");
			buffy.append(Strings.unquote(conceptName).replace("&", "&amp;"));
			buffy.append("</B>");
			buffy.append("</TD>");
			buffy.append("</TR>");

			for (Edge edge : edges) {
				buffy.append("<TR>");
				buffy.append("<TD BORDER=\"1\">");
				buffy.append(edge.getPredicate().replace("&", "&amp;"));
				buffy.append("</TD>");

				buffy.append("<TD BORDER=\"1\">");
				buffy.append(edge.getObject().getConceptLabel().replace("&", "&amp;"));
				buffy.append("</TD>");
				buffy.append("</TR>");
			}
			buffy.append("</TABLE>");
			buffy.append(">");  // start html label
			return buffy.toString();
		}
	}

	private static String createNodeLabel(String name, RenderingStyle.Fontstyle f) {
		String nodeLabel = Utils.prepareLabel(name);
		if (f != RenderingStyle.Fontstyle.NORMAL) {
			nodeLabel = styleLabel(nodeLabel, f);
		}
		else {
			nodeLabel = "\"" + nodeLabel + "\"";
		}
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

	private static void appendEdgeSource(Map<String, String> parameters, StringBuilder dotSource, Edge key) {
		String label = DOTRenderer.innerRelation(key.getPredicate(),
				parameters.get(GraphDataBuilder.RELATION_COLOR_CODES));
		if (key.isOuter()) {
			boolean arrowHead = key.getSubject().isOuter();
			label = DOTRenderer.getOuterEdgeLabel(key.getPredicate(), arrowHead);
		}
		dotSource.append("\"" + key.getSubject().getName() + "\"" + " -> " + "\""
				+ key.getObject().getName() + "\" "
				+ label);
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

	private static String insertPraefixed(String dotSource, Map<String, String> parameters) {
		String added = parameters.get(GraphDataBuilder.ADD_TO_DOT);
		if (added != null) dotSource += added;

		return dotSource;
	}

	/**
	 * @created 30.10.2012
	 */
	private static String setSizeAndRankDir(String rankDirSetting, String width, String height, String graphSize, int numberOfConcepts) {
		String rankDir = "LR";
		String size = "";
		String ratio = "";

		if (rankDirSetting != null) {
			rankDir = rankDirSetting;
		}

		if (width != null || height != null) {
			if (width != null) {
				if (width.matches("\\d+px")) {
					width = width.substring(0, width.length() - 2);
				}
				if (width.matches("\\d+")) {
					width = String.valueOf(Double.valueOf(width) * 0.010415597);
				}
			}

			if (height != null) {
				if (height.matches("\\d+px")) {
					height = height.substring(0, height.length() - 2);
				}
				if (height.matches("\\d+")) {
					height = String.valueOf(Double.valueOf(height) * 0.010415597);
				}
			}

			if (height != null && width != null) {
				// ratio = " ratio=\"" + Double.valueOf(height)/Double.valueOf(width) + "\" ";
				ratio = "ratio=\"fill\" ";
				size = width + "," + height + "!";
			}
			else if (height != null) {
				size = "10000000," + height + "!";
			}
			else if (width != null) {
				size = width + ",10000000!";
			}
		}
		else if (graphSize != null) {
			if (graphSize.matches("\\d+px")) {
				graphSize = graphSize.substring(0, graphSize.length() - 2);
			}
			if (graphSize.matches("\\d+")) {
				size = String.valueOf(Double.valueOf(graphSize) * 0.010415597) + "!";
			}
			else {
				size = calculateAutomaticGraphSize(numberOfConcepts) + "!";
			}
		}
		else {
			if (numberOfConcepts == 1 || numberOfConcepts == 2) {
				size = calculateAutomaticGraphSize(numberOfConcepts) + "!";
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

	private static String calculateAutomaticGraphSize(int numberOfConcepts) {
		if (numberOfConcepts == 1) return "1";
		if (numberOfConcepts == 2) return "3";
		return null;
	}

	public static void deleteVisualizationFiles(String filePrefix) {
		File dot = createFile("dot", filePrefix);
		File svg = createFile("svg", filePrefix);
		File png = createFile("png", filePrefix);

		dot.delete();
		svg.delete();
		png.delete();
	}

	/**
	 * The dot, svg and png files are created and written.
	 *
	 * @created 20.08.2012
	 */
	public static void createAndwriteDOTFiles(String filePath, String dotSource, String user_app_path) {
		File dot = createFile("dot", filePath);
		File svg = createFile("svg", filePath);
		File png = createFile("png", filePath);

		dot.delete();
		svg.delete();
		png.delete();

		FileUtils.writeFile(dot, dotSource);
		// create svg

        /*
		We try to call the dot process at low priority not to slow down the machine
         */
		String lowPriorityCall = "";
		if (Utils.isMac()) {
			lowPriorityCall = "nice -19n ";
		}
		else if (Utils.isWindows()) {
			// TODO: find windows way to start process on low priority
			lowPriorityCall = "";
		}
		else {
			// assume to be linux
			lowPriorityCall = "nice -n 19 ";
		}

		String dotApp = getDOTApp(user_app_path);
		boolean exists = new File(dotApp).exists();
		if (!exists) {
			dotApp = "dot";
		}

		String command = lowPriorityCall + dotApp + " " + dot.getAbsolutePath() +
				" -Tsvg -o " + svg.getAbsolutePath() + "";
		if (Utils.isWindows()) {
			command = lowPriorityCall + dotApp + " \"" + dot.getAbsolutePath() +
					"\" -Tsvg -o \"" + svg.getAbsolutePath() + "\"";
		}

		try {

			createFileOutOfDot(svg, dot, command);

			// create png
			command = dotApp + " " + dot.getAbsolutePath() +
					" -Tpng -o " + png.getAbsolutePath() + "";
			if (Utils.isWindows()) {
				command = dotApp + " \"" + dot.getAbsolutePath() +
						"\" -Tpng -o \"" + png.getAbsolutePath() + "\"";
			}

			createFileOutOfDot(png, dot, command);
			int timeout = 50000;
			prepareSVG(svg, timeout);
		}
		catch (FileNotFoundException e) {
			Log.warning(e.getMessage(), e);
		}
		catch (IOException e) {
			Log.warning(e.getMessage(), e);
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

	private static String getDOTApp(String user_def_app) {
		ResourceBundle rb = ResourceBundle.getBundle("dotInstallation");
		String app = rb.getString("path");
		if (user_def_app != null) {
			if (app.endsWith(FileUtils.FILE_SEPARATOR)) {
				app += user_def_app;
			}
			else {
				app = app.substring(0, app.lastIndexOf(FileUtils.FILE_SEPARATOR))
						+ FileUtils.FILE_SEPARATOR
						+ user_def_app;
			}
		}
		return app;
	}

	/**
	 * Adds the target-tag to every URL in the svg-file
	 *
	 * @created 01.08.2012
	 */
	private static void prepareSVG(final File svg, final int timeout) throws IOException {
		Log.finest("Starting write SVG: " + svg.getAbsolutePath());
		try {

			// check if svg file is closed, otherwise wait timeout second
			ScheduledExecutorService executor = Executors.newScheduledThreadPool(2);
			final Future<Boolean> handler = executor.submit(new Callable() {
				@Override
				public Boolean call() throws Exception {
					while (!Utils.isFileClosed(svg)) {
						// wait
					}
					return true;
				}
			});

			// cancel handler after timeout seconds
			executor.schedule(new Runnable() {
				@Override
				public void run() {
					handler.cancel(true);
				}
			}, timeout, TimeUnit.MILLISECONDS);

			// svg hasn't been closed yet, return
			if (!handler.get()) return;

			Document doc = SAXBuilderSingleton.getInstance().build(svg);
			Element root = doc.getRootElement();
			if (root == null) return;

			findAElements(root);

			XMLOutputter xmlOutputter = new XMLOutputter(Format.getPrettyFormat());
			xmlOutputter.output(doc, new FileWriter(svg));
			Log.finest("Finished writing SVG: " + svg.getAbsolutePath());
		}
		catch (JDOMException e) {
			Log.warning(e.getMessage(), e);
		}
		catch (InterruptedException e) {
			Log.warning(e.getMessage(), e);
		}
		catch (ExecutionException e) {
			Log.warning(e.getMessage(), e);
		}
	}

	/**
	 * Iterates through all the children of root to find all a-tag elements.
	 *
	 * @created 21.12.2013
	 */
	private static void findAElements(Element root) {
		List<?> children = root.getChildren();
		Iterator<?> iter = children.iterator();
		while (iter.hasNext()) {
			Element childElement = (Element) iter.next();
			if (childElement.getName().equals("a")) {
				addTargetAttribute(childElement);
			}
			else {
				findAElements(childElement);
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

	private static void createFileOutOfDot(File file, File dot, String command) throws IOException {
		FileUtils.checkWriteable(file);
		FileUtils.checkReadable(dot);
		try {
			Process process = Runtime.getRuntime().exec(command);
			process.waitFor();
			int exitValue = process.exitValue();
			if (exitValue != 0) {
				FileUtils.printStream(process.getErrorStream());
				throw new IOException("Command could not successfully be executed: " + command);
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
