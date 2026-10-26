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

import org.jetbrains.annotations.NotNull;
import org.junit.Test;

import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.user.UserContext;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class RightPanelTest {

	private static final RightPanelTab WATCHES = tab("watches", false);
	private static final RightPanelTab CUSTOM = tab("custom", false);
	private static final RightPanelTab DEBUGGER = tab("debugger", true);
	private static final List<RightPanelTab> ALL = List.of(WATCHES, CUSTOM, DEBUGGER);

	@Test
	public void hiddenIfNotChosen() {
		assertFalse(RightPanel.isOpen(false, null, ALL, ALL));
		assertFalse(RightPanel.isOpen(false, "debugger", ALL, ALL));
	}

	@Test
	public void hiddenWithoutTabs() {
		assertFalse(RightPanel.isOpen(true, null, ALL, List.of()));
	}

	@Test
	public void openWithoutRecentTab() {
		assertTrue(RightPanel.isOpen(true, null, ALL, List.of(CUSTOM)));
		assertTrue(RightPanel.isOpen(true, "", ALL, List.of(CUSTOM)));
	}

	@Test
	public void openIfRecentTabAvailable() {
		assertTrue(RightPanel.isOpen(true, "debugger", ALL, ALL));
		assertTrue(RightPanel.isOpen(true, "watches", ALL, List.of(WATCHES, CUSTOM)));
	}

	@Test
	public void openIfMissingRecentTabIsNotContextual() {
		assertTrue(RightPanel.isOpen(true, "watches", ALL, List.of(CUSTOM)));
		// a tab of a plugin that is no longer installed
		assertTrue(RightPanel.isOpen(true, "removed", ALL, List.of(CUSTOM)));
	}

	@Test
	public void collapsedIfMissingRecentTabIsContextual() {
		assertFalse(RightPanel.isOpen(true, "debugger", ALL, List.of(WATCHES, CUSTOM)));
	}

	private static RightPanelTab tab(String id, boolean contextual) {
		return new RightPanelTab(id, 5, new RightPanelTabProvider() {
			@Override
			public @NotNull String getTitle(UserContext user) {
				return id;
			}

			@Override
			public boolean isContextual() {
				return contextual;
			}

			@Override
			public void renderContent(UserContext context, RenderResult result) {
			}
		});
	}
}
