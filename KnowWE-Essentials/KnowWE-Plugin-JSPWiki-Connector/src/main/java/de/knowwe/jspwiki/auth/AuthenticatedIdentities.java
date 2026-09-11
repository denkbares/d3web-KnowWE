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
 * login name. A wiki authenticating against an identity provider keeps no user database, so this is the only place a
 * commit author can be resolved from once the request that signed the user in is gone, which is where the git
 * providers are when they attribute a save.
 * <p>
 * Keyed by the login name alone, deliberately: it is the one name the provider keeps unique, while a display name is
 * the user's own choice, and keying by it would let one user's chosen name overwrite another user's entry. The login
 * name is also what JSPWiki hands on as the author of a save, so nothing else is needed.
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
	}

	/**
	 * Records who the user with this login name is, replacing what an earlier sign-in of the same user recorded.
	 */
	public static void remember(@NotNull Engine engine, @NotNull Identity identity) {
		identities(engine).put(identity.loginName(), identity);
	}

	/**
	 * The identity of the user with the given login name, or null if no such user signed in against an identity
	 * provider during the life of this wiki.
	 */
	public static @Nullable Identity of(@NotNull Engine engine, @Nullable String loginName) {
		if (Strings.isBlank(loginName)) {
			return null;
		}
		return identities(engine).get(loginName);
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
