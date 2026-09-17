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

package de.knowwe.search.index;

import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jetbrains.annotations.NotNull;

/**
 * Reads who may view a page out of its {@code [{ALLOW …}]} entries.
 * <p>
 * Read from the raw text on purpose. {@code DefaultAclManager.getPermissions(page)} would answer the same question, but
 * on a page whose ACL is not cached it renders the <b>entire page to HTML</b> just to find the entries -- once per page,
 * during a full index of a wiki that may hold fifty thousand of them.
 * <p>
 * Only actions that imply viewing count, see {@link #GRANTS_VIEW}. JSPWiki's {@code PagePermission} derives view from
 * comment, and comment from edit and modify, and view from upload -- but <b>not</b> from delete or rename. So
 * {@code [{ALLOW delete Admins}]} says nothing about who may read the page, and treating it as a view grant would hide
 * the page from everyone else for no reason.
 *
 * @author Albrecht Striffler (denkbares GmbH) + Claude for wiki-search
 */
public class AclExtractor {

	/**
	 * The same expression {@code DefaultAclManager.ACL_PATTERN} uses: group 1 is the action, group 2 the principals,
	 * separated by commas. Written out rather than referenced so the index does not depend on a JSPWiki internal that
	 * happens to be public.
	 */
	private static final Pattern ACL = Pattern.compile(
			"\\[\\{\\s*ALLOW\\s+(comment|delete|edit|modify|rename|upload|view)\\s*(.*?)\\s*}]");

	/** Actions from which JSPWiki derives the right to view; delete and rename are deliberately not among them. */
	private static final Set<String> GRANTS_VIEW = Set.of("view", "comment", "edit", "modify", "upload");

	/**
	 * @return the principals allowed to view, or an empty set when the page says nothing about viewing -- which means
	 *         it is not restricted, not that nobody may read it
	 */
	public @NotNull Set<String> viewPrincipals(@NotNull String pageText) {
		Set<String> principals = new LinkedHashSet<>();
		Matcher matcher = ACL.matcher(pageText);
		while (matcher.find()) {
			if (!GRANTS_VIEW.contains(matcher.group(1).toLowerCase(Locale.ROOT))) continue;
			for (String principal : matcher.group(2).split(",")) {
				String name = principal.trim();
				if (!name.isEmpty()) principals.add(name);
			}
		}
		return principals;
	}
}
