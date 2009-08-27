package de.d3web.we.terminology.local;

import java.util.ArrayList;
import java.util.List;

import de.d3web.kernel.domainModel.NamedObject;
import de.d3web.kernel.supportknowledge.Property;

public class D3webNamedObjectTerminologyHandler extends LocalTerminologyHandler<NamedObject, NamedObject> {
	
	public D3webNamedObjectTerminologyHandler() {
		super();
	}

	
	protected List<NamedObject> fifo(NamedObject no) {
		List<NamedObject> queue = new ArrayList<NamedObject>();
		queue.add(no);
		List<NamedObject> result = new ArrayList<NamedObject>();
		fifo(queue, result);
		for (NamedObject object : queue) {
			Boolean privat = (Boolean) object.getProperties().getProperty(Property.PRIVATE);
			if(privat == null || !privat) {
				result.add(object);
			}
		}
		return result;
	}
	
	private void fifo(List<? extends NamedObject> toExpand, List<NamedObject> result) {
		if(toExpand == null || toExpand.isEmpty()) return;
		List<NamedObject> newToExpand = new ArrayList<NamedObject>();
		for (NamedObject each : toExpand) {
			if(checkFilter(each) && !result.contains(each)) {
				//if(!each.getText().equals("P000") && !each.getText().equals("Q000")) {
				result.add(each);
				//}
				for (NamedObject child : each.getChildren()) {
					if(!toExpand.contains(child) && !result.contains(child)) {
						newToExpand.add(child);
					}
				}
			}
		}
		fifo(newToExpand, result);
	}
	
	
	
	public D3webNamedObjectTerminologyHandler newInstance() {
		return new D3webNamedObjectTerminologyHandler();
	}


	@Override
	public NamedObject getTerminologicalObject(String id) {
		for (NamedObject no : this) {
			if(no.getId().equals(id)) {
				return no;
			}
		}
		return null;
	}
	
	
	
}
