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
package de.knowwe.core.taghandler;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.defaultMarkup.DefaultMarkupRenderer;
import de.d3web.we.kdom.defaultMarkup.DefaultMarkupType;
import de.d3web.we.kdom.objects.KnowWETerm;
import de.d3web.we.kdom.objects.TermDefinition;
import de.d3web.we.kdom.objects.TermReference;
import de.d3web.we.taghandler.AbstractTagHandler;
import de.d3web.we.terminology.TerminologyHandler;
import de.d3web.we.tools.Tool;
import de.d3web.we.tools.ToolUtils;
import de.d3web.we.utils.KnowWEUtils;
import de.d3web.we.wikiConnector.KnowWEUserContext;

/**
 * ObjectInfo TagHandler
 *
 * This TagHandler gathers information about a specified Object. The TagHanlder
 * shows the article in which the object is defined and all articles with
 * references to this object.
 *
 * Additionally there is a possibility to rename this object in all articles and
 * to create a wiki page for this object.
 *
 * @author Sebastian Furth
 * @created 01.12.2010
 */
public class ObjectInfoTagHandler extends AbstractTagHandler {

	// Paremeter used in the request
	public static final String OBJECTNAME = "objectname";
	private static final String HIDEDEF = "hideDefinition";
	private static final String HIDEREFS = "hideReferences";

	// KnowWE-ResourceBundle
	private ResourceBundle rb;

	public ObjectInfoTagHandler() {
		super("ObjectInfo", true);
	}

	@Override
	public final String render(KnowWEArticle article, Section<?> section, KnowWEUserContext userContext, Map<String, String> parameters) {
		rb = KnowWEEnvironment.getInstance().getKwikiBundle();
		String content = renderContent(article, section, userContext, parameters);
		Section<TagHandlerTypeContent> tagNameSection = section.findSuccessor(TagHandlerTypeContent.class);
		String sectionID = section.getID();
		Tool[] tools = ToolUtils.getTools(article, tagNameSection, userContext);

		StringBuilder buffer = new StringBuilder();
		DefaultMarkupRenderer.renderDefaultMarkupStyled(
				getTagName(), content, sectionID, tools, buffer);
		return buffer.toString();
	}

	private String renderContent(KnowWEArticle article, Section<?> section, KnowWEUserContext user, Map<String, String> parameters) {

		Map<String, String> urlParameters = user.getUrlParameterMap();

		// First try the URL-Parameter, if null try the TagHandler-Parameter.
		String objectName = urlParameters.get(OBJECTNAME) != null
				? KnowWEUtils.html_escape(urlParameters.get(OBJECTNAME))
				: KnowWEUtils.html_escape(parameters.get(OBJECTNAME));

		// If name is not defined -> render search form!
		if (objectName == null) {
			return KnowWEUtils.maskHTML(renderLookUpForm(article));
		}

		StringBuilder html = new StringBuilder();
		html.append(renderHeader(objectName));
		// TODO: After Moneypenny
		// html.append(renderRenamingForm(objectName));
		html.append(renderObjectInfo(objectName, article.getWeb(), parameters));

		return KnowWEUtils.maskHTML(html.toString());
	}

	private String renderHeader(String objectName) {
		StringBuilder html = new StringBuilder();
		html.append("<strong><span id=\"objectinfo-src\">");
		html.append(objectName);
		html.append("</span></strong><br />\n");
		return html.toString();
	}

	private String renderLookUpForm(KnowWEArticle article) {
		StringBuilder html = new StringBuilder();

		html.append("<div>");
		html.append("<p>");
		html.append(rb.getString("KnowWE.ObjectInfoTagHandler.look_up"));
		html.append("</p>");

		html.append("<form action=\"\" method=\"get\">");
		html.append("<input type=\"hidden\" name=\"page\" value=\""
				+ KnowWEUtils.urlencode(article.getTitle())
				+ "\" />");
		html.append("<input type=\"text\" name=\"" + OBJECTNAME + "\" /> ");
		html.append("<input type=\"submit\" value=\"&rarr;\" />");
		html.append("</form>");
		html.append("</div>\n");

		return html.toString();
	}

	// TODO: After Moneypenny:
	// private String renderRenamingForm(String objectName) {
	// StringBuilder html = new StringBuilder();
	//
	// html.append("<div>");
	// html.append("<form action=\"\" method=\"post\">");
	// html.append(rb.getString("KnowWE.ObjectInfoTagHandler.rename_to"));
	// html.append("<input type=\"hidden\" id=\"objectinfo-target\" value=\"" +
	// objectName
	// + "\" />");
	// html.append("<input type=\"text\" id=\"objectinfo-replacement\" /> ");
	// html.append("<input type=\"submit\" id=\"objectinfo-replace\" value=\"&rarr;\" />");
	// html.append("</form>");
	// html.append("</div>\n");
	// html.append("<div style=\"margin-left:-4px; height:1px; width:100%; background-color:#DDDDDD;\"></div>");
	//
	// return html.toString();
	// }

	private String renderObjectInfo(String objectName, String web, Map<String, String> parameters) {
		StringBuilder html = new StringBuilder();

		TerminologyHandler th = KnowWEUtils.getTerminologyHandler(web);
		Section<? extends TermDefinition> definition;
		Set<Section<? extends TermDefinition>> definitions = new HashSet<Section<? extends TermDefinition>>();
		Set<Section<? extends TermReference>> references = new HashSet<Section<? extends TermReference>>();
		Set<Section<? extends TermReference>> temp = new HashSet<Section<? extends TermReference>>();

		Iterator<KnowWEArticle> iter = KnowWEEnvironment.getInstance().getArticleManager(web).getArticleIterator();
		KnowWEArticle currentArticle;

		while (iter.hasNext()) {
			currentArticle = iter.next();

			// Check if there is a TermDefinition
			definition = th.getTermDefiningSection(currentArticle, objectName, KnowWETerm.LOCAL);
			if (definition != null) {
				definitions.add(definition);
			}

			// Check if there are References
			temp = th.getTermReferenceSections(currentArticle, objectName, KnowWETerm.LOCAL);
			if (temp != null && temp.size() > 0) {
				references.addAll(temp);
			}
		}

		boolean hideDefinition = parameters.get(HIDEDEF) != null
				? Boolean.parseBoolean(parameters.get(HIDEDEF))
				: false;
		boolean hideReferences = parameters.get(HIDEREFS) != null
				? Boolean.parseBoolean(parameters.get(HIDEREFS))
				: false;

		if (!hideDefinition) html.append(renderTermDefinitions(definitions));
		if (!hideReferences) html.append(renderTermReferences(references));

		return html.toString();
	}

	private String renderTermDefinitions(Set<Section<? extends TermDefinition>> definitions) {
		StringBuilder html = new StringBuilder();

		html.append("<div>");
		if (definitions.size() > 0) {
			html.append("<p><strong>");
			html.append(rb.getString("KnowWE.ObjectInfoTagHandler.definition"));
			html.append("</strong></p>");
			html.append("<p>");
			for (Section<? extends TermDefinition> definition : definitions) {
				html.append(definition.getObjectType().getName());
				html.append(" in ");
				html.append("<a href=\"Wiki.jsp?page=");
				html.append(definition.getArticle().getTitle());
				html.append("#");
				html.append(definition.getID());
				html.append("\" >");
				html.append(definition.getTitle());
				html.append("</a>");
			}
			html.append("</p>");
		}
		html.append("</div>\n");
		html.append("<div style=\"margin-left:-4px; height:1px; width:102%; background-color:#DDDDDD;\"></div>");

		return html.toString();
	}

	private String renderTermReferences(Set<Section<? extends TermReference>> references) {

		StringBuilder html = new StringBuilder();

		if (references.size() > 0) {

			html.append("<div>");
			html.append("<p><strong>");
			html.append(rb.getString("KnowWE.ObjectInfoTagHandler.references"));
			html.append("</strong></p>");

			// Group References by article
			Map<KnowWEArticle, List<Section<? extends TermReference>>> groupedReferences = groupByArticle(references);

			// counter, necessary for unique ids
			int c = 0;

			// For each article
			for (KnowWEArticle article : groupedReferences.keySet()) {
				html.append("<p id=\"objectinfo-" + c++
						+ "-show-extend\" class=\"show-extend pointer extend-panel-right\" >");
				html.append("<strong>");
				html.append(article.getTitle());
				html.append("</strong>");
				html.append("</p>");
				html.append("<div class=\"hidden\">");
				html.append("<ul>");
				// render references for current article
				for (Section<? extends TermReference> reference : groupedReferences.get(article)) {
					html.append("<li>");
					html.append(reference.getObjectType().getName());
					html.append(" in ");
					html.append(renderLinkToSection(reference));
					html.append("</li>");
				}
				html.append("</ul>");
				html.append("</div>");
			}
			html.append("</div>\n");

		}

		return html.toString();
	}

	private String renderLinkToSection(Section<? extends TermReference> reference) {
		StringBuilder html = new StringBuilder();

		if (reference != null) {
			// Render link to anchor
			html.append("<a href=\"Wiki.jsp?page=");
			html.append(reference.getArticle().getTitle());
			html.append("#");
			html.append(reference.getID());
			html.append("\" >");

			// Get a nice name
			Section<DefaultMarkupType> root = reference.findAncestorOfType(DefaultMarkupType.class);
			html.append(root != null
					? root.getObjectType().getName()
					: reference.getFather().getObjectType().getName());

			html.append("</a>");
		}

		return html.toString();
	}

	private Map<KnowWEArticle, List<Section<? extends TermReference>>> groupByArticle(Set<Section<? extends TermReference>> references) {

		Map<KnowWEArticle, List<Section<? extends TermReference>>> result = new HashMap<KnowWEArticle, List<Section<? extends TermReference>>>();
		KnowWEArticle article;

		for (Section<? extends TermReference> reference : references) {
			article = reference.getArticle();
			List<Section<? extends TermReference>> existingReferences = result.get(article);
			if (existingReferences == null) {
				existingReferences = new LinkedList<Section<? extends TermReference>>();
			}
			existingReferences.add(reference);
			result.put(article, existingReferences);
		}

		return result;
	}

}
