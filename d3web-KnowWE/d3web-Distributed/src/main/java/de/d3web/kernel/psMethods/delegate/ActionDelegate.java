package de.d3web.kernel.psMethods.delegate;

import java.util.ArrayList;

import de.d3web.kernel.domainModel.NamedObject;
import de.d3web.kernel.domainModel.RuleAction;
import de.d3web.kernel.domainModel.RuleComplex;

public class ActionDelegate extends AbstractActionDelegate {

	private static final long serialVersionUID = -9002222137674579476L;

	public ActionDelegate(RuleComplex theCorrespondingRule) {
		super(theCorrespondingRule);
	}

	@Override
	public RuleAction copy() {
		ActionDelegate result = new ActionDelegate(getCorrespondingRule());
		result.setNamedObjects(new ArrayList<NamedObject>(getNamedObjects()));
		result.setTargetNamespace(new String(getTargetNamespace()));
		return result;
	}
	
	public int hashCode() {
		if(getNamedObjects() != null)
			return (getNamedObjects().hashCode()) + 37 * getTargetNamespace().hashCode();
		return 0;
	}
	
	public boolean equals(Object o) {
		if (o==this) {
			return true;
		}
		if (o instanceof ActionDelegate) {
			ActionDelegate a = (ActionDelegate)o;
			return isSame(a.getNamedObjects(), getNamedObjects()) 
				&& getTargetNamespace().equals(a.getTargetNamespace()) 
				&& (isTemporary() == a.isTemporary());
		} else {
			return false;
		}
	}
	
}
