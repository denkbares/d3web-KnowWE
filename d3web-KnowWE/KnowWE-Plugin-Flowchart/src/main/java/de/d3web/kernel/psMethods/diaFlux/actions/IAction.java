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
