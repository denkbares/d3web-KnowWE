package de.d3web.we.object;

import java.util.Collection;
import java.util.Collections;

import de.d3web.core.knowledge.terminology.NamedObject;
import de.knowwe.core.report.Message;

public class AbortCheck {

	private Collection<Message> msgs = Collections.emptyList();

	private boolean hasErrors = false;
	private boolean termExists = false;
	private NamedObject namedObject = null;

	public void setHasErrors(boolean hasErrors) {
		this.hasErrors = hasErrors;
	}

	public void setTermExists(boolean termExists) {
		this.termExists = termExists;
	}

	public void setMessages(Collection<Message> messages) {
		this.msgs = messages;
	}

	public void setNamedObject(NamedObject namedObject) {
		this.namedObject = namedObject;
	}

	public boolean termExist() {
		return this.termExists;
	}

	public boolean hasErrors() {
		return this.hasErrors || !this.msgs.isEmpty();
	}

	public NamedObject getNamedObject() {
		return this.namedObject;
	}

	public Collection<Message> getErrors() {
		return this.msgs;
	}

}
