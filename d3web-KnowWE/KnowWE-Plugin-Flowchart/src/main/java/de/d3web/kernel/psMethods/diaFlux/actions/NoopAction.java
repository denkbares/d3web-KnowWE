package de.d3web.kernel.psMethods.diaFlux.actions;

import java.util.ArrayList;
import java.util.List;

import de.d3web.kernel.XPSCase;
import de.d3web.kernel.domainModel.RuleAction;
import de.d3web.kernel.psMethods.diaFlux.FluxSolver;

/**
 * 
 * @author Reinhard Hatko
 * Created: 14.09.2009
 *
 */
public class NoopAction extends RuleAction {
	
	public static final NoopAction INSTANCE = new NoopAction();
	
	private NoopAction() {
		super(null);
		
	}

	public RuleAction copy() {
		return this;
	}

	@Override
	public void doIt(XPSCase theCase) {

	}

	@Override
	public Class getProblemsolverContext() {
		return FluxSolver.class;
	}

	@Override
	public List getTerminalObjects() {
		return new ArrayList(0);
	}

	@Override
	public void undo(XPSCase theCase) {

	}

}
