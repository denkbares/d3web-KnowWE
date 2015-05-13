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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
	public static final String outerLabel = "[ shape=\"none\" fontsize=\"0\" fontcolor=\"white\" ];\n";
	public static final int TIMEOUT = 50000;

	private static final Set<String> cleanedCacheDirectories = new HashSet<>();

	public static String getFilePath(Config config) {
		return getDirectoryPath(config) + config.getCacheFileID();
	}

	public static String getDirectoryPath(Config config) {
		String path = config.getCacheDirectoryPath() + FileUtils.FILE_SEPARATOR + FileUtils.KNOWWEEXTENSION_FOLDER + FileUtils.FILE_SEPARATOR
				+ FileUtils.TMP_FOLDER
				+ FileUtils.FILE_SEPARATOR;
		handleCleanup(path);
		return path;
	}

	/**
	 * Cleans up all cache directories once on startup.
	 */
	protected static void handleCleanup(String path) {
		if (cleanedCacheDirectories.add(path)) {
			File dir = new File(path);
			if (dir.exists() && dir.isDirectory()) {
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
	public static String createDotSources(SubGraphData data, Config config) {
		String dotSource = "digraph {\n";
		dotSource = insertPraefixed(dotSource, config);
		dotSource += DOTRenderer.setSizeAndRankDir(config.getRankDir(),
				config.getWidth(), config.getHeight(),
				config.getSize(), data.getConceptDeclarations().size());

		dotSource += generateGraphSource(data, config);

		dotSource += "}";

		return dotSource;
	}

	private static String generateGraphSource(SubGraphData data, Config config) {
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
						!config.isShowLabels()) {
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
			appendEdgeSource(config, dotSource, key);
		}

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

	private static void appendEdgeSource(Config config, StringBuilder dotSource, Edge key) {
		String label = DOTRenderer.innerRelation(key.getPredicate(),
				config.getRelationColors());
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

	private static String insertPraefixed(String dotSource, Config config) {
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
	public static File[] createAndWriteFiles(Config config, String dotSource) {
		File dotFile = createFile("dot", getFilePath(config));
		File svgFile = createFile("svg", getFilePath(config));
		// File pngFile = createFile("png", filePath);

		dotFile.delete();
		svgFile.delete();
		// pngFile.delete();

		FileUtils.writeFile(dotFile, dotSource);

		// create svg

		try {
			convertDot(svgFile, dotFile, getSVGCommand(config.getDotApp(), dotFile, svgFile));

			// convertDot(png, dot, createPngCommand(dotApp, dot, png));

			augmentSVG(svgFile);
		}
		catch (IOException e) {
			Log.warning("Exception while generating visualization", e);
		}
		return new File[] { dotFile, svgFile };
	}

	protected static String[] getSVGCommand(String dotApp, File dot, File svg) {
		return new String[] { dotApp, dot.getAbsolutePath(), "-Tsvg", "-o", svg.getAbsolutePath() };
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
	public static boolean checkDotInstallation(Config config) {
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
