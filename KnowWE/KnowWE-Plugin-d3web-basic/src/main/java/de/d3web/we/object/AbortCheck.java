package de.d3web.we.object;

import java.util.Collection;
import java.util.Collections;

import de.knowwe.core.report.Message;

public class AbortCheck {

	private Collection<Message> msgs = Collections.emptyList();

	private boolean termExists = false;

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
		return !this.msgs.isEmpty();
	}

	public Collection<Message> getErrors() {
		return this.msgs;
	}

}
