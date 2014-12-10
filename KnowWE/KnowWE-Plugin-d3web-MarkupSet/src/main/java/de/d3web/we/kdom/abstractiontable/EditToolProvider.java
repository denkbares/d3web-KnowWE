package de.d3web.we.kdom.abstractiontable;

import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.user.UserContext;
import de.knowwe.instantedit.tools.InstantEditTool;
import de.knowwe.instantedit.tools.InstantEditToolProvider;
import de.knowwe.tools.Tool;
import de.knowwe.util.Icon;

public class EditToolProvider extends InstantEditToolProvider {

	@Override
	protected Tool getQuickEditPageTool(Section<?> section, UserContext userContext) {
		return new InstantEditTool(
				Icon.EDITTABLE,
				"Edit Table",
				"Edit this abstraction table in a spreadsheet-like editor",
				section,
				"KNOWWE.plugin.abstractionTable.editTool");
	}
}
