package de.knowwe.tools;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.user.UserContext;

public class FutureToolSet implements ToolSet {

	private final Section<?> section;
	private final UserContext userContext;

	private transient List<Tool> tools = null;
	private transient Boolean hasTools = null;

	public FutureToolSet(Section<?> section, UserContext userContext) {
		this.section = section;
		this.userContext = userContext;
	}

	@Override
	public Iterator<Tool> iterator() {
		return getToolInstances().iterator();
	}

	@Override
	public Tool[] getTools() {
		List<Tool> result = getToolInstances();
		return result.toArray(new Tool[result.size()]);
	}

	private List<Tool> getToolInstances() {
		if (this.tools == null) {
			this.tools = Collections.unmodifiableList(
					ToolUtils.getToolInstances(section, userContext));
		}
		return tools;
	}

	@Override
	public boolean hasTools() {
		if (hasTools == null) {
			hasTools = ToolUtils.hasToolInstances(section, userContext);
			if (!hasTools) {
				tools = Collections.emptyList();
			}
		}
		return hasTools;
	}

}
