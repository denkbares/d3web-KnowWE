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

package de.knowwe.tools;

import org.jetbrains.annotations.NotNull;

/**
 * Describes the element a tool acts on, if the same action is offered for several elements of one tool menu, e.g.
 * "Show Info" for the unit, the socket and the pin of the path <tt>Unit.Socket.Pin</tt>. All tools of a menu with the
 * same {@link #group()} are rendered as one tool titled with the group (see {@link ToolUtils#mergeTargets(ToolSet)}):
 * clicking it executes the tool of the deepest target, the other targets are offered in a small dropdown of the tool.
 *
 * @param group     the title of the merged tool, tools with the same group are merged, e.g. "Show Info"
 * @param label     the label of the element in the dropdown, may contain html like the tool titles
 * @param depth     the depth of the element (e.g. in a path), the targets are sorted and indented by it, the deepest
 *                  (most specific) target is the default of the merged tool
 * @param secondary if the element is only related to the element the tool menu is about (e.g. a child of it), so it is
 *                  listed but not used as the default of the merged tool (unless all targets are secondary)
 * @author Albrecht Striffler (denkbares GmbH) + Claude
 * @created 16.11.2026
 */
public record ToolTarget(@NotNull String group, @NotNull String label, int depth, boolean secondary) {

	public ToolTarget(@NotNull String group, @NotNull String label, int depth) {
		this(group, label, depth, false);
	}

	/**
	 * Returns this target as secondary target, see {@link #secondary()}.
	 */
	public ToolTarget asSecondary() {
		return new ToolTarget(group, label, depth, true);
	}
}
