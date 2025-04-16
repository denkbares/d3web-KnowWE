package de.knowwe.snapshot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.user.UserContext;
import de.knowwe.tools.DefaultTool;
import de.knowwe.tools.Tool;
import de.knowwe.tools.ToolProvider;
import de.knowwe.util.Icon;

public class CreateSnapshotToolProvider implements ToolProvider {

	private static final Logger LOGGER = LoggerFactory.getLogger(CreateSnapshotToolProvider.class);

	public static final String SNAPSHOT = "Snapshot";

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
