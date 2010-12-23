/*
 * Copyright (C) 2010 University Wuerzburg, Computer Science VI
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package de.d3web.we.tools;


/**
 * A CustomTool is a special form of Tool that takes care of rendering on its own.
 * For consistency's sake you usually should prefer using {@link DefaultTool} and/or the {@link Tool} interface.
 * 
 * @author Alex Legler
 * @created 19.12.2010 
 */
public abstract class CustomTool implements Tool {
	
	@Override
	public String getDescription() {
		return null;
	}
	
	@Override
	public String getIconPath() {
		return null;
	}
	
	@Override
	public String getJSAction() {
		return null;
	}

	@Override
	public String getTitle() {
		return null;
	}

	/**
	 * Determines whether a CustomTool has any content to render
	 * 
	 * @created 19.12.2010
	 * @return true when there is any content to render
	 */
	public abstract boolean hasContent();

	
	/**
	 * Renders the HTML code for the tool.
	 * 
	 * @created 19.12.2010
	 * @return HTML code for displaying the tool.
	 */
	public abstract String render();
}
