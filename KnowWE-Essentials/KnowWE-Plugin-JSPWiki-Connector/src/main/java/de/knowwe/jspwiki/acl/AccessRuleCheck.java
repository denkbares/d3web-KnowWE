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

package de.knowwe.jspwiki.acl;

import java.security.Principal;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.regex.Matcher;

import org.apache.wiki.auth.AuthenticationManager;
import org.apache.wiki.auth.AuthorizationManager;
import org.apache.wiki.auth.GroupPrincipal;
import org.apache.wiki.auth.NoSuchPrincipalException;
import org.apache.wiki.auth.acl.DefaultAclManager;
import org.apache.wiki.auth.acl.UnresolvedPrincipal;
import org.apache.wiki.auth.user.UserDatabase;
import org.apache.wiki.auth.user.UserProfile;
import org.jetbrains.annotations.Nullable;

/**
 * Finds the entries of a page ACL ({@code [{ALLOW edit A, B}]}) that do not grant what they appear to grant.
 * <p>
 * Each entry is resolved exactly as JSPWiki does when it checks permissions
 * ({@link AuthorizationManager#resolvePrincipal(String)}: built-in roles, container roles, groups, then users by full,
 * wiki and login name), so a warning only appears where the ACL really does not or not unambiguously take effect. Since
 * JSPWiki 3 (JSPWIKI-130) a session no longer carries the user's full name, so an entry naming a user by full name
 * grants nothing, unless {@code jspwiki.security.useOldPageAccessControlLogic} is set.
 *
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 28.10.2026
 */
public class AccessRuleCheck {

	/**
	 * A problem of a single ACL entry. The arguments are inserted into the localized text of the message key.
	 */
	public record Problem(String key, Object... arguments) {

		public String verbalize(ResourceBundle bundle) {
			return MessageFormat.format(bundle.getString(key), arguments);
		}
	}

	static final String FULL_NAME = "KnowWE.acl.fullName";
	static final String AMBIGUOUS_USERS = "KnowWE.acl.ambiguousUsers";
	static final String ROLE_OR_GROUP_HIDES_USERS = "KnowWE.acl.roleOrGroupHidesUsers";
	static final String UNKNOWN = "KnowWE.acl.unknown";

	private final AuthorizationManager authorizationManager;
	private final UserDatabase userDatabase;
	private final ResourceBundle bundle;

	public AccessRuleCheck(AuthorizationManager authorizationManager, UserDatabase userDatabase, ResourceBundle bundle) {
		this.authorizationManager = authorizationManager;
		this.userDatabase = userDatabase;
		this.bundle = bundle;
	}

	/**
	 * Checks all entries of the ACL markup, e.g. {@code [{ALLOW edit Max Mustermann, Admin}]}.
	 *
	 * @return the problems of the entries, empty if all of them take effect as written
	 */
	public List<Problem> checkAccessRule(String accessRule) {
		Matcher matcher = DefaultAclManager.ACL_PATTERN.matcher(accessRule);
		if (!matcher.find()) return List.of();
		List<Problem> problems = new ArrayList<>();
		// split like DefaultAclManager#parseAcl
		for (String entry : matcher.group(2).split(",")) {
			String name = entry.trim();
			if (name.isEmpty()) continue;
			Problem problem = checkEntry(name);
			if (problem != null) problems.add(problem);
		}
		return problems;
	}

	@Nullable
	Problem checkEntry(String name) {
		Principal principal = authorizationManager.resolvePrincipal(name);
		Map<UserProfile, String> users = usersByLoginOrWikiName(name);

		if (principal instanceof UnresolvedPrincipal) {
			// JSPWiki looks users up by full name first: if that matches, it stops there, even if the name is
			// also the login or wiki name of someone else
			UserProfile byFullName = findByFullName(name);
			if (byFullName != null && !isEmpty(byFullName.getWikiName())) {
				return new Problem(FULL_NAME, name, byFullName.getWikiName());
			}
			return new Problem(UNKNOWN, name);
		}
		if (principal instanceof GroupPrincipal || AuthenticationManager.isRolePrincipal(principal)) {
			// roles and groups are resolved before users, so users of the same name are not meant
			if (users.isEmpty()) return null;
			String roleOrGroup = MessageFormat.format(bundle.getString(principal instanceof GroupPrincipal
					? "KnowWE.acl.group" : "KnowWE.acl.role"), name);
			return new Problem(ROLE_OR_GROUP_HIDES_USERS, name, roleOrGroup, describe(users));
		}
		// a user principal: JSPWiki compares it by name with the login and wiki names of every session
		if (users.size() > 1) {
			return new Problem(AMBIGUOUS_USERS, name, describe(users));
		}
		return null;
	}

	/** The users having the name as login or wiki name, with the bundle key telling which of both. */
	private Map<UserProfile, String> usersByLoginOrWikiName(String name) {
		Map<UserProfile, String> users = new LinkedHashMap<>();
		try {
			users.put(userDatabase.findByLoginName(name), "KnowWE.acl.loginName");
		}
		catch (NoSuchPrincipalException ignored) {
			// no such login name
		}
		try {
			UserProfile profile = userDatabase.findByWikiName(name);
			// the same user may have the name as login and wiki name, e.g. "Alice"
			users.putIfAbsent(profile, "KnowWE.acl.wikiName");
		}
		catch (NoSuchPrincipalException ignored) {
			// no such wiki name
		}
		return users;
	}

	private String describe(Map<UserProfile, String> users) {
		List<String> descriptions = new ArrayList<>();
		users.forEach((profile, nameKind) -> descriptions.add(MessageFormat.format(bundle.getString("KnowWE.acl.user"),
				isEmpty(profile.getFullname()) ? profile.getLoginName() : profile.getFullname(),
				bundle.getString(nameKind))));
		return String.join(", ", descriptions);
	}

	@Nullable
	private UserProfile findByFullName(String name) {
		try {
			return userDatabase.findByFullName(name);
		}
		catch (NoSuchPrincipalException e) {
			return null;
		}
	}

	private static boolean isEmpty(String s) {
		return s == null || s.isEmpty();
	}
}
