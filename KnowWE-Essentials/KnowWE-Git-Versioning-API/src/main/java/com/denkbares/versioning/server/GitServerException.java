/*
 * Copyright (C) 2026 denkbares GmbH. All rights reserved.
 */

package com.denkbares.versioning.server;

import org.apache.http.HttpException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A contact with the git server that did not succeed, named by the reason it did not. A caller reacting to a
 * failure reads the reason, a caller only reporting it reads the message, which carries what the server said.
 */
public class GitServerException extends HttpException {

	/**
	 * What kept a request from succeeding, as far as the server said.
	 */
	public enum Reason {
		/**
		 * An open merge request for the same source branch exists already, and it is not the one that was asked for.
		 */
		MERGE_REQUEST_EXISTS,
		/**
		 * The merge request is not in a state that can be merged, for example while a pipeline or an approval is
		 * still pending, or while it is marked as a draft.
		 */
		NOT_MERGEABLE,
		/**
		 * Source and target hold changes that contradict each other.
		 */
		CONFLICT,
		/**
		 * The branch moved on since the state the request was built on, so the same request has to be made again.
		 */
		OUT_OF_DATE,
		/**
		 * There are no credentials, they expired, or they do not carry the right for this.
		 */
		NOT_AUTHORIZED,
		/**
		 * The repository, the branch, or the merge request does not exist.
		 */
		NOT_FOUND,
		/**
		 * The server refused the request as invalid, for example a merge request of a branch onto itself.
		 */
		REJECTED,
		/**
		 * The server could not be reached or failed on its own account, so the same request may well succeed later.
		 */
		TRANSIENT,
		/**
		 * The server named nothing that is told apart here.
		 */
		UNKNOWN
	}

	private final Reason reason;
	private final int statusCode;
	private final GitServerConnector.MergeRequest mergeRequest;

	public GitServerException(@NotNull Reason reason, int statusCode, String message) {
		this(reason, statusCode, message, null, null);
	}

	public GitServerException(@NotNull Reason reason, int statusCode, String message, @Nullable Throwable cause) {
		this(reason, statusCode, message, null, cause);
	}

	/**
	 * @param mergeRequest the merge request the failure is about, where the server named one
	 */
	public GitServerException(@NotNull Reason reason, int statusCode, String message,
							  @Nullable GitServerConnector.MergeRequest mergeRequest, @Nullable Throwable cause) {
		super(message, cause);
		this.reason = reason;
		this.statusCode = statusCode;
		this.mergeRequest = mergeRequest;
	}

	public @NotNull Reason reason() {
		return reason;
	}

	/**
	 * The HTTP status the server answered with, or 0 where it never answered.
	 */
	public int statusCode() {
		return statusCode;
	}

	/**
	 * The merge request this failure is about, or null where there is none to point at.
	 */
	public @Nullable GitServerConnector.MergeRequest mergeRequest() {
		return mergeRequest;
	}
}
