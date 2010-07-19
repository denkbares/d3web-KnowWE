/*
 * Copyright (C) 2009 Chair of Artificial Intelligence and Applied Informatics
 *                    Computer Science VI, University of Wuerzburg
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
package de.d3web.diaFlux;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.d3web.core.inference.PSMethod;
import de.d3web.core.inference.Rule;
import de.d3web.core.inference.PSAction;
import de.d3web.core.knowledge.terminology.NamedObject;
import de.d3web.core.session.Session;
import de.d3web.diaFlux.flow.Flow;
import de.d3web.diaFlux.flow.FlowSet;
import de.d3web.diaFlux.flow.StartNode;
import de.d3web.diaFlux.inference.FluxSolver;

/**
 *
 * @author Reinhard Hatko
 * Created on: 03.11.2009
 */
public class IndicateFlowAction extends PSAction {
	
	private final String flowName;
	private final String startNodeName;

	public IndicateFlowAction(String flow, String node) {
		this.flowName = flow;
		this.startNodeName = node;
	}

	@Override
	public PSAction copy() {
		return new IndicateFlowAction(flowName, startNodeName);
	}

	@Override
	public void doIt(Session session, Object source, PSMethod psmethod) {
		
		
		log("Indicating Startnode '"  + startNodeName +"' of flow '" + flowName + "'.", Level.FINE);
		
		StartNode startNode = findStartNode(session);
		
		FluxSolver.indicateFlow((Rule) source, startNode, session);

	}
	
	/**
	 * returns the StartNode that is called by the supplied action
	 */
	private StartNode findStartNode(Session session) {
		
		FlowSet flowSet = FluxSolver.getFlowSet(session);
		
		Flow subflow = flowSet.getByName(flowName);
		List<StartNode> startNodes = subflow.getStartNodes();
		
		for (StartNode iNode : startNodes) {
			if (iNode.getName().equalsIgnoreCase(startNodeName)) {
				return iNode;
				
			}
		}
		
		log("Startnode '" + startNodeName + "' of flow '" + flowName +"' not found.", Level.SEVERE);
		return null;
		
	}
	
	private void log(String message, Level level) {
		Logger.getLogger(getClass().getName()).log(level, message);
	}

	@Override 
	public List<? extends NamedObject> getTerminalObjects() {
		return new ArrayList<NamedObject>(0);
	}

	@Override
	public void undo(Session session, Object source, PSMethod psmethod) {
		
		
		
	}
	
	public String getFlowName() {
		return flowName;
	}
	
	public String getStartNodeName() {
		return startNodeName;
	}

}
