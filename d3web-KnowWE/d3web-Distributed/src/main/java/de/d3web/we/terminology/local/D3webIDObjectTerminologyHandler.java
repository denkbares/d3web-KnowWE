package de.d3web.we.terminology.local;

import java.util.ArrayList;
import java.util.List;

import de.d3web.kernel.domainModel.Answer;
import de.d3web.kernel.domainModel.IDObject;
import de.d3web.kernel.domainModel.NamedObject;
import de.d3web.kernel.domainModel.qasets.QuestionChoice;
import de.d3web.kernel.supportknowledge.PropertiesContainer;
import de.d3web.kernel.supportknowledge.Property;

public class D3webIDObjectTerminologyHandler extends LocalTerminologyHandler<IDObject, IDObject> {

	protected List<IDObject> fifo(IDObject no) {
		List<IDObject> queue = new ArrayList<IDObject>();
		queue.add(no);
		List<IDObject> result = new ArrayList<IDObject>();
		fifo(queue, result);
		for (IDObject object : queue) {
			if(object instanceof PropertiesContainer) {
				Boolean privat = (Boolean) ((PropertiesContainer)object).getProperties().getProperty(Property.PRIVATE);
				if(privat == null || !privat) {
					result.add(object);
				}
			} else {
				result.add(object);
			}
		}
		return result;
	}
	
	
	
	private void fifo(List<? extends IDObject> toExpand, List<IDObject> result) {
		if(toExpand == null || toExpand.isEmpty()) return;
		List<IDObject> newToExpand = new ArrayList<IDObject>();
		for (IDObject each : toExpand) {
			if(checkFilter(each) && !result.contains(each)) {
				result.add(each);
				if(each instanceof NamedObject) {
					NamedObject no = (NamedObject) each;
					for (NamedObject child : no.getChildren()) {
						if(!toExpand.contains(child) && !result.contains(child)) {
							newToExpand.add(child);
						}
					}
				}
				if(each instanceof QuestionChoice) {
					QuestionChoice qc = (QuestionChoice) each;
					for (Answer answer : qc.getAllAlternatives()) {
						if(!toExpand.contains(answer) && !result.contains(answer)) {
							newToExpand.add(answer);
						}
					}
				}
			}
		}
		fifo(newToExpand, result);
	}
	
	public D3webIDObjectTerminologyHandler newInstance() {
		return new D3webIDObjectTerminologyHandler();
	}

	@Override
	public IDObject getTerminologicalObject(String id) {
		for (IDObject ido : this) {
			if(ido.getId().equals(id)) {
				return ido;
			}
		}
		return null;
	}
	
}
