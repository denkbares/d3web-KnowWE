/*
 * Copyright (C) 2026 denkbares GmbH, Germany
 */

package de.knowwe.tools;

import java.util.List;
import java.util.stream.Collectors;

import org.jetbrains.annotations.NotNull;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Tests {@link TargetedTool#mergeTargets(ToolSet)}.
 *
 * @author Albrecht Striffler (denkbares GmbH) + Claude
 * @created 16.11.2026
 */
public class ToolTargetMergeTest {

	private static DefaultTool tool(String title, String action) {
		return new DefaultTool(null, title, "", action, Tool.CATEGORY_INFO);
	}

	@Test
	public void mergesToolsOfSameGroup() {
		Tool other = tool("Other", "other()");
		Tool pin = tool("Show in 3D (Pin A)", "show('pin')").setTarget("Show in 3D", "A", 3);
		Tool unit = tool("Show in 3D (Unit U)", "show('unit')").setTarget("Show in 3D", "U", 1);
		Tool socket = tool("Show in 3D (Socket S)", "show('socket')").setTarget("Show in 3D", "S", 2);
		Tool last = tool("Last", "last()");

		Tool[] merged = TargetedTool.mergeTargets(new DefaultToolSet(other, pin, unit, socket, last)).getTools();

		assertEquals(3, merged.length);
		assertSame(other, merged[0]);
		assertSame(last, merged[2]);
		TargetedTool targeted = (TargetedTool) merged[1];
		assertEquals("Show in 3D", targeted.getTitle());
		// the deepest target is the default
		assertSame(pin, targeted.getDefaultTool());
		assertEquals("show('pin')", targeted.getAction());
		// targets in the order of their depth
		assertEquals(List.of(unit, socket, pin), targeted.getTargetTools());
	}

	@Test
	public void keepsSingleToolOfGroup() {
		Tool pin = tool("Show in 3D (Pin A)", "show('pin')").setTarget("Show in 3D", "A", 3);
		Tool info = tool("Show Info: A", "info()").setTarget("Show Info", "A", 3);

		Tool[] merged = TargetedTool.mergeTargets(new DefaultToolSet(pin, info)).getTools();

		assertArrayEquals(new Tool[] { pin, info }, merged);
		assertEquals("Show in 3D (Pin A)", merged[0].getTitle());
	}

	@Test
	public void dropsDuplicateActions() {
		Tool first = tool("Show Info: A", "info('a')").setTarget("Show Info", "A", 0);
		Tool duplicate = tool("Show Info: A", "info('a')").setTarget("Show Info", "A", 0);

		Tool[] merged = TargetedTool.mergeTargets(new DefaultToolSet(first, duplicate)).getTools();

		assertArrayEquals(new Tool[] { first }, merged);
	}

	@Test
	public void dropsLinksToSamePageWithSameLabel() {
		Tool open = new DefaultTool(null, "Open 'P'", "", "Wiki.jsp?page=P#section-a", Tool.ActionType.HREF, Tool.CATEGORY_NAVIGATE)
				.setTarget("Open page", "P", 0);
		Tool sameLabel = new DefaultTool(null, "Open 'P' (Unit U)", "", "Wiki.jsp?page=P#section-b", Tool.ActionType.HREF, Tool.CATEGORY_NAVIGATE)
				.setTarget("Open page", "P", 0);
		Tool other = new DefaultTool(null, "Open 'Q'", "", "Wiki.jsp?page=Q", Tool.ActionType.HREF, Tool.CATEGORY_NAVIGATE)
				.setTarget("Open page", "Q", 0);

		TargetedTool merged = (TargetedTool) TargetedTool.mergeTargets(new DefaultToolSet(open, sameLabel, other)).getTools()[0];

		assertEquals("Open page", merged.getTitle());
		assertEquals(List.of(open, other), merged.getTargetTools());
	}

	@Test
	public void firstOfDeepestTargetsIsDefault() {
		Tool question = tool("Show Info: Q", "info('q')").setTarget("Show Info", "Q", 0);
		Tool numeric = tool("Show Info: Q_numeric", "info('qn')").setTarget("Show Info", "Q_numeric", 0);

		TargetedTool merged = (TargetedTool) TargetedTool.mergeTargets(new DefaultToolSet(question, numeric)).getTools()[0];

		assertSame(question, merged.getDefaultTool());
	}

	@Test
	public void highestPriorityIsDefault() {
		Tool plug = tool("Show Info: P", "info('p')").setTarget(new ToolTarget("Show Info", "P", 2, -1));
		Tool child = tool("Show Info: P.child", "info('c')").setTarget(new ToolTarget("Show Info", "child", 3, -2));

		TargetedTool merged = (TargetedTool) TargetedTool.mergeTargets(new DefaultToolSet(child, plug)).getTools()[0];

		assertSame(plug, merged.getDefaultTool());
		assertEquals(List.of(plug, child), merged.getTargetTools());

		// a target with higher priority is the default, even if it is less deep
		Tool solution = tool("Show Info: S", "info('s')").setTarget("Show Info", "S", 0);
		merged = (TargetedTool) TargetedTool.mergeTargets(new DefaultToolSet(child, plug, solution)).getTools()[0];
		assertSame(solution, merged.getDefaultTool());
		assertEquals(List.of(solution, plug, child), merged.getTargetTools());
	}

	/** A tool executed for all its targets at once, as long as all targets are of this kind. */
	private static class CombinedInfo extends DefaultTool implements CombinableTool {
		private final String element;

		CombinedInfo(String element, int depth) {
			super(null, "Show Info", "Shows " + element, "info('" + element + "')", Tool.CATEGORY_INFO);
			this.element = element;
			setTarget("Show Info", element, depth);
		}

		@Override
		public String getCombinedAction(@NotNull List<Tool> targetTools) {
			if (!targetTools.stream().allMatch(t -> t instanceof CombinedInfo)) return null;
			return "info(" + targetTools.stream().map(t -> "'" + ((CombinedInfo) t).element + "'")
					.collect(Collectors.joining(",")) + ")";
		}

		@Override
		public String getCombinedDescription(@NotNull List<Tool> targetTools) {
			return "Shows all";
		}
	}

	@Test
	public void combinesCombinableTools() {
		Tool unit = new CombinedInfo("U", 1);
		Tool pin = new CombinedInfo("P", 2);

		TargetedTool merged = (TargetedTool) TargetedTool.mergeTargets(new DefaultToolSet(pin, unit)).getTools()[0];

		assertTrue(merged.isCombined());
		assertSame(pin, merged.getDefaultTool());
		assertEquals("info('U','P')", merged.getAction());
		assertEquals("Shows all", merged.getDescription());
		// the targets still execute the tool of their element
		assertEquals("info('U')", merged.getTargetTools().get(0).getAction());
	}

	@Test
	public void doesNotCombineWithOtherTools() {
		Tool unit = new CombinedInfo("U", 2);
		Tool question = tool("Show Info: Q", "info('Q')").setTarget("Show Info", "Q", 1);

		TargetedTool merged = (TargetedTool) TargetedTool.mergeTargets(new DefaultToolSet(question, unit)).getTools()[0];

		// the default tool is combinable, but not the other target, so only the default is executed
		assertSame(unit, merged.getDefaultTool());
		assertFalse(merged.isCombined());
		assertEquals("info('U')", merged.getAction());
	}

	@Test
	public void leavesToolsWithoutTargetsUnchanged() {
		ToolSet tools = new DefaultToolSet(tool("A", "a()"), tool("A", "b()"));
		assertSame(tools, TargetedTool.mergeTargets(tools));
	}
}
