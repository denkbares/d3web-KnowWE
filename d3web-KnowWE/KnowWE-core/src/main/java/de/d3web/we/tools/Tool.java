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

package de.d3web.we.tools;

/**
 * This interface provides a definition of a javascript tool that can be
 * integrated into the rendered wiki pages.
 * 
 * @author volker_belli
 * @created 23.09.2010
 */
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
