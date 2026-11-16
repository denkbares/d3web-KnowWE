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
import org.jetbrains.annotations.Nullable;

/**
 * Describes the element a tool acts on, if the same action is offered for several elements of one tool menu, e.g.
 * "Show Info" for the unit, the socket and the pin of the path <tt>Unit.Socket.Pin</tt>. All tools of a menu with the
 * same {@link #group()} are rendered as one tool titled with the group (see {@link ToolUtils#mergeTargets(ToolSet)}):
 * clicking it executes the tool of the deepest target, the other targets are offered in a small dropdown of the tool.
 *
 * @param group     the title of the merged tool, tools with the same group are merged, e.g. "Show Info"
 * @param label     the label of the element in the dropdown, may contain html like the tool titles
 * @param depth    the depth of the element (e.g. in a path), the targets are sorted and indented by it
 * @param priority the priority of the element as default of the merged tool: the default is the target with the
 *                 highest priority, among them the deepest (most specific) one, among them the first one; use a
 *                 negative priority for elements that are only related to the element the tool menu is about (e.g. the
 *                 elements of a path it contains), so they are listed but are the default only without other targets
 * @param context  the element the target belongs to, if the label does not name it (e.g. the label is the page to
 *                 open, and the context the term defined there), or null; it is shown behind the label in the dropdown
 *                 only if the targets of the merged tool belong to different elements
 * @author Albrecht Striffler (denkbares GmbH) + Claude
 * @created 16.11.2026
 */
public record ToolTarget(@NotNull String group, @NotNull String label, int depth, int priority, @Nullable String context) {

	public ToolTarget(@NotNull String group, @NotNull String label, int depth) {
		this(group, label, depth, 0, null);
	}

	public ToolTarget(@NotNull String group, @NotNull String label, int depth, int priority) {
		this(group, label, depth, priority, null);
	}

	/**
	 * Returns this target with the given priority, see {@link #priority()}.
	 */
	public ToolTarget withPriority(int priority) {
		return new ToolTarget(group, label, depth, priority, context);
	}

	/**
	 * Returns this target with the given context, see {@link #context()}.
	 */
	public ToolTarget withContext(@Nullable String context) {
		return new ToolTarget(group, label, depth, priority, context);
	}
}
