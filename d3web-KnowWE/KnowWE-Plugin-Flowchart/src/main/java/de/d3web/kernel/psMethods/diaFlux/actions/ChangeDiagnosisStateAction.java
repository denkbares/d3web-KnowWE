package de.d3web.kernel.psMethods.diaFlux.actions;

import de.d3web.kernel.XPSCase;
import de.d3web.kernel.domainModel.CaseObjectSource;
import de.d3web.kernel.domainModel.Diagnosis;
import de.d3web.kernel.domainModel.DiagnosisState;
import de.d3web.kernel.dynamicObjects.CaseDiagnosis;
import de.d3web.kernel.dynamicObjects.XPSCaseObject;
import de.d3web.kernel.psMethods.diaFlux.FluxSolver;

/**
 * @author Reinhard Hatko
 * Created: 14.09.2009
 *
 */
public class ChangeDiagnosisStateAction implements IAction {

	
	private final Diagnosis diagnosis;
	private final DiagnosisState state;
	
	
	
	public ChangeDiagnosisStateAction(Diagnosis diagnosis, DiagnosisState state) {
		this.diagnosis = diagnosis;
		this.state = state;
		
	}
	
	@Override
	public void act(XPSCase theCase) {
		CaseDiagnosis caseDiag = (CaseDiagnosis) theCase.getCaseObject(diagnosis);
		
		Object oldValue = caseDiag.getValue(FluxSolver.class);
		
		
		caseDiag.setValue(state, FluxSolver.class);
		
		
	}

	@Override
	public Object getObject() {
		return diagnosis;
	}

	@Override
	public boolean isUndoable() {
		return true;
	}

	@Override
	public void undo() {
		//TODO alten Wert setzen
		//wo merken??
	}

}
