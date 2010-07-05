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

/**
 * 
 */
package de.d3web.diaFlux.flow;

import java.util.List;

import de.d3web.core.inference.PSAction;
import de.d3web.core.session.CaseObjectSource;

/**
 * @author hatko
 *
 */
public interface INode extends CaseObjectSource {
	
	/**
	 * 
	 * @return s a list of this node's outgoing edges.
	 */
	List<IEdge> getOutgoingEdges();
	
	
	/**
	 * 
	 * @return s the action this node is doing when reached
	 */
	PSAction getAction();
	
	/**
	 * @return s the id of the node
	 */
	String getID();
	
	
	/**
	 * @return s the flow this node belongs to
	 */
	Flow getFlow();
	
	
	/**
	 * sets this nodes containing flow
	 */
	void setFlow(Flow flow);
	
	
	
	
	
	

}
