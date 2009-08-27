package de.d3web.we.core.dialog;

import de.d3web.we.core.knowledgeService.KnowledgeServiceSession;

public class Dialog {
	
	private final KnowledgeServiceSession dialog;
	private final KnowledgeServiceSession reason;
	private final String comment;
	private boolean finished;
	private boolean cancelled;
	
	

	public Dialog(KnowledgeServiceSession dialog, KnowledgeServiceSession reason, String comment) {
		super();
		this.dialog = dialog;
		this.reason = reason;
		this.comment = comment;
		finished = false;
		cancelled = false;
	}

	public boolean isActive() {
		if(isFinished() || isCancelled()) {
			return false;
		}
		return true;
	}
	
	public boolean equals(Object o) {
		if(o == this) return true;
		if(!(o instanceof Dialog)) return false;
		Dialog dialog = (Dialog) o;
		return dialog.getDialog().equals(getDialog());
	}
	
	public int hashCode() {
		return getDialog().hashCode();
	}
	
	public KnowledgeServiceSession getDialog() {
		return dialog;
	}

	public KnowledgeServiceSession getReason() {
		return reason;
	}

	public String getComment() {
		return comment;
	}
	
	public boolean isFinished() {
		return finished;
	}
	
	public void setFinished(boolean finished) {
		this.finished = finished;
	}

	public boolean isCancelled() {
		return cancelled;
	}

	public void setCancelled(boolean cancelled) {
		this.cancelled = cancelled;
	}
}
