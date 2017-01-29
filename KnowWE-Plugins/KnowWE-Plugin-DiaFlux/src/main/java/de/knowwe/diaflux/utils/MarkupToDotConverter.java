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

import com.denkbares.strings.Strings;
import com.denkbares.utils.XMLUtils;

/**
 * @author Adrian Müller
 * @created 10.01.17
 * <p>
 */
public class MarkupToDotConverter extends AbstractDiaFluxToDotConverter {

	private Node head;
	private List<Node> nodes, edges;

	public String toDot(String markup) throws IOException {
		Document doc = XMLUtils.streamToDocument(new ByteArrayInputStream(markup.getBytes(StandardCharsets.UTF_8)));
		head = doc.getFirstChild();
		NodeList list = head.getChildNodes();
		nodes = new ArrayList<>();
		edges = new ArrayList<>();
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
		return convert();
	}

	@Override
	protected void createHeader() {
		NamedNodeMap attr = head.getAttributes();
		res.append("\tgraph [");
		res.append("name=\"")
				.append(escapeQuoteAndBackslash(attr.getNamedItem("name").getNodeValue()))
				.append("\",\n");
		res.append("\t\tautostart=\"")
				.append(attr.getNamedItem("autostart").getNodeValue())
				.append("\"];");
	}

	@Override
	protected void createNodeList() {
		for (Node node : nodes) {
			String label = "", name = "";
			String fcid = node.getAttributes().getNamedItem("fcid").getNodeValue();
			res.append("\t").append(Strings.quote(fcid)).append("\t [");
			for (int i = 0; i < node.getChildNodes().getLength(); i++) {
				Node attr = node.getChildNodes().item(i);
				switch (attr.getNodeName()) {
					case "action":
						String markup = node.getChildNodes()
								.item(3)
								.getAttributes()
								.getNamedItem("markup")
								.getNodeValue();
						res.append("markup=\"").append(escapeQuoteAndBackslash(markup)).append("\"");
						res.append(",\n\t\t");
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
			res.append("label=\"").append(label).append("\"");
			res.append(",\n\t\tfcid=\"").append(escapeQuoteAndBackslash(fcid)).append("\"");
			res.append(",\n\t\tname=\"").append(escapeQuoteAndBackslash(name)).append("\"");
			res.append("];\n");
		}
	}

	@Override
	protected void createEdgeList() {
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
			res.append("\t")
					.append(Strings.quote(attrs.get("origin")))
					.append(" -> ")
					.append(Strings.quote(attrs.get("target")))
					.append("\t [")
					.append("fcid=\"")
					.append(escapeQuoteAndBackslash(edge.getAttributes().getNamedItem("fcid").getNodeValue()))
					.append("\"");
			String markup = attrs.get("markup");
			if (markup != null) {
				res.append(",\n\t\tmarkup=\"")
						.append(markup)
						.append("\"")
						.append(",\n\t\tguard=\"")
						.append(escapeQuoteAndBackslash(attrs.get("guard")))
						.append("\"");
			}
			res.append("];\n");
		}
	}

}
