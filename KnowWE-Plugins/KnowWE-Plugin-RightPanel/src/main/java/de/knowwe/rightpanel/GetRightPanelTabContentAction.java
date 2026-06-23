/*
 * Copyright (C) 2026 denkbares GmbH. All rights reserved.
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
package de.knowwe.rightpanel;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import de.knowwe.core.action.AbstractAction;
import de.knowwe.core.action.UserActionContext;
import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.rightpanel.RightPanelTab;
import de.knowwe.plugin.Plugins;

/**
 * Renders the body of a single right-panel tab on demand, identified by the {@code tab} request parameter. Used by the
 * client to fetch a lazy tab's body on its first activation (a non-lazy tab's body already ships inline in the
 * {@link RightPanelAppendHandler} scaffold, so it never reaches this action).
 * <p>
 * Availability is re-checked here, mirroring the scaffold filter, so a tab that is not available on the current page
 * yields an empty body even if a stale client requests it.
 */
public class GetRightPanelTabContentAction extends AbstractAction {

	@Override
	public void execute(UserActionContext context) throws IOException {
		String tabId = context.getParameter("tab");
		if (tabId == null) {
			context.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing 'tab' parameter.");
			return;
		}

		RightPanelTab tab = Plugins.getRightPanelTabs().stream()
				.filter(t -> t.id().equals(tabId))
				.findFirst().orElse(null);

		// unknown tab, or not available on the current page -> empty body
		if (tab == null || !tab.provider().isAvailable(context)) return;

		RenderResult result = new RenderResult(context);
		tab.provider().renderContent(context, result);
		context.setContentType(HTML);
		context.getWriter().write(result.toString());
	}
}
