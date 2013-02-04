package de.knowwe.core.kdom.renderer;

import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.user.UserContext;
import de.knowwe.tools.DefaultTool;
import de.knowwe.tools.Tool;
import de.knowwe.tools.ToolProvider;

public class KDOMRendererToolProvider implements ToolProvider {

	private final String jsActionExpand = "jq$('.treeTable').expandAll();";
	private final String jsActionCollapse = "jq$('.treeTable').collapseAll();";

	@Override
	public Tool[] getTools(Section<?> section, UserContext userContext) {
		return new Tool[] {
				getExpandAllTool(section, userContext), getCollapseAllTool(section, userContext) };
	}

	protected Tool getExpandAllTool(Section<?> section, UserContext userContext) {
		return new DefaultTool(
				"KnowWEExtension/images/expandall_16x16.png",
				"Expand All",
				"Expand All", jsActionExpand);
	}

	protected Tool getCollapseAllTool(Section<?> section, UserContext userContext) {
		return new DefaultTool(
				"KnowWEExtension/images/collapseall_16x16.png",
				"Collapse All",
				"Collapse All", jsActionCollapse);
	}

}
