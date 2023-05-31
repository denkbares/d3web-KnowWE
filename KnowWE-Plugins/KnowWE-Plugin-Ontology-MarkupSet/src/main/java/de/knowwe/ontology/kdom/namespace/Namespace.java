/*
 * Copyright (C) 2018 denkbares GmbH. All rights reserved.
 */

package de.knowwe.ontology.kdom.namespace;

import org.jetbrains.annotations.NotNull;

/**
 * Simple data structure holding the abbreviation and the uri of a namespace
 *
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 04.10.18
 */
public class Namespace {

	private final String abbreviation;
	private final String uri;

	public Namespace(@NotNull String abbreviation, @NotNull String uri) {
		this.abbreviation = abbreviation;
		this.uri = uri;
	}

	@NotNull
	public String getAbbreviation() {
		return abbreviation;
	}

	@NotNull
	public String getUri() {
		return uri;
	}
}
