/*
 * Copyright (C) 2026 denkbares GmbH, Germany
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

package de.knowwe.core.showinfo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.denkbares.strings.Identifier;
import com.denkbares.strings.Strings;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.objectinfo.ObjectInfoRenderer;
import de.knowwe.core.preview.PreviewManager;
import de.knowwe.core.user.UserContext;

/**
 * Renders the content of the "Show Info" dialog for one or several terms (elements): the definitions of all elements
 * together, then the references grouped by category. Occurrences that share a preview are combined within their
 * category. Each preview marks the occurrences of all elements in that category.
 * <p>
 * For several elements, the rendered parts are tagged with the elements they belong to (attribute
 * <code>data-elements</code>, the indices of the elements separated by spaces), so the dialog can hide the parts of
 * hidden elements; the previews of the references are rendered later, their elements are given by
 * {@link #getReferenceElements()}.
 *
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 16.11.2026
 */
public class ShowInfoRenderer {

	/**
	 * One of the shown terms, with its definitions and references.
	 */
	private record Element(int index, Identifier identifier, Set<Section<?>> definitions, Set<Section<?>> references) {
	}

	private final UserContext user;
	/**
	 * The section the dialog was opened for (e.g. by its tool menu), or null
	 */
	@Nullable
	private final Section<?> context;
	private final List<Element> elements = new ArrayList<>();
	private final Map<Section<?>, Set<Integer>> definitions = new LinkedHashMap<>();
	private final Map<Section<?>, Set<Integer>> references = new LinkedHashMap<>();

	/**
	 * @param identifiers the terms to show
	 * @param context     the section the dialog was opened for (e.g. by its tool menu), the elements are labeled as
	 *                    rendered there, see {@link #renderLabel(Identifier, RenderResult)}; may be null
	 * @param user        the user
	 */
	public ShowInfoRenderer(@NotNull List<Identifier> identifiers, @Nullable Section<?> context, @NotNull UserContext user) {
		this.user = user;
		this.context = context;
		for (Identifier identifier : new LinkedHashSet<>(identifiers)) {
			Element element = new Element(elements.size(), identifier,
					ObjectInfoRenderer.findTermDefinitionSections(user.getArticleManager(), identifier),
					ObjectInfoRenderer.findTermReferenceSections(user.getArticleManager(), identifier));
			elements.add(element);
			addSections(definitions, element.definitions(), element.index());
			addSections(references, element.references(), element.index());
		}
	}

	private static void addSections(Map<Section<?>, Set<Integer>> elementsBySection, Set<Section<?>> sections, int elementIndex) {
		for (Section<?> section : sections) {
			elementsBySection.computeIfAbsent(section, k -> new TreeSet<>()).add(elementIndex);
		}
	}

	public boolean isMultiple() {
		return elements.size() > 1;
	}

	public List<Identifier> getIdentifiers() {
		return elements.stream().map(Element::identifier).toList();
	}

	/**
	 * Returns the type of the term, e.g. "QuestionOC", or an empty string if it is unknown.
	 */
	public String getTypeName(Identifier identifier) {
		return ObjectInfoRenderer.getTermObjectClassesVerbalization(user, identifier);
	}

	/**
	 * Returns the sections that can be edited in the dialog: the definitions and references of all elements.
	 */
	public Set<Section<?>> getEditableSections() {
		Set<Section<?>> editable = new LinkedHashSet<>(definitions.keySet());
		editable.addAll(references.keySet());
		return editable;
	}

	/**
	 * Returns the elements by reference section ID. Each preview uses the IDs of the references in its category.
	 * Empty for a single element.
	 */
	public Map<String, String> getReferenceElements() {
		if (!isMultiple()) return Map.of();
		Map<String, String> result = new LinkedHashMap<>();
		references.forEach((section, indices) -> result.put(section.getID(), formatElementIndices(indices)));
		return result;
	}

	/**
	 * Returns the value of the attribute <code>data-elements</code> for a part showing the given sections: the indices
	 * of their elements, separated by spaces, or null for a single element.
	 */
	@Nullable
	private String getElementsAttribute(Collection<Section<?>> sections, Map<Section<?>, Set<Integer>> elementsBySection) {
		if (!isMultiple()) return null;
		Set<Integer> indices = new TreeSet<>();
		for (Section<?> section : sections) {
			indices.addAll(elementsBySection.get(section));
		}
		return formatElementIndices(indices);
	}

	private static String formatElementIndices(Set<Integer> indices) {
		return indices.stream().map(String::valueOf).collect(Collectors.joining(" "));
	}

	/**
	 * Renders the title of the dialog: the term for a single element, otherwise the number of elements.
	 */
	public void renderHeader(RenderResult result) {
		if (isMultiple()) {
			result.appendHtmlTag("span", "class", "objectinfo-header-name");
			result.append(CompositeEditOpenDialogAction.TITLE);
			result.appendHtmlTag("/span");
			result.appendHtmlTag("span", "class", "objectinfo-header-class");
			result.append("(" + elements.size() + " elements)");
			result.appendHtmlTag("/span");
		}
		else {
			ObjectInfoRenderer.renderHeader(elements.get(0).identifier(), user, result);
		}
	}

	/**
	 * Renders the element as in the wiki, preferably as in the section the dialog was opened for: e.g. for the tool
	 * menu of "Unit.UI@state" the state question as "Unit.UI@state", the unit as "Unit" and the UI as "UI". Otherwise,
	 * one of its definitions, or one of its references if it has no definition, preferring the ones naming the element
	 * as a whole. Types rendering more than the element itself render the label as {@link ShowInfoLabelRenderer}.
	 */
	public void renderLabel(Identifier identifier, RenderResult result) {
		Element element = elements.stream().filter(e -> e.identifier().equals(identifier)).findFirst().orElseThrow();
		String name = Strings.unquote(identifier.getLastPathElement());
		List<Section<?>> candidates = new ArrayList<>();
		element.definitions().stream().sorted().forEach(candidates::add);
		element.references().stream().sorted().forEach(candidates::add);
		Section<?> section = candidates.stream().filter(candidate -> isAncestor(context, candidate)).findFirst()
				.or(() -> candidates.stream().filter(candidate -> isAncestor(candidate, context)).findFirst())
				.or(() -> candidates.stream()
						.filter(candidate -> Strings.unquote(candidate.getText().trim()).equals(name))
						.findFirst())
				.orElse(candidates.isEmpty() ? null : candidates.get(0));
		if (section == null) {
			result.append(identifier.toPrettyPrint());
		}
		else if (section.get() instanceof ShowInfoLabelRenderer labelRenderer) {
			labelRenderer.renderShowInfoLabel(section, user, result);
		}
		else {
			result.append(section, user);
		}
	}

	/**
	 * Renders the definitions and then the references of all elements.
	 */
	public void renderContent(RenderResult result) {
		renderDefinitions(result);
		renderReferences(result);
	}

	private void renderDefinitions(RenderResult result) {
		ObjectInfoRenderer.renderSectionStart("Definition", result);
		for (Element element : elements) {
			if (!element.definitions().isEmpty()) continue;
			boolean referenced = !element.references().isEmpty();
			result.appendHtmlTag("p", "class", "showinfo-no-definition", "data-elements", getElementsAttribute(element),
					"style", referenced ? "color:red" : "color:#888;font-style:italic");
			if (isMultiple()) result.append(element.identifier().toPrettyPrint() + ": ");
			result.append(referenced ? "No definitions found!" : "Term not defined");
			result.appendHtmlTag("/p");
		}
		Map<Section<?>, Collection<Section<?>>> previews = PreviewManager.getInstance().groupByPreview(definitions.keySet());
		for (Map.Entry<Section<?>, Collection<Section<?>>> entry : previews.entrySet()) {
			Section<?> previewSection = entry.getKey();
			Collection<Section<?>> sections = entry.getValue();
			String elementsAttribute = getElementsAttribute(sections.isEmpty() ? List.of(previewSection) : sections, definitions);
			result.appendHtmlTag("div", "class", "articleName", "data-elements", elementsAttribute);
			result.appendHtml(ObjectInfoRenderer.getNameForSection(user, previewSection));
			result.appendHtmlTag("/div");
			result.appendHtmlTag("div", "class", "previewItem", "data-elements", elementsAttribute);
			ObjectInfoRenderer.renderTermPreview(previewSection, sections, user, "definition", result);
			result.appendHtmlTag("/div");
			// the definitions can be edited in the dialog like the references, see RenderPreviewAction
			result.appendHtmlTag("param", "class", "editanchor", "sectionid", previewSection.getID());
		}
		if (!definitions.isEmpty()) {
			String highlighted = definitions.keySet().stream().map(Section::getID).collect(Collectors.joining(","));
			result.appendHtmlTag("div", "class", "relevantSections hidden", "rel", highlighted);
			result.appendHtmlTag("/div");
		}
		ObjectInfoRenderer.renderSectionEnd(result);
	}

	private void renderReferences(RenderResult result) {
		ObjectInfoRenderer.renderSectionStart("References", result);
		if (references.isEmpty()) {
			result.appendHtml("<p style=\"color:#888;font-style:italic\">");
			result.append("No references found!");
			result.appendHtml("</p>");
		}
		else {
			boolean packages = elements.stream()
					.allMatch(element -> ObjectInfoRenderer.getTermObjectClasses(user, element.identifier())
							.contains(Package.class));
			ObjectInfoRenderer.renderReferences(references.keySet(), packages, user, result,
					sections -> getElementsAttribute(sections, references));
		}
		ObjectInfoRenderer.renderSectionEnd(result);
	}

	/**
	 * Returns whether the ancestor is the section or one of its ancestors.
	 */
	private static boolean isAncestor(@Nullable Section<?> ancestor, @Nullable Section<?> section) {
		if (ancestor == null) return false;
		for (Section<?> current = section; current != null; current = current.getParent()) {
			if (current == ancestor) return true;
		}
		return false;
	}

	@Nullable
	private String getElementsAttribute(Element element) {
		return isMultiple() ? String.valueOf(element.index()) : null;
	}
}
