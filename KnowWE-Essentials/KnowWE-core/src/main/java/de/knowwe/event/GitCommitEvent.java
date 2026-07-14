/*
 * Copyright (C) 2026 denkbares GmbH. All rights reserved.
 */

package de.knowwe.event;

import java.util.Collection;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.denkbares.events.Event;

/**
 * Signals that a commit was created in one of the wiki's git repositories. Fired once per repository commit by the
 * git-backed page and attachment providers, carrying everything a consumer needs to react without further lookups,
 * most notably which repository was committed to (multi-wiki setups hold one repository per sub-wiki folder).
 * <p>
 * Unlike {@link ArticleUpdateEvent}, which signals a content-level page change, this event means exactly "a local
 * commit exists now". Consumers that mirror commits to a remote (e.g. an async push listener) should listen to this
 * event and skip {@link Origin#PULL}.
 */
public record GitCommitEvent(
		String repoPath,
		String commitHash,
		Collection<String> pages,
		String author,
		Origin origin
) implements Event {

	/**
	 * What produced the commit.
	 */
	public enum Origin {
		/**
		 * A regular local change: page or attachment save, delete, move, or a committed transaction batch.
		 */
		LOCAL_SAVE,
		/**
		 * A commit that arrived by pulling from the remote. Never needs to be pushed back.
		 */
		PULL,
		/**
		 * A sweep-up reconciliation commit that self-heals a dirty working tree (e.g. at provider startup).
		 */
		RECONCILIATION
	}

	/**
	 * @param repoPath   absolute path of the repository's working tree (the sub-wiki folder)
	 * @param commitHash hash of the created commit
	 * @param pages      global (prefixed) page names contained in the commit, may be empty for reconciliation commits
	 * @param author     author of the commit, may be null
	 * @param origin     what produced the commit
	 */
	public GitCommitEvent(@NotNull String repoPath, @NotNull String commitHash, @NotNull Collection<String> pages,
	                      @Nullable String author, @NotNull Origin origin) {
		this.repoPath = repoPath;
		this.commitHash = commitHash;
		this.pages = pages;
		this.author = author;
		this.origin = origin;
	}

	@Override
	@NotNull
	public String repoPath() {
		return repoPath;
	}

	@Override
	@NotNull
	public String commitHash() {
		return commitHash;
	}

	@Override
	@NotNull
	public Collection<String> pages() {
		return pages;
	}

	@Override
	@Nullable
	public String author() {
		return author;
	}

	@Override
	@NotNull
	public Origin origin() {
		return origin;
	}
}
