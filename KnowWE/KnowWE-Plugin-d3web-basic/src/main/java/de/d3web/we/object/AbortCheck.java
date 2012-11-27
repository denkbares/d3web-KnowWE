package de.d3web.we.object;

import java.util.Collection;
import java.util.Collections;

import de.knowwe.core.report.Message;

public class AbortCheck {

	private Collection<Message> msgs = Collections.emptyList();

	private boolean hasErrors = false;
	private boolean termExists = false;

	public void setHasErrors(boolean hasErrors) {
		this.hasErrors = hasErrors;
	}

	public void setTermExists(boolean termExists) {
		this.termExists = termExists;
	}

	public void setMessages(Collection<Message> messages) {
		this.msgs = messages;
	}

	public boolean termExist() {
		return this.termExists;
	}

	public boolean hasErrors() {
		return this.hasErrors || !this.msgs.isEmpty();
	}

	public Collection<Message> getErrors() {
		return this.msgs;
	}

}
