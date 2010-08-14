package de.d3web.we.flow.diff;

public class FlowchartEdge {

	private String id;
	private String source = "";
	private String target = "";
	private String guard = "";

	public FlowchartEdge(String id) {
		this.id = id;
	}

	public FlowchartEdge(String id, String source, String target, String guard) {
		this.id = id;
		if (source != null) {
			this.source = source;
		}

		if (target != null) {
			this.target = target;
		}
		if (guard != null) {
			this.guard = guard;
		}
	}

	public String getID() {
		return this.id;
	}

	public String getSource() {
		return this.source;
	}

	public String getTarget() {
		return this.target;
	}

	public String getGuard() {
		return this.guard;
	}

	public void setID(String id) {
		this.id = id;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public void setTarget(String target) {
		this.target = target;
	}

	public void setGuard(String guard) {
		this.guard = guard;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + this.getID().hashCode();
		result = prime * result + this.getSource().hashCode();
		result = prime * result + this.getTarget().hashCode();
		result = prime * result + this.getGuard().hashCode();
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		FlowchartEdge other = (FlowchartEdge) obj;
		if (!this.getID().equals(other.getID())) {
			return false;
		}
		else if (!this.getSource().equals(other.getSource())) {
			return false;
		}
		else if (!this.getTarget().equals(other.getTarget())) {
			return false;
		}
		else if (!this.getGuard().equals(other.getGuard())) {
			return false;
		}

		return true;
	}

	@Override
	public String toString() {
		return "{" + this.getID() + ", " + this.getSource() + ", "
				+ this.getTarget() + ", " + this.getGuard() + "}";
	}

}
