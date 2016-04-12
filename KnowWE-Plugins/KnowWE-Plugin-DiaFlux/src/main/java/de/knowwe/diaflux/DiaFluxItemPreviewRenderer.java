package de.knowwe.diaflux;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.preview.AbstractPreviewRenderer;
import de.knowwe.core.user.UserContext;
import de.knowwe.diaflux.type.ActionType;
import de.knowwe.diaflux.type.CommentType;
import de.knowwe.diaflux.type.DecisionType;
import de.knowwe.diaflux.type.EdgeType;
import de.knowwe.diaflux.type.ExitType;
import de.knowwe.diaflux.type.FlowchartType;
import de.knowwe.diaflux.type.FlowchartXMLHeadType;
import de.knowwe.diaflux.type.FlowchartXMLHeadType.FlowchartTermDef;
import de.knowwe.diaflux.type.GuardType;
import de.knowwe.diaflux.type.NodeContentType;
import de.knowwe.diaflux.type.NodeType;
import de.knowwe.diaflux.type.SnapshotType;
import de.knowwe.diaflux.type.StartType;
import de.knowwe.kdom.xml.AbstractXMLType;

public class DiaFluxItemPreviewRenderer extends AbstractPreviewRenderer {

	@Override
	public void render(Section<?> section, Collection<Section<?>> relevantSubSections, UserContext user, RenderResult result) {
		Section<FlowchartType> self = Sections.successor(section, FlowchartType.class);
		renderFlowchartName(self, user, result);

		// build sets first to avoid duplicates
		Set<Section<NodeType>> nodes = new LinkedHashSet<Section<NodeType>>();
		Set<Section<EdgeType>> edges = new LinkedHashSet<Section<EdgeType>>();
		if (relevantSubSections.size() <= 1) {
			relevantSubSections.addAll(Sections.successors(section, StartType.class));
			relevantSubSections.addAll(Sections.successors(section, ExitType.class));
		}
		for (Section<?> item : relevantSubSections) {
			Section<NodeType> node = Sections.ancestor(item, NodeType.class);
			if (node != null) {
				nodes.add(node);
			}
			Section<EdgeType> edge = Sections.ancestor(item, EdgeType.class);
			if (edge != null) {
				edges.add(edge);
			}
		}

		// then render the created sets
		for (Section<NodeType> node : nodes) {
			renderNode(node, user, result);
		}
		for (Section<EdgeType> edge : edges) {
			renderEdge(edge, user, result);
		}
	}

	private void renderFlowchartName(Section<FlowchartType> self, UserContext user, RenderResult result) {
		Section<FlowchartXMLHeadType> head =
				Sections.successor(self, FlowchartXMLHeadType.class);
		if (head != null) {
			Section<FlowchartTermDef> term = Sections.successor(head, FlowchartTermDef.class);
			if (term != null) {
				result.append(term, user);
				return;
			}
		}
		String name = FlowchartType.getFlowchartName(self);
		result.append(name);
	}

	private void renderEdge(Section<EdgeType> edge, UserContext user, RenderResult result) {
		result.appendHtml("<div class='preview edge ")
				.append(getSessionStateCSS(edge, user))
				.appendHtml("'>&nbsp;&nbsp;").append("- Edge ");
		Section<GuardType> guard = Sections.successor(edge, GuardType.class);
		result.append(guard, user);
		result.appendHtml("</div>");
	}

	private void renderNode(Section<NodeType> node, UserContext user, RenderResult result) {
		Section<NodeContentType> action = Sections.successor(node, NodeContentType.class);
		String nodeTypeName = "Node";
		if (Sections.successor(node, StartType.class) != null) {
			nodeTypeName = "StartNode";
		}
		else if (Sections.successor(node, ExitType.class) != null) {
			nodeTypeName = "ExitNode";
		}
		result.appendHtml("<div class='preview node ")
				.append(getNodeCSS(action))
				.append(" ")
				.append(getSessionStateCSS(node, user))
				.appendHtml("'>&nbsp;&nbsp;").append("- " + nodeTypeName + " ");
		renderNodePreviewHtml(action, user, result);
		result.appendHtml("</div>");
	}

	private static String getSessionStateCSS(Section<? extends AbstractXMLType> elem, UserContext user) {
		return FlowchartUtils.getElementState(elem, user).toString();
	}

	private String getNodeCSS(Section<NodeContentType> action) {
		if (Sections.child(action, StartType.class) != null) return "type_Start";
		if (Sections.child(action, ExitType.class) != null) return "type_Exit";
		if (Sections.child(action, ActionType.class) != null) return "type_Action";
		if (Sections.child(action, CommentType.class) != null) return "type_Comment";
		if (Sections.child(action, SnapshotType.class) != null) return "type_Snapshot";
		if (Sections.child(action, DecisionType.class) != null) return "type_Decision";
		return "type_Unexpected";
	}

	private void renderNodePreviewHtml(Section<NodeContentType> nodeSection, UserContext user, RenderResult result) {
		Section<?> section = Sections.child(nodeSection, StartType.class);
		if (section != null) {
			result.append(section, user);
			return;
		}

		section = Sections.child(nodeSection, ExitType.class);
		if (section != null) {
			result.append(section, user);
			return;
		}

		section = Sections.child(nodeSection, ActionType.class);
		if (section != null) {
			result.append(section, user);
			return;
		}

		section = Sections.child(nodeSection, CommentType.class);
		if (section != null) {
			renderPrefixedXMLContent("Comment: ", section, user, result);
			return;
		}

		section = Sections.child(nodeSection, SnapshotType.class);
		if (section != null) {
			renderPrefixedXMLContent("", section, user, result);
			return;
		}

		section = Sections.child(nodeSection, DecisionType.class);
		if (section != null) {
			result.append(section, user);
			return;
		}

		result.appendEntityEncoded(nodeSection.getText());
	}

	private void renderPrefixedXMLContent(String prefix, Section<?> section, UserContext user, RenderResult result) {
		result.append(prefix);
		result.appendEntityEncoded(section.getText());
	}

}
