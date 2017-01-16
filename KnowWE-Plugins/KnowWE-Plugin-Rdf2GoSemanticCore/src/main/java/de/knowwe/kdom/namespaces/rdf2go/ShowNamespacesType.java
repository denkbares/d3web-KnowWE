/*
 * < * Copyright (C) 2009 Chair of Artificial Intelligence and Applied
 * Informatics Computer Science VI, University of Wuerzburg
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
package de.knowwe.kdom.namespaces.rdf2go;

import java.util.Map;
import java.util.Map.Entry;

import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.user.UserContext;
import de.knowwe.kdom.defaultMarkup.DefaultMarkup;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupRenderer;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupType;
import de.knowwe.rdf2go.Rdf2GoCore;
import de.knowwe.rdf2go.utils.Rdf2GoUtils;

public class ShowNamespacesType extends DefaultMarkupType {

	private static DefaultMarkup MARKUP = null;

	static {
		MARKUP = new DefaultMarkup("ShowNamespaces");
	}

	public ShowNamespacesType() {
		super(MARKUP);
		this.setRenderer(new NamespacesRenderer());
	}

	private static class NamespacesRenderer extends DefaultMarkupRenderer {

		@Override
		protected void renderContents(Section<?> section, UserContext user, RenderResult string) {

			if (user.isRenderingPreview()) {
				string.append("%%information Namespaces are not rendered in live preview. /%");
				return;
			}

			Rdf2GoCore core = Rdf2GoUtils.getRdf2GoCore(Sections.cast(section,
					ShowNamespacesType.class));

			Map<String, String> namespaces = core.getNamespaces();

			string.appendHtml("<table>");
			string.appendHtml("<tr><th align='left'>Appreviations</th><th align='left'>Namespaces</th></tr>");
			for (Entry<String, String> cur : namespaces.entrySet()) {
				string.appendHtml("<tr><td>");
				string.append(cur.getKey());
				string.appendHtml("</td><td>");
				string.append(cur.getValue());
				string.appendHtml("</td></tr>");
			}
			string.appendHtml("</table>");

		}
	}

}
