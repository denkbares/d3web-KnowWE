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

import javax.swing.undo.CannotUndoException;

import de.d3web.kernel.XPSCase;

/**
 * 
 * @author hatko
 *
 */
public interface IAction {
	
	IAction NOOP = new IAction() {
		
		@Override
		public void undo() {
			
		}
		
		@Override
		public boolean isUndoable() {
			return true;
		}
		
		@Override
		public Object getObject() {
			return null;
		}
		
		@Override
		public void act(XPSCase theCase) {
			
		}
		
	};
	

	
	/**
	 * Conducts this action.
	 * @param theCase theCase
	 */
	void act(XPSCase theCase);
	
	
	boolean isUndoable();
	
	
	/**
	 * undoes this action's effects if possible 
	 * if not an exception is thrown 
	 * 
	 * @throws CannotUndoException if the action is not undoable
	 */
	void undo();
	
	/**
	 * 
	 * @return s the object this action acts upon
	 */
	Object getObject();
	
	

}
