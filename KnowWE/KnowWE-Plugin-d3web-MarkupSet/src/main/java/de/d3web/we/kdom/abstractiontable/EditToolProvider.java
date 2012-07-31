package de.d3web.we.kdom.abstractiontable;

import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.user.UserContext;
import de.knowwe.instantedit.tools.DefaultEditTool;
import de.knowwe.tools.Tool;
import de.knowwe.tools.ToolProvider;

public class EditToolProvider implements ToolProvider {

	@Override
	public Tool[] getTools(Section<?> section, UserContext userContext) {
		return new Tool[] { getEditTool(section, userContext) };
	}

	private Tool getEditTool(Section<?> section, UserContext userContext) {

		String jsAction = "KNOWWE.plugin.tableEditTool.supportLinks('" + section.getID()
				+ "', false);"
				+ "KNOWWE.plugin.instantEdit.enable("
				+ "'"
				+ section.getID()
				+ "', KNOWWE.plugin.tableEditTool);";
		return new DefaultEditTool(
				"KnowWEExtension/images/pencil.png",
				"Edit Table",
				"Edit this table in a spreadsheet-like editor",
				jsAction);
	}

}
