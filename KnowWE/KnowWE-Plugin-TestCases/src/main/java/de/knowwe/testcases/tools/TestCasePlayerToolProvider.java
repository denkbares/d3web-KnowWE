package de.knowwe.testcases.tools;

import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.user.UserContext;
import de.knowwe.tools.DefaultTool;
import de.knowwe.tools.Tool;
import de.knowwe.tools.ToolProvider;

public class TestCasePlayerToolProvider implements ToolProvider {

	@Override
	public Tool[] getTools(Section<?> section, UserContext userContext) {
		return new Tool[] {
				getGoToTools(section, userContext), getDownloadCaseTool(section, userContext),
				getDownlaodCasesZipTool(section, userContext),
				getExpandTool(section, userContext), getCollapseTool(section, userContext) };
	}

	public Tool getCollapseTool(Section<?> section, UserContext userContext) {
		String jsAction = "TestCasePlayer.toggleFindings('" + section.getID() + "', 'collapse');";
		Tool collapseTool = new DefaultTool("KnowWEExtension/images/collapseall_16x16.png",
				"Collapse findings", "Collapses all finding columns of the current test case",
				jsAction);
		return collapseTool;
	}

	public Tool getExpandTool(Section<?> section, UserContext userContext) {
		String jsAction = "TestCasePlayer.toggleFindings('" + section.getID() + "', 'expand');";
		Tool expandTool = new DefaultTool("KnowWEExtension/images/expandall_16x16.png",
				"Expand findings", "Expands all finding columns of the current test case",
				jsAction);
		return expandTool;
	}

	public Tool getGoToTools(Section<?> section, UserContext userContext) {

		String jsAction = "var goToLink = jq$('#"
				+ section.getID()
				+ "').find('select').find(':selected').attr('caselink');if (goToLink) window.location=goToLink;";
		Tool goToTool = new DefaultTool("KnowWEExtension/testcaseplayer/icon/testcaselink.png",
				"Go to test case", "Go to the currently selected test case", jsAction);
		return goToTool;
	}

	public Tool getDownloadCaseTool(Section<?> section, UserContext userContext) {
		String jsAction = "TestCasePlayer.downloadCase('" + section.getID() + "')";
		Tool downloadTool = new DefaultTool("KnowWEExtension/d3web/icon/download16.gif",
				"Download case", "Downloads the currently selected test case", jsAction);
		return downloadTool;
	}

	public Tool getDownlaodCasesZipTool(Section<?> section, UserContext userContext) {
		String jsAction = "TestCasePlayer.downloadCasesZip('" + section.getID() + "')";
		Tool downloadTool = new DefaultTool("KnowWEExtension/d3web/icon/download16.gif",
				"Download all cases", "Downloads all available test cases as a zip file", jsAction);
		return downloadTool;
	}

}
