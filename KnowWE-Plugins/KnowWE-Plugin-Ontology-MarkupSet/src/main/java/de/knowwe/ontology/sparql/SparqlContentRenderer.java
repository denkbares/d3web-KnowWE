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
package de.knowwe.ontology.sparql;

import java.util.Set;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;

import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.kdom.rendering.DelegateRenderer;
import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.kdom.rendering.Renderer;
import de.knowwe.core.user.UserContext;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupType;
import de.knowwe.rdf2go.Rdf2GoCore;
import de.knowwe.rdf2go.sparql.utils.RenderOptions;
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
		Section<SparqlType> sparqlTypeSection = Sections.cast(section, SparqlType.class);
		Section<DefaultMarkupType> markupSection = Sections.ancestor(section, DefaultMarkupType.class);
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
		if ("true".equalsIgnoreCase(showQueryFlag)) {
			// we need an opening html element around all the content as for
			// some reason the ajax insert only inserts one (the first) html
			// element into the page
			result.appendHtml("<div>");

			// render query text
			result.appendHtml("<span>");
			DelegateRenderer.getInstance().render(section, user, result);
			result.appendHtml("</span>");
		}

		String sparqlString = Rdf2GoUtils.createSparqlString(core, section.getText());

		if (sparqlString.toLowerCase().startsWith("construct")) {
			final Set<Statement> statementsFromCache = core.getStatementsFromCache(section);
			if (!statementsFromCache.isEmpty()) {
				final SparqlResultRenderer sparqlResultRenderer = SparqlResultRenderer.getInstance();
				int limit = 20;
				int count = 0;
				result.append("(" + statementsFromCache.size() + " statements constructed)");
				result.appendHtml("<table>");
				for (Statement statement : statementsFromCache) {
					count++;
					if (count > limit) {
						// TODO: implement pagination and remove limit
						result.appendHtml("<tr>");
						int moreStatements = statementsFromCache.size() - limit;
						result.append("\n(" + moreStatements + " statements hidden)");
						result.appendHtml("</tr>");
						break;
					}
					result.appendHtml("<tr>");

					result.appendHtml("<td>");
					final Resource subject = statement.getSubject();
					result.appendHtml(sparqlResultRenderer.renderNode(subject, "", false, user, core, RenderMode.HTML));
					result.appendHtml("</td>");

					result.appendHtml("<td>");
					final IRI predicate = statement.getPredicate();
					result.appendHtml(sparqlResultRenderer.renderNode(predicate, "", false, user, core, RenderMode.HTML));
					result.appendHtml("</td>");

					result.appendHtml("<td>");
					final Value object = statement.getObject();
					result.appendHtml(sparqlResultRenderer.renderNode(object, "", false, user, core, RenderMode.HTML));
					result.appendHtml("</td>");

					result.appendHtml("</tr>");
				}
				result.appendHtml("</table>");
			}
			else {
				result.append("(No statements constructed)");
			}

		}
		else if (sparqlString.toLowerCase().startsWith("ask")) {
			// process sparql ask query
			RenderOptions opts = sparqlTypeSection.get().getRenderOptions(sparqlTypeSection, user);
			try {
				String query = sparqlTypeSection.get().getSparqlQuery(sparqlTypeSection, user);
				boolean askResult = opts.getRdf2GoCore().sparqlAsk(query, new Rdf2GoCore.Options(opts.getTimeout()));
				result.appendHtml("<div class='sparqlAsk' sparqlSectionId='" + opts.getId() + "'>");
				if (opts.isBorder()) result.appendHtml("<div class='border'>");
				result.append(Boolean.valueOf(askResult).toString());
				if (opts.isBorder()) result.appendHtml("</div>");
				result.appendHtml("</div>");
			}
			catch (RuntimeException e) {
				SparqlResultRenderer.handleRuntimeException(sparqlTypeSection, user, result, e);
			}
		}
		else {
			SparqlResultRenderer.getInstance()
					.renderSparqlResult(sparqlTypeSection, user, result);
		}
		if ("true".equalsIgnoreCase(showQueryFlag)) {
			// we need an opening html element around all the content as
			// for some reason the ajax insert only inserts one (the
			// first) html element into the page
			result.appendHtml("</div>");
		}
	}

}
