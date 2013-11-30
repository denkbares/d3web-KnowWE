package de.knowwe.tools;

import java.util.Arrays;
import java.util.Iterator;

public class DefaultToolSet implements ToolSet {

	private final Tool[] tools;

	public DefaultToolSet(Tool... tools) {
		if (tools == null) tools = new Tool[0];
		this.tools = tools;
	}

	@Override
	public Iterator<Tool> iterator() {
		return Arrays.asList(tools).iterator();
	}

	@Override
	public Tool[] getTools() {
		return tools;
	}

	@Override
	public boolean hasTools() {
		return tools.length > 0;
	}

}
