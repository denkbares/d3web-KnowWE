/*
 * Copyright (C) 2018 denkbares GmbH. All rights reserved.
 */

package de.knowwe.ontology.kdom.namespace;

/**
 * Simple data structure holding the abbreviation and the uri of a namespace
 *
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 04.10.18
 */
public class Namespace {

	private final String abbreviation;
	private final String uri;

	public Namespace(String abbreviation, String uri) {
		this.abbreviation = abbreviation;
		this.uri = uri;
	}

	public String getAbbreviation() {
		return abbreviation;
	}

	public String getUri() {
		return uri;
	}
}
