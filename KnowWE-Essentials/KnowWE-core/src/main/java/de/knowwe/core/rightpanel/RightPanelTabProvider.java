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

import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.user.UserContext;
import de.knowwe.plugin.Plugins;
import de.knowwe.util.Icon;

/**
 * Contributes a single tab to the KnowWE right panel. Plugins implement this interface and register it via the
 * {@code RightPanelTab} extension point (declared in {@code plugin.xml}) or programmatically through
 * {@link Plugins#registerRightPanelTabProvider(String, RightPanelTabProvider, double)}.
 * <p>
 * One provider == one tab. A plugin that needs several tabs registers several providers. The right panel renders all
 * available providers, sorted by priority ascending, then title, then provider class as a stable final tiebreaker.
 * The id is a stable, space-free identifier, unique across right-panel tabs, used as the DOM {@code data-tab} key, the
 * client storage key and the lazy-fetch parameter (KnowWE convention: higher priority == lower number).
 */
public interface RightPanelTabProvider {

	/**
	 * Tab label shown in the header (when more than one tab is visible).
	 */
	String getTitle(UserContext user);

	/**
	 * Tab-header icon.
	 */
	Icon getIcon();

	/**
	 * Per-page availability, re-evaluated on every panel load (i.e. every page load). Return {@code false} to hide this
	 * tab for the current request/page. The context provides the page (title/article) and compiled types needed to
	 * decide; it is intentionally a {@link UserContext} so the same check works both from the server-side bootstrap
	 * (page render) and from the AJAX action.
	 */
	default boolean isAvailable(UserContext context) {
		return true;
	}

	/**
	 * Render the body only on first activation instead of upfront. A lazy tab's body is fetched separately when the tab
	 * is first activated.
	 */
	default boolean isLazy() {
		return false;
	}

	/**
	 * Renders the server-side body of this tab. A client-rendered tab writes an empty container here and fills it from
	 * a JS init hook registered for the tab's id.
	 */
	void render(UserContext context, RenderResult result);
}
