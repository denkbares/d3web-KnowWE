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

import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A tool that can be executed for several targets at once. If tools of the same {@link ToolTarget#group()} are merged
 * into one {@link TargetedTool} and the default tool of the group is a combinable tool, clicking the merged tool
 * executes the combined action for all targets, the dropdown of the targets still executes the tool of a single
 * target. E.g. "Show Info" shows one dialog with all the elements of the tool menu.
 *
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 16.11.2026
 */
public interface CombinableTool extends Tool {

	/**
	 * Returns the action executing the tool for all the given tools at once, of the same action type as this tool.
	 *
	 * @param targetTools the tools of all targets of the group, this one included, in the order of the dropdown
	 * @return the combined action, or null if the tools cannot be combined (then the default tool is executed)
	 */
	@Nullable
	String getCombinedAction(@NotNull List<Tool> targetTools);

	/**
	 * Returns the description of the combined tool, see {@link #getCombinedAction(List)}.
	 */
	default String getCombinedDescription(@NotNull List<Tool> targetTools) {
		return getDescription();
	}
}
