/*
 * Copyright (C) 2009 Chair of Artificial Intelligence and Applied Informatics
 * Computer Science VI, University of Wuerzburg
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

package de.knowwe.kdom.table;

import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.rendering.DelegateRenderer;
import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.kdom.rendering.Renderer;
import de.knowwe.core.user.UserContext;

/**
 * This is a renderer for the TableLine. It wraps the <code>TableLine</code>
 * into the according HTML element and delegates the rendering of each
 * <code>TableCell</code> to its own renderer.
 * 
 * @author smark
 */
public class TableLineRenderer implements Renderer {

	@Override
	public void render(Section<?> sec, UserContext user, RenderResult string) {

		renderLineBeginning(sec, user, string);
		DelegateRenderer.getInstance().render(sec, user, string);

		string.appendHtml("</tr>\n");

	}

	protected void renderLineBeginning(Section<?> sec, UserContext user, RenderResult string) {
		string.appendHtml("<tr");

		string.appendHtml(" id='").append(sec.getID()).append("'");

		String classes = getClasses(sec, user);

		if (!classes.isEmpty()) {
			string.appendHtml(" class='" + classes + "'");
		}

		string.appendHtml(">");
	}

	/**
	 * Returns the CSS-class for the given table line.
	 * 
	 * @created 16.03.2011
	 * @return an empty string, if no classes should be assigned to the table
	 *         line, a string of CSS classes otherwise
	 */
	protected String getClasses(Section<?> tableLine, UserContext user) {
		return "";
	}

}
