/*
 * Copyright (C) 2026 denkbares GmbH. All rights reserved.
 */

package de.knowwe.jspwiki.auth;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.wiki.api.core.Engine;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.denkbares.strings.Strings;

/**
 * Who the wiki's users are, as their identity provider stated it when they signed in: full name and mail address by
 * every name the wiki knows the user under. A wiki authenticating against an identity provider keeps no user database,
 * so this is the only place a commit author can be resolved from once the request that signed the user in is gone,
 * which is where the git providers are when they attribute a save.
 * <p>
 * A login module that learns a user's identity records it here; the connector's author resolution and the user's
 * mail address read it. A wiki with a user database is unaffected, the database is consulted first and this is only
 * asked where it has no answer.
 */
public final class AuthenticatedIdentities {

	private static final String ATTRIBUTE = AuthenticatedIdentities.class.getName();

	private AuthenticatedIdentities() {
	}

	/**
	 * One user as the identity provider stated them.
	 *
	 * @param loginName the name the user signs in with
	 * @param fullName  the user's readable name, the one a commit is attributed to
	 * @param email     the user's mail address, or null where the provider stated none
	 */
	public record Identity(@NotNull String loginName, @NotNull String fullName, @Nullable String email) {

		/**
		 * The full name without whitespace, which is what JSPWiki uses as the user's wiki name.
		 */
		public @NotNull String wikiName() {
			return fullName.replaceAll("\\s", "");
		}
	}

	/**
	 * Records who a user is, under every name the wiki may later refer to them by.
	 */
	public static void remember(@NotNull Engine engine, @NotNull Identity identity) {
		Map<String, Identity> identities = identities(engine);
		identities.put(identity.loginName(), identity);
		identities.put(identity.fullName(), identity);
		identities.put(identity.wikiName(), identity);
	}

	/**
	 * The identity of the user known under the given login, full or wiki name, or null if no such user signed in
	 * against an identity provider during the life of this wiki.
	 */
	public static @Nullable Identity of(@NotNull Engine engine, @Nullable String name) {
		if (Strings.isBlank(name)) {
			return null;
		}
		return identities(engine).get(name);
	}

	private static Map<String, Identity> identities(Engine engine) {
		synchronized (engine) {
			Map<String, Identity> identities = engine.getAttribute(ATTRIBUTE);
			if (identities == null) {
				identities = new ConcurrentHashMap<>();
				engine.setAttribute(ATTRIBUTE, identities);
			}
			return identities;
		}
	}
}
