/*
 * Copyright (C) 2013 University Wuerzburg, Computer Science VI
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
package de.knowwe.core.objectinfo;

import de.d3web.collections.CountingSet;
import de.d3web.strings.Identifier;
import de.d3web.strings.Strings;
import de.knowwe.core.Environment;
import de.knowwe.core.compile.terminology.TerminologyManager;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.ArticleComparator;
import de.knowwe.core.kdom.basicType.PlainText;
import de.knowwe.core.kdom.objects.Term;
import de.knowwe.core.kdom.objects.TermInfo;
import de.knowwe.core.kdom.objects.TermUtils;
import de.knowwe.core.kdom.parsing.KDOMPositionComparator;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.kdom.rendering.Renderer;
import de.knowwe.core.preview.PreviewManager;
import de.knowwe.core.preview.PreviewRenderer;
import de.knowwe.core.taghandler.TagHandlerType;
import de.knowwe.core.user.UserContext;
import de.knowwe.core.utils.KnowWEUtils;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupRenderer;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupType;
import de.knowwe.kdom.search.Result;
import de.knowwe.kdom.search.SearchEngine;
import de.knowwe.kdom.search.SearchOption;
import de.knowwe.tools.ToolSet;
import de.knowwe.tools.ToolUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;

/**
 * @author stefan
 * @created 09.12.2013
 */
public class ObjectInfoRenderer implements Renderer {

	// Parameter used in the request
	public static final String OBJECT_NAME = "objectname";
	public static final String TERM_IDENTIFIER = "termIdentifier";
	// private static final String HIDE_DEF = "hideDefinition";
	// private static final String HIDE_REFS = "hideReferences";
	// private static final String HIDE_PLAIN = "hidePlainTextOccurrences";
	// private static final String HIDE_RENAME = "hideRename";
	// private static final String RENAMED_ARTICLES = "renamedArticles";

	private static DefaultMarkupRenderer defaultMarkupRenderer = new DefaultMarkupRenderer();

	@Override
	public final synchronized void render(Section<?> section, UserContext userContext, RenderResult result) {

		RenderResult content = new RenderResult(userContext);

		Identifier termIdentifier = getTermIdentifier(userContext, section);
		renderContent(termIdentifier, userContext, content);

		Section<ObjectInfoType> tagNameSection = Sections.findSuccessor(
				section, ObjectInfoType.class);
		String sectionID = section.getID();
		ToolSet tools = ToolUtils.getTools(tagNameSection, userContext);

		// RenderResult jspMasked = new RenderResult(result);
		String cssClassName = "type_" + section.get().getName();
		defaultMarkupRenderer.renderDefaultMarkupStyled("ObjectInfo", content.toStringRaw(),
				sectionID, cssClassName, tools, userContext, result);
		// result.appendJSPWikiMarkup(jspMasked);
	}

	private void renderContent(Identifier termIdentifier, UserContext user,
							   RenderResult result) {

		// renderLookUpForm(identifier, user, result);

		// Render
		ObjectInfoRenderer.renderHeader(termIdentifier, user, result);
		ObjectInfoRenderer.renderLookUpForm(user, result);
		ObjectInfoRenderer.renderRenamingForm(termIdentifier, user, result);
		ObjectInfoRenderer.renderTermDefinitions(termIdentifier, user, result);
		ObjectInfoRenderer.renderTermReferences(termIdentifier, user, result);
		ObjectInfoRenderer.renderPlainTextOccurrences(termIdentifier, user, result);
		// renderHeader(identifier.toExternalForm(),
		// getTermObjectClass(user, identifier), result);
		// renderRenamingForm(identifier,
		// user, result);
		// renderObjectInfo(identifier, user, result);
		// renderPlainTextOccurrences(identifier.getLastPathElement(),
		// user.getWeb(), result);
		// result.append("\n");
	}

	/**
	 * Renders the specified list of term references (usually of one specific
	 * article). The method renders the previews of the specified sections,
	 * grouped by their preview. Each preview may render one or multiple of the
	 * specified sections.
	 *
	 * @param sections the section to be rendered in their previews
	 * @param user     the user context
	 * @param result   the buffer to render into
	 * @created 29.11.2013
	 */

	public static void renderTermReferencesPreviews(List<Section<?>> sections, UserContext user, RenderResult result) {
		if (!KnowWEUtils.canView(sections, user)) {
			result.appendHtml("<i>You are not allowed to view this article.</i>");
			return;
		}
		result.appendHtmlTag("ul", "class", "nodisc");
		Map<Section<?>, Collection<Section<?>>> groupedByPreview = ObjectInfoRenderer.groupByPreview(sections);
		boolean first = true;
		for (Entry<Section<?>, Collection<Section<?>>> entry : groupedByPreview.entrySet()) {
			Section<?> previewSection = entry.getKey();
			Collection<Section<?>> group = entry.getValue();

			result.appendHtml("<li><div id='" + previewSection.getID() + "'>");
			ObjectInfoRenderer.renderLinkToSection(previewSection, result);
			ObjectInfoRenderer.renderTermPreview(previewSection, group, user, "reference", result);
			String clazz = "editanchor";
			if (first) {
				// we mark the first (and closing) anchors to not interfere with
				// page appends while enabling edit mode
				clazz += " first";
				first = false;
			}
			result.appendHtml("<param class='" + clazz + "' sectionid='"
					+ previewSection.getID() + "' />");

			result.appendHtml("</div></li>");
		}
		result.appendHtml("</ul>");
	}

	public static void renderLookUpForm(UserContext user, RenderResult result) {
		renderSectionStart("Look up object information", result);
		result.appendHtml("<form action=\"\" method=\"get\" class=\"ui-widget\" >")
				.appendHtml("<input type=\"hidden\" id=\"objectinfo-web-lookup\" value=\"")
				.append(user.getWeb())
				.appendHtml("\" />");
		result.appendHtml("<input type=\"hidden\" name=\"page\" value=\"")
				.append("ObjectInfoPage")
				.appendHtml("\" />");
		result.appendHtml("<div style=\"display:none\" id=\"objectinfo-terms\" name=\"terms\" >");
		result.appendJSPWikiMarkup(ObjectInfoRenderer.getTerms(user).toString()
				.replaceAll("([^\\\\]\\\"),\\\"", "$1,\n\""));
		result.appendHtml("</div>");
		result.appendHtml("<input type=\"text\" size=\"60\" name=\"")
				.append(ObjectInfoRenderer.OBJECT_NAME)
				.appendHtml("\" id=\"objectinfo-search\" />&nbsp;");
		result.appendHtml("<input type=\"submit\" value=\"go to\" />");
		result.appendHtml("</form>");
		renderSectionEnd(result);
	}

	public static void renderRenamingForm(Identifier identifier, UserContext user, RenderResult result) {

		renderSectionStart("Rename to", result);

		String escapedExternalTermIdentifierForm = Strings.encodeHtml(identifier.toExternalForm());

		result.appendHtml("<input type=\"hidden\" id=\"objectinfo-target\" value=\""
				+ escapedExternalTermIdentifierForm + "\" />");
		result.appendHtml("<input type=\"hidden\" id=\"objectinfo-web-rename\" value=\""
				+ user.getWeb() + "\" />");
		result.appendHtml("<input action=\"" + getRenamingAction()
				+ "\" type=\"text\" size=\"60\" value=\"" + identifier.getLastPathElement()
				+ "\" id=\"objectinfo-replacement\" />&nbsp;");
		result.appendHtml("<input type=\"button\" id=\"objectinfo-replace-button\" value=\"rename\" />");
		result.appendHtml("&nbsp;<span id=\"objectinfo-rename-result\">");

		result.appendHtml("</span>");

		renderSectionEnd(result);

	}

	public static void renderTermDefinitions(Identifier identifier, UserContext user, RenderResult result) {
		Set<Section<?>> definitions = findTermDefinitionSections(user.getWeb(), identifier);
		renderSectionStart("Definition", result);
		if (definitions.size() > 0) {
			result.appendHtml("<p>");
			if (definitions.size() > 1) result.appendHtmlTag("ul", "class", "nodisc");

			Map<Section<?>, Collection<Section<?>>> groupedByPreview =
					groupByPreview(definitions);
			for (Entry<Section<?>, Collection<Section<?>>> entry : groupedByPreview.entrySet()) {
				Section<?> previewSection = entry.getKey();
				Collection<Section<?>> group = entry.getValue();

				if (definitions.size() > 1) result.appendHtml("<li>");
				result.appendHtml("<div>");
				result.appendHtml("<strong>");
				result.append("Article '[").append(previewSection.getTitle()).append("]' ");
				result.appendHtml("</strong>");
				result.append("(");
				renderLinkToSection(previewSection, result);
				result.append(")");
				renderTermPreview(previewSection, group, user, "definition", result);
				result.appendHtml("</div>");
				if (definitions.size() > 1) result.appendHtml("</li>");
			}

			if (definitions.size() > 1) result.appendHtml("</ul>");
			result.appendHtml("</p>");
		}
		renderSectionEnd(result);
	}

	public static void renderTermReferences(Identifier identifier, UserContext user, RenderResult result) {
		Set<Section<?>> definitions = findTermDefinitionSections(user.getWeb(), identifier);
		Set<Section<?>> references = findTermReferenceSections(user.getWeb(), identifier);

		renderSectionStart("References", result);
		if (references.size() > 0) {

			// Render a warning if there is no definition for the references
			if (definitions.size() == 0) {
				result.appendHtml("<p style=\"color:red;\">");
				result.append("no Definition Available");
				result.appendHtml("</p>");
			}

			Map<Article, List<Section<?>>> groupedReferences = groupByArticle(references);
			for (Article article : groupedReferences.keySet()) {
				List<Section<?>> referencesGroup = groupedReferences.get(article);
				RenderResult innerResult = new RenderResult(result);
				renderTermReferencesPreviewsAsync(referencesGroup, user, innerResult);
				wrapInExtendPanel(
						"Article '" + article.getTitle() + "'",
						getSurroundingMarkupNames(referencesGroup),
						innerResult, result);
			}
		}
		renderSectionEnd(result);

		// render some js to update the async previews
		result.appendHtml(
				"<script>jq$('.extend-panel-right').click(function() {KNOWWE.core.plugin.objectinfo.loadPreviews("
						+ Strings.quote(user.getWeb()) + ","
						+ Strings.quote(user.getTitle()) + ", this._next());});</script>");

		// the following statement will preload all previews in the background
		// instead of using ajax mode only. Comment in/out to change behaviour
		result.appendHtml(
				"<script>jq$(document).ready(function() {KNOWWE.core.plugin.objectinfo.loadPreviews("
						+ Strings.quote(user.getWeb()) + "," + Strings.quote(user.getTitle())
						+ ");});</script>");
	}

	public static void renderSectionStart(String title, RenderResult result) {
		result.appendHtml("<div>");
		result.appendHtml("<p><strong>");
		result.appendHtml(title);
		result.appendHtml("</strong></p>");
	}

	public static void renderSectionEnd(RenderResult result) {
		result.appendHtml("</div>\n");
	}

	protected static Set<Section<?>> findTermDefinitionSections(String web, Identifier termIdentifier) {
		TermInfo termInfo = TermUtils.getTermInfo(web, termIdentifier, false);
		Set<Section<?>> sections = new HashSet<Section<?>>();
		if (termInfo == null) {
			return sections;
		}
		for (TerminologyManager termManager : termInfo) {
			sections.addAll(termManager.getTermDefiningSections(termIdentifier));
		}
		return sections;
	}

	protected static Set<Section<?>> findTermReferenceSections(String web, Identifier termIdentifier) {
		TermInfo termInfo = TermUtils.getTermInfo(web, termIdentifier, false);
		Set<Section<?>> sections = new HashSet<Section<?>>();
		if (termInfo == null) {
			return sections;
		}
		for (TerminologyManager termManager : termInfo) {
			sections.addAll(termManager.getTermReferenceSections(termIdentifier));
		}
		return sections;
	}

	private static String getTermObjectClass(UserContext user, Identifier identifier) {
		Set<Section<?>> definitions = findTermDefinitionSections(user.getWeb(), identifier);
		Set<Section<?>> references = findTermReferenceSections(user.getWeb(), identifier);
		String termObjectClassString = "Object";
		Section<?> termSection = null;
		if (!definitions.isEmpty()) {
			termSection = definitions.iterator().next();
		}
		else if (!references.isEmpty()) {
			termSection = references.iterator().next();
		}
		if (termSection != null && termSection.get() instanceof Term) {
			Section<Term> simpleTermSection = Sections.cast(termSection,
					Term.class);
			Class<?> termObjectClass = simpleTermSection.get()
					.getTermObjectClass(simpleTermSection);
			termObjectClassString = termObjectClass.getSimpleName();
		}
		return termObjectClassString;
	}

	public static void renderHeader(Identifier identifier, UserContext user, RenderResult result) {
		result.appendHtml("<h3><span id=\"objectinfo-src\">");
		result.append(identifier.getLastPathElement());
		result.appendHtml("</span>");
		// Render type of (first) TermDefinition
		result.appendHtml(" <em>(");
		result.append(getTermObjectClass(user, identifier));
		result.appendHtml(")</em>");
		result.appendHtml("</h3>\n");
	}

	protected static String getRenamingAction() {
		return "TermRenamingAction";
	}

	private static Map<Article, List<Section<?>>> groupByArticle(Set<Section<?>> references) {
		Map<Article, List<Section<?>>> result =
				new TreeMap<Article, List<Section<?>>>(ArticleComparator.getInstance());
		for (Section<?> reference : references) {
			Article article = reference.getArticle();
			List<Section<?>> existingReferences = result.get(article);
			if (existingReferences == null) {
				existingReferences = new LinkedList<Section<?>>();
			}
			existingReferences.add(reference);
			result.put(article, existingReferences);
		}

		return result;
	}

	/**
	 * Groups the specified sections by the ancestor section to be rendered as a
	 * preview. If a section has no ancestor to be rendered, the section itself
	 * will be used as a group with an empty collection of grouped sections.
	 *
	 * @param items list of sections to be grouped
	 * @return the groups of sections
	 * @created 16.08.2013
	 */
	private static Map<Section<?>, Collection<Section<?>>> groupByPreview(Collection<Section<?>> items) {
		List<Section<?>> list = new ArrayList<Section<?>>(items);
		Collections.sort(list, KDOMPositionComparator.getInstance());
		Map<Section<?>, Collection<Section<?>>> result = new LinkedHashMap<Section<?>, Collection<Section<?>>>();
		for (Section<?> section : list) {
			Section<?> previewSection = PreviewManager.getInstance().getPreviewAncestor(section);
			// handle if the section has no preview renderer
			if (previewSection == null) {
				result.put(section, Collections.<Section<?>>emptyList());
				continue;
			}
			// otherwise add section to preview group
			// or create group if it is new
			Collection<Section<?>> group = result.get(previewSection);
			if (group == null) {
				group = new LinkedList<Section<?>>();
				result.put(previewSection, group);
			}
			group.add(section);
		}
		return result;
	}

	public static void renderLinkToSection(Section<?> reference, RenderResult result) {
		if (reference == null) return;
		// Render link to anchor (=uses div id as anchor))
		// html.append("<a href=\"Wiki.jsp?page=");
		// html.append(Strings.encodeURL(reference.getArticle()
		// .getTitle()));
		// html.append("#header_");
		// html.append(reference.getID());
		// html.append("\" >");
		result.appendHtml("<a href='");
		result.append(KnowWEUtils.getURLLink(reference));
		result.appendHtml("' class='onlyObjectInfoPage'>");
		// html.append(reference.getTitle());
		// html.append(" (");
		// Get a nice name
		result.append(getSurroundingMarkupName(reference));
		// html.append(")");
		result.appendHtml("</a>");
	}

	private static String getSurroundingMarkupName(Section<?> section) {
		if (section.get() instanceof DefaultMarkupType) return section.get().getName();
		Section<?> root = Sections.findAncestorOfType(section, DefaultMarkupType.class);
		if (root != null) return root.get().getName();
		root = Sections.findAncestorOfType(section, TagHandlerType.class);
		if (root != null) return root.get().getName();
		return section.getParent().get().getName();
	}

	public static void renderTermPreview(Section<?> previewSection, Collection<Section<?>> relevantSubSections, UserContext user, String cssClass, RenderResult result) {
		int count = relevantSubSections.size();
		if (count == 0) return;

		// if (count > 1) {
		// result.append(" (").append(count).append(" occurences)");
		// }

		result.appendHtml("<div class='objectinfo preview ").append(cssClass).appendHtml("'>");
		result.appendHtml("<div class='objectinfo type_")
				.append(previewSection.get().getName()).appendHtml("'>");

		// render the preview content part, avoiding double returns
		PreviewManager previewManager = PreviewManager.getInstance();
		PreviewRenderer renderer = previewManager.getPreviewRenderer(previewSection);
		RenderResult part = new RenderResult(result);
		renderer.render(previewSection, relevantSubSections, user, part);
		result.appendAvoidParagraphs(part);

		result.appendHtml("</div>");
		result.appendHtml("</div>");
	}

	public static void renderTermReferencesPreviewsAsync(List<Section<?>> sections, UserContext user, RenderResult result) {
		String id = UUID.randomUUID().toString();
		StringBuilder sectionIDs = new StringBuilder();
		for (Section<?> section : sections) {
			if (sectionIDs.length() > 0) sectionIDs.append(",");
			sectionIDs.append(section.getID());
		}

		result.appendHtml("<span class='asynchronPreviewRenderer'")
				.append(" id='").append(id).append("'")
				.append(" rel='").append(sectionIDs).append("'")
				.appendHtml("></span>");
	}

	/**
	 * Get a counting set of all markup names that surrounds the specified list
	 * of sections. The count is not the number of sections contained in a
	 * specific markup, but it is the number of preview sections required to
	 * display these sections (some preview sections may display multiple of the
	 * specified sections).
	 *
	 * @param sections the section to get the markup names for
	 * @return the counting set of markup names
	 * @created 29.11.2013
	 */
	private static CountingSet<String> getSurroundingMarkupNames(List<Section<?>> sections) {
		CountingSet<String> types = new CountingSet<String>();
		Map<Section<?>, Collection<Section<?>>> groupedByPreview = groupByPreview(sections);
		for (Section<?> preview : groupedByPreview.keySet()) {
			types.add(getSurroundingMarkupName(preview));
		}
		return types;
	}

	private static void wrapInExtendPanel(String title, CountingSet<String> occurences, RenderResult content, RenderResult result) {
		StringBuilder info = new StringBuilder();
		for (String string : occurences) {
			if (info.length() > 0) info.append(", ");
			int count = occurences.getCount(string);
			if (count > 1) info.append(count).append("&times; ");
			info.append(string);
		}
		wrapInExtendPanel(title, info.toString(), content, result);
	}

	private static void wrapInExtendPanel(String title, RenderResult content, RenderResult result) {
		wrapInExtendPanel(title, (String) null, content, result);
	}

	private static void wrapInExtendPanel(String title, String info, RenderResult content, RenderResult result) {
		result.appendHtml("<p class=\"show-extend pointer extend-panel-right\" >");
		result.appendHtml("<strong>");
		result.append(title);
		result.appendHtml("</strong>");
		if (!Strings.isBlank(info)) {
			result.append(" (").append(info).append(")");
		}
		result.appendHtml("</p>");
		result.appendHtml("<div class=\"hidden\" style=\"display:none\">");
		result.append(content);
		result.appendHtml("</div>");
	}

	public static void renderPlainTextOccurrences(Identifier identifier, UserContext user, RenderResult result) {

		// Check if rendering is suppressed - other solution
		// if (checkParameter(HIDE_PLAIN, parameters)) return;
		renderSectionStart("Other occurrences", result);

		// Search for plain text occurrences
		SearchEngine se = new SearchEngine(Environment.getInstance()
				.getArticleManager(user.getWeb()));
		se.setOption(SearchOption.FUZZY);
		se.setOption(SearchOption.CASE_INSENSITIVE);
		se.setOption(SearchOption.DOTALL);
		Map<Article, Collection<Result>> results = se.search(identifier.getLastPathElement(),
				PlainText.class);

		// Flag which is set to true if there are appropriate Sections
		boolean appropriateSections = false;

		for (Article article : results.keySet()) {
			RenderResult innerResult = new RenderResult(result);
			innerResult.appendHtml("<ul style=\"list-style-type:none;\">");
			for (Result r : results.get(article)) {
				Section<?> s = r.getSection();
				if (s.getParent() != null
						&& s.getParent().get().equals(article.getRootType())) {
					appropriateSections = true;
					innerResult.appendHtml("<li>");
					innerResult.appendHtml("<pre style=\"margin:1em -1em;\">");
					String textBefore = r.getAdditionalContext(-35).replaceAll("(\\{|\\})", "");
					if (!article.getRootSection().getText().startsWith(textBefore)) {
						innerResult.appendHtml("...");
					}
					innerResult.append(textBefore);
					innerResult.appendHtml("<a href=\"Wiki.jsp?page=");
					innerResult.append(article.getTitle());
					innerResult.appendHtml("#");
					innerResult.appendHtml(s.getID());
					innerResult.appendHtml("\" >");
					innerResult.append(s.getText().substring(r.getStart(), r.getEnd()));
					innerResult.appendHtml("</a>");
					String textAfter = r.getAdditionalContext(40).replaceAll("(\\{|\\})", "");
					innerResult.append(textAfter);
					if (!article.getRootSection().getText().endsWith(textAfter)) innerResult.appendHtml("...");
					innerResult.appendHtml("</pre>");
					innerResult.appendHtml("</li>");
				}
			}
			innerResult.appendHtml("</ul>");
			// append the html only if there are appropriate sections!
			if (appropriateSections) {
				wrapInExtendPanel(article.getTitle(), innerResult, result);
				appropriateSections = false;
			}
		}
		renderSectionEnd(result);
	}

	public static JSONObject getTerms(UserContext user) {
		// gathering all terms
		List<String> allTerms = new ArrayList<String>();

		Collection<TerminologyManager> terminologyManagers = KnowWEUtils.getTerminologyManagers(user.getArticleManager());
		for (TerminologyManager terminologyManager : terminologyManagers) {
			Collection<Identifier> allDefinedTerms = terminologyManager
					.getAllDefinedTerms();
			for (Identifier definition : allDefinedTerms) {
				String externalForm = definition.toExternalForm()
						.replaceAll("&", "&amp;")
						.replaceAll("<", "&lt;");
				if (!allTerms.contains(externalForm)) {
					allTerms.add(externalForm);
				}
			}
		}
		JSONObject response = new JSONObject();
		try {
			response.accumulate("allTerms", allTerms);
		}
		catch (JSONException e) {
			e.printStackTrace();
		}
		return response;

	}

	public static Identifier getTermIdentifier(UserContext user, Section<?> section) {
		Map<String, String> urlParameters = user.getParameters();

		// First try the URL-Parameter, if null try the TagHandler-Parameter.
		String objectName = null;
		if (urlParameters.get(OBJECT_NAME) != null) {
			objectName = Strings.decodeURL(urlParameters.get(OBJECT_NAME));
		}

		// If name is not defined stop rendering contents
		// TODO: catch?
		if (Strings.isBlank(objectName)) return null;

		String externalTermIdentifierForm = null;
		if (urlParameters.get(TERM_IDENTIFIER) != null) {
			externalTermIdentifierForm = Strings.decodeURL(urlParameters
					.get(TERM_IDENTIFIER));
		}

		// decode term identifier
		// use object name as identifier for compatibility issues
		Identifier termIdentifier;
		if (externalTermIdentifierForm == null) {
			externalTermIdentifierForm = objectName;
			termIdentifier = Identifier.fromExternalForm(externalTermIdentifierForm);
			objectName = termIdentifier.getLastPathElement();
		}
		else {
			termIdentifier = Identifier.fromExternalForm(externalTermIdentifierForm);
		}
		return termIdentifier;
	}

}