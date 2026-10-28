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
package de.knowwe.core.rightpanel;

import java.util.List;
import java.util.Objects;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import de.knowwe.core.user.UserContext;
import de.knowwe.core.utils.KnowWEUtils;
import de.knowwe.plugin.Plugins;

/**
 * Server side state of the right panel. The panel is shown and hidden in the browser, which mirrors the user's choice
 * into two cookies, so the server can render a page depending on whether the panel will be shown, e.g. to render
 * content either inside the page or into a tab of the panel.
 */
public final class RightPanel {

	/**
	 * Cookie holding "true" if the user chose to show the right panel (see KnowWE-Plugin-RightPanel.js).
	 */
	public static final String OPEN_COOKIE = "knowwe-rightpanel-open";

	/**
	 * Cookie holding the id of the tab the user activated most recently (see KnowWE-Plugin-RightPanel.js).
	 */
	public static final String TAB_COOKIE = "knowwe-rightpanel-tab";

	/**
	 * Name of the action rendering the content of a lazy tab (in KnowWE-Plugin-RightPanel).
	 */
	public static final String TAB_CONTENT_ACTION = "GetRightPanelTabContentAction";

	private RightPanel() {
	}

	/**
	 * Returns true if the user's request renders the content of a lazy tab. The content is new on the page, so it is
	 * rendered like on a page view, e.g. with the markers to rerender sections.
	 */
	public static boolean isTabContentRequest(UserContext user) {
		return TAB_CONTENT_ACTION.equalsIgnoreCase(user.getParameter("action"));
	}

	/**
	 * Returns the tabs available on the current page of the user, sorted like they are shown.
	 */
	@NotNull
	public static List<RightPanelTab> getAvailableTabs(UserContext user) {
		return Plugins.getRightPanelTabs(user.getEnvironment()).stream()
				.filter(tab -> tab.provider().isAvailable(user))
				.toList();
	}

	/**
	 * Returns true if the right panel is shown on the current page of the user (on the right, or on the bottom of
	 * small windows). The browser decides the same way when the page is loaded. If the cookies are missing, e.g.
	 * because the browser has not loaded a page with the right panel since the cookies were introduced, the panel is
	 * assumed to be hidden, and the browser notifies about the difference with the event "rightPanelShown".
	 */
	public static boolean isOpen(UserContext user) {
		if (!Boolean.parseBoolean(KnowWEUtils.getCookie(OPEN_COOKIE, user))) return false;
		return isOpen(user, getAvailableTabs(user));
	}

	/**
	 * Same as {@link #isOpen(UserContext)}, for the already known tabs available on the current page.
	 */
	public static boolean isOpen(UserContext user, List<RightPanelTab> available) {
		return isOpen(Boolean.parseBoolean(KnowWEUtils.getCookie(OPEN_COOKIE, user)),
				KnowWEUtils.getCookie(TAB_COOKIE, user), Plugins.getRightPanelTabs(user.getEnvironment()), available);
	}

	/**
	 * Decides whether the panel is shown, see {@link RightPanelTabProvider#isContextual()} and
	 * {@link RightPanelTabProvider#getGroup()}.
	 *
	 * @param open      whether the user chose to show the panel
	 * @param lastTab   the id of the tab the user activated most recently, or null
	 * @param tabs      all tabs
	 * @param available the tabs available on the current page
	 */
	static boolean isOpen(boolean open, @Nullable String lastTab, List<RightPanelTab> tabs, List<RightPanelTab> available) {
		if (!open || available.isEmpty()) return false;
		if (lastTab == null || lastTab.isEmpty()) return true;
		if (available.stream().anyMatch(tab -> tab.id().equals(lastTab))) return true;
		// an available tab of the same group is shown instead
		String group = tabs.stream().filter(tab -> tab.id().equals(lastTab))
				.map(tab -> tab.provider().getGroup()).filter(Objects::nonNull).findFirst().orElse(null);
		if (group != null && available.stream().anyMatch(tab -> group.equals(tab.provider().getGroup()))) return true;
		return tabs.stream().noneMatch(tab -> tab.id().equals(lastTab) && tab.provider().isContextual());
	}
}
