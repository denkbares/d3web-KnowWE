/*
 * Copyright (C) 2026 denkbares GmbH. All rights reserved.
 */

package de.knowwe.core.action;

import java.io.IOException;

import org.json.JSONObject;

/**
 * Returns the anti-CSRF token of the current session, so pages can continue to post after the session changed, e.g.
 * by a login in another tab. Browsers only let same-origin scripts read the response.
 *
 * @author Konstantin Herud (denkbares GmbH)
 * @created 2026-10-27
 */
public class GetAntiCsrfTokenAction extends AbstractAction {

	/**
	 * Anonymous sessions need their token as well.
	 */
	@Override
	public Access requiredAccess() {
		return Access.NONE;
	}

	@Override
	public void execute(UserActionContext context) throws IOException {
		String token = context.getEnvironment().getWikiConnector().getAntiCsrfToken(context);
		context.setContentType(Action.JSON);
		context.getResponse().setHeader("Cache-Control", "no-store");
		context.getWriter().write(new JSONObject().put("token", token).toString());
	}
}
