package de.knowwe.tools;

/**
 * Interface to access a number of tools, usually for a specific section in a
 * specific user context. This class encapsulates the creation of the tools, so
 * they are only instantiated if they are really required.
 * 
 * @author Volker Belli (denkbares GmbH)
 * @created 29.11.2013
 */
public interface ToolSet extends Iterable<Tool> {

	/**
	 * Returns the tools of this set. You may call this method only if you
	 * really need the tools itself. If you only want to test if there are any
	 * tools, use {@link #hasTools()} instead.
	 * 
	 * @created 29.11.2013
	 * @return the tools of this tool set
	 */
	Tool[] getTools();

	/**
	 * Returns if this tool set has at least one tool.
	 * 
	 * @created 29.11.2013
	 * @return if the tool set is not empty
	 */
	boolean hasTools();
}
