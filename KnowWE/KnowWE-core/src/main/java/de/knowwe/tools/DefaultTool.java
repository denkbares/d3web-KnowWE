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

/**
 * This class is a default implementation of the {@link Tool} interface.
 * 
 * @author volker_belli
 * @created 23.09.2010
 */
public class DefaultTool implements Tool {

	private final String iconPath;
	private final String title;
	private final String description;
	private final String jsAction;
	private final ActionType type;
	private final String category;

	public DefaultTool(String iconPath, String title, String description, String jsAction, ActionType type, String category) {
		this.iconPath = iconPath;
		this.title = title;
		this.description = description;
		this.jsAction = jsAction;
		this.type = type;
		this.category = category;
	}

	public DefaultTool(String iconPath, String title, String description, String jsAction, String category) {
		this(iconPath, title, description, jsAction, ActionType.HREF_SCRIPT, category);
	}

	public DefaultTool(String iconPath, String title, String description, String jsAction) {
		this(iconPath, title, description, jsAction, ActionType.HREF_SCRIPT, null);
	}

	@Override
	public String getIconPath() {
		return iconPath;
	}

	@Override
	public String getTitle() {
		return title;
	}

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public String getAction() {
		return jsAction;
	}

	@Override
	public ActionType getActionType() {
		return type;
	}

	@Override
	public String getCategory() {
		return category;
	}

}
