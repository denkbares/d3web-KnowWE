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

/**
 * A single right panel tab: a {@link RightPanelTabProvider} paired with its declared metadata (id and sort priority).
 * The id and priority are the extension {@code id} attribute / {@code priority} parameter (for
 * {@code plugin.xml}-declared tabs) or the registration arguments (for programmatically registered tabs). The id is a
 * stable, space-free identifier, unique across right-panel tabs, used as the DOM {@code data-tab} key, the client
 * storage key and the lazy-fetch parameter.
 *
 * @param id       the stable, unique tab id
 * @param priority the sort priority of the tab (ascending; KnowWE convention: lower number == earlier/leftmost)
 * @param provider the provider rendering the tab
 */
public record RightPanelTab(String id, double priority, RightPanelTabProvider provider) {
}
