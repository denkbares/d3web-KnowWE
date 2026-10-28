/*
 * Copyright (C) 2026 denkbares GmbH, Germany
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package de.knowwe.core.change;

import org.jetbrains.annotations.NotNull;

import de.knowwe.core.Environment;

/**
 * Vetoes an {@link ArticleChangeSet} before it is applied. Plugins contribute guards through the extension point
 * {@code ArticleChangeGuard} to enforce rules of their own, for example that an article may only be changed within the
 * unit of work that locked it.
 * <p>
 * A guard is consulted once per change set, before anything is written, and for every way a set is applied. It must
 * not change the wiki and should be fast, since it delays the change. To reject the set, the guard throws an
 * {@link ArticleChangeException} whose message explains the rejection to the user.
 */
@FunctionalInterface
public interface ArticleChangeGuard {

	/**
	 * Checks whether the given author may apply the given changes.
	 *
	 * @param environment the wiki the changes are applied to
	 * @param author      the name of the user the changes are attributed to
	 * @param changes     the changes to apply, never empty
	 * @throws ArticleChangeException if the changes must not be applied
	 */
	void check(@NotNull Environment environment, @NotNull String author, @NotNull ArticleChangeSet changes) throws ArticleChangeException;
}
