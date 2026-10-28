/*
 * Copyright (C) 2026 denkbares GmbH, Germany
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package de.knowwe.core.change;

import java.io.Serial;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Signals that an {@link ArticleChangeSet} was rejected or could not be applied.
 */
public class ArticleChangeException extends Exception {

	@Serial
	private static final long serialVersionUID = 1L;

	/**
	 * Why the change set was not applied.
	 */
	public enum Reason {
		/**
		 * the user lacks the permission to change an article
		 */
		FORBIDDEN,
		/**
		 * an article is locked by another user or another unit of work
		 */
		LOCKED,
		/**
		 * an article does not have the expected version or existence
		 */
		CONFLICT,
		/**
		 * writing or committing the changes failed
		 */
		FAILED
	}

	@NotNull
	private final Reason reason;
	@Nullable
	private final String title;

	public ArticleChangeException(@NotNull Reason reason, @Nullable String title, @NotNull String message) {
		this(reason, title, message, null);
	}

	public ArticleChangeException(@NotNull Reason reason, @Nullable String title, @NotNull String message, @Nullable Throwable cause) {
		super(message, cause);
		this.reason = reason;
		this.title = title;
	}

	@NotNull
	public Reason reason() {
		return reason;
	}

	/**
	 * The article that caused the failure, null if it does not concern a single article.
	 */
	@Nullable
	public String title() {
		return title;
	}
}
