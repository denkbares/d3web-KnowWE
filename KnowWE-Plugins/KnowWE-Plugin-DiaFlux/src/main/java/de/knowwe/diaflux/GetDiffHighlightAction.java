/*
 * Copyright (C) 2012 University Wuerzburg, Computer Science VI
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
package de.knowwe.diaflux;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import de.d3web.utils.Pair;
import de.d3web.utils.Triple;
import de.knowwe.core.action.AbstractAction;
import de.knowwe.core.action.UserActionContext;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.diaflux.type.EdgeType;
import de.knowwe.diaflux.type.FlowchartType;
import de.knowwe.diaflux.type.NodeType;
import de.knowwe.kdom.xml.AbstractXMLType;

/**
 * 
 * @author Reinhard Hatko
 * @created 23.10.2012
 */
public class GetDiffHighlightAction extends AbstractAction {

	private static final String LEFT_PARENT = "LEFT";
	private static final String RIGHT_PARENT = "RGHT";

	private static final String PREFIX = "diff";
	private static final String REMOVED = PREFIX + "Removed";
	private static final String ADDED = PREFIX + "Added";
	private static final String CHANGED = PREFIX + "Changed";

	@Override
	public void execute(UserActionContext context) throws IOException {

		String leftkdomid = context.getParameter(LEFT_PARENT + "kdomid");
		String rightkdomid = context.getParameter(RIGHT_PARENT + "kdomid");
		String parentid = context.getParameter(GetTraceHighlightAction.PARENTID);
		parentid = parentid.substring(0, parentid.length() - 5);

		Section<FlowchartType> leftFlow = Sections.get(leftkdomid, FlowchartType.class);
		Section<FlowchartType> rightFlow = Sections.get(rightkdomid, FlowchartType.class);

		Highlight leftHighlight = new Highlight(parentid + "-" + LEFT_PARENT, PREFIX);
		Highlight rightHighlight = new Highlight(parentid + "-" + RIGHT_PARENT, PREFIX);

		highlightNodes(leftFlow, rightFlow, leftHighlight, rightHighlight);
		highlightEdges(leftFlow, rightFlow, leftHighlight, rightHighlight);

		write(leftHighlight, rightHighlight, context);

		// TODO here, the registered sections should be unregistered again
		// ...unless one of them was the current version
	}

	/**
	 * 
	 * @created 25.10.2012
	 * @param leftHighlight
	 * @param rightHighlight
	 * @param context
	 * @throws IOException
	 */
	private void write(Highlight leftHighlight, Highlight rightHighlight, UserActionContext context) throws IOException {
		context.setContentType("text/xml");
		context.getWriter().write("<highlights>");
		context.getWriter().write(leftHighlight.getXML());
		context.getWriter().write(rightHighlight.getXML());
		context.getWriter().write("</highlights>");

	}


	public static void highlightNodes(Section<FlowchartType> leftFlow, Section<FlowchartType> rightFlow, Highlight leftHighlight, Highlight rightHighlight) {

		Triple<Collection<Section<NodeType>>, Collection<Section<NodeType>>, Collection<Section<NodeType>>> changes = getChanges(
				leftFlow, rightFlow, NodeType.class);

		addNodeHighlight(rightHighlight, changes.getA(), REMOVED);

		addNodeHighlight(leftHighlight, changes.getB(), ADDED);

		addNodeHighlight(rightHighlight, changes.getC(), CHANGED);
		addNodeHighlight(leftHighlight, changes.getC(), CHANGED);
	}

	public static void highlightEdges(Section<FlowchartType> leftFlow, Section<FlowchartType> rightFlow, Highlight leftHighlight, Highlight rightHighlight) {

		Triple<Collection<Section<EdgeType>>, Collection<Section<EdgeType>>, Collection<Section<EdgeType>>> changes = getChanges(
				leftFlow, rightFlow, EdgeType.class);

		addEdgeHighlight(rightHighlight, changes.getA(), REMOVED);

		addEdgeHighlight(leftHighlight, changes.getB(), ADDED);

		addEdgeHighlight(rightHighlight, changes.getC(), CHANGED);
		addEdgeHighlight(leftHighlight, changes.getC(), CHANGED);
	}

	static <T extends AbstractXMLType> Triple<Collection<Section<T>>, Collection<Section<T>>, Collection<Section<T>>> getChanges(Section<FlowchartType> leftFlow, Section<FlowchartType> rightFlow, Class<T> clazz) {
		Collection<Pair<Section<T>, Section<T>>> containedElements = getCorrespondingElements(
				leftFlow, rightFlow, clazz);

		Collection<Section<T>> changedElements = filterUnchangedElements(containedElements);
		Collection<Section<T>> addedElements = getOtherElements(rightFlow, getB(containedElements),
				clazz);
		Collection<Section<T>> removedElements = getOtherElements(leftFlow,
				getA(containedElements),
				clazz);

		return new Triple<>(
				addedElements, removedElements, changedElements);

	}


	private static <T extends AbstractXMLType> void addNodeHighlight(Highlight highlight, Collection<Section<T>> elements, String cssClass) {
		
		for (Section<T> section : elements) {
			String id = LoadFlowchartAction.getFlowchartId(section);
			highlight.addNode(id, Highlight.CSS_CLASS, cssClass);
		}
		
	}

	private static <T extends AbstractXMLType> void addEdgeHighlight(Highlight highlight, Collection<Section<T>> elements, String cssClass) {

		for (Section<T> section : elements) {
			String id = LoadFlowchartAction.getFlowchartId(section);
			highlight.addEdge(id, Highlight.CSS_CLASS, cssClass);
		}

	}

	private static <T extends AbstractXMLType> Collection<Section<T>> filterUnchangedElements(Collection<Pair<Section<T>, Section<T>>> pairs) {
		Collection<Section<T>> result = new LinkedList<>();
		for (Pair<Section<T>, Section<T>> pair : pairs) {
			// TODO implement better comparison, e.g. ignoring slight position
			// changes
			if (!pair.getA().getText().equals(pair.getB().getText())) {
				// both section have same fcid, so it shouldn't matter which one
				// to add
				result.add(pair.getA());
			}
		}

		return result;
	}

	private static <T extends AbstractXMLType> Collection<Section<T>> getA(Collection<Pair<Section<T>, Section<T>>> pairs) {
		Collection<Section<T>> result = new ArrayList<>(pairs.size());

		for (Pair<Section<T>, Section<T>> pair : pairs) {
			result.add(pair.getA());
		}

		return result;
	}

	private static <T extends AbstractXMLType> Collection<Section<T>> getB(Collection<Pair<Section<T>, Section<T>>> pairs) {
		Collection<Section<T>> result = new ArrayList<>(pairs.size());

		for (Pair<Section<T>, Section<T>> pair : pairs) {
			result.add(pair.getB());
		}

		return result;
	}

	private static <T extends AbstractXMLType> Collection<Section<T>> getOtherElements(Section<FlowchartType> flow, Collection<Section<T>> elements, Class<T> clazz) {
		List<Section<T>> childs = Sections.successors(flow, clazz);
		childs.removeAll(elements);
		return childs;

	}

	/**
	 * looks for elements with the same flowchart id in both sections
	 * 
	 * @created 25.10.2012
	 * @param flow1
	 * @param flow2
	 * @param clazz
	 * @return
	 */
	private static <T extends AbstractXMLType> Collection<Pair<Section<T>, Section<T>>> getCorrespondingElements(Section<FlowchartType> flow1, Section<FlowchartType> flow2, Class<T> clazz) {
		List<Section<T>> childs1 = Sections.successors(flow1, clazz);
		List<Section<T>> childs2 = Sections.successors(flow2, clazz);

		Collection<Pair<Section<T>, Section<T>>> result = new LinkedList<>();
		for (Section<T> child1 : childs1) {
			Section<T> otherchild = find(child1, childs2);
			if (otherchild != null) {
				result.add(new Pair<>(child1, otherchild));
			}
		}

		return result;
	}

	private static <T extends AbstractXMLType> Section<T> find(Section<T> child, List<Section<T>> childs) {
		String fcid = LoadFlowchartAction.getFlowchartId(child);
		for (Section<T> otherChild : childs) {
			if (LoadFlowchartAction.getFlowchartId(otherChild).equals(fcid)) {
				return otherChild;
			}

		}
		return null;
	}

}
