/*
 * Copyright (C) 2026 denkbares GmbH, Germany
 *
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option) any
 * later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */

package org.apache.wiki.providers.git;

import java.util.Collection;
import java.util.Properties;

import org.apache.wiki.api.core.Engine;
import org.apache.wiki.auth.NoSuchPrincipalException;
import org.apache.wiki.auth.UserManager;
import org.apache.wiki.auth.user.UserProfile;
import org.apache.wiki.cache.CachingManager;
import org.apache.wiki.event.GitVersioningWikiEvent;
import org.apache.wiki.event.WikiEventManager;
import org.apache.wiki.providers.commentStrategy.GitCommentStrategy;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.denkbares.events.EventManager;
import de.knowwe.event.GitCommitEvent;
import de.uniwue.d3web.gitConnector.CommitUserData;

/**
 * The engine facing side of a git backed page or attachment provider, holding the wiki {@link Engine} and the
 * configured {@link GitCommentStrategy} so a provider resolves both once at initialization.
 * <p>
 * Everything a git provider needs from the wiki beyond plain file and git access goes through here, the commit author
 * from the user database, the two commit notifications and the page cache eviction. The per repository components
 * around it ({@link GitPageHistory}, {@link GitRepoIndex}, {@link GitCommitBatchRegistry}) stay engine free.
 * <p>
 * A page provider and its sibling attachment provider are meant to share one instance, so the attachment provider does
 * not have to reach back into the page provider for author resolution or the comment strategy.
 */
public final class WikiGitContext {

	private static final Logger LOGGER = LoggerFactory.getLogger(WikiGitContext.class);

	private final Engine engine;
	private final GitCommentStrategy commentStrategy;

	public WikiGitContext(Engine engine, Properties properties) {
		this(engine, GitCommentStrategy.fromProperties(properties));
	}

	public WikiGitContext(Engine engine, GitCommentStrategy commentStrategy) {
		this.engine = engine;
		this.commentStrategy = commentStrategy;
	}

	public Engine engine() {
		return engine;
	}

	public GitCommentStrategy commentStrategy() {
		return commentStrategy;
	}

	/**
	 * Resolves the commit author, email and message for a wiki user name from the wiki user database.
	 */
	public CommitUserData userData(String author, String comment) {
		UserProfile userProfile = getUserProfile(author);
		String userName = author;
		String email = "";
		if (userProfile != null) {
			userName = userProfile.getFullname();
			email = userProfile.getEmail();
		}
		// SSO often makes the author an email address, derive a readable name and use the address as the email
		if (userName != null && userName.contains("@")) {
			String local = author.split("@")[0];
			if (local.contains(".")) {
				userName = capitalize(local.split("\\.")[0]) + " " + capitalize(local.split("\\.")[1]);
			}
			else {
				userName = local;
			}
			email = author;
		}
		return new CommitUserData(userName, email, comment);
	}

	@Nullable
	private UserProfile getUserProfile(String user) {
		try {
			return engine.getManager(UserManager.class).getUserDatabase().findByFullName(user);
		}
		catch (NoSuchPrincipalException e) {
			LOGGER.debug("No user profile for '{}'; using raw author for the commit.", user);
			return null;
		}
	}

	private static String capitalize(String name) {
		if (name == null || name.isEmpty()) {
			return name;
		}
		return name.substring(0, 1).toUpperCase() + name.substring(1).toLowerCase();
	}

	/**
	 * Fires both commit notifications, the JSPWiki bus {@link GitVersioningWikiEvent} for wiki internal consumers and
	 * the denkbares bus {@link GitCommitEvent} carrying the repository, so cross plugin consumers (for example the
	 * async push listener) can route without any further lookup.
	 *
	 * @param source    the provider firing the event, used as the JSPWiki event source
	 * @param eventType one of the {@link GitVersioningWikiEvent} types
	 * @param repoPath  working tree path of the repository the commit was made in
	 */
	public void fireCommitted(Object source, int eventType, String author, Collection<String> pages,
							  String commitHash, String repoPath) {
		WikiEventManager.fireEvent(source, new GitVersioningWikiEvent(source, eventType, author, pages, commitHash));
		EventManager.getInstance()
				.fireEvent(new GitCommitEvent(repoPath, commitHash, pages, author, GitCommitEvent.Origin.LOCAL_SAVE));
	}

	/**
	 * Evicts the given pages from the JSPWiki page caches, so reads after a batch commit or rollback see the current
	 * on disk state. Evicts directly via the {@link CachingManager} rather than through {@code PageManager}, whose
	 * delegate call would reach the git providers' throwing {@code deleteVersion} and skip the history cache eviction.
	 */
	public void evictPages(Collection<String> pageNames) {
		CachingManager cachingManager = engine.getManager(CachingManager.class);
		if (cachingManager == null) {
			return;
		}
		for (String pageName : pageNames) {
			cachingManager.remove(CachingManager.CACHE_PAGES, pageName);
			cachingManager.remove(CachingManager.CACHE_PAGES_TEXT, pageName);
			cachingManager.remove(CachingManager.CACHE_PAGES_HISTORY, pageName);
		}
	}
}
