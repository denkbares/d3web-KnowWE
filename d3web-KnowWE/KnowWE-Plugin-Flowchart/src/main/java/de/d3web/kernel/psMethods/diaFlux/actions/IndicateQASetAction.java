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

package de.d3web.kernel.psMethods.diaFlux.actions;

import de.d3web.kernel.XPSCase;
import de.d3web.kernel.domainModel.QASet;

/**
 * @author Reinhard Hatko
 * Created: 14.09.2009
 *
 */
public class IndicateQASetAction implements IAction {

	
	private final QASet qaSet;
	
	
	public IndicateQASetAction(QASet qaSet) {
		this.qaSet = qaSet; 
	}
	
	@Override
	public void act(XPSCase theCase) {

		theCase.getQASetManager().getQASetQueue().add(qaSet);
		//TODO notify???
		
	}

	@Override
	public Object getObject() {
		return qaSet;
	}

	@Override
	public boolean isUndoable() {
		return true; //not really if questions were asked, but has it sideeffects?
	}

	@Override
	public void undo() {
		// do something?? z.b. Werte von nur 1 mal zu erfragenden Fragen lï¿½schen?
		
		//QASet von Agenda lï¿½schen, falls noch nicht gefragt?s
	}
	
	@Override
	public String toString() {
		return "Indicate (" + qaSet + ")";
	}
	

}
