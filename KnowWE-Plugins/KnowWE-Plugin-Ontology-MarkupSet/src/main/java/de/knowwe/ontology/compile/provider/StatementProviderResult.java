/*
 * Copyright (C) 2018 denkbares GmbH. All rights reserved.
 */

package de.knowwe.ontology.compile.provider;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;

import de.knowwe.core.report.Message;
import de.knowwe.core.report.Messages;
import de.knowwe.rdf2go.Rdf2GoCompiler;
import de.knowwe.rdf2go.Rdf2GoCore;

public class StatementProviderResult {

	private final Rdf2GoCore core;
	private final Collection<Message> messages = new ArrayList<>(0);
	private final Collection<Statement> statements = new ArrayList<>();

	public StatementProviderResult(Rdf2GoCompiler compiler) {
		this(compiler.getRdf2GoCore());
	}

	public StatementProviderResult(Rdf2GoCore core) {
		this.core = core;
	}

	public void addMessage(Message m) {
		messages.add(m);
	}

	public void addStatement(Statement s) {
		statements.add(s);
	}

	public Collection<Message> getMessages() {
		return messages;
	}

	public Collection<Statement> getStatements() {
		return statements;
	}

	public StatementProviderResult error(String msg) {
		addMessage(Messages.error(msg));
		return this;
	}

	public StatementProviderResult warning(String msg) {
		addMessage(Messages.warning(msg));
		return this;
	}

	public StatementProviderResult addStatement(Resource subject, IRI predicate, Value object) {
		addStatement(core.createStatement(subject, predicate, object));
		return this;
	}

	public StatementProviderResult addStatement(Resource subject, URI predicate, Value object) {
		addStatement(subject, core.createIRI(predicate.toString()), object);
		return this;
	}

	public StatementProviderResult addStatement(String subject, URI predicate, Value object) {
		addStatement(core.createIRI(subject), core.createIRI(predicate.toString()), object);
		return this;
	}

	public StatementProviderResult addStatement(String subject, URI predicate, URI object) {
		addStatement(core.createIRI(subject), core.createIRI(predicate.toString()), core.createIRI(object.toString()));
		return this;
	}

	public StatementProviderResult addStatement(IRI subject, URI predicate, URI object) {
		addStatement(subject, core.createIRI(predicate.toString()), core.createIRI(object.toString()));
		return this;
	}

	public StatementProviderResult addLiteralStatement(URI subject, URI predicate, String literal) {
		addStatement(core.createIRI(subject.toString()), core.createIRI(predicate.toString()), core.createLiteral(literal));
		return this;
	}

	public StatementProviderResult addLiteralStatement(String subject, URI predicate, String literal) {
		addStatement(core.createIRI(subject), core.createIRI(predicate.toString()), core.createLiteral(literal));
		return this;
	}

	public StatementProviderResult addLiteralStatement(URI subject, URI predicate, boolean literal) {
		addStatement(core.createIRI(subject.toString()), core.createIRI(predicate.toString()), core.createDatatypeLiteral(literal));
		return this;
	}

	public StatementProviderResult addLiteralStatement(String subject, URI predicate, boolean literal) {
		addStatement(core.createIRI(subject), core.createIRI(predicate.toString()), core.createDatatypeLiteral(literal));
		return this;
	}

	public StatementProviderResult addLiteralStatement(URI subject, URI predicate, double literal) {
		addStatement(core.createIRI(subject.toString()), core.createIRI(predicate.toString()), core.createDatatypeLiteral(literal));
		return this;
	}

	public StatementProviderResult addLiteralStatement(IRI subject, URI predicate, double literal) {
		addStatement(subject, core.createIRI(predicate.toString()), core.createDatatypeLiteral(literal));
		return this;
	}

	public StatementProviderResult addLiteralStatement(String subject, URI predicate, double literal) {
		addStatement(core.createIRI(subject), core.createIRI(predicate.toString()), core.createDatatypeLiteral(literal));
		return this;
	}

	public StatementProviderResult addLiteralStatement(URI subject, URI predicate, int literal) {
		addStatement(core.createIRI(subject.toString()), core.createIRI(predicate.toString()), core.createDatatypeLiteral(literal));
		return this;
	}

	public StatementProviderResult addLiteralStatement(String subject, URI predicate, int literal) {
		addStatement(core.createIRI(subject), core.createIRI(predicate.toString()), core.createDatatypeLiteral(literal));
		return this;
	}

	public StatementProviderResult addLiteralStatement(IRI subject, URI predicate, String literal) {
		addStatement(subject, core.createIRI(predicate.toString()), core.createLiteral(literal));
		return this;
	}

	public StatementProviderResult addLiteralStatement(IRI subject, URI predicate, Literal literal) {
		addStatement(subject, core.createIRI(predicate.toString()), literal);
		return this;
	}

	public StatementProviderResult addResourceStatement(URI subject, URI predicate, URI object) {
		addStatement(core.createIRI(subject.toString()), core.createIRI(predicate.toString()), core.createIRI(object.toString()));
		return this;
	}

	public StatementProviderResult addResourceStatement(URI subject, URI predicate, IRI object) {
		addStatement(core.createIRI(subject.toString()), core.createIRI(predicate.toString()), object);
		return this;
	}

	public StatementProviderResult addResourceStatement(String subject, URI predicate, URI object) {
		addStatement(core.createIRI(subject), core.createIRI(predicate.toString()), core.createIRI(object.toString()));
		return this;
	}

	public StatementProviderResult addResourceStatement(String subject, URI predicate, IRI object) {
		addStatement(core.createIRI(subject), core.createIRI(predicate.toString()), object);
		return this;
	}

	public StatementProviderResult addResourceStatement(URI subject, URI predicate, String object) {
		addStatement(core.createIRI(subject.toString()), core.createIRI(predicate.toString()), core.createIRI(object));
		return this;
	}

	public StatementProviderResult addResourceStatement(String subject, URI predicate, String object) {
		addStatement(core.createIRI(subject), core.createIRI(predicate.toString()), core.createIRI(object));
		return this;
	}

	public StatementProviderResult addStatement(URI subject, URI predicate, URI object) {
		addStatement(core.createIRI(subject), core.createIRI(predicate.toString()), core.createIRI(object));
		return this;
	}
}
