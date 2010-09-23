package de.d3web.we.tools;

public interface Tool {

	/**
	 * Returns the icon for the tool. The icon should have a height of 24 pixels
	 * and based on a transparent background.
	 * 
	 * @created 23.09.2010
	 * @return the path to the icon
	 */
	String getIconPath();

	/**
	 * Returns the title of the tool's action. The title is used e.g. as the
	 * menu item text for the tool's menu entry.
	 * 
	 * @created 23.09.2010
	 * @return the tools title
	 */
	String getTitle();

	/**
	 * Returns the description of the tool's action. The description is used
	 * e.g. as the tooltip text item text for the tool's menu entry. it sould
	 * only consists of plain text.
	 * 
	 * @created 23.09.2010
	 * @return the tools description
	 */
	String getDescription();

	/**
	 * Returns a javascript action that should be executed if the tool is
	 * selected by the user. The javascript should not contain any double
	 * quotes, because they may be used to integrate the action into the html
	 * dom tree.
	 * 
	 * @created 23.09.2010
	 * @return the javascript action to be executed
	 */
	String getJSAction();

}
