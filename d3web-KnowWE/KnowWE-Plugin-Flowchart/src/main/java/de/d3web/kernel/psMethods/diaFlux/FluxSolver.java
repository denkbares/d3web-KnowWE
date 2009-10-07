package de.d3web.kernel.psMethods.diaFlux;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.junit.Assert;

import de.d3web.kernel.XPSCase;
import de.d3web.kernel.domainModel.Diagnosis;
import de.d3web.kernel.domainModel.DiagnosisState;
import de.d3web.kernel.domainModel.KnowledgeSlice;
import de.d3web.kernel.domainModel.RuleAction;
import de.d3web.kernel.domainModel.ruleCondition.AbstractCondition;
import de.d3web.kernel.domainModel.ruleCondition.NoAnswerException;
import de.d3web.kernel.domainModel.ruleCondition.TerminalCondition;
import de.d3web.kernel.domainModel.ruleCondition.UnknownAnswerException;
import de.d3web.kernel.psMethods.MethodKind;
import de.d3web.kernel.psMethods.PSMethod;
import de.d3web.kernel.psMethods.PropagationEntry;
import de.d3web.kernel.psMethods.diaFlux.actions.IAction;
import de.d3web.kernel.psMethods.diaFlux.flow.EdgeData;
import de.d3web.kernel.psMethods.diaFlux.flow.Flow;
import de.d3web.kernel.psMethods.diaFlux.flow.FlowData;
import de.d3web.kernel.psMethods.diaFlux.flow.FlowFactory;
import de.d3web.kernel.psMethods.diaFlux.flow.IEdge;
import de.d3web.kernel.psMethods.diaFlux.flow.IEdgeData;
import de.d3web.kernel.psMethods.diaFlux.flow.INode;
import de.d3web.kernel.psMethods.diaFlux.flow.INodeData;
import de.d3web.kernel.psMethods.diaFlux.flow.SnapshotNode;
import de.d3web.kernel.psMethods.diaFlux.flow.StartNode;

/**
 * 
 * @author Reinhard Hatko
 * Created: 10.09.2009
 *
 */
public class FluxSolver implements PSMethod {
	
	
	public static final MethodKind DIAFLUX = new MethodKind("DIAFLUX");
	
	private Flow flowDeclaration;
	private XPSCase theCase;
	private FlowData flow;
	
	private final List<PathEntry> pathEnds;
	
	
	public FluxSolver() {
		this.pathEnds = new ArrayList<PathEntry>();
		
	}
	

	@Override
	public void init(XPSCase theCase) {
		this.theCase = theCase;
		
		flowDeclaration = (Flow) theCase.getKnowledgeBase().getKnowledge(getClass(), DIAFLUX);
		
		flow = (FlowData) getCase().getCaseObject(getFlowDeclaration());
		
		
		INode nodeDeclaration = flowDeclaration.getStartNodes().get(0);
		
		
		addPathEntryForNode(null, null, FlowFactory.getInstance().createEdge(null, nodeDeclaration, new TerminalCondition(null) {
			@Override
			public boolean eval(XPSCase theCase) throws NoAnswerException,
					UnknownAnswerException {
				return true;
			}
			
			@Override
			public AbstractCondition copy() {
				return this;
			}
		}));
		
	}
	
	
	/**
	 * Adds a path entry for the current node. Predecessor's entry is removed by this method.

	 * @param currentNode  
	 * @param currentEntry
	 * @param nextNode
	 * @return the new {@link PathEntry} for node
	 */
	private PathEntry addPathEntryForNode(INode currentNode, PathEntry currentEntry, IEdge edge) {
		
		
		INode nextNode = edge.getEndNode(); 
		
		// both are null if a new flow is started (at first start, after snapshot, (after fork?))
		PathEntry stack = null;
		PathEntry predecessor = null;
		
		if (currentNode instanceof SnapshotNode) {
			//starts new flow -> stack = null, pred = null 
			
		} else if (currentNode != null) { //continue old flow 
			
			predecessor = currentEntry;
			
			if (nextNode instanceof StartNode) { // node is composed node -> new stack
				stack = currentEntry;
			} else { //normal node
				stack = currentEntry.getStack();
			}
		} 
		
		INodeData nodeData = flow.getDataForNode(nextNode);
		
		PathEntry newEntry = new PathEntry(predecessor, stack, nodeData, edge);
		
		if (currentEntry != null) { //entry for this flow already in pathends
			replacePathEnd(currentEntry, newEntry);
		} else { //new flow
			addPathEnd(newEntry);
		}
		
		
		return newEntry;
		
		
	}




	private void addPathEnd(PathEntry newEntry) {
		pathEnds.add(newEntry);
		newEntry.getNodeData().addSupport(newEntry);
	}


	private void replacePathEnd(PathEntry currentEntry, PathEntry newEntry) {
		
		boolean remove = pathEnds.remove(currentEntry);
		
		//Exception: fork? (would fail after first successor)
		Assert.assertTrue("Predecessor '" + currentEntry + "' not found in PathEnds: " + pathEnds, remove);
		
		addPathEnd(newEntry);
	}
	


	/**
	 * Continues the flow from the supplied entry on. 
	 * At first the next node is selected by {@link #selectNextEdge(INode)}.
	 * If the next node is the current node flow execution stalls.
	 * Otherwise:
	 * If the next node is not yet active in the case {@link INodeData#isActive()},
	 * it is activated. Otherwise flow execution stalls.  
	 *
	 * @param entry
	 */
	private void flow(PathEntry entry) {

		
		while (true) {
			
			INode currentNode = entry.getNodeData().getNode();
			IEdge edge = selectNextEdge(currentNode);
			
			if (edge == null) { //no edge to take
				return;
			} else if (currentNode instanceof SnapshotNode){
				takeSnapshot(entry);
			}
			
			
			INodeData nextNodeData = flow.getDataForNode(edge.getEndNode());
			
			if (nextNodeData.isActive()) {
				//TODO what's next??? just stall?
			}
			else {
				
				followEdge(currentNode, entry, edge);
				
				
			}
 			
		}
		
		
	}
	
	
	/**
	 * Takes a snapshot of the System when leaving the node that belongs to the given entry.
	 * @param entry
	 */
	private void takeSnapshot(PathEntry entry) {
		Assert.assertFalse(entry.getNodeData().getNode() instanceof SnapshotNode);
		
		
	}



	/**
	 * Activates the given node coming from the given {@link PathEntry}.
	 * Steps:
	 * 1. Sets the node to active.
	 * 2. Conducts its action {@link IAction}.
	 * 3. Add {@link PathEntry} for node.  
	 * @param currentNode TODO
	 * @param entry the pathentry from where to activate the node
	 * @param nextNode the node to be activated
	 */
	private void followEdge(INode currentNode, PathEntry entry, IEdge edge) {
		
		INodeData nextNodeData = flow.getDataForNode(edge.getEndNode());
		
		Assert.assertFalse(nextNodeData.isActive());
		
		nextNodeData.setActive(true);
		
		RuleAction action = nextNodeData.getNode().getAction();
		
		action.doIt(theCase);
		
		addPathEntryForNode(currentNode, entry, edge);
		
		
	}



	/**
	 * Selects appropriate successor of {@code node} according to the current state of the case.
	 * 
	 * @param node
	 * @return
	 */
	private IEdge selectNextEdge(INode node) {
		
		INodeData Node = flow.getDataForNode(node);
		Node.setActive(true);
		
		Iterator<IEdge> edges = node.getOutgoingEdges().iterator();
		
		while (edges.hasNext()) {
			
			IEdge edge = edges.next();
			
			try {
				if (edge.getCondition().eval(theCase)) {
					
					//ausser fork
					assertOtherEdgesFalse(edges); //all other edges' predicates must be false
					
					
					IEdgeData edgeData = flow.getDataForEdge(edge);
					edgeData.fire(); 
					
					return edge;
					
				}
			} catch (NoAnswerException e) {
				// TODO 
			} catch (UnknownAnswerException e) {
				// TODO 
			}
		}
		
//		throw new UnsupportedOperationException("can not stay in node '" + nodeDeclaration + "'");
		
		return null;
		
		
	}
	
	

	private void assertOtherEdgesFalse(Iterator<IEdge> edges) throws NoAnswerException, UnknownAnswerException {
		while (edges.hasNext()) { 
			if (edges.next().getCondition().eval(theCase)) {
				throw new IllegalStateException("");
			}
			
		}
	}


	public Flow getFlowDeclaration() {
		return flowDeclaration;
	}
	
	public XPSCase getCase() {
		return theCase;
	}


	@Override
	public void propagate(XPSCase theCase, Collection<PropagationEntry> changes) {
	
		
		for (PathEntry entry : new ArrayList<PathEntry>(pathEnds)) {
			
			maintainTruth(entry, changes);
			
		}
		
		for (PathEntry entry : new ArrayList<PathEntry>(pathEnds)) {
			
			flow(entry);
		}
		
	}
	


	
	private void maintainTruth(PathEntry startEntry, Collection<PropagationEntry> changes) {

		PathEntry pathend = startEntry;
		PathEntry entry = startEntry;
		
		while (entry != null) {
			
			INodeData data = entry.getNodeData();
			INode node = data.getNode();
			
			IEdge edge = entry.getEdge();
			
			
			boolean eval;
			
			try {
				eval = edge.getCondition().eval(theCase);
				
			} catch (NoAnswerException e) {
				eval = false;
			} catch (UnknownAnswerException e) {
				eval = false;
			}
			
			if (!eval) {
				
				IEdgeData edgeData = flow.getDataForEdge(edge);
				edgeData.unfire();

				data.setActive(false);
				data.removeSupport(entry);
				
				node.getAction().undo(theCase);
				
				replacePathEnd(entry, pathend);
				pathend = entry;
				
			}
			
			
			entry = entry.getPath();
		}
		
		
	}



	@Override
	public DiagnosisState getState(XPSCase theCase, Diagnosis theDiagnosis) {
		// TODO Auto-generated method stub
		return null;
	}
	
	
	@Override
	public boolean isContributingToResult() {
		// TODO Auto-generated method stub
		return false;
	}
	
//	@Override
//	public void propagate(XPSCase theCase, Collection<PropagationEntry> changes) {
		

		
//		Set<IEdgeData> nowTrueEdges = new HashSet<IEdgeData>();
//		Set<IEdgeData> nowFalseEdges = new HashSet<IEdgeData>();
//		
//		for (PropagationEntry entry : changes) { //for all changed objects
//			
//			
//			NodeAndEdgeSet set = (NodeAndEdgeSet) entry.getObject().getKnowledge(FluxSolver.class, DIAFLUX).get(0);
//			
//			//check all edges where it is used...
//			for (IEdge edge : set.getEdges()) {
//				
//				IEdgeData edgeData = flow.getDataForEdge(edge);
//				if (edgeData.hasFired()) { //if the edge had fired
//					
//					try {
//						boolean eval = edge.getCondition().eval(theCase);
//						if (!eval) { //... and is no longer
//							nowFalseEdges.add(edgeData); //...put it in set NF
//						}
//					} catch (NoAnswerException e) { // if edge had fired and now has no answer anymore, it is now false
//						nowFalseEdges.add(edgeData);
//					} catch (UnknownAnswerException e) { //TODO right? There could be an "unknown"-Edge that was taken
//						nowFalseEdges.add(edgeData);
//					}
//				} else { //edge had not fired...
//					
//					try {
//						boolean eval = edge.getCondition().eval(theCase);
//						if (eval) {//...but is now
//							nowTrueEdges.add(edgeData); // put it in set NT
//						}
//					} catch (NoAnswerException e) { // an edge with no answer is not true
//					} catch (UnknownAnswerException e) { //TODO right? There could be an "unknown"-Edge that has to be taken
//					}
//					
//				}
//			}
//			
//			
//		}
//		for (IEdgeData edgeData : nowFalseEdges) {
//				
//				edgeData.unfire();
//				
//				
//				
//				
//			}
//	}
	
	
	
	public static void main(String[] args) {
		
		INode startNode = new StartNode("start");
		
//		INode volt = FlowFactory.getInstance().createNode();
		
		
	}
	
	
	
	

}
