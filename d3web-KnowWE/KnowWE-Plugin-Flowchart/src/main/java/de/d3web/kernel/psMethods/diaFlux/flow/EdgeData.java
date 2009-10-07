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
