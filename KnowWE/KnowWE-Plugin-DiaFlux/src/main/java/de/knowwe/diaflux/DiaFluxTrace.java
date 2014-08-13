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

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;

import de.d3web.core.inference.PSMethod;
import de.d3web.core.inference.PropagationEntry;
import de.d3web.core.inference.PropagationListener;
import de.d3web.core.session.Session;
import de.d3web.core.session.SessionObjectSource;
import de.d3web.core.session.blackboard.SessionObject;
import de.d3web.diaFlux.flow.DiaFluxCaseObject;
import de.d3web.diaFlux.flow.DiaFluxElement;
import de.d3web.diaFlux.flow.Edge;
import de.d3web.diaFlux.flow.FlowRun;
import de.d3web.diaFlux.flow.Node;
import de.d3web.diaFlux.flow.SnapshotNode;
import de.d3web.diaFlux.inference.DiaFluxUtils;
import de.d3web.diaFlux.inference.FluxSolver;

/**
 * This class traces the currently active nodes and edges.
 *
 * @author Reinhard Hatko
 * @created 02.04.2012
 */
public class DiaFluxTrace implements SessionObject {

	public enum State {
		active, traced, none
	}

	public static final PropagationListener LISTENER = new PropagationListener() {

		@Override
		public void propagationStarted(Session session, Collection<PropagationEntry> entries) {
		}

		@Override
		public void propagationFinished(Session session, Collection<PropagationEntry> entries) {
		}

		@Override
		public void propagating(Session session, PSMethod psMethod, Collection<PropagationEntry> entries) {
		}

		;

		@Override
		public void postPropagationStarted(Session session, Collection<PropagationEntry> entries) {
			if (!DiaFluxUtils.isFlowCase(session)) {
				return;
			}
			FlowchartUtils.getTrace(session).update();
		}

		@Override
		public boolean equals(Object obj) {
			return obj.getClass() == getClass();
		}

		@Override
		public int hashCode() {
			return getClass().hashCode();
		}

	};

	private final Session session;
	private final Collection<Node> tracedActiveNodes = new HashSet<Node>();
	private final Collection<Edge> tracedActiveEdges = new HashSet<Edge>();

	public static final SessionObjectSource<DiaFluxTrace> SOURCE = new SessionObjectSource<DiaFluxTrace>() {

		@Override
		public DiaFluxTrace createSessionObject(Session session) {
			return new DiaFluxTrace(session);
		}
	};

	public DiaFluxTrace(Session session) {
		this.session = session;
	}

	protected void update() {
		DiaFluxCaseObject caseObject = DiaFluxUtils.getDiaFluxCaseObject(session);
		// we clear the current trace if the last snapshot is out-dated.
		// we do not if the propagation time is still the same (so we are in
		// the
		// same propagation cycle from the users perspective)
		Date lastTime = caseObject.getLatestSnaphotTime();
		long thisTime = session.getPropagationManager().getPropagationTime();
		if (lastTime == null || lastTime.getTime() < thisTime) {
			tracedActiveNodes.clear();
			tracedActiveEdges.clear();
		}

		Collection<SnapshotNode> enteredSnapshots = caseObject.getActivatedSnapshots();
		if (enteredSnapshots.isEmpty()) {
			return;
		}

		Collection<Node> snappyNodes = collectActiveNodes(caseObject, enteredSnapshots);

		traceNodesAndEdges(session, snappyNodes, enteredSnapshots);

	}

	public Session getSession() {
		return session;
	}

	public static Collection<Node> collectActiveNodes(DiaFluxCaseObject caseObject, Collection<SnapshotNode> enteredSnapshots) {
		Collection<FlowRun> snappyFlows = FluxSolver.getFlowRunsWithEnteredSnapshot(
				enteredSnapshots, caseObject);

		Collection<Node> snappyNodes = new HashSet<Node>();
		for (SnapshotNode snapshotNode : enteredSnapshots) {
			snappyNodes.addAll(
					FluxSolver.getAllActiveNodesOfRunsWithSnapshot(snapshotNode,
							snappyFlows));
		}
		return snappyNodes;
	}

	/**
	 * Traces the Nodes and active outgoing edges.
	 *
	 * @param session the current session
	 * @param enteredSnapshots
	 * @created 01.03.2011
	 */
	private void traceNodesAndEdges(Session session, Collection<Node> tracedNodes, Collection<SnapshotNode> enteredSnapshots) {

		for (Node node : tracedNodes) {
			traceNodes(node);
			for (Edge edge : node.getOutgoingEdges()) {
				// Do not trace the edge that starts at an active snapshot node.
				// Otherwise it is counted twice.
				if (FluxSolver.evalEdge(session, edge)
						&& !enteredSnapshots.contains(edge.getStartNode())) {
					traceEdges(edge);
				}
			}
		}
	}

	/**
	 * Adds a set of edges to the traced edges before the snapshot will be taken. The traced
	 * information are intended to describe the state before the last snapshot has been taken.
	 *
	 * @param edges the edges to be added.
	 * @created 01.03.2011
	 */
	private void traceEdges(Edge... edges) {
		Collections.addAll(this.tracedActiveEdges, edges);
	}

	/**
	 * Adds a set of nodes to the traced nodes before the snapshot will be taken. The traced
	 * information are intended to describe the state before the last snapshot has been taken.
	 *
	 * @param nodes the nodes to be added.
	 * @created 01.03.2011
	 */
	private void traceNodes(Node... nodes) {
		Collections.addAll(this.tracedActiveNodes, nodes);
	}

	/**
	 * Returns the set of traced edges. These are the active edges before the snapshot has been
	 * taken.
	 *
	 * @return the traced edges
	 * @created 01.03.2011
	 */
	public Collection<Edge> getTracedEdges() {
		return Collections.unmodifiableCollection(this.tracedActiveEdges);
	}

	/**
	 * Returns the set of traced nodes. These are the active nodes before the snapshot has been
	 * taken.
	 *
	 * @return the traced nodes
	 * @created 01.03.2011
	 */
	public Collection<Node> getTracedNodes() {
		return Collections.unmodifiableCollection(this.tracedActiveNodes);
	}

	public static State getState(DiaFluxElement element, Session session) {
		DiaFluxTrace trace = FlowchartUtils.getTrace(session);
		DiaFluxCaseObject diaFluxCaseObject = DiaFluxUtils.getDiaFluxCaseObject(session);
		if (element instanceof Node) {
			for (FlowRun run : diaFluxCaseObject.getRuns()) {
				if (run.isActive((Node) element)) return State.active;
			}
			if (trace.getTracedNodes().contains(element)) return State.traced;

		}
		else if (element instanceof Edge) {
			for (FlowRun run : diaFluxCaseObject.getRuns()) {
				if (run.isActivated((Edge) element)) return State.active;
			}
			if (trace.getTracedEdges().contains(element)) return State.traced;
		}
		return State.none;

	}

}
