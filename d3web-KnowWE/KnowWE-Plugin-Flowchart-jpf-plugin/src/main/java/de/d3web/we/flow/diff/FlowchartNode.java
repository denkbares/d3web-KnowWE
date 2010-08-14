package de.d3web.we.flow.diff;

public class FlowchartNode {

	private String id = "";
	private int left = -1;
	private int top = -1;
	private String text = "";
	private FlowchartNodeType NodeType = FlowchartNodeType.none;

	public FlowchartNode(String id) {
		this.id = id;
	}

	public FlowchartNode(String id, int left, int top) {
		this.id = id;
		this.left = left;
		this.top = top;
	}

	public FlowchartNode(int left, int top) {
		this.id = "temp";
		this.left = left;
		this.top = top;
	}

	public FlowchartNode(String id, int left, int top,
			FlowchartNodeType nodeType, String text) {

		this.id = id;
		this.left = left;
		this.top = top;
		this.NodeType = nodeType;
		this.text = text;
	}

	public void setLeft(int left) {
		this.left = left;
	}

	public void setTop(int top) {
		this.top = top;
	}

	public void setNodeType(FlowchartNodeType nodeType) {
		this.NodeType = nodeType;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getID() {
		return this.id;
	}

	public int getLeft() {
		return this.left;
	}

	public int getTop() {
		return this.top;
	}

	public FlowchartNodeType getNodeType() {
		return this.NodeType;
	}

	public String getText() {
		return this.text;
	}

	@Override
	public String toString() {
		return "{" + this.getID() + ", " + this.getLeft() + ", "
				+ this.getTop() + ", " + this.getNodeType() + ", "
				+ this.getText() + "}";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + prime * this.getID().hashCode();
		result = prime * result + prime * this.getLeft();
		result = prime * result + prime * this.getTop();
		result = prime * result + prime * this.getNodeType().hashCode();
		result = prime * result + prime * this.getText().hashCode();
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		FlowchartNode other = (FlowchartNode) obj;
		if (!this.getID().equals(other.getID())) {
			return false;
		}
		else if (this.getLeft() != other.getLeft()) {
			return false;
		}
		else if (this.getTop() != other.getTop()) {
			return false;
		}
		else if (!this.getNodeType().equals(other.getNodeType())) {
			return false;
		}
		else if (!this.getText().equals(other.getText())) {
			return false;
		}
		return true;
	}

	public boolean equalsPositionOnly(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		FlowchartNode other = (FlowchartNode) obj;
		if (this.getLeft() != other.getLeft()) {
			return false;
		}
		if (this.getTop() != other.getTop()) {
			return false;
		}
		return true;
	}

}
