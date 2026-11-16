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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.jetbrains.annotations.NotNull;

import de.knowwe.util.Icon;

/**
 * A tool merged from several tools with the same {@link ToolTarget#group()}, one per target element. The tool is titled
 * with the group and acts like the tool of its default target (the deepest one), the other targets are offered in a
 * dropdown of the tool. Created by {@link ToolUtils#mergeTargets(ToolSet)}.
 *
 * @author Albrecht Striffler (denkbares GmbH) + Claude
 * @created 16.11.2026
 */
public class TargetedTool implements Tool {

	private final String title;
	private final Tool defaultTool;
	private final List<Tool> targetTools;

	/**
	 * @param title       the title of the tool, usually the target group
	 * @param defaultTool the tool executed when clicking this tool, must be one of the target tools
	 * @param targetTools the tools of all targets, sorted by the depth of their targets
	 */
	public TargetedTool(@NotNull String title, @NotNull Tool defaultTool, @NotNull List<Tool> targetTools) {
		this.title = title;
		this.defaultTool = defaultTool;
		this.targetTools = List.copyOf(targetTools);
	}

	@NotNull
	public Tool getDefaultTool() {
		return defaultTool;
	}

	@Override
	public @NotNull List<Tool> getTargetTools() {
		return targetTools;
	}

	@Override
	public Icon getIcon() {
		return defaultTool.getIcon();
	}

	@Override
	public String getTitle() {
		return title;
	}

	@Override
	public String getDescription() {
		return defaultTool.getDescription();
	}

	@Override
	public String getAction() {
		return defaultTool.getAction();
	}

	@Override
	public ActionType getActionType() {
		return defaultTool.getActionType();
	}

	@Override
	public String getCategory() {
		return defaultTool.getCategory();
	}

	@Override
	public String getId() {
		return defaultTool.getId();
	}

	@Override
	public Map<String, String> getAttributes() {
		return defaultTool.getAttributes();
	}

	/**
	 * Merges the tools with the same {@link ToolTarget#group()}, see {@link ToolUtils#mergeTargets(ToolSet)}.
	 */
	public static ToolSet mergeTargets(ToolSet tools) {
		Map<String, List<Tool>> groups = new LinkedHashMap<>();
		boolean hasTargets = false;
		for (Tool tool : tools) {
			ToolTarget target = tool.getTarget();
			if (target == null) continue;
			hasTargets = true;
			List<Tool> group = groups.computeIfAbsent(target.group(), k -> new ArrayList<>());
			if (group.stream().noneMatch(other -> isSameAction(tool, other))) {
				group.add(tool);
			}
		}
		if (!hasTargets) return tools;

		List<Tool> result = new ArrayList<>();
		for (Tool tool : tools) {
			ToolTarget target = tool.getTarget();
			if (target == null) {
				result.add(tool);
				continue;
			}
			List<Tool> group = groups.remove(target.group());
			if (group == null) continue; // already added with the first tool of the group
			if (group.size() == 1) {
				result.add(group.get(0));
				continue;
			}
			group.sort(Comparator.comparingInt(t -> Objects.requireNonNull(t.getTarget()).depth()));
			// the default is the first of the deepest targets, preferring the non-secondary ones
			List<Tool> candidates = group.stream().filter(t -> !Objects.requireNonNull(t.getTarget()).secondary()).toList();
			if (candidates.isEmpty()) candidates = group;
			int maxDepth = candidates.stream().mapToInt(t -> Objects.requireNonNull(t.getTarget()).depth()).max().orElseThrow();
			Tool defaultTool = candidates.stream()
					.filter(t -> Objects.requireNonNull(t.getTarget()).depth() == maxDepth)
					.findFirst().orElseThrow();
			result.add(new TargetedTool(target.group(), defaultTool, group));
		}
		return new DefaultToolSet(result);
	}

	private static boolean isSameAction(Tool tool, Tool other) {
		return tool.getActionType() == other.getActionType() && Objects.equals(tool.getAction(), other.getAction());
	}
}
