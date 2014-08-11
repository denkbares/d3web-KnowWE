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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.d3web.core.inference.PSAction;
import de.d3web.core.inference.PSMethod;
import de.d3web.core.inference.PropagationEntry;
import de.d3web.core.inference.PropagationListener;
import de.d3web.core.knowledge.InfoStore;
import de.d3web.core.knowledge.TerminologyObject;
import de.d3web.core.knowledge.ValueObject;
import de.d3web.core.knowledge.terminology.info.MMInfo;
import de.d3web.core.session.Session;
import de.d3web.core.session.SessionObjectSource;
import de.d3web.core.session.Value;
import de.d3web.core.session.blackboard.SessionObject;
import de.d3web.diaFlux.flow.ActionNode;
import de.d3web.diaFlux.flow.DiaFluxCaseObject;
import de.d3web.diaFlux.flow.FlowRun;
import de.d3web.diaFlux.flow.Node;
import de.d3web.diaFlux.flow.SnapshotNode;
import de.d3web.diaFlux.inference.DiaFluxUtils;
import de.d3web.we.utils.D3webUtils;

/**
 * This class traces the values the terminology objects of nodes had, when they
 * were snapshotted.
 * 
 * @author Reinhard Hatko
 * @created 02.04.2012
 */
public class DiaFluxValueTrace implements SessionObject {

	public static final PropagationListener LISTENER = new PropagationListener() {

		@Override
		public void propagationStarted(Session session, Collection<PropagationEntry> entries) {
		}

		@Override
		public void propagationFinished(Session session, Collection<PropagationEntry> entries) {
		}

		@Override
		public void propagating(Session session, PSMethod psMethod, Collection<PropagationEntry> entries) {
		};

		@Override
		public void postPropagationStarted(Session session, Collection<PropagationEntry> entries) {
			if (!DiaFluxUtils.isFlowCase(session)) {
				return;
			}
			FlowchartUtils.getValueTrace(session).update();

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

	public static final SessionObjectSource<DiaFluxValueTrace> SOURCE = new SessionObjectSource<DiaFluxValueTrace>() {

		@Override
		public DiaFluxValueTrace createSessionObject(Session session) {
			return new DiaFluxValueTrace(session);
		}
	};

	private final Session session;
	private final Map<Node, Value> tracedValues = new HashMap<Node, Value>();

	public DiaFluxValueTrace(Session session) {
		this.session = session;
	}

	public Session getSession() {
		return session;
	}

	private static TerminologyObject getTermObject(Node node) {
		if (node instanceof ActionNode) {
			PSAction action = ((ActionNode) node).getAction();
			List<? extends TerminologyObject> objects = action.getBackwardObjects();
			if (!objects.isEmpty()) {
				// There should be only 1 backward object
				return objects.get(0);
			}
		}
		return null;
	}

	private void update() {
		DiaFluxCaseObject caseObject = DiaFluxUtils.getDiaFluxCaseObject(session);
		// we clear the current trace if the last snapshot is out-dated.
		// we do not if the propagation time is still the same (so we are in
		// the
		// same propagation cycle from the users perspective)
		Date lastTime = caseObject.getLatestSnaphotTime();
		long thisTime = session.getPropagationManager().getPropagationTime();
		if (lastTime == null || lastTime.getTime() < thisTime) {
			tracedValues.clear();
		}

		// Do not trace, if no snapshots have been entered
		Collection<SnapshotNode> enteredSnapshots = caseObject.getActivatedSnapshots();
		if (enteredSnapshots.isEmpty()) {
			return;
		}

		List<FlowRun> runs = caseObject.getRuns();
		for (FlowRun flowRun : runs) {
			for (Node node : flowRun.getActiveNodes()) {
				TerminologyObject termObject = getTermObject(node);
				if (termObject instanceof ValueObject) {
					Value value = session.getBlackboard().getValue((ValueObject) termObject);
					tracedValues.put(node, value);
				}

			}
		}
	}

	public Map<Node, Value> getTracedValues() {
		return Collections.unmodifiableMap(tracedValues);
	}

	/**
	 * Returns the value the associated terminology object of the node had at
	 * the time the snapshot was taken, or null, if no value for this node has
	 * been recorded.
	 * 
	 * @created 04.04.2012
	 * @param node
	 * @return s
	 */
	public Value getValue(Node node) {
		return tracedValues.get(node);
	}

	public String getValueString(Node node) {
		TerminologyObject termObject = getTermObject(node);
		if (termObject == null) {
			// Node does not have reference TermObject
			return null;
		}

		Value tracedValue = tracedValues.get(node);
		Value value = null;
		if (termObject instanceof ValueObject) {
			value = D3webUtils.getValueNonBlocking(session, (ValueObject) termObject);
		}
		String tooltip = "";
		if (tracedValue == null) {
			// Node has reference TermObject, but was not active during
			// snapshot: show current value
			if (value != null) {
				tooltip = getValueString(termObject, value);
			}
		}
		else {
			// Node was active: show that value
			tooltip = getValueString(termObject, tracedValue);
			// add current value, if not equal to value during snapshot
			if (value != null && !tracedValue.equals(value)) {
				tooltip += " (Current: '" + value.toString() + "')";
			}

		}
		return tooltip;

	}

	private static String getValueString(TerminologyObject object, Value value) {
		String unit = "";
		InfoStore infoStore = object.getInfoStore();
		if (infoStore.contains(MMInfo.UNIT)) {
			unit = " " + infoStore.getValue(MMInfo.UNIT);
		}
		return object.getName() + " = '" + value.toString() + unit + "'";
	}

}
