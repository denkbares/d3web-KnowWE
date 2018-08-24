/*
 * Copyright (C) ${year} denkbares GmbH, Germany
 *
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option) any
 * later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */

package de.knowwe.tools;

import de.knowwe.util.Icon;

/**
 * This interface provides a definition of a javascript tool that can be integrated into the rendered wiki pages.
 *
 * @author volker_belli
 * @created 23.09.2010
 */
public interface Tool {

	enum ActionType {
		/**
		 * The action is added as a href="javascript:..."<br/> This is the default (for historical reasons).
		 */
		HREF_SCRIPT,
		/**
		 * The action is added as a normal href="..."
		 */
		HREF,
		/**
		 * The action is added as onclick="..."
		 */
		ONCLICK
	}

	/**
	 * Category for tools that execute or run stuff.
	 */
	String CATEGORY_EXECUTE = "00-execute";

	/**
	 * Category for all tools providing additional information for the tool-supporting item.
	 */
	String CATEGORY_INFO = "01-info/01-info";

	/**
	 * Category for all tools providing navigation to more information.
	 */
	String CATEGORY_NAVIGATE = "01-info/02-nav";

	/**
	 * Category for all tools providing utilities to get more information, e.g. watches, debugger, etc.
	 */
	String CATEGORY_UTIL = "01-info/03-util";

	/**
	 * Category for all tools providing utilities to get help on the markup or the object.
	 */
	String CATEGORY_HELP = "01-info/04-help";

	/**
	 * Category for all tools that modify the content of the tool-supporting item, or provides edit functionality to the
	 * user.
	 */
	String CATEGORY_EDIT = "02-edit";

	/**
	 * Category for all tools that provides tha ability to download the content of the tool-supporting item or any
	 * additionally generated content related to the item.
	 */
	String CATEGORY_DOWNLOAD = "03-download";

	/**
	 * Category for correction suggestions to correct the item. Even this is also a form of editing the item, they have
	 * their own category to place these (typically longish) list at the end of the tool menu.
	 */
	String CATEGORY_CORRECT = "04-correct";

	/**
	 * Special category to inline html functionality directly into the tool menu, e.g. for having zoom buttons.
	 */
	String CATEGORY_INLINE = "inline";

	/**
	 * Special category to sort Tools into the last category of the tool menu, probably because they are not important.
	 */
	String CATEGORY_LAST = "last";

	/**
	 * Returns the icon for the tool. The icon should have a height of 24 pixels and based on a transparent background.
	 *
	 * @return the path to the icon
	 * @created 23.09.2010
	 */
	Icon getIcon();

	/**
	 * Returns the title of the tool's action. The title is used e.g. as the menu item text for the tool's menu entry.
	 *
	 * @return the tools title
	 * @created 23.09.2010
	 */
	String getTitle();

	/**
	 * Returns the description of the tool's action. The description is used e.g. as the tooltip text item text for the
	 * tool's menu entry. it sould only consists of plain text.
	 *
	 * @return the tools description
	 * @created 23.09.2010
	 */
	String getDescription();

	/**
	 * Returns an action that should be executed if the tool is selected by the user. The javascript should not contain
	 * any double quotes, because they may be used to integrate the action into the html dom tree. How the action is
	 * integrated is also determined by the ActionType!
	 *
	 * @return the action to be executed
	 * @created 23.09.2010
	 */
	String getAction();

	/**
	 * Returns what type of action is used in this tool. The action of this tool will be integrated depending on this
	 * action. As the default ActionType.HREF_SCRIPT will be used.
	 *
	 * @return the type of the action
	 */
	ActionType getActionType();

	/**
	 * Returns the category of the tool.
	 * <p>
	 * Three types of categories are possible: <ul> <li><i>(null/empty):</i> Item will not be grouped.</li>
	 * <li><tt>category</tt>: Item belongs to group <em>category</em> and will be on top of this category (likely used
	 * for group headers).</li> <li><tt>category/subcategory</tt>: Item belongs to group <em>category</em> and will be
	 * sorted according to
	 * <em>subcategory</em>.</li> </ul>
	 * <p>
	 * Generally, tools are displayed in alphabetical order of their category, then subcategory and then in order as
	 * given by the getTools() method of {@link ToolUtils}.
	 *
	 * @return The category of the tool
	 * @created 20.02.2011
	 */
	String getCategory();
}
