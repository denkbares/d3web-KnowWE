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
package de.d3web.kernel.psMethods.diaFlux.flow;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

import de.d3web.kernel.domainModel.CaseObjectSource;
import de.d3web.kernel.domainModel.RuleAction;

/**
 * @author hatko
 *
 */
public interface INode extends Serializable, CaseObjectSource {
	
	
	/**
	 * 
	 * @return s a list of this node's incoming edges. 
	 */
	List<IEdge> getIncomingEdges();
	
	/**
	 * 
	 * @return s a list of this node's outgoing edges.
	 */
	List<IEdge> getOutgoingEdges();
	
	
	/**
	 * 
	 * @return s the action this node is doing when reached
	 */
	RuleAction getAction();
	
	
	
	
	
	

}
