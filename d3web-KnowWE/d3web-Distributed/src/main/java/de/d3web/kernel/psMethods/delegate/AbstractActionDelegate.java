package de.d3web.kernel.psMethods.delegate;

import java.util.ArrayList;
import java.util.List;

import de.d3web.kernel.XPSCase;
import de.d3web.kernel.dialogControl.QASetManager;
import de.d3web.kernel.domainModel.NamedObject;
import de.d3web.kernel.domainModel.RuleAction;
import de.d3web.kernel.domainModel.RuleComplex;

public abstract class AbstractActionDelegate extends RuleAction {

	private static final long serialVersionUID = -3105617138912654581L;

	private List<NamedObject> namedObjects;
	private String targetNamespace;
	private boolean temporary;
	
	public AbstractActionDelegate(RuleComplex theCorrespondingRule) {
		super(theCorrespondingRule);
		namedObjects = new ArrayList<NamedObject>();
		targetNamespace = "";
		temporary = true;
	}

	@Override
	public abstract RuleAction copy();

	@Override
	public void doIt(XPSCase theCase) {
		QASetManager manager = theCase.getQASetManager();
		for (NamedObject no : getNamedObjects()) {
			manager.propagate(no, getCorrespondingRule(), theCase.getPSMethodInstance(getProblemsolverContext()));
		}
	}

	@Override
	public void undo(XPSCase theCase) {
		// can not undo this kind of action
	}
	
	@Override
	public Class getProblemsolverContext() {
		return PSMethodDelegate.class;
	}	
	
	@Override
	public List getTerminalObjects() {
		return namedObjects;
	}

	public List<NamedObject> getNamedObjects() {
		return namedObjects;
	}
	
	public void setNamedObjects(List<NamedObject> nos) {
		namedObjects = nos;
	}
	
	protected boolean isSame(Object obj1, Object obj2) {
		if(obj1 == null && obj2 == null) return true;
		if(obj1 != null && obj2 != null) return obj1.equals(obj2);
		return false;
	}

	public String getTargetNamespace() {
		return targetNamespace;
	}

	public void setTargetNamespace(String foreignNamespace) {
		this.targetNamespace = foreignNamespace;
	}

	public boolean isTemporary() {
		return temporary;
	}

	public void setTemporary(boolean temporary) {
		this.temporary = temporary;
	}
}
