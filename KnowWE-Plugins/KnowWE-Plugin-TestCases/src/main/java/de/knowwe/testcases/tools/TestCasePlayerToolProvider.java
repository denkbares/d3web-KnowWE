package de.knowwe.testcases.tools;

import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.user.UserContext;
import de.knowwe.tools.DefaultTool;
import de.knowwe.tools.Tool;
import de.knowwe.tools.ToolProvider;
import de.knowwe.util.Icon;

public class TestCasePlayerToolProvider implements ToolProvider {

	@Override
	public boolean hasTools(Section<?> section, UserContext userContext) {
		return true;
	}

	@Override
	public Tool[] getTools(Section<?> section, UserContext userContext) {
		return new Tool[] {
				getGoToTools(section, userContext), getExpandTool(section, userContext),
				getCollapseTool(section, userContext), getDownloadCaseTool(section, userContext) };
	}

	public Tool getCollapseTool(Section<?> section, UserContext userContext) {
		String jsAction = "TestCasePlayer.toggleFindings('" + section.getID() + "', 'collapse');";
		Tool collapseTool = new DefaultTool(Icon.COLLAPSE_OUTLINE,
				"Collapse findings", "Collapses all finding columns of the current test case",
				jsAction, Tool.CATEGORY_LAST);
		return collapseTool;
	}

	public Tool getExpandTool(Section<?> section, UserContext userContext) {
		String jsAction = "TestCasePlayer.toggleFindings('" + section.getID() + "', 'expand');";
		Tool expandTool = new DefaultTool(Icon.EXPAND_OUTLINE,
				"Expand findings", "Expands all finding columns of the current test case",
				jsAction, Tool.CATEGORY_LAST);
		return expandTool;
	}

	public Tool getGoToTools(Section<?> section, UserContext userContext) {

		String jsAction = "var goToLink = jq$('#"
				+ section.getID()
				+ "').find('select').find(':selected').attr('caselink');if (goToLink) window.location=goToLink;";
		Tool goToTool = new DefaultTool(Icon.OPENTESTCASE,
				"Open test case", "Opens the currently selected test case source", jsAction, Tool.CATEGORY_NAVIGATE);
		return goToTool;
	}

	public Tool getDownloadCaseTool(Section<?> section, UserContext userContext) {
		String jsAction = "TestCasePlayer.downloadCase('" + section.getID() + "')";
		Tool downloadTool = new DefaultTool(Icon.DOWNLOAD,
				"Download case", "Downloads the currently selected test case", jsAction, Tool.CATEGORY_DOWNLOAD);
		return downloadTool;
	}

}
