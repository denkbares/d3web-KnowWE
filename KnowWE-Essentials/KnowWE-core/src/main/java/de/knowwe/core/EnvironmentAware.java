/*
 * Copyright (C) 2026 denkbares GmbH. All rights reserved.
 */

package de.knowwe.core;

import org.jetbrains.annotations.NotNull;

/**
 * Plugged objects that are created per {@link Environment} (like the plugged event listeners) may implement this
 * interface to receive the environment they belong to, right after their creation and before they are put to use.
 * Store the environment and resolve everything (article manager, compilers, wiki connector) from it instead of
 * looking up a "current" environment.
 *
 * @author Albrecht Striffler (denkbares GmbH) + Claude for env-instance
 */
public interface EnvironmentAware {

	/**
	 * Attaches this object to the environment it was created for. Called exactly once, before the object is used.
	 */
	void attach(@NotNull Environment environment);
}
