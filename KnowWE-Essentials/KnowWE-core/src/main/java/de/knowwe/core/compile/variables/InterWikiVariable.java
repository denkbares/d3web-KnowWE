/*
 * Copyright (C) 2026 denkbares GmbH, Germany
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package de.knowwe.core.compile.variables;

import org.jetbrains.annotations.NotNull;

import com.denkbares.strings.Identifier;

/**
 * The term class of the variables defined with {@link InterWikiVariableMarkup}. The variables are registered as terms
 * of the global compiler, so the usual tools for terms are available at the definitions and at the places using a
 * variable, e.g. to show all usages or to open the definition.
 *
 * @author Albrecht Striffler (denkbares GmbH) + Claude
 * @created 25.11.2026
 */
public final class InterWikiVariable {

	/**
	 * First path element of the identifiers of the variables, to keep them apart from other terms of the global
	 * compiler with the same name, e.g. the title of a page.
	 */
	public static final String IDENTIFIER_PREFIX = "Wiki variable";

	private InterWikiVariable() {
	}

	/**
	 * Returns the term identifier of the variable with the given name.
	 */
	@NotNull
	public static Identifier toIdentifier(@NotNull String name) {
		return new Identifier(IDENTIFIER_PREFIX, name);
	}
}
