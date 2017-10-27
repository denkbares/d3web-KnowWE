package de.knowwe.ontology.kdom.table;

import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.user.UserContext;
import de.knowwe.instantedit.tools.InstantEditTool;
import de.knowwe.tools.Tool;
import de.knowwe.tools.ToolProvider;
import de.knowwe.util.Icon;

/**
 * @author Jochen Reutelshoefer (denkbares GmbH)
 * @created 27.10.17.
 */
public class HierarchyTableToolProvider implements ToolProvider {

	@Override
	public boolean hasTools(Section<?> section, UserContext userContext) {
		return true;
	}

	@Override
	public Tool[] getTools(Section<?> section, UserContext userContext) {
		return new Tool[] { new InstantEditTool(Icon.EDITTABLE, "Edit Table",
				"Provides a spreadsheet like editor.", section, "KNOWWE.plugin.tableEditTool") };
	}

}
