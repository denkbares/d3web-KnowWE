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
package de.knowwe.rdf2go.sparql;

import java.util.regex.Pattern;

import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.kdom.rendering.DelegateRenderer;
import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.kdom.rendering.Renderer;
import de.knowwe.core.user.UserContext;
import de.knowwe.core.utils.KnowWEUtils;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupType;
import de.knowwe.rdf2go.Rdf2GoCore;
import de.knowwe.rdf2go.utils.Rdf2GoUtils;

public class SparqlContentRenderer implements Renderer {

	private static SparqlContentRenderer instance = null;

	public static SparqlContentRenderer getInstance() {
		if (instance == null) {
			instance = new SparqlContentRenderer();
		}
		return instance;
	}

	@Override
	public void render(Section<?> section, UserContext user, RenderResult result) {

		KnowWEUtils.cleanupSectionCookies(user, Pattern.compile("^SparqlRenderer-(.+)$"), 1);

		Section<SparqlMarkupType> markupSection = Sections.ancestor(section,
				SparqlMarkupType.class);
		Rdf2GoCore core = Rdf2GoUtils.getRdf2GoCore(markupSection);
		if (core == null) {
			// we render an empty div, otherwise the ajax rerendering does not
			// work properly
			result.appendHtmlElement("div", "");
			return;
		}

		/*
		 * Show query text above of query result
		 */
		String showQueryFlag = DefaultMarkupType.getAnnotation(markupSection,
				SparqlMarkupType.RENDER_QUERY);
		if (showQueryFlag != null && showQueryFlag.equalsIgnoreCase("true")) {
			/*
			 * we need an opening html element around all the content as for
			 * some reason the ajax insert onyl inserts one (the first) html
			 * element into the page
			 */
			result.appendHtml("<div>");

			/*
			 * render query text
			 */
			result.appendHtml("<span>");
			DelegateRenderer.getInstance().render(section, user, result);
			result.appendHtml("</span>");
		}

		String sparqlString = Rdf2GoUtils.createSparqlString(core, section.getText());

		if (sparqlString.toLowerCase().startsWith("construct")) {
			result.appendHtml("<tt>");
			result.append(section.getText());
			result.appendHtml("</tt>");
		}
		else {

			SparqlResultRenderer.getInstance()
					.renderSparqlResult(Sections.cast(section, SparqlType.class), user, result);

			if (showQueryFlag != null && showQueryFlag.equalsIgnoreCase("true")) {
					/*
					 * we need an opening html element around all the content as
					 * for some reason the ajax insert onyl inserts one (the
					 * first) html element into the page
					 */
				result.appendHtml("</div>");
			}

		}
	}

}
