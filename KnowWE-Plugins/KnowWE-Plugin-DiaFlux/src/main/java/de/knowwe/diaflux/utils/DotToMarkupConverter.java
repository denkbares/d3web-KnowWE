package de.knowwe.diaflux.utils;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Set;

import org.jgrapht.ext.ImportException;
import org.jgrapht.graph.DirectedMultigraph;

/**
 * Converts a dotfile to DiaFlux-Markup. A dotfile should contain only ONE flow. An object of this class can be reused
 * for multiple flows by calling {@link #toMarkup(String)} multiple times.
 * <p>
 * See {@link Node} and {@link Edge} for observed attributes.
 *
 * @author Adrian Müller
 * @created 21.10.16
 */
public class DotToMarkupConverter extends AbstractDiaFluxConverter {

	private static final DOTImporter<NamedNode, Edge> doti = new DOTImporter<>((s, map) -> {
		if ("graph".equals(s)) {
			Header h = new Header();
			String[] dims = map.get("bb").split(",");
			h.width = Double.parseDouble(dims[2]) * 2;
			h.height = Double.parseDouble(dims[3]);
			h.name = map.get("name");
			h.autostart = Boolean.parseBoolean(map.get("autostart"));
			h.fcid = map.get("fcid");
			return h;
		}
		if (map.get("fcid") == null) {
			return null;
		}
		Node n = new Node();
		String[] pos = map.get("pos").split(",");
		n.posx = Double.parseDouble(pos[0]);
		n.posy = Double.parseDouble(pos[1]);
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

	private DirectedMultigraph<NamedNode, Edge> graph;
	private int fcid = 1, idCounter = 1;
	private Header h;

	public StringBuilder toMarkup(String dotfile) throws IOException, ImportException {
		idCounter = 1;
		graph = new DirectedMultigraph<>(Edge.class);
		doti.importGraph(graph, new StringReader(dotfile));
		findHeader();
		traceMaxIdFromSet(graph.vertexSet());
		traceMaxIdFromSet(graph.edgeSet());
		return convert();
	}

	private void findHeader() throws ImportException {
		for (NamedNode n : graph.vertexSet()) {
			if (n instanceof Header) {
				h = (Header) n;
				break;
			}
		}
		if (h == null) {
			throw new ImportException("Head of graph is missing.");
		}
		graph.removeVertex(h);
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

	private StringBuilder convert() {
		return convert("%%DiaFlux", "%");
	}

	@Override
	protected void createHeader() {
		res.append("<flowchart fcid=\"flow_")
				.append(Integer.toHexString(fcid))
				.append("\" name=\"")
				.append(fitEncoding(removeQuoteEscaping(h.name)))
				.append("\" width=\"")
				.append(h.width)
				.append("\" height=\"")
				.append(h.height + 42)
				.append("\" autostart=\"")
				.append(h.autostart)
				.append("\" idCounter=\"")
				.append(idCounter)
				.append("\">\n");
		fcid++;
	}

	@Override
	protected void createNodeList() {
		ArrayList<Node> nodeList = new ArrayList<>();
		for (NamedNode n : graph.vertexSet()) {
			if (n instanceof Node) {
				nodeList.add((Node) n);
			}
		}
		nodeList.sort(Comparator.comparingInt(this::getIdNumber));
		res.append("\t<!-- nodes of the flowchart -->\n");
		for (Node node : nodeList) {
			node.calcPos(h.height);
			res.append("\t<node fcid=\"")
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
					res.append("\t\t<action markup=\"")
							.append(node.markup)
							.append("\">");
					res.append(removeQuoteEscaping(node.name));
					res.append("</action>\n");
					break;
				case "start":
				case "decision":
				case "exit":
				case "comment":
				case "snapshot":
					res.append("\t\t<")
							.append(node.label)
							.append(">");
					if ("decision".equals(node.label)) {
						res.append(removeQuoteEscaping(node.name));
					}
					else {
						res.append(fitEncoding(removeQuoteEscaping(node.name)));
					}
					res.append("</").append(node.label).append(">\n");
					break;
				default:
					// should be only these
			}
			res.append("\t</node>\n\n");
		}
	}

	@Override
	protected void createEdgeList() {
		ArrayList<Edge> edgeList = new ArrayList<>(graph.edgeSet());
		edgeList.sort(Comparator.comparingInt(this::getIdNumber));
		res.append("\t<!-- rules of the flowchart -->\n");
		for (Edge edge : edgeList) {
			res.append("\t<edge fcid=\"")
					.append(fitEncoding(edge.fcid))
					.append("\">\n")
					.append("\t\t<origin>")
					.append(fitEncoding(edge.origin.fcid))
					.append("</origin>\n")
					.append("\t\t<target>")
					.append(fitEncoding(edge.target.fcid))
					.append("</target>\n");
			if (edge.guard != null) {
				String guard = edge.guard;
				res.append("\t\t<guard markup=\"")
						.append(removeQuoteEscaping(edge.markup))
						.append("\">");
				if (guard.endsWith(" = known")) {
					guard = guard.substring(0, guard.length() - " = known".length());
					res.append("KNOWN[\"").append(removeQuoteEscaping(guard)).append("\"]");
				}
				else {
					res.append(removeQuoteEscaping(edge.guard));
				}
				res.append("</guard>\n");
			}
			res.append("\t</edge>\n\n");
		}
		res.append("</flowchart>\n");
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

	private static class NamedNode extends FCIDed {
		String name;
	}

	private static class Node extends NamedNode {
		double posx, posy;
		String label, markup;

		void calcPos(double graphHeight) {
			posx = posx * 2;
			posy = posy * 1;
			posy = -(posy) + graphHeight;
		}
	}

	private static class Edge extends FCIDed {
		String guard, markup;
		NamedNode origin, target;
	}

	private static class Header extends NamedNode {
		double width, height;
		boolean autostart;
	}
}
