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

import java.util.List;
import java.util.stream.Collectors;

import com.denkbares.strings.Strings;
import de.knowwe.core.append.PageAppendHandler;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.rightpanel.RightPanel;
import de.knowwe.core.rightpanel.RightPanelTab;
import de.knowwe.core.user.UserContext;
import de.knowwe.plugin.Plugins;

/**
 * Scaffolds the right panel DOM inline during page render. Runs as a post {@link PageAppendHandler}, so its output is
 * appended to the viewed article's HTML (the framework already skips support / non-viewed articles, leaving exactly
 * one scaffold per page view). It queries {@link Plugins#getRightPanelTabs(Environment)}, drops tabs whose
 * {@link de.knowwe.core.rightpanel.RightPanelTabProvider#isAvailable(UserContext) isAvailable} is false and, if any
 * remain, emits the panel structure. When no tab is available it emits nothing at all, so no panel and no toggle
 * appear.
 * <p>
 * The scaffold starts {@code hidden} and sits inside the page content body; the RightPanel JS relocates it to the
 * stable panel parent and un-hides it on dom-ready.
 */
public class RightPanelAppendHandler implements PageAppendHandler {

	@Override
	public void append(Article article, UserContext user, RenderResult result) {
		// no article on preview renders; nothing to scaffold
		if (article == null) return;

		List<RightPanelTab> available = RightPanel.getAvailableTabs(user);

		// zero tabs -> no panel at all (and the JS adds no toggle)
		if (available.isEmpty()) return;

		// the JS keeps the panel collapsed if the most recently activated tab is contextual and missing on this page,
		// and notifies if it shows or hides the panel unlike the server assumed when rendering the page
		String contextual = Plugins.getRightPanelTabs(user.getEnvironment()).stream()
				.filter(tab -> tab.provider().isContextual())
				.map(RightPanelTab::id)
				.collect(Collectors.joining(" "));
		// a missing tab is replaced by an available tab of its group, so the JS needs the groups of all tabs
		String groups = Plugins.getRightPanelTabs(user.getEnvironment()).stream()
				.filter(tab -> tab.provider().getGroup() != null)
				.map(tab -> tab.id() + "=" + tab.provider().getGroup())
				.collect(Collectors.joining(" "));
		result.appendHtml("<div id=\"rightPanel\" hidden");
		result.appendHtml(" data-contextual=\"" + Strings.encodeHtml(contextual) + "\"");
		result.appendHtml(" data-groups=\"" + Strings.encodeHtml(groups) + "\"");
		result.appendHtml(" data-open=\"" + RightPanel.isOpen(user, available) + "\"");
		result.appendHtml(">");

		// tab list header, hides itself in CSS when there is only one tab
		result.appendHtml("<div class=\"right-panel-tablist\">");
		for (int i = 0; i < available.size(); i++) {
			RightPanelTab tab = available.get(i);
			boolean active = (i == 0);
			result.appendHtml("<button data-tab=\"" + Strings.encodeHtml(tab.id()) + "\"");
			if (active) result.appendHtml(" data-active");
			if (tab.provider().isLazy()) result.appendHtml(" data-lazy");
			String description = tab.provider().getDescription(user);
			if (!Strings.isBlank(description)) {
				result.appendHtml(" title=\"" + Strings.encodeHtml(description) + "\"");
			}
			result.appendHtml(">");
			tab.provider().renderIcon(user, result);
			result.appendHtmlElement("span", tab.provider().getTitle(user), "class", "right-panel-tab-label");
			result.appendHtml("</button>");
		}
		result.appendHtml("</div>");

		// one body container per tab, the active one is visible, the rest are hidden
		for (int i = 0; i < available.size(); i++) {
			RightPanelTab tab = available.get(i);
			boolean active = (i == 0);
			result.appendHtml("<div class=\"right-panel-tab\" data-tab=\"" + Strings.encodeHtml(tab.id()) + "\"");
			// inactive tabs are not rendered again on updates, but when they are shown (see KNOWWE.core.rerendercontent)
			if (!active) result.appendHtml(" hidden data-defer-rerender");
			result.appendHtml(">");
			// non-lazy bodies ship inline, lazy bodies are fetched on first activation
			if (!tab.provider().isLazy()) {
				tab.provider().renderContent(user, result);
			}
			result.appendHtml("</div>");
		}

		result.appendHtml("</div>");
	}
}
