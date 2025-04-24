package de.knowwe.snapshot;

import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.user.UserContext;
import de.knowwe.tools.DefaultTool;
import de.knowwe.tools.Tool;
import de.knowwe.tools.ToolProvider;
import de.knowwe.util.Icon;

/**
 * ToolProvider that provides a button that creates a snapshot of the current wiki content as a zip file.
 * The file will instantly be downloaded and additionally be stored in the tmp file folder for later use.
 */
public class CreateSnapshotToolProvider implements ToolProvider {

	public static final String SNAPSHOT = "Snapshot";
	public static final String AUTOSAVE_SNAPSHOT = "AutosaveSnapshot";

	@Override
	public Tool[] getTools(Section<?> section, UserContext userContext) {
		String jsAction = DefaultTool.createServerAction(userContext, CreateSnapshotAction.class.getSimpleName());
		return new Tool[] {
				new DefaultTool(
						Icon.FILE_ZIP,
						"Create Wiki Content Snapshot ",
						"Creates a Snapshot of the complete wiki content and stores it in the tmp repo. It can then be downloaded.",
						jsAction, Tool.CATEGORY_EXECUTE)
		};
	}

	@Override
	public boolean hasTools(Section<?> section, UserContext userContext) {
		return userContext.userIsAdmin();
	}
}
