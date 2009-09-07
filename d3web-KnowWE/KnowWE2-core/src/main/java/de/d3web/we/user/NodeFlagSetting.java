package de.d3web.we.user;

public class NodeFlagSetting {
	
	private String nodeID = null;
	
	boolean quickEdit = false;
	
	public boolean isQuickEdit() {
		return quickEdit;
	}

	public void setQuickEdit(boolean quickEdit) {
		this.quickEdit = quickEdit;
	}

	public NodeFlagSetting(String id) {
		this.nodeID = id;
	}

}
