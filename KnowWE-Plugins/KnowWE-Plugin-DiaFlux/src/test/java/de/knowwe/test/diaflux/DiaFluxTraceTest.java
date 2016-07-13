package de.knowwe.test.diaflux;
/*
 * Copyright (C) 2012 University Wuerzburg, Computer Science VI
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import de.d3web.core.inference.PropagationManager;
import de.d3web.core.inference.condition.CondEqual;
import de.d3web.core.inference.condition.Condition;
import de.d3web.core.inference.condition.ConditionTrue;
import de.d3web.core.knowledge.KnowledgeBase;
import de.d3web.core.knowledge.TerminologyObject;
import de.d3web.core.knowledge.terminology.QuestionYN;
import de.d3web.core.manage.KnowledgeBaseUtils;
import de.d3web.core.session.Session;
import de.d3web.core.session.SessionFactory;
import de.d3web.core.session.Value;
import de.d3web.core.session.blackboard.FactFactory;
import de.d3web.diaFlux.flow.ActionNode;
import de.d3web.diaFlux.flow.DiaFluxElement;
import de.d3web.diaFlux.flow.Edge;
import de.d3web.diaFlux.flow.EndNode;
import de.d3web.diaFlux.flow.Flow;
import de.d3web.diaFlux.flow.FlowFactory;
import de.d3web.diaFlux.flow.Node;
import de.d3web.diaFlux.flow.SnapshotNode;
import de.d3web.diaFlux.flow.StartNode;
import de.d3web.indication.ActionInstantIndication;
import com.denkbares.plugin.test.InitPluginManager;
import de.knowwe.diaflux.DiaFluxTrace;
import de.knowwe.diaflux.FlowchartUtils;

/**
 * 
 * Tests the DiaFlux tracing of nodes and edges
 * 
 * @author Reinhard Hatko
 * @created 23.04.2012
 */
public class DiaFluxTraceTest {

	private KnowledgeBase kb;
	private QuestionYN questionYN1;
	private Value yes;
	private Value no;
	private ActionNode questionNode1;
	private ActionNode questionNode2;
	private Node startNode;
	private Node endNode;
	private Node snaphot1;
	private Node snaphot2;
	private Edge startToQuestion1;
	private Edge question1ToSnap;
	private Edge snap1ToQuestion2;
	private Edge question2ToSnap2;
	private SnapshotNode snaphot3;
	private Edge snap3ToEnd;
	private Edge snap2ToQuestion3;
	private QuestionYN questionYN2;
	private ActionNode questionNode3;
	private Edge question3ToSnap3;

	@Test
	public void testTrace() throws IOException {
		Session session = SessionFactory.createSession(kb);

		DiaFluxTrace trace = FlowchartUtils.getTrace(session);
		Assert.assertNotNull(trace);

		// nothing traced
		assertTrace(trace.getTracedNodes());
		assertTrace(trace.getTracedEdges());

		propagate(session, questionYN1, yes, 0);

		// Path to first snap is active
		assertTrace(trace.getTracedNodes(), startNode, questionNode1, snaphot1);
		assertTrace(trace.getTracedEdges(), startToQuestion1, question1ToSnap);

		propagate(session, questionYN1, no, 1);

		// Path to second snap is active
		assertTrace(trace.getTracedNodes(), snaphot1, questionNode2, snaphot2);
		assertTrace(trace.getTracedEdges(), snap1ToQuestion2, question2ToSnap2);

		propagate(session, questionYN2, no, 1);

		// Path from second snap to question3 is active, as time was not
		// advanced in last propagation
		assertTrace(trace.getTracedNodes(), snaphot1, questionNode2, snaphot2, questionNode3,
				snaphot3);
		assertTrace(trace.getTracedEdges(), snap1ToQuestion2, question2ToSnap2,
				snap2ToQuestion3, question3ToSnap3);


	}

	void assertTrace(Collection<? extends DiaFluxElement> elements, DiaFluxElement... expected) {
		Assert.assertEquals(expected.length, elements.size());

		for (DiaFluxElement diaFluxElement : expected) {
			Assert.assertTrue(elements.contains(diaFluxElement));
		}
	}

	private void propagate(Session session, TerminologyObject object, Value value, long time) {
		PropagationManager manager = session.getPropagationManager();
		manager.openPropagation(time);
		session.getBlackboard().addValueFact(FactFactory.createUserEnteredFact(object, value));
		manager.commitPropagation();
	}

	// Structure:
	// Start
	// |
	// v
	// questionNode1
	// | questionYN1 == YES
	// v
	// snapshotNode1
	// |
	// v
	// questionNode2
	// | questionYN1 == No
	// v
	// snapshotNode2
	// |
	// v
	// questionNode3
	// | questionYN2 == No
	// v
	// snapshotNode3
	// |
	// v
	// End
	@Before
	public void setUpFlux() throws IOException {
		InitPluginManager.init();

		// Adding this listener is usually done in FlowchartType
		SessionFactory.addPropagationListener(DiaFluxTrace.LISTENER);

		kb = KnowledgeBaseUtils.createKnowledgeBase();
		questionYN1 = new QuestionYN(kb.getRootQASet(), "YesNoQuestion1");
		questionYN2 = new QuestionYN(kb.getRootQASet(), "YesNoQuestion2");
		yes = KnowledgeBaseUtils.findValue(questionYN1, "Yes");
		no = KnowledgeBaseUtils.findValue(questionYN1, "No");

		startNode = new StartNode("Start_ID", "Start");
		endNode = new EndNode("End_ID", "Ende");
		snaphot1 = new SnapshotNode("Snap1_ID", "Snapshot1");
		snaphot2 = new SnapshotNode("Snap2_ID", "Snapshot2");
		snaphot3 = new SnapshotNode("Snap3_ID", "Snapshot3");

		ActionInstantIndication instantIndication1 = new ActionInstantIndication();
		instantIndication1.setQASets(questionYN1);
		questionNode1 = new ActionNode("questionNode1_ID", instantIndication1);

		ActionInstantIndication instantIndication2 = new ActionInstantIndication();
		instantIndication2.setQASets(questionYN1);
		questionNode2 = new ActionNode("questionNode2_ID", instantIndication2);

		ActionInstantIndication instantIndication3 = new ActionInstantIndication();
		instantIndication3.setQASets(questionYN2);
		questionNode3 = new ActionNode("questionNode3_ID", instantIndication3);

		LinkedList<Node> nodesList = new LinkedList<>(Arrays.asList(startNode, endNode,
				questionNode1, snaphot1, snaphot2, questionNode2, snaphot3, questionNode3));

		// ---------------------------------

		startToQuestion1 = createEdge(startNode, questionNode1);

		question1ToSnap = createEdge(questionNode1, snaphot1, new CondEqual(questionYN1, yes));

		snap1ToQuestion2 = createEdge(snaphot1, questionNode2);

		question2ToSnap2 = createEdge(questionNode2, snaphot2, new CondEqual(questionYN1, no));

		snap2ToQuestion3 = createEdge(snaphot2, questionNode3);

		question3ToSnap3 = createEdge(questionNode3, snaphot3, new CondEqual(questionYN2, no));

		snap3ToEnd = createEdge(snaphot3, endNode);

		List<Edge> edgesList = new LinkedList<>(Arrays.asList(startToQuestion1,
				question1ToSnap, snap1ToQuestion2, question2ToSnap2, snap2ToQuestion3,
				question3ToSnap3, snap3ToEnd));

		Flow testFlow = FlowFactory.createFlow(kb, "Main", nodesList, edgesList);
		testFlow.setAutostart(true);

		// ----------------------------------

	}

	private Edge createEdge(Node fromNode, Node toNode) {
		return createEdge(fromNode, toNode, ConditionTrue.INSTANCE);
	}

	private Edge createEdge(Node fromNode, Node toNode, Condition condition) {
		return FlowFactory.createEdge(fromNode.getID() + "To" + toNode.getID(), fromNode,
				toNode, condition);
	}

}
