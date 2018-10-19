/*
 * Copyright (C) 2018 denkbares GmbH. All rights reserved.
 */

package de.knowwe.ontology.compile.provider;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.URI;
import org.eclipse.rdf4j.model.Value;

import de.knowwe.core.report.Message;
import de.knowwe.core.report.Messages;
import de.knowwe.rdf2go.Rdf2GoCompiler;
import de.knowwe.rdf2go.Rdf2GoCore;

public class StatementProviderResult {

	private final Rdf2GoCore core;
	private final Collection<Message> messages = new ArrayList<>(0);
	private final Collection<Statement> statments = new ArrayList<>();

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
		statments.add(s);
	}

	public Collection<Message> getMessages() {
		return messages;
	}

	public Collection<Statement> getStatements() {
		return statments;
	}

	public StatementProviderResult error(String msg) {
		addMessage(Messages.error(msg));
		return this;
	}

	public StatementProviderResult warning(String msg) {
		addMessage(Messages.warning(msg));
		return this;
	}

	public StatementProviderResult addStatement(Resource subject, URI predicate, Value object) {
		addStatement(core.createStatement(subject, predicate, object));
		return this;
	}

	public StatementProviderResult addStatement(Resource subject, java.net.URI predicate, Value object) {
		addStatement(subject, core.createURI(predicate), object);
		return this;
	}

	public StatementProviderResult addStatement(String subject, java.net.URI predicate, Value object) {
		addStatement(core.createURI(subject), core.createURI(predicate), object);
		return this;
	}

	public StatementProviderResult addStatement(String subject, java.net.URI predicate, java.net.URI object) {
		addStatement(core.createURI(subject), core.createURI(predicate), core.createURI(object));
		return this;
	}

	public StatementProviderResult addStatement(URI subject, java.net.URI predicate, java.net.URI object) {
		addStatement(subject, core.createURI(predicate), core.createURI(object));
		return this;
	}

	public StatementProviderResult addLiteralStatement(java.net.URI subject, java.net.URI predicate, String literal) {
		addStatement(core.createURI(subject), core.createURI(predicate), core.createLiteral(literal));
		return this;
	}

	public StatementProviderResult addLiteralStatement(String subject, java.net.URI predicate, String literal) {
		addStatement(core.createURI(subject), core.createURI(predicate), core.createLiteral(literal));
		return this;
	}

	public StatementProviderResult addLiteralStatement(java.net.URI subject, java.net.URI predicate, boolean literal) {
		addStatement(core.createURI(subject), core.createURI(predicate), core.createDatatypeLiteral(literal));
		return this;
	}

	public StatementProviderResult addLiteralStatement(String subject, java.net.URI predicate, boolean literal) {
		addStatement(core.createURI(subject), core.createURI(predicate), core.createDatatypeLiteral(literal));
		return this;
	}

	public StatementProviderResult addLiteralStatement(java.net.URI subject, java.net.URI predicate, double literal) {
		addStatement(core.createURI(subject), core.createURI(predicate), core.createDatatypeLiteral(literal));
		return this;
	}

	public StatementProviderResult addLiteralStatement(String subject, java.net.URI predicate, double literal) {
		addStatement(core.createURI(subject), core.createURI(predicate), core.createDatatypeLiteral(literal));
		return this;
	}

	public StatementProviderResult addLiteralStatement(java.net.URI subject, java.net.URI predicate, int literal) {
		addStatement(core.createURI(subject), core.createURI(predicate), core.createDatatypeLiteral(literal));
		return this;
	}

	public StatementProviderResult addLiteralStatement(String subject, java.net.URI predicate, int literal) {
		addStatement(core.createURI(subject), core.createURI(predicate), core.createDatatypeLiteral(literal));
		return this;
	}

	public StatementProviderResult addLiteralStatement(URI subject, java.net.URI predicate, String literal) {
		addStatement(subject, core.createURI(predicate), core.createLiteral(literal));
		return this;
	}

	public StatementProviderResult addLiteralStatement(URI subject, java.net.URI predicate, Literal literal) {
		addStatement(subject, core.createURI(predicate), literal);
		return this;
	}

	public StatementProviderResult addResourceStatement(java.net.URI subject, java.net.URI predicate, java.net.URI object) {
		addStatement(core.createURI(subject), core.createURI(predicate), core.createURI(object));
		return this;
	}

	public StatementProviderResult addResourceStatement(java.net.URI subject, java.net.URI predicate, URI object) {
		addStatement(core.createURI(subject), core.createURI(predicate), object);
		return this;
	}

	public StatementProviderResult addResourceStatement(String subject, java.net.URI predicate, java.net.URI object) {
		addStatement(core.createURI(subject), core.createURI(predicate), core.createURI(object));
		return this;
	}

	public StatementProviderResult addResourceStatement(String subject, java.net.URI predicate, URI object) {
		addStatement(core.createURI(subject), core.createURI(predicate), object);
		return this;
	}

	public StatementProviderResult addResourceStatement(java.net.URI subject, java.net.URI predicate, String object) {
		addStatement(core.createURI(subject), core.createURI(predicate), core.createURI(object));
		return this;
	}

	public StatementProviderResult addResourceStatement(String subject, java.net.URI predicate, String object) {
		addStatement(core.createURI(subject), core.createURI(predicate), core.createURI(object));
		return this;
	}

}
