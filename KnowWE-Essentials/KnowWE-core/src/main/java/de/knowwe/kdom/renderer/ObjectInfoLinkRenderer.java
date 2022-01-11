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

package de.knowwe.kdom.renderer;

import com.denkbares.strings.Strings;
import de.knowwe.core.Environment;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.rendering.DefaultTextRenderer;
import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.kdom.rendering.Renderer;
import de.knowwe.core.user.UserContext;

public class ObjectInfoLinkRenderer implements Renderer {

	Renderer renderer = new DefaultTextRenderer();

	public ObjectInfoLinkRenderer(Renderer renderer) {
		super();
		this.renderer = renderer;
	}

	@Override
	public void render(Section<?> sec, UserContext user, RenderResult string) {

		RenderResult b = new RenderResult(string);
		renderer.render(sec, user, b);

		String objectName = sec.getText().trim();
		boolean pageExists = Environment.getInstance().getWikiConnector().doesArticleExist(objectName);

		String encodedObjectName = Strings.encodeURL(objectName);
		if (pageExists) {
			string.appendHtml("<a href=\"Wiki.jsp?page=" + encodedObjectName + "\">");
			string.append(b);
			string.appendHtml("</a>");
			string.appendHtml(" <a href=\"Wiki.jsp?page=" + encodedObjectName + "\">"
					+ "<img style='vertical-align:middle;' title='-> Wikipage "
					+ encodedObjectName
					+ "' src='KnowWEExtension/images/dt_icon_premises.gif' height='11' /></a>");
		}
		else {
			string.appendHtml(
					"<a href=\"Wiki.jsp?page=ObjectInfoPage&amp;objectname="
							+ encodedObjectName
							+ "\">");
			string.append(b);
			string.appendHtml("</a>");
		}
	}
}
