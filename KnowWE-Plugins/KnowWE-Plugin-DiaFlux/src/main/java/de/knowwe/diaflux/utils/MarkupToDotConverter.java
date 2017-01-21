package de.knowwe.diaflux.utils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.denkbares.utils.XMLUtils;

/**
 * @author Adrian Müller
 * @created 10.01.17
 * <p>
 */
public class MarkupToDotConverter {

	private StringBuilder dot;

	public String toDot(String markup) throws IOException {
		Document doc = XMLUtils.streamToDocument(new ByteArrayInputStream(markup.getBytes(StandardCharsets.UTF_8)));
		Node flowchart = doc.getFirstChild();
		NodeList list = flowchart.getChildNodes();
		List<Node> nodes = new ArrayList<>(), edges = new ArrayList<>();
		for (int i = 0; i < list.getLength(); i++) {
			Node n = list.item(i);
			switch (n.getNodeName()) {
				case "node":
					nodes.add(n);
					break;
				case "edge":
					edges.add(n);
					break;
				default:
					// ignore
			}
		}
		dot = new StringBuilder();
		dot.append("digraph G {\n");
		createHeader(flowchart.getAttributes());
		dot.append("\n");
		createNodeList(nodes);
		dot.append("\n");
		createEdgeList(edges);
		dot.append("}\n");
		return dot.toString();
	}

	private void createHeader(NamedNodeMap attr) {
		dot.append("\tgraph [");
		dot.append("name=\"")
				.append(replaceQuotWithEscapedQuot(attr.getNamedItem("name").getNodeValue()))
				.append("\",\n");
		dot.append("\t\tautostart=\"")
				.append(attr.getNamedItem("autostart").getNodeValue())
				.append("\"];");
	}

	private void createNodeList(List<Node> nodes) {
		for (Node node : nodes) {
			String label = "", name = "";
			String fcid = node.getAttributes().getNamedItem("fcid").getNodeValue();
			dot.append("\t").append(fcid.substring(1)).append("\t [");
			for (int i = 0; i < node.getChildNodes().getLength(); i++) {
				Node attr = node.getChildNodes().item(i);
				switch (attr.getNodeName()) {
					case "action":
						String markup = node.getChildNodes()
								.item(3)
								.getAttributes()
								.getNamedItem("markup")
								.getNodeValue();
						dot.append("markup=\"").append(replaceQuotWithEscapedQuot(markup)).append("\"");
						dot.append(",\n\t\t");
						// fallthrough
					case "decision":
					case "start":
					case "exit":
					case "comment":
					case "snapshot":
						label = attr.getNodeName();
						name = attr.getFirstChild().getNodeValue();
						break;
					default:
						// no other
				}
			}
			dot.append("label=\"").append(label).append("\"");
			dot.append(",\n\t\tfcid=\"").append(replaceQuotWithEscapedQuot(fcid)).append("\"");
			dot.append(",\n\t\tname=\"").append(replaceQuotWithEscapedQuot(name)).append("\"");
			dot.append("];\n");
		}
	}

	private void createEdgeList(List<Node> edges) {
		for (Node edge : edges) {
			Map<String, String> attrs = new HashMap<>();
			for (int i = 0; i < edge.getChildNodes().getLength(); i++) {
				Node attr = edge.getChildNodes().item(i);
				switch (attr.getNodeName()) {
					case "guard":
						attrs.put("markup", attr.getAttributes().getNamedItem("markup").getNodeValue());
						// fallthrough
					case "origin":
					case "target":
						attrs.put(attr.getNodeName(), attr.getFirstChild().getNodeValue());
						break;
					default:
						// no other
				}
			}
			dot.append("\t")
					.append(attrs.get("origin").substring(1))
					.append(" -> ")
					.append(attrs.get("target").substring(1))
					.append("\t [")
					.append("fcid=\"")
					.append(replaceQuotWithEscapedQuot(edge.getAttributes().getNamedItem("fcid").getNodeValue()))
					.append("\"");
			if (attrs.containsKey("markup")) {
				dot.append(",\n\t\tmarkup=\"KnOffice\"")
						.append(",\n\t\tguard=\"")
						.append(replaceQuotWithEscapedQuot(attrs.get("guard")))
						.append("\"");
			}
			dot.append("];\n");
		}
	}

	private String replaceQuotWithEscapedQuot(String value) {
		return value.replace("\\", "\\\\").replace("\"", "\\\"").
				replace("<", "&lt;").
				replace(">", "&gt;");
	}
}