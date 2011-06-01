/*
 * Copyright (C) 2010 University Wuerzburg, Computer Science VI
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
package de.knowwe.core.taghandler;

import java.util.Collection;
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
import de.d3web.we.kdom.Sections;
import de.d3web.we.kdom.basic.PlainText;
import de.d3web.we.kdom.defaultMarkup.DefaultMarkupRenderer;
import de.d3web.we.kdom.defaultMarkup.DefaultMarkupType;
import de.d3web.we.kdom.objects.KnowWETerm.Scope;
import de.d3web.we.kdom.objects.TermDefinition;
import de.d3web.we.kdom.objects.TermReference;
import de.d3web.we.kdom.search.Result;
import de.d3web.we.kdom.search.SearchEngine;
import de.d3web.we.kdom.search.SearchOption;
import de.d3web.we.taghandler.AbstractTagHandler;
import de.d3web.we.terminology.TerminologyHandler;
import de.d3web.we.tools.Tool;
import de.d3web.we.tools.ToolUtils;
import de.d3web.we.user.UserContext;
import de.d3web.we.utils.KnowWEUtils;

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

	// Parameter used in the request
	public static final String OBJECTNAME = "objectname";
	private static final String HIDEDEF = "hideDefinition";
	private static final String HIDEREFS = "hideReferences";
	private static final String HIDEPLAIN = "hidePlainTextOccurrences";
	private static final String HIDERENAME = "hideRename";

	// internal counter used to create unique IDs
	private int panelCounter = 0;
	private int sectionCounter = 0;

	// KnowWE-ResourceBundle
	private ResourceBundle rb;

	public ObjectInfoTagHandler() {
		super("ObjectInfo", true);
	}

	@Override
	public String getExampleString() {
		StringBuilder example = new StringBuilder();
		example.append("[{KnowWEPlugin objectInfo [");
		example.append(", objectname=<name of object> ");
		example.append(", hideDefinition=<true|false> ");
		example.append(", hideReferences=<true|false> ");
		example.append(", hidePlainTextOccurrences=<true|false> ");
		example.append(", hideRename=<true|false>] ");
		example.append("}])\n ");
		example.append("The parameters in [ ] are optional.");
		return example.toString();
	}

	@Override
	public final String render(KnowWEArticle article, Section<?> section, UserContext userContext, Map<String, String> parameters) {
		panelCounter = 0;
		sectionCounter = 0;
		rb = KnowWEEnvironment.getInstance().getKwikiBundle(userContext);
		String content = renderContent(article, section, userContext, parameters);
		Section<TagHandlerTypeContent> tagNameSection = Sections.findSuccessor(section,
				TagHandlerTypeContent.class);
		String sectionID = section.getID();
		Tool[] tools = ToolUtils.getTools(article, tagNameSection, userContext);

		StringBuilder buffer = new StringBuilder();
		DefaultMarkupRenderer.renderDefaultMarkupStyled(
				getTagName(), content, sectionID, tools, buffer);
		return buffer.toString();
	}

	private String renderContent(KnowWEArticle article, Section<?> section, UserContext user, Map<String, String> parameters) {

		Map<String, String> urlParameters = user.getParameters();

		// First try the URL-Parameter, if null try the TagHandler-Parameter.
		String objectName = null;
		if (urlParameters.get(OBJECTNAME) != null) {
			objectName = KnowWEUtils.urldecode(urlParameters.get(OBJECTNAME));
		}
		else if (parameters.get(OBJECTNAME) != null) {
			objectName = KnowWEUtils.urldecode(parameters.get(OBJECTNAME));
		}

		// If name is not defined -> render search form!
		if (objectName == null || objectName.isEmpty()) {
			return KnowWEUtils.maskHTML(renderLookUpForm(article));
		}

		// Get TermDefinitions and TermReferences
		TerminologyHandler th = KnowWEUtils.getTerminologyHandler(article.getWeb());
		Set<Section<? extends TermDefinition<?>>> definitions = new HashSet<Section<? extends TermDefinition<?>>>();
		Set<Section<? extends TermReference<?>>> references = new HashSet<Section<? extends TermReference<?>>>();

		Iterator<KnowWEArticle> iter = KnowWEEnvironment.getInstance().getArticleManager(
				article.getWeb()).getArticleIterator();
		KnowWEArticle currentArticle;

		while (iter.hasNext()) {
			currentArticle = iter.next();
			// Get global and local term definitions
			getTermDefinitions(currentArticle, objectName, th, Scope.GLOBAL, definitions);
			getTermDefinitions(currentArticle, objectName, th, Scope.LOCAL, definitions);
			// Get global and local term refereces
			getTermReferences(currentArticle, objectName, th, Scope.GLOBAL, references);
			getTermReferences(currentArticle, objectName, th, Scope.LOCAL, references);
		}

		// Render
		StringBuilder html = new StringBuilder();
		html.append(renderHeader(objectName, definitions));
		html.append(renderRenamingForm(objectName, article.getWeb(), parameters));
		html.append(renderObjectInfo(definitions, references, parameters));
		html.append(renderPlainTextOccurrences(objectName, article.getWeb(), parameters));

		return KnowWEUtils.maskHTML(html.toString());
	}

	private String renderHeader(String objectName, Set<Section<? extends TermDefinition<?>>> definitions) {
		StringBuilder html = new StringBuilder();
		html.append("<h3><span id=\"objectinfo-src\">");
		html.append(objectName);
		html.append("</span>");
		// Render type of (first) TermDefinition
		if (definitions != null) {
			for (Section<? extends TermDefinition<?>> definition : definitions) {
				html.append(" <em>(");
				html.append(definition.get().getTermObjectClass().getSimpleName());
				html.append(")</em>");
				break;
			}

		}

		html.append("</h3>\n");
		return html.toString();
	}

	private String renderLookUpForm(KnowWEArticle article) {
		StringBuilder html = new StringBuilder();

		html.append("<form action=\"\" method=\"get\">");
		html.append("<input type=\"hidden\" name=\"page\" value=\""
				+ KnowWEUtils.urlencode(article.getTitle())
				+ "\" />");
		html.append("<input type=\"text\" name=\"" + OBJECTNAME + "\" /> ");
		html.append("<input type=\"submit\" value=\"&rarr;\" />");
		html.append("</form>");

		return renderSection(rb.getString("KnowWE.ObjectInfoTagHandler.lookUp"),
				html.toString());
	}

	private String renderRenamingForm(String objectName, String web, Map<String, String> parameters) {

		// Check if rendering is suppressed
		if (checkParameter(HIDERENAME, parameters)) return "";

		StringBuilder html = new StringBuilder();

		html.append("<form action=\"\" method=\"post\">");
		html.append(rb.getString("KnowWE.ObjectInfoTagHandler.renameTo"));
		html.append("<input type=\"hidden\" id=\"objectinfo-target\" value=\"" +
				objectName
				+ "\" />");
		html.append("<input type=\"hidden\" id=\"objectinfo-web\" value=\"" +
				web
				+ "\" />");
		html.append("<input type=\"text\" id=\"objectinfo-replacement\" />&nbsp;");
		html.append("<input type=\"button\" id=\"objectinfo-replace-button\" value=\"&rarr;\" />");
		html.append("&nbsp;<span id=\"objectinfo-rename-result\"></span>");
		html.append("</form>");

		return renderSection(rb.getString("KnowWE.ObjectInfoTagHandler.renameTo"),
				html.toString());
	}

	private String renderObjectInfo(Set<Section<? extends TermDefinition<?>>> definitions, Set<Section<? extends TermReference<?>>> references, Map<String, String> parameters) {
		StringBuilder html = new StringBuilder();
		if (!checkParameter(HIDEDEF, parameters)) html.append(renderTermDefinitions(definitions));
		if (!checkParameter(HIDEREFS, parameters)) html.append(renderTermReferences(references,
				definitions));
		return html.toString();
	}

	private void getTermDefinitions(KnowWEArticle currentArticle, String objectName, TerminologyHandler th, Scope scope, Set<Section<? extends TermDefinition<?>>> definitions) {
		Section<? extends TermDefinition<?>> definition;
		definition = th.getTermDefiningSection(currentArticle, objectName, scope);
		if (definition != null) {
			definitions.add(definition);
		}

	}

	private void getTermReferences(KnowWEArticle currentArticle, String objectName, TerminologyHandler th, Scope scope, Set<Section<? extends TermReference<?>>> references) {
		Set<Section<? extends TermReference<?>>> temp = new HashSet<Section<? extends TermReference<?>>>();
		temp = th.getTermReferenceSections(currentArticle, objectName, scope);
		if (temp != null && temp.size() > 0) {
			references.addAll(temp);
		}

	}

	private String renderTermDefinitions(Set<Section<? extends TermDefinition<?>>> definitions) {
		StringBuilder html = new StringBuilder();

		if (definitions.size() > 0) {
			html.append("<p>");
			html.append(definitions.size() > 1 ? "<ul>" : "");
			for (Section<? extends TermDefinition<?>> definition : definitions) {
				html.append(definitions.size() > 1 ? "<li>" : "");
				html.append(definition.get().getName());
				html.append(" in ");
				html.append("<a href=\"Wiki.jsp?page=");
				html.append(definition.getArticle().getTitle());
				html.append("#");
				html.append(definition.getID());
				html.append("\" >");
				html.append(definition.getTitle());
				html.append("</a>");
				html.append(definitions.size() > 1 ? "</li>" : "");
			}
			html.append(definitions.size() > 1 ? "</ul>" : "");
			html.append("</p>");
		}

		return renderSection(rb.getString("KnowWE.ObjectInfoTagHandler.definition"),
				html.toString());
	}

	private String renderTermReferences(Set<Section<? extends TermReference<?>>> references, Set<Section<? extends TermDefinition<?>>> definitions) {

		StringBuilder html = new StringBuilder();

		if (references.size() > 0) {

			// Render a warning if there is no definition for the references
			if (definitions.size() == 0) {
				html.append("<p style=\"color:red;\">");
				html.append(rb.getString("KnowWE.ObjectInfoTagHandler.noDefinitionAvailable"));
				html.append("</p>");
			}

			Map<KnowWEArticle, List<Section<? extends TermReference<?>>>> groupedReferences = groupByArticle(references);
			for (KnowWEArticle article : groupedReferences.keySet()) {
				StringBuilder innerHTML = new StringBuilder();
				innerHTML.append("<ul>");
				for (Section<? extends TermReference<?>> reference : groupedReferences.get(article)) {
					innerHTML.append("<li>");
					innerHTML.append(reference.get().getName());
					innerHTML.append(" in ");
					innerHTML.append(renderLinkToSection(reference));
					innerHTML.append("</li>");
				}
				innerHTML.append("</ul>");
				html.append(wrapInExtendPanel(article.getTitle(), innerHTML.toString()));
			}
		}

		return renderSection(rb.getString("KnowWE.ObjectInfoTagHandler.references"),
				html.toString());
	}

	private String renderLinkToSection(Section<? extends TermReference<?>> reference) {
		StringBuilder html = new StringBuilder();

		if (reference != null) {
			// Render link to anchor
			html.append("<a href=\"Wiki.jsp?page=");
			html.append(reference.getArticle().getTitle());
			html.append("#");
			html.append(reference.getID());
			html.append("\" >" + reference.getArticle().getTitle() + " (");

			// Get a nice name
			Section<DefaultMarkupType> root = Sections.findAncestorOfType(reference,
					DefaultMarkupType.class);
			html.append(root != null
					? root.get().getName()
					: reference.getFather().get().getName());

			html.append(")</a>");
		}

		return html.toString();
	}

	private Map<KnowWEArticle, List<Section<? extends TermReference<?>>>> groupByArticle(Set<Section<? extends TermReference<?>>> references) {

		Map<KnowWEArticle, List<Section<? extends TermReference<?>>>> result = new HashMap<KnowWEArticle, List<Section<? extends TermReference<?>>>>();
		KnowWEArticle article;

		for (Section<? extends TermReference<?>> reference : references) {
			article = reference.getArticle();
			List<Section<? extends TermReference<?>>> existingReferences = result.get(article);
			if (existingReferences == null) {
				existingReferences = new LinkedList<Section<? extends TermReference<?>>>();
			}
			existingReferences.add(reference);
			result.put(article, existingReferences);
		}

		return result;
	}

	private String renderPlainTextOccurrences(String objectName, String web, Map<String, String> parameters) {

		// Check if rendering is suppressed
		if (checkParameter(HIDEPLAIN, parameters)) return "";

		StringBuilder html = new StringBuilder();

		// Search for plain text occurrences
		SearchEngine se = new SearchEngine(KnowWEEnvironment.getInstance().getArticleManager(web));
		se.setOption(SearchOption.FUZZY);
		se.setOption(SearchOption.CASE_INSENSITIVE);
		se.setOption(SearchOption.DOTALL);
		Map<KnowWEArticle, Collection<Result>> results = se.search(objectName,
				PlainText.class);

		// Flag which is set to true if there are appropriate Sections
		boolean appropriateSections = false;

		for (KnowWEArticle article : results.keySet()) {
			StringBuilder innerHTML = new StringBuilder();
			innerHTML.append("<ul style=\"list-style-type:none;\">");
			for (Result r : results.get(article)) {
				Section<?> s = r.getSection();
				if (s.getFather() != null
						&& s.getFather().get().equals(article.getRootType())) {
					appropriateSections = true;
					innerHTML.append("<li>");
					innerHTML.append("<pre style=\"margin:1em -1em;\">");
					String textBefore = r.getAdditionalContext(-35).replaceAll("(\\{|\\})", "");
					if (!article.getSection().getOriginalText().startsWith(textBefore)) innerHTML.append("...");
					innerHTML.append(textBefore);
					innerHTML.append("<a href=\"Wiki.jsp?page=");
					innerHTML.append(article.getTitle());
					innerHTML.append("#");
					innerHTML.append(s.getID());
					innerHTML.append("\" >");
					innerHTML.append(s.getOriginalText().substring(r.getStart(), r.getEnd()));
					innerHTML.append("</a>");
					String textAfter = r.getAdditionalContext(40).replaceAll("(\\{|\\})", "");
					innerHTML.append(textAfter);
					if (!article.getSection().getOriginalText().endsWith(textAfter)) innerHTML.append("...");
					innerHTML.append("</pre>");
					innerHTML.append("</li>");
				}
			}
			innerHTML.append("</ul>");
			// append the html only if there are appropriate sections!
			if (appropriateSections) {
				html.append(wrapInExtendPanel(article.getTitle(), innerHTML.toString()));
				appropriateSections = false;
			}
		}

		return renderSection(rb.getString("KnowWE.ObjectInfoTagHandler.plaintextoccurrences"),
				html.toString());
	}

	private String wrapInExtendPanel(String title, String text) {
		StringBuilder html = new StringBuilder();
		html.append("<p id=\"objectinfo-" + panelCounter++
				+ "-show-extend\" class=\"show-extend pointer extend-panel-right\" >");
		html.append("<strong>");
		html.append(title);
		html.append("</strong>");
		html.append("</p>");
		html.append("<div class=\"hidden\">");
		html.append(text);
		html.append("</div>");
		return html.toString();
	}

	private String renderHR() {
		return "<div style=\"margin-left:-4px; height:1px; width:102%; background-color:#DDDDDD;\"></div>";
	}

	private String renderSection(String title, String innerHTML) {
		StringBuilder html = new StringBuilder();
		html.append(sectionCounter > 0 ? renderHR() : "");
		html.append("<div>");
		html.append("<p><strong>");
		html.append(title);
		html.append("</strong></p>");
		html.append(innerHTML.length() > 0 ? innerHTML : "N/A");
		html.append("</div>\n");
		sectionCounter++;
		return html.toString();
	}

	private boolean checkParameter(String parameter, Map<String, String> parameters) {
		return parameters.get(parameter) != null
				? Boolean.parseBoolean(parameters.get(parameter))
				: false;
	}

}
