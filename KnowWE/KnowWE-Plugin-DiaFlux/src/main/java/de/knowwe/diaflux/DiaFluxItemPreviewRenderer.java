package de.knowwe.diaflux;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.kdom.rendering.DelegateRenderer;
import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.preview.PreviewRenderer;
import de.knowwe.core.user.UserContext;
import de.knowwe.diaflux.type.ActionType;
import de.knowwe.diaflux.type.CommentType;
import de.knowwe.diaflux.type.DecisionType;
import de.knowwe.diaflux.type.EdgeType;
import de.knowwe.diaflux.type.ExitType;
import de.knowwe.diaflux.type.FlowchartType;
import de.knowwe.diaflux.type.GuardType;
import de.knowwe.diaflux.type.NodeContentType;
import de.knowwe.diaflux.type.NodeType;
import de.knowwe.diaflux.type.SnapshotType;
import de.knowwe.diaflux.type.StartType;

public class DiaFluxItemPreviewRenderer implements PreviewRenderer {

	@Override
	public void render(Section<?> section, Collection<Section<?>> relevantSubSections, UserContext user, RenderResult result) {
		Section<FlowchartType> self = Sections.cast(section, FlowchartType.class);
		String name = FlowchartType.getFlowchartName(self);
		result.append(name);

		// build sets first to avoid duplicates
		Set<Section<NodeType>> nodes = new LinkedHashSet<Section<NodeType>>();
		Set<Section<EdgeType>> edges = new LinkedHashSet<Section<EdgeType>>();
		for (Section<?> item : relevantSubSections) {
			Section<NodeType> node = Sections.findAncestorOfType(item, NodeType.class);
			if (node != null) {
				nodes.add(node);
			}
			Section<EdgeType> edge = Sections.findAncestorOfType(item, EdgeType.class);
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

	private void renderEdge(Section<EdgeType> edge, UserContext user, RenderResult result) {
		result.appendHtml("<div class='preview edge'>&nbsp;&nbsp;").append("- edge ");
		Section<GuardType> guard = Sections.findSuccessor(edge, GuardType.class);
		DelegateRenderer.getRenderer(guard, user).render(guard, user, result);
		result.appendHtml("</div>");
	}

	private void renderNode(Section<NodeType> node, UserContext user, RenderResult result) {
		Section<NodeContentType> action = Sections.findSuccessor(node, NodeContentType.class);
		result.appendHtml("<div class='preview node ")
				.append(getNodeCSS(action))
				.appendHtml("'>&nbsp;&nbsp;").append("- node ");
		DelegateRenderer.getRenderer(action, user).render(action, user, result);
		result.appendHtml("</div>");
	}

	private String getNodeCSS(Section<NodeContentType> action) {
		if (Sections.findChildOfType(action, StartType.class) != null) return "type_Start";
		if (Sections.findChildOfType(action, ExitType.class) != null) return "type_Exit";
		if (Sections.findChildOfType(action, ActionType.class) != null) return "type_Action";
		if (Sections.findChildOfType(action, CommentType.class) != null) return "type_Comment";
		if (Sections.findChildOfType(action, SnapshotType.class) != null) return "type_Snapshot";
		if (Sections.findChildOfType(action, DecisionType.class) != null) return "type_Decision";
		return "type_Unexpected";
	}

}
