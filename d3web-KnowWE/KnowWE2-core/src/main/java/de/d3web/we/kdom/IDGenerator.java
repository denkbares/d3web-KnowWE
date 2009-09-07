package de.d3web.we.kdom;

import java.util.ArrayList;
import java.util.List;

public class IDGenerator {
	
	private int index;
	
	private List<String> assignedIDs = new ArrayList<String>();
	
	public IDGeneratorOutput newID() {
		return newID(nextGeneratedID());
	}
	
	
	public IDGeneratorOutput newID(String id) {
		if (id == null || id.equals("")) {
			id = nextGeneratedID();
		}
		boolean idConflict = false;
		if (assignedIDs.contains(id)) {
			idConflict = true;
			while (assignedIDs.contains(id)) {
				id = nextGeneratedID();
			}
		}
		assignedIDs.add(id);
		return new IDGeneratorOutput(id, idConflict);
	}
	
	
	// TODO: Generate infinite number of IDs...
	private String nextGeneratedID() {
		index++;
		return "Node" + index;
	}
	
	
	
}
