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
import java.util.LinkedList;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import de.d3web.core.inference.PropagationManager;
import de.d3web.core.inference.condition.CondEqual;
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
import de.d3web.diaFlux.flow.Edge;
import de.d3web.diaFlux.flow.EndNode;
import de.d3web.diaFlux.flow.Flow;
import de.d3web.diaFlux.flow.FlowFactory;
import de.d3web.diaFlux.flow.Node;
import de.d3web.diaFlux.flow.SnapshotNode;
import de.d3web.diaFlux.flow.StartNode;
import de.d3web.indication.ActionInstantIndication;
import de.d3web.plugin.test.InitPluginManager;
import de.knowwe.diaflux.DiaFluxValueTrace;
import de.knowwe.diaflux.FlowchartUtils;

/**
 * 
 * Tests, if the value trace correctly holds the value a node had, when its
 * active path entered a snapshot node
 * 
 * @author Reinhard Hatko
 * @created 11.04.2012
 */
public class DiaFluxValueTraceTest {

	private KnowledgeBase kb;
	private QuestionYN questionYN;
	private Value yes;
	private Value no;
	private ActionNode questionNode1;
	private ActionNode questionNode2;

	@Test
	public void testValueTrace() throws IOException {
		Session session = SessionFactory.createSession(kb);

		DiaFluxValueTrace trace = FlowchartUtils.getValueTrace(session);
		Assert.assertNotNull(trace);

		propagate(session, questionYN, yes);

		// After the first snapshot, a value has been traced only for node1
		Assert.assertEquals(yes, trace.getValue(questionNode1));
		Assert.assertNull(trace.getValue(questionNode2));

		propagate(session, questionYN, no);
		// after the second one, the value for the first node must still be the
		// same
		Assert.assertEquals(yes, trace.getValue(questionNode1));
		Assert.assertEquals(no, trace.getValue(questionNode2));

		// The value string should contain the old and the current value
		Assert.assertTrue(trace.getValueString(questionNode1).contains(yes.toString()));
		Assert.assertTrue(trace.getValueString(questionNode1).contains(no.toString()));

	}

	private void propagate(Session session, TerminologyObject object, Value value) {
		PropagationManager manager = session.getPropagationManager();
		manager.openPropagation(0);
		session.getBlackboard().addValueFact(FactFactory.createUserEnteredFact(object, value));
		manager.commitPropagation();
	}

	// Structure:
	// Start
	// |
	// v
	// questionNode1
	// | questionYN == YES
	// v
	// snapshotNode1
	// |
	// v
	// questionNode2
	// | questionYN == No
	// v
	// snapshotNode2
	// |
	// v
	// End
	@Before
	public void setUpFlux() throws IOException {
		InitPluginManager.init();

		// Adding this listener is usually done in FlowchartType
		SessionFactory.addPropagationListener(DiaFluxValueTrace.LISTENER);

		kb = KnowledgeBaseUtils.createKnowledgeBase();
		questionYN = new QuestionYN(kb.getRootQASet(), "QuestionYN");
		yes = KnowledgeBaseUtils.findValue(questionYN, "Yes");
		no = KnowledgeBaseUtils.findValue(questionYN, "No");

		Node startNode = new StartNode("Start_ID", "Start");
		Node endNode = new EndNode("End_ID", "Ende");
		Node snaphot1 = new SnapshotNode("Snap1_ID", "Snapshot1");
		Node snaphot2 = new SnapshotNode("Snap2_ID", "Snapshot2");

		ActionInstantIndication instantIndication1 = new ActionInstantIndication();
		instantIndication1.setQASets(questionYN);
		questionNode1 = new ActionNode("questionNode1_ID", instantIndication1);

		ActionInstantIndication instantIndication2 = new ActionInstantIndication();
		instantIndication2.setQASets(questionYN);
		questionNode2 = new ActionNode("questionNode2_ID", instantIndication2);

		LinkedList<Node> nodesList = new LinkedList<>(Arrays.asList(startNode, endNode,
				questionNode1, snaphot1, snaphot2, questionNode2));

		// ---------------------------------

		Edge startToQuestion1 = FlowFactory.createEdge("startToQuestionEdge1_ID", startNode,
				questionNode1,
				ConditionTrue.INSTANCE);

		Edge question1ToSnap = FlowFactory.createEdge("question1ToSnap_ID", questionNode1,
				snaphot1, new CondEqual(questionYN, yes));

		Edge snap1ToQuestion2 = FlowFactory.createEdge("snap1ToQuestion2_ID", snaphot1,
				questionNode2, ConditionTrue.INSTANCE);

		Edge question2ToSnap2 = FlowFactory.createEdge("question2ToSnap2_ID", questionNode2,
				snaphot2, new CondEqual(questionYN, no));

		Edge snap2ToEnd = FlowFactory.createEdge("snap2ToEnd_ID", snaphot2, endNode,
				ConditionTrue.INSTANCE);

		List<Edge> edgesList = new LinkedList<>(Arrays.asList(startToQuestion1,
				question1ToSnap, snap1ToQuestion2, question2ToSnap2, snap2ToEnd));

		Flow testFlow = FlowFactory.createFlow(kb, "Main", nodesList, edgesList);
		testFlow.setAutostart(true);

		// ----------------------------------

	}

}
