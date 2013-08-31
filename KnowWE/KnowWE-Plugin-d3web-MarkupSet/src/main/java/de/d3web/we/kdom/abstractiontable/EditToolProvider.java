package de.d3web.we.kdom.abstractiontable;

import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.user.UserContext;
import de.knowwe.instantedit.tools.InstantEditTool;
import de.knowwe.instantedit.tools.InstantEditToolProvider;
import de.knowwe.tools.Tool;

public class EditToolProvider extends InstantEditToolProvider {

	@Override
	protected Tool getQuickEditPageTool(Section<?> section, UserContext userContext) {
		return new InstantEditTool(
				"KnowWEExtension/images/pencil.png",
				"Edit Table",
				"Edit this abstraction table in a spreadsheet-like editor",
				section,
				"KNOWWE.plugin.abstractionTable.editTool");
	}
}
