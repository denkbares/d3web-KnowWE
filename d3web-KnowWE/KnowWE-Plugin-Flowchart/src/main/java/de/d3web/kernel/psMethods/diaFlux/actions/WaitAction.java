package de.d3web.kernel.psMethods.diaFlux.actions;

import de.d3web.kernel.XPSCase;

public class WaitAction implements IAction {
	
	private final long millis;
	
	public WaitAction(long millis) {
		this.millis = millis;
	}

	@Override
	public void act(XPSCase theCase) {

		//TODO How to wait?!?1
	}

	@Override
	public Object getObject() {
		return new Long(millis);
	}

	@Override
	public boolean isUndoable() {
		return false;
	}

	@Override
	public void undo() {
		throw new UnsupportedOperationException("Can not undo :" + this);
	}
	
	
	@Override
	public String toString() {
		return "Wait(" + millis + ")";
	}

}
