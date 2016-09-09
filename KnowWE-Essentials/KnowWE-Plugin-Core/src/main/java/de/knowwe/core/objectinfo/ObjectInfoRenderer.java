/*
 * Copyright (C) 2014 denkbares GmbH, Germany
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.UUID;

import org.json.JSONException;
import org.json.JSONObject;

import com.denkbares.collections.CountingSet;
import com.denkbares.strings.Identifier;
import com.denkbares.strings.Strings;
import com.denkbares.utils.Log;
import de.knowwe.core.Environment;
import de.knowwe.core.compile.terminology.TerminologyManager;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.ArticleComparator;
import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.basicType.PlainText;
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
import de.knowwe.util.Icon;

/**
 * @author stefan
 * @created 09.12.2013
 */
public class ObjectInfoRenderer implements Renderer {

	// Parameter used in the request
	public static final String OBJECT_NAME = "objectname";
	public static final String TERM_IDENTIFIER = "termIdentifier";
	public static final int MAX_NUMBER_BY_TYPE = 50;

	private static final DefaultMarkupRenderer defaultMarkupRenderer = new DefaultMarkupRenderer();

	@Override
	public final synchronized void render(Section<?> section, UserContext userContext, RenderResult result) {

		RenderResult content = new RenderResult(userContext);

		Identifier termIdentifier = getTermIdentifier(userContext, section);
		renderContent(termIdentifier, userContext, content);

		Section<ObjectInfoType> tagNameSection = Sections.successor(
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
		if (termIdentifier != null) {
			// Render
			ObjectInfoRenderer.renderHeader(termIdentifier, user, result);
			//ObjectInfoRenderer.renderLookUpForm(user, result);
			//ObjectInfoRenderer.renderRenamingForm(termIdentifier, user, result);
			ObjectInfoRenderer.renderTermDefinitions(termIdentifier, user, result);
			ObjectInfoRenderer.renderTermReferences(termIdentifier, user, result);
			//ObjectInfoRenderer.renderPlainTextOccurrences(termIdentifier, user, result);
		}
		else {
			ObjectInfoRenderer.renderLookUpForm(user, result);
		}
	}

	/**
	 * Renders the specified list of term references (usually of one specific article). The method renders the previews
	 * of the specified sections, grouped by their preview. Each preview may render one or multiple of the specified
	 * sections.
	 *
	 * @param sections the section to be rendered in their previews
	 * @param user     the user context
	 * @param result   the buffer to render into
	 * @created 29.11.2013
	 */

	public static void renderTermReferencesPreviews(List<Section<?>> sections, UserContext user, RenderResult result) {
//		if (!KnowWEUtils.canView(sections, user)) {
//			result.appendHtml("<i>You are not allowed to view this article.</i>");
//			return;
//		}
		Map<Section<?>, Collection<Section<?>>> groupedByPreview = ObjectInfoRenderer.groupByPreview(sections);
		boolean first = true;
		for (Entry<Section<?>, Collection<Section<?>>> entry : groupedByPreview.entrySet()) {
			Section<?> previewSection = entry.getKey();
			Collection<Section<?>> group = entry.getValue();

			result.appendHtml("<div class='previewItem'>");
			//ObjectInfoRenderer.renderLinkToSection(previewSection, result);
			ObjectInfoRenderer.renderTermPreview(previewSection, group, user, "reference", result);
			String clazz = "editanchor";
			if (first) {
				// we mark the first (and closing) anchors to not interfere with
				// page appends while enabling edit mode
				clazz += " first";
				first = false;
			}

			result.appendHtml("</div>");
			result.appendHtml("<param class='" + clazz + "' sectionid='"
					+ previewSection.getID() + "' />");

		}
	}

	public static void renderLookUpForm(UserContext user, RenderResult result) {
		result.appendHtml("<div>");
		result.appendHtml("<div style=\"display:none\" class=\"objectinfo-terms\" name=\"terms\" >");
		result.appendJSPWikiMarkup(ObjectInfoRenderer.getTerms(user).toString()
				.replaceAll("([^\\\\]\\\"),\\\"", "$1,\n\""));
		result.appendHtml("</div>");
		result.appendHtml("<input type=\"text\" placeholder=\"Look up terms\" size=\"20\" name=\"")
				.append(ObjectInfoRenderer.OBJECT_NAME)
				.appendHtml("\" class=\"objectinfo-search\" />&nbsp;");
		//result.appendHtml("<input type=\"submit\" value=\"Go to\" style=\"display:none\"/>");
		renderSectionEnd(result);
	}

	public static void renderTermDefinitions(Identifier identifier, UserContext user, RenderResult result) {
		Set<Section<?>> definitions = findTermDefinitionSections(user.getWeb(), identifier);
		renderSectionStart("Definition", result);
		if (definitions.isEmpty()) {
			Set<Section<?>> references = findTermReferenceSections(user.getWeb(), identifier);
			if (references.isEmpty()) {
				result.appendHtml("<p style=\"color:#888;font-style:italic\">");
				result.append("Term not defined");
				result.appendHtml("</p>");
			}
			else {
				// Render a warning if there is no definition for the references
				result.appendHtml("<p style=\"color:red;\">");
				result.append("No definitions found!");
				result.appendHtml("</p>");
			}
		}
		else {
			Map<Section<?>, Collection<Section<?>>> groupedByPreview =
					groupByPreview(definitions);
			for (Entry<Section<?>, Collection<Section<?>>> entry : groupedByPreview.entrySet()) {
				Section<?> previewSection = entry.getKey();
				Collection<Section<?>> group = entry.getValue();
				result.appendHtml("<div class='articleName'>");
				result.appendHtml(getSurroundingMarkupName(previewSection).getName());
				result.appendHtml("</div>");
				result.appendHtml("<div class=\"previewItem\">");
				renderTermPreview(previewSection, group, user, "definition", result);
				result.appendHtml("</div>");
			}
		}
		renderSectionEnd(result);
	}

	public static void renderTermReferences(Identifier identifier, UserContext user, RenderResult result) {
		Set<Section<?>> references = findTermReferenceSections(user.getWeb(), identifier);
		Collection<Class<?>> termObjectClasses = getTermObjectClasses(user, identifier);
		renderSectionStart("References", result);
		if (references.isEmpty()) {
			result.appendHtml("<p style=\"color:#888;font-style:italic\">");
			result.append("No references found!");
			result.appendHtml("</p>");
		}
		else {
			if (termObjectClasses.contains(Package.class)) {
				renderGroupedByArticle(references, user, result);
			}
			else {
				renderGroupedByType(user, result, references);
			}
		}
		renderSectionEnd(result);

	}

	private static void renderGroupedByType(UserContext user, RenderResult result, Set<Section<?>> references) {
		Map<Type, List<Section<?>>> typeGroups = groupByType(references);
		for (Entry<Type, List<Section<?>>> typeEntry : typeGroups.entrySet()) {
			List<Section<?>> sectionsOfType = typeEntry.getValue();
			// if we have to many sections of one type, we additionally group by article
			if (sectionsOfType.size() > MAX_NUMBER_BY_TYPE) {
				for (Entry<Article, List<Section<?>>> articleEntry : groupByArticle(sectionsOfType).entrySet()) {
					RenderResult innerResult = new RenderResult(result);
					List<Section<?>> sectionOfArticle = articleEntry.getValue();
					renderTermReferencesPreviewsAsync(sectionOfArticle, user, innerResult);
					wrapInExtendPanel(typeEntry.getKey().getName() + " in " + articleEntry.getKey().getTitle(),
							String.valueOf(sectionOfArticle.size()),
							innerResult, result);
				}
			}
			// just group by type
			else {
				RenderResult innerResult = new RenderResult(result);
				renderTermReferencesPreviewsAsync(sectionsOfType, user, innerResult);
				String info = sectionsOfType.size() > 1 ? String.valueOf(sectionsOfType.size()) : null;
				wrapInExtendPanel(typeEntry.getKey().getName(), info, innerResult, result);
			}
		}
	}

	private static void renderGroupedByArticle(Set<Section<?>> references, UserContext user, RenderResult result) {
		Map<Article, List<Section<?>>> articleGroups = groupByArticle(references);
		for (Entry<Article, List<Section<?>>> articleEntry : articleGroups.entrySet()) {
			RenderResult innerResult = new RenderResult(result);
			for (Entry<Type, List<Section<?>>> typeEntry : groupByType(articleEntry.getValue()).entrySet()) {
				renderTermReferencesPreviewsAsync(typeEntry.getValue(), user, innerResult);
			}
			wrapInExtendPanel(articleEntry.getKey().getTitle(),
					getSurroundingMarkupNames(articleEntry.getValue()), innerResult, result);
		}
	}

	public static void renderSectionStart(String title, RenderResult result) {
		result.appendHtml("<div>");
		result.appendHtml("<div class='sectionStart'>");
		result.appendHtml(title);
		result.appendHtml("</div>");
	}

	public static void renderSectionEnd(RenderResult result) {
		result.appendHtml("</div>\n");
	}

	protected static Set<Section<?>> findTermDefinitionSections(String web, Identifier termIdentifier) {
		TermInfo termInfo = TermUtils.getTermInfo(web, termIdentifier, false);
		Set<Section<?>> sections = new HashSet<>();
		if (termInfo == null) {
			return sections;
		}
		for (TerminologyManager termManager : termInfo) {
			sections.addAll(termManager.getTermDefiningSections(termIdentifier));
		}
		return sections;
	}

	public static Set<Section<?>> findTermReferenceSections(String web, Identifier termIdentifier) {
		TermInfo termInfo = TermUtils.getTermInfo(web, termIdentifier, false);
		Set<Section<?>> sections = new HashSet<>();
		if (termInfo == null) {
			return sections;
		}
		for (TerminologyManager termManager : termInfo) {
			sections.addAll(termManager.getTermReferenceSections(termIdentifier));
		}
		return sections;
	}

	private static String getTermObjectClassesVerbalization(UserContext user, Identifier identifier) {
		TreeSet<String> termClasses = new TreeSet<>();
		for (Class<?> termObjectClass : getTermObjectClasses(user, identifier)) {
			termClasses.add(termObjectClass.getSimpleName());
		}
		return Strings.concat(", ", termClasses);
	}

	private static Collection<Class<?>> getTermObjectClasses(UserContext user, Identifier identifier) {
		Set<Class<?>> termClasses = new HashSet<>();
		for (TerminologyManager manager : KnowWEUtils.getTerminologyManagers(KnowWEUtils.getArticleManager(user.getWeb()))) {
			termClasses.addAll(manager.getTermClasses(identifier));
		}
		return termClasses;
	}

	public static void renderHeader(Identifier identifier, UserContext user, RenderResult result) {
		result.appendHtmlTag("span", "id", "objectinfo-src", "class", "objectinfo-header-name");
		result.append(identifier.toExternalForm());
		result.appendHtmlTag("/span");
		result.appendHtmlTag("span", "class", "objectinfo-header-class");
		result.append("(" + getTermObjectClassesVerbalization(user, identifier) + ")");
		result.appendHtmlTag("/span");
	}

	protected static String getRenamingAction() {
		return "TermRenamingAction";
	}

	private static Map<Article, List<Section<?>>> groupByArticle(Collection<Section<?>> references) {
		Map<Article, List<Section<?>>> result =
				new TreeMap<>(ArticleComparator.getInstance());
		for (Section<?> reference : references) {
			Article article = reference.getArticle();
			List<Section<?>> existingReferences = result.get(article);
			if (existingReferences == null) {
				existingReferences = new LinkedList<>();
			}
			existingReferences.add(reference);
			result.put(article, existingReferences);
		}

		return result;
	}

	private static Map<Type, List<Section<? extends Type>>> groupByType(Collection<Section<?>> references) {
		Map<Type, List<Section<? extends Type>>> result = new TreeMap<>(
				Comparator.comparing(type -> type.getName() == null ? "" : type.getName()));
		for (Section<?> reference : references) {
			Type surroundingMarkupType = getSurroundingMarkupName(reference);
			List<Section<? extends Type>> sectionsForType = result.get(surroundingMarkupType);
			if (sectionsForType == null) {
				sectionsForType = new LinkedList<>();
			}
			sectionsForType.add(reference);
			result.put(surroundingMarkupType, sectionsForType);
		}
		return result;
	}

	/**
	 * Groups the specified sections by the ancestor section to be rendered as a preview. If a section has no ancestor
	 * to be rendered, the section itself will be used as a group with an empty collection of grouped sections.
	 *
	 * @param items list of sections to be grouped
	 * @return the groups of sections
	 * @created 16.08.2013
	 */
	private static Map<Section<?>, Collection<Section<?>>> groupByPreview(Collection<Section<?>> items) {
		List<Section<?>> list = new ArrayList<>(items);
		Collections.sort(list, KDOMPositionComparator.getInstance());
		Map<Section<?>, Collection<Section<?>>> result = new LinkedHashMap<>();
		for (Section<?> section : list) {
			Section<?> previewSection = PreviewManager.getInstance().getPreviewAncestor(section);
			// handle if the section has no preview renderer
			if (previewSection == null) {
				result.put(section, Collections.emptyList());
				continue;
			}
			// otherwise add section to preview group
			// or create group if it is new
			Collection<Section<?>> group = result.get(previewSection);
			if (group == null) {
				group = new LinkedList<>();
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
		result.append(getSurroundingMarkupName(reference).getName());
		// html.append(")");
		result.appendHtml("</a>");
	}

	private static Type getSurroundingMarkupName(Section<?> section) {
		if (section.get() instanceof DefaultMarkupType) return section.get();
		Section<?> root = Sections.ancestor(section, DefaultMarkupType.class);
		if (root != null) return root.get();
		root = Sections.ancestor(section, TagHandlerType.class);
		if (root != null) return root.get();
		return section.get();
	}

	public static void renderTermPreview(Section<?> previewSection, Collection<Section<?>> relevantSubSections, UserContext user, String cssClass, RenderResult result) {
		int count = relevantSubSections.size();
		if (count == 0) {
			result.appendHtml(KnowWEUtils.getLinkHTMLToSection(previewSection));
			return;
		}

		// if (count > 1) {
		// result.append(" (").append(count).append(" occurences)");
		// }

		result.appendHtml("<div class='objectinfo preview defaultMarkupFrame ").append(cssClass).appendHtml("'>");
		result.appendHtml("<div class='objectinfo type_")
				.append(previewSection.get().getName()).appendHtml(" markupHeaderFrame headerMenu'>");
		result.appendHtml("<div class='markupHeader'>");
		result.appendHtml(previewSection.getTitle());
		result.appendHtml("</div>");
		result.appendHtml("<div class='markupMenu'>");
		result.appendHtml("<div class='markupMenuItem'>");
		result.appendHtml("<a class='markupMenuItem' href='" + KnowWEUtils.getURLLink(previewSection) + "' onclick='_CE.disable();'>");
		result.appendHtml(Icon.LINK.fixWidth().toHtml());
		result.appendHtml("<span>Open</span></a>");
		result.appendHtml("</div>");
		result.appendHtml("</div>");

		result.appendHtml("</div>");

		if (KnowWEUtils.canView(previewSection, user)) {
			// render the preview content part, avoiding double returns
			PreviewRenderer renderer = PreviewManager.getInstance().getPreviewRenderer(previewSection);
			RenderResult part = new RenderResult(result);
			renderer.render(previewSection, relevantSubSections, user, part);
			result.appendAvoidParagraphs(part);
		}
		else {
			result.appendHtml("<i>You don't have read access on the article containing this section.</i>");
		}

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
	 * Get a counting set of all markup names that surrounds the specified list of sections. The count is not the
	 * number
	 * of sections contained in a specific markup, but it is the number of preview sections required to display these
	 * sections (some preview sections may display multiple of the specified sections).
	 *
	 * @param sections the section to get the markup names for
	 * @return the counting set of markup names
	 * @created 29.11.2013
	 */
	private static CountingSet<Type> getSurroundingMarkupNames(List<Section<?>> sections) {
		CountingSet<Type> types = new CountingSet<>();
		Map<Section<?>, Collection<Section<?>>> groupedByPreview = groupByPreview(sections);
		for (Section<?> preview : groupedByPreview.keySet()) {
			types.add(getSurroundingMarkupName(preview));
		}
		return types;
	}

	private static void wrapInExtendPanel(String surroundingMarkupType, CountingSet<Type> occurrences, RenderResult content, RenderResult result) {
		StringBuilder info = new StringBuilder();
		for (Type occurrence : occurrences) {
			if (info.length() > 0) info.append(", ");
			int count = occurrences.getCount(occurrence);
			if (count > 1) info.append(count).append("&times; ");
			info.append(occurrence.getName());
		}
		wrapInExtendPanel(surroundingMarkupType, info.toString(), content, result);
	}

	private static void wrapInExtendPanel(String surroundingMarkupType, RenderResult content, RenderResult result) {
		wrapInExtendPanel(surroundingMarkupType, (String) null, content, result);
	}

	private static void wrapInExtendPanel(String surroundingMarkupType, String info, RenderResult content, RenderResult result) {
		result.appendHtml("<p class=\"show-extend pointer extend-panel-right\" >");
		result.appendHtml("<strong>");
		result.appendHtml(surroundingMarkupType);
		result.appendHtml("</strong>");
		if (!Strings.isBlank(info)) {
			result.appendHtml("<span class='typeInfo'>");
			result.append(" (").append(info).append(")");
			result.appendHtml("</span>");
		}
		result.appendHtml("</p>");
		result.appendHtml("<div class=\"objectInfoPanel hidden\" style=\"display:none !important\">");
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
					if (!article.getRootSection().getText().endsWith(textAfter)) {
						innerResult.appendHtml("...");
					}
					innerResult.appendHtml("</pre>");
					innerResult.appendHtml("</li>");
				}
			}
			innerResult.appendHtml("</ul>");
			// append the html only if there are appropriate sections!
			if (appropriateSections) {
				//wrapInExtendPanel(article.getTitle(), innerResult, result);
				appropriateSections = false;
			}
		}
		renderSectionEnd(result);
	}

	public static JSONObject getTerms(UserContext user) {
		// gathering all terms
		Set<String> allTerms = new HashSet<>();

		Collection<TerminologyManager> terminologyManagers = KnowWEUtils.getTerminologyManagers(user
				.getArticleManager());
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
			Log.severe("Exception while writing available terms to JSON", e);
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