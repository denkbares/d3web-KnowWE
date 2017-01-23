package de.knowwe.diaflux.utils;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Set;

import org.jgrapht.ext.ImportException;
import org.jgrapht.graph.DirectedMultigraph;

/**
 * Converts a dotfile to DiaFlux-Markup.
 * <p>
 * See {@link Node} and {@link Edge} for observed attributes. {@link Node#posx} and {@link Node#posy} are generated from
 * attribute "pos" of nodes.
 *
 * @author Adrian Müller
 * @created 21.10.16
 */
public class DotToMarkupConverter {

	private final DirectedMultigraph<Node, Edge> graph = new DirectedMultigraph<>(Edge.class);
	private StringBuilder markup;
	private String name;
	private int fcid = 1, idCounter = 1;
	private double width, height;
	private boolean autostart;

	public StringBuilder toMarkup(String dotfile) throws IOException, ImportException {
		importGraph(dotfile);
		traceMaxIdFromSet(graph.vertexSet());
		traceMaxIdFromSet(graph.edgeSet());
		convertGraphToMarkup();
		return markup;
	}

	private void traceMaxIdFromSet(Set<? extends FCIDed> elements) {
		for (FCIDed e : elements) {
			int idNumber = getIdNumber(e);
			if (idNumber > idCounter) {
				idCounter = idNumber;
			}
		}
	}

	private int getIdNumber(FCIDed o) {
		try {
			return Integer.parseInt(o.fcid.substring(o.fcid.lastIndexOf("_") + 1));
		}
		catch (NumberFormatException e) {
			return 0;
		}
	}

	private void importGraph(String dot) throws ImportException {
		DOTImporter<Node, Edge> doti = new DOTImporter<>((s, map) -> {
			if ("graph".equals(s)) {
				String[] dims = map.get("bb").split(",");
				width = Double.parseDouble(dims[2]) * 2;
				height = Double.parseDouble(dims[3]);
				name = map.get("name");
				autostart = Boolean.parseBoolean(map.get("autostart"));
				return null;
			}
			if ("node".equals(s)) {
				return null;
			}
			Node n = new Node();
			n.calcPos(map.get("pos").split(","), height);
			n.fcid = map.get("fcid");
			n.name = map.get("name");
			n.label = map.get("label");
			// Will be null, if not available
			n.markup = map.get("markup");
			// Skip width and height

			return n;
		}, (node, v1, s, map) -> {
			// s is null
			Edge e = new Edge();
			e.fcid = map.get("fcid");
			// ignore pos
			e.origin = node;
			e.target = v1;
			e.guard = map.get("guard");
			e.markup = map.get("markup");
			return e;
		});
		doti.importGraph(graph, new StringReader(dot));
	}

	private StringBuilder convertGraphToMarkup() {
		markup = new StringBuilder();
		markup.append("%%DiaFlux\n").append("<flowchart fcid=\"flow_");
		createHeader();
		markup.append("\n");
		createNodeList();
		markup.append("\n");
		createEdgeList();
		markup.append("</flowchart>\n").append("%\n");
		return markup;
	}

	private void createHeader() {
		markup.append(Integer.toHexString(fcid))
				.append("\" name=\"")
				.append(fitEncoding(removeQuoteEscaping(name)))
				.append("\" width=\"")
				.append(width)
				.append("\" height=\"")
				.append(height + 42)
				.append("\" autostart=\"")
				.append(autostart)
				.append("\" idCounter=\"")
				.append(idCounter)
				.append("\">\n");
		fcid++;
	}

	private void createNodeList() {
		ArrayList<Node> nodeList = new ArrayList<>(graph.vertexSet());
		nodeList.sort(Comparator.comparingInt(this::getIdNumber));
		markup.append("\t<!-- nodes of the flowchart -->\n");
		for (Node node : nodeList) {
			markup.append("\t<node fcid=\"")
					.append(fitEncoding(node.fcid))
					.append("\">\n")
					.append("\t\t<position left=\"")
					.append(node.posx)
					.append("\" top=\"")
					.append(node.posy)
					.append("\"></position>\n");

			switch (node.label) {
				case "action":
					if (node.name.startsWith("INDICATED")) {
						node.name = node.name.substring(11, node.name.length() - 1);
					}
					markup.append("\t\t<action markup=\"")
							.append(node.markup)
							.append("\">");
					markup.append(removeQuoteEscaping(node.name));
					markup.append("</action>\n");
					break;
				case "start":
				case "decision":
				case "exit":
				case "comment":
				case "snapshot":
					markup.append("\t\t<")
							.append(node.label)
							.append(">");
					if ("decision".equals(node.label)) {
						markup.append(removeQuoteEscaping(node.name));
					}
					else {
						markup.append(fitEncoding(removeQuoteEscaping(node.name)));
					}
					markup.append("</").append(node.label).append(">\n");
					break;
				default:
					// should be only these
			}
			markup.append("\t</node>\n\n");
		}
	}

	private void createEdgeList() {
		ArrayList<Edge> edgeList = new ArrayList<>(graph.edgeSet());
		edgeList.sort(Comparator.comparingInt(this::getIdNumber));
		markup.append("\t<!-- rules of the flowchart -->\n");
		for (Edge edge : edgeList) {
			markup.append("\t<edge fcid=\"")
					.append(fitEncoding(edge.fcid))
					.append("\">\n")
					.append("\t\t<origin>")
					.append(fitEncoding(edge.origin.fcid))
					.append("</origin>\n")
					.append("\t\t<target>")
					.append(fitEncoding(edge.target.fcid))
					.append("</target>\n");
			if (edge.guard != null) {
				markup.append("\t\t<guard markup=\"")
						.append(removeQuoteEscaping(edge.markup))
						.append("\">");
				if (name.endsWith(" = known")) {
					name = name.substring(0, name.length() - " = known".length());
					markup.append("KNOWN[\"").append(removeQuoteEscaping(edge.guard)).append("\"]");
				}
				else {
					markup.append(removeQuoteEscaping(edge.guard));
				}
				markup.append("</guard>\n");
			}
			markup.append("\t</edge>\n\n");
		}
	}

	private String removeQuoteEscaping(String value) {
		return value.replace("\\\\", "\\").replace("\\\"", "\"");
	}

	private String fitEncoding(String value) {
		return value.replace("&", "&amp;").
				replace("\"", "&quot;").
				replace("'", "&apos;").
				replace("<", "&lt;").
				replace(">", "&gt;");
	}

	private static abstract class FCIDed {
		String fcid;
	}

	private static class Node extends FCIDed {
		double posx, posy;
		String name, label, markup;

		void calcPos(String[] pos, double graphHeight) {
			posx = Double.parseDouble(pos[0]) * 2;
			posy = Double.parseDouble(pos[1]);
			posy = -(posy) + graphHeight;
		}
	}

	private static class Edge extends FCIDed {
		String guard, markup;
		Node origin, target;
	}
}
