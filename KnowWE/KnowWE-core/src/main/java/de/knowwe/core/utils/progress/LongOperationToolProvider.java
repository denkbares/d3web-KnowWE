package de.knowwe.core.utils.progress;

import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.user.UserContext;
import de.knowwe.tools.DefaultTool;
import de.knowwe.tools.Tool;
import de.knowwe.tools.ToolProvider;
import de.knowwe.util.Icon;

/**
 * Tool for defining a menu item starting a long operation.
 *
 * @author Volker Belli (denkbares GmbH)
 * @created 30.07.2013
 */
public abstract class LongOperationToolProvider implements ToolProvider {

	private final Icon icon;
	private final String title;
	private final String description;
	private final String category;

	public LongOperationToolProvider(Icon icon, String title, String description, String category) {
		this.icon = icon;
		this.title = title;
		this.description = description;
		this.category = category;
	}

	public LongOperationToolProvider(Icon icon, String title, String description) {
		this(icon, title, description, null);
	}

	private String createJSAction(Section<?> section) {
		String id = LongOperationUtils.registerLongOperation(section, getOperation(section));
		return "KNOWWE.core.plugin.progress.startLongOperation('" + section.getID() + "','" + id
				+ "');";
	}

	@Override
	public Tool[] getTools(Section<?> section, UserContext userContext) {
		return new Tool[] {
				new DefaultTool(icon, title, description, createJSAction(section), category)
		};
	}

	public abstract LongOperation getOperation(Section<?> section);

	@Override
	public final boolean hasTools(Section<?> section, UserContext userContext) {
		LongOperationUtils.registerLongOperation(section, getOperation(section));
		return true;
	}

}
