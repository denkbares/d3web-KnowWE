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
package de.knowwe.core.action;

import java.io.IOException;

import org.json.JSONObject;

import com.denkbares.strings.Strings;
import de.knowwe.core.Environment;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.utils.KnowWEUtils;
import de.knowwe.core.wikiConnector.WikiConnector;

/**
 * Generic action to acquire or release the wiki page edit lock from the client — e.g. when a custom in-page editor
 * (dialog) is opened or closed. JSPWiki-style: the lock is a hint, not a hard block.
 * <ul>
 *     <li>Open ({@code release} absent/false): locks the page for the current user (unless someone else already holds
 *     the lock) and returns JSON {@code {"lockedByOther": boolean, "lockingUser": string?}} so the client can show a
 *     "currently edited by X" hint.</li>
 *     <li>Release ({@code release=true}): frees the current user's own lock.</li>
 * </ul>
 * The target page is the {@code title} parameter if given, otherwise the current page ({@link #getTitle}).
 *
 * @author Albrecht Striffler (denkbares GmbH)
 */
public class PageLockAction extends AbstractAction {

	public static final String PARAM_TITLE = "title";
	public static final String PARAM_RELEASE = "release";

	@Override
	public void execute(UserActionContext context) throws IOException {
		String title = context.getParameter(PARAM_TITLE);
		if (Strings.isBlank(title)) title = context.getTitle();
		if (Strings.isBlank(title)) {
			fail(context, 400, "Missing page title");
		}
		Article article = context.getArticleManager().getArticle(title);
		if (article == null) {
			fail(context, 404, "Page not found: " + title);
		}
		if (!KnowWEUtils.canWrite(article, context)) {
			fail(context, 403, "Not authorized to edit page: " + title);
		}

		String user = context.getUserName();
		WikiConnector wikiConnector = Environment.getInstance().getWikiConnector();
		JSONObject response = new JSONObject();

		if (Boolean.parseBoolean(context.getParameter(PARAM_RELEASE))) {
			wikiConnector.unlockArticle(title, user);   // releases this user's own lock
		}
		else {
			String lockingUser = wikiConnector.getLockingUser(title);   // determine before acquiring our own lock
			boolean lockedByOther = lockingUser != null && !lockingUser.equals(user);
			if (!wikiConnector.isArticleLocked(title)) {
				wikiConnector.lockArticle(title, user);
			}
			response.put("lockedByOther", lockedByOther);
			if (lockedByOther) response.put("lockingUser", lockingUser);
		}

		context.setContentType(JSON);
		response.write(context.getWriter());
	}
}
