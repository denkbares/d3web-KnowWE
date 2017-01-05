package de.knowwe.ontology.turtle.compile;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.rdf4j.model.Statement;

import de.knowwe.core.report.Message;


public class StatementProviderResult {

	private final Collection<Message> messages = new ArrayList<>(0);
	private final Collection<Statement> statments = new ArrayList<>();

	public void addMessage(Message m) {
		messages.add(m);
	}

	public void addStatement(Statement s) {
		statments.add(s);
	}

	public Collection<Message> getMessages() {
		return messages;
	}

	public Collection<Statement> getStatements() {
		return statments;
	}

}
