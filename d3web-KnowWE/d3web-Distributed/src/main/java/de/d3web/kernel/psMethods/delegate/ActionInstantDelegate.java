package de.d3web.kernel.psMethods.delegate;

import java.util.ArrayList;

import de.d3web.kernel.domainModel.NamedObject;
import de.d3web.kernel.domainModel.RuleAction;
import de.d3web.kernel.domainModel.RuleComplex;

public class ActionInstantDelegate extends AbstractActionDelegate {
	
	private static final long serialVersionUID = 7810881477953980445L;

	public ActionInstantDelegate(RuleComplex theCorrespondingRule) {
		super(theCorrespondingRule);
	}

	@Override
	public RuleAction copy() {
		ActionInstantDelegate result = new ActionInstantDelegate(getCorrespondingRule());
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
		if (o instanceof ActionInstantDelegate) {
			ActionInstantDelegate a = (ActionInstantDelegate)o;
			return isSame(a.getNamedObjects(), getNamedObjects()) 
				&& getTargetNamespace().equals(a.getTargetNamespace()) 
				&& (isTemporary() == a.isTemporary());
		} else {
			return false;
		}
	}
}
