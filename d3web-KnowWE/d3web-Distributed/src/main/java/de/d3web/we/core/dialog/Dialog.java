/*
 * Copyright (C) 2009 Chair of Artificial Intelligence and Applied Informatics
 *                    Computer Science VI, University of Wuerzburg
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

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
