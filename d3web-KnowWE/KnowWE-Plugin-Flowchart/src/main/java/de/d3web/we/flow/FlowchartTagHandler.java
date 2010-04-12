package de.d3web.we.flow;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import de.d3web.core.session.CaseObjectSource;
import de.d3web.core.session.Session;
import de.d3web.diaFlux.flow.DiaFluxCaseObject;
import de.d3web.diaFlux.flow.EdgeSupport;
import de.d3web.diaFlux.flow.FlowSet;
import de.d3web.diaFlux.flow.INode;
import de.d3web.diaFlux.flow.ISupport;
import de.d3web.diaFlux.inference.FluxSolver;
import de.d3web.diaFlux.inference.PathEntry;
import de.d3web.we.core.KnowWEArticleManager;
import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.flow.type.FlowchartType;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.xml.AbstractXMLObjectType;
import de.d3web.we.logging.Logging;
import de.d3web.we.taghandler.AbstractTagHandler;
import de.d3web.we.utils.D3webUtils;
import de.d3web.we.wikiConnector.KnowWEUserContext;

/**
 * [{KnowWEPlugin Flowchart}]
 * 
 * @author Florian Ziegler
 */
public class FlowchartTagHandler extends AbstractTagHandler {

	public FlowchartTagHandler() {
		super("flowchart");
		Logging.getInstance().addHandlerToLogger(
				Logging.getInstance().getLogger(), "flowchartTagHandler.txt");
	}

	@Override
	public String render(String topic, KnowWEUserContext user,
			Map<String, String> values, String web) {

		Session theCase = D3webUtils.getXPSCase(topic, user, web);

		if (!FluxSolver.getInstance().isFlowCase(theCase)) {
			return "No Flowchart found.";
		}

		KnowWEArticleManager artManager = KnowWEEnvironment.getInstance().getArticleManager(web);
		KnowWEArticle article = artManager.getArticle(topic);

		List<Section<FlowchartType>> flows = new ArrayList<Section<FlowchartType>>();

		article.getSection().findSuccessorsOfType(FlowchartType.class, flows);

		StringBuilder builder = new StringBuilder();

		if (flows.isEmpty()) {
			builder.append("No Flowcharts found in KB.");
		}

		// Debug
		if (isDebug(user.getUrlParameterMap())) builder.append(getPathendText(theCase));
		//

		for (Section<FlowchartType> section : flows) {

			Map<String, String> attributeMap = AbstractXMLObjectType.getAttributeMapFor(section);
			String name = attributeMap.get("name");

			builder.append("<div>");
			builder.append("<h3>");
			builder.append("Diagnostic Flow '");
			builder.append(name);
			builder.append("'");
			builder.append("</h3>");

			if (isActive(section, theCase)) {
				builder.append(createPreviewWithHighlightedPath(section, theCase));
			}

			builder.append("</div>");
			builder.append("<p/><p/>");

		}

		return builder.toString();
	}

	private boolean isActive(Section section, Session theCase) {

		// TODO
		// String flowID =
		// AbstractXMLObjectType.getAttributeMapFor(section).get("id");
		//		
		// CaseObjectSource flowSet = FluxSolver.getFlowSet(theCase);
		//		
		// DiaFluxCaseObject caseObject = (DiaFluxCaseObject)
		// theCase.getCaseObject(flowSet);
		//       
		return true;
	}

	private String getPathendText(Session theCase) {

		if (theCase == null) return "";

		FlowSet set = FluxSolver.getFlowSet(theCase);

		DiaFluxCaseObject caseObject = (DiaFluxCaseObject) theCase.getCaseObject(set);
		List<PathEntry> pathEnds = caseObject.getPathEnds();

		StringBuilder builder = new StringBuilder();

		builder.append("<b>Current Pathends:</b>");
		builder.append("<br/>");
		builder.append(pathEnds);
		builder.append("<br/>");
		builder.append("<br/>");
		builder.append("Pathes:");

		int i = 0;

		for (PathEntry start : pathEnds) {

			builder.append(++i + ". Path:");
			builder.append("<br/>");
			PathEntry entry = start;

			while (entry != null) {
				builder.append(entry);
				builder.append("<br/>");
				entry = entry.getPath();

			}

		}

		builder.append("<br/>");

		return builder.toString();
	}

	private String createPreviewWithHighlightedPath(Section section, Session xpsCase) {

		String preview = FlowchartUtils.extractPreview(section);

		if (xpsCase == null) return preview;

		String flowID = AbstractXMLObjectType.getAttributeMapFor(section).get("fcid");

		CaseObjectSource flowSet = FluxSolver.getFlowSet(xpsCase);

		DiaFluxCaseObject caseObject = (DiaFluxCaseObject) xpsCase.getCaseObject(flowSet);

		for (PathEntry entry : caseObject.getPathEnds()) {

			preview = highlightPath(preview, flowID, entry);

		}

		return FlowchartUtils.createPreview(preview);
	}

	private String highlightPath(String preview, String flowID, PathEntry startEntry) {
		// get all the nodes
		String[] nodes = preview.split("<DIV class=\"Node\" id=\"");
		String[] edges = preview.split("<DIV class=\"Rule\" id=\"");

		PathEntry currentEntry = startEntry;

		while (currentEntry != null) {

			INode node = currentEntry.getNode();

			if (!node.getFlow().getId().equals(flowID)) return preview;

			String nodeId = node.getID();
			for (int i = 1; i < nodes.length; i++) {
				if (nodes[i].contains(nodeId + "\"")) {
					preview = colorNode(nodes[i], preview);
				}
			}

			ISupport support = currentEntry.getSupport();
			if ((support instanceof EdgeSupport)) {

				String edgeId = ((EdgeSupport) support).getEdge().getID();

				for (int i = 0; i < edges.length; i++) {
					if (edges[i].contains(edgeId + "\"")) {
						preview = colorEdge(edges[i], preview);
					}
				}
			}

			currentEntry = currentEntry.getPath();

		}
		return preview;
	}

	private String colorNode(String node, String preview) {

		// is node in current flowchart?
		// TODO as FC change along PathEntries, the node might not be in the
		// current FC
		int nodeIndex = preview.indexOf(node);

		if (nodeIndex == -1) return preview;

		// if yes, add the additional class
		String inputHelper1 = preview.substring(0, nodeIndex - 6);
		String inputHelper2 = preview.substring(preview.indexOf(node));
		preview = inputHelper1 + " added" + "\" id=\"" + inputHelper2;

		return preview;
	}

	private String colorEdge(String edge, String preview) {
		// set the additional class of the yet to be colored nodes
		String alteration = "added";

		String temp = preview;

		String[] parts = edge.split("<DIV class=\"");
		for (String s : parts) {
			String type = s.substring(0, s.indexOf("\""));

			// for simple lines
			if (type.equals("h_line") || type.equals("v_line") || type.equals("no_arrow")) {
				String inputHelper1 = temp.substring(0, temp.indexOf(s));
				String inputHelper2 = temp.substring(temp.indexOf(s));
				temp = inputHelper1 + alteration + "\" id=\"" + inputHelper2;

				// for arrows
			}
			else if (type.equals("arrow_up") || type.equals("arrow_down")
					|| type.equals("arrow_left") || type.equals("arrow_right")) {
				int size = type.length();
				String arrowAlteration = "_" + alteration;
				String inputHelper1 = temp.substring(0, temp.indexOf(s) + size);
				String inputHelper2 = temp.substring(temp.indexOf(s));
				temp = inputHelper1 + arrowAlteration + "\" id=\"" + inputHelper2;

				// for the rest
			}
			else if (type.equals("GuardPane") || type.equals("value")) {
				// Logging.getInstance().log(Level.INFO, "type: " + type);
				String inputHelper1 = temp.substring(0, temp.indexOf(s));
				String inputHelper2 = temp.substring(temp.indexOf(s));
				temp = inputHelper1 + alteration + "Text\" id=\"" + inputHelper2;
			}
		}

		return temp;
	}

	private boolean isDebug(Map<String, String> urlParameterMap) {
		String debug = urlParameterMap.get("debug");
		return debug != null && debug.equals("true");
	}

}
