package de.d3web.we.object;

import java.util.Collection;
import java.util.Collections;

import de.d3web.core.knowledge.terminology.NamedObject;
import de.knowwe.core.report.Message;

public class AbortCheck<TermObject extends NamedObject> {

	private Collection<Message> msgs = Collections.emptyList();

	private boolean hasErrors = false;
	private boolean termExists = false;
	private TermObject namedObject = null;
	private boolean skipCreation = false;

	public void setHasErrors(boolean hasErrors) {
		this.hasErrors = hasErrors;
	}

	public void setTermExists(boolean termExists) {
		this.termExists = termExists;
	}

	public void setMessages(Collection<Message> messages) {
		this.msgs = messages;
	}

	public void setNamedObject(TermObject namedObject) {
		this.namedObject = namedObject;
	}

	public boolean termExist() {
		return this.termExists;
	}

	public boolean hasErrors() {
		return this.hasErrors || !this.msgs.isEmpty();
	}

	public TermObject getNamedObject() {
		return this.namedObject;
	}

	public Collection<Message> getErrors() {
		return this.msgs;
	}

	public void setSkipCreation(boolean skipCreation) {
		this.skipCreation = skipCreation;
	}

	public boolean skipCreation() {
		return skipCreation;
	}
}
