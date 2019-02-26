package de.knowwe.tools;

import java.util.Arrays;
import java.util.Iterator;

import org.jetbrains.annotations.NotNull;

public class DefaultToolSet implements ToolSet {

	private final Tool[] tools;

	public DefaultToolSet(Tool... tools) {
		this.tools = (tools == null) ? new Tool[0] : tools;
	}

	@NotNull
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
