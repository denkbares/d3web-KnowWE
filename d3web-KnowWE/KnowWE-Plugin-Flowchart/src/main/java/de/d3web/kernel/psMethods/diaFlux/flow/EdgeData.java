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

package de.d3web.kernel.psMethods.diaFlux.flow;

import de.d3web.kernel.dynamicObjects.XPSCaseObject;

/**
 * @author Reinhard Hatko
 * Created: 14.09.2009
 *
 */
public class EdgeData extends XPSCaseObject implements IEdgeData {
	
	private boolean eval;
	private final IEdge edge;
	private boolean fired;
	

	/**
	 * @param eval
	 * @param edge
	 */
	public EdgeData(IEdge edge) {
		super(edge);
		this.edge = edge;
		this.fired = false;
		
	}

	@Override
	public IEdge getEdge() {
		return edge;
	}

	@Override
	public boolean getEvaluation() {
		return eval;
	}
	
	public void setEvaluation(boolean eval) {
		this.eval = eval;
	}
	
	@Override
	public boolean hasFired() {
		return fired;
	}
	
	protected void setFired(boolean fired) {
		this.fired = fired;
	}
	
	@Override
	public void fire() {
		setFired(true);
	}
	
	@Override
	public void unfire() {
		setFired(false);	
	}

}
