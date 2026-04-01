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

package de.knowwe.jspwiki;

import java.util.Map;
import java.util.Objects;

import javax.servlet.http.HttpServletRequest;

import org.apache.wiki.util.CsrfProtectionAllowListChecker;

import de.knowwe.core.action.Action;
import de.knowwe.plugin.Plugins;

/**
 * Allowlist checker for KnowWE actions. A request is only allowlisted if it exclusively addresses the configured
 * action via the single request parameter {@code action=<actionName>}. This should only be done for action addressed
 * via POST requests that do NOT change anything in the wiki.
 */
public final class ActionAllowListChecker implements CsrfProtectionAllowListChecker {

	private final String actionName;

	public ActionAllowListChecker(Class<? extends Action> clazz) {
		this(Plugins.getAction(clazz));
	}

	public ActionAllowListChecker(String actionName) {
		this.actionName = actionName;
	}

	/**
	 * Returns whether the request addresses exactly the configured action, either via the single request parameter
	 * {@code action=<actionName>} or via a path ending in {@code /action/<actionName>} or
	 * {@code /_action/<actionName>}.
	 *
	 * @param request the current HTTP request
	 * @return {@code true} if the request is allow-listed, otherwise {@code false}
	 */
	@Override
	public boolean isAllowListed(HttpServletRequest request) {
		if (request == null) return false;

		Map<String, String[]> parameterMap = request.getParameterMap();
		if (parameterMap.size() == 1) {
			String[] actionValues = parameterMap.get("action");
			return actionValues != null
					&& actionValues.length == 1
					&& Objects.equals(actionName, actionValues[0]);
		}

		if (!parameterMap.isEmpty()) return false;

		String requestUri = request.getRequestURI();
		return requestUri != null
				&& (requestUri.endsWith("/action/" + actionName)
				|| requestUri.endsWith("/_action/" + actionName));
	}

	@Override
	public boolean equals(Object object) {
		if (this == object) return true;
		if (!(object instanceof ActionAllowListChecker that)) return false;
		return Objects.equals(actionName, that.actionName);
	}

	@Override
	public int hashCode() {
		return Objects.hash(actionName);
	}
}
