/*
 * Copyright (C) 2024 denkbares GmbH, Germany
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

package de.knowwe.core.doc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.jetbrains.annotations.NotNull;

import com.denkbares.strings.Strings;
import de.knowwe.core.kdom.RootType;
import de.knowwe.core.kdom.Types;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.kdom.rendering.Renderer;
import de.knowwe.core.kdom.rendering.elements.HtmlElement;
import de.knowwe.core.kdom.rendering.elements.HtmlNode;
import de.knowwe.core.kdom.rendering.elements.HtmlProvider;
import de.knowwe.core.kdom.rendering.elements.Li;
import de.knowwe.core.kdom.rendering.elements.Span;
import de.knowwe.core.user.UserContext;
import de.knowwe.kdom.defaultMarkup.DefaultMarkup;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupType;

/**
 * Markup to shown documentation for other markups
 *
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 26.07.2024
 */
public class MarkupDocumentationMarkup extends DefaultMarkupType {

	private static final DefaultMarkup MARKUP;

	private static final String ANNOTATION_MARKUP = "markup";

	static {
		MARKUP = new DefaultMarkup("MarkupDocumentation");
		MARKUP.setDocumentation("Markup to show documentation for one or all markups.");
		MARKUP.setTemplate("%%MarkupDocumentation «MarkupName» %");
		MARKUP.addAnnotation(ANNOTATION_MARKUP, false);
	}

	public MarkupDocumentationMarkup() {
		super(MARKUP);
		this.setRenderer(new MarkupDocumentationRenderer());
	}

	private static class MarkupDocumentationRenderer implements Renderer {

		@Override
		public void render(Section<?> section, UserContext user, RenderResult result) {

			String markupName = DefaultMarkupType.getAnnotation(section, ANNOTATION_MARKUP);
			if (Strings.isBlank(markupName)) {
				markupName = DefaultMarkupType.getContent(section);
				if (markupName.matches("\\H+\\h+%\\s+")) {
					markupName = Strings.trimRight(Strings.trimRight(markupName).replaceAll("%$", ""));
				}
			}

			List<DefaultMarkupType> markupTypes = Types.getAllChildrenTypesRecursive(RootType.getInstance())
					.stream()
					.filter(t -> t instanceof DefaultMarkupType)
					.map(t -> (DefaultMarkupType) t)
					.collect(Collectors.groupingBy(DefaultMarkupType::getName))
					.values().stream()
					.map(c -> c.iterator().next())
					.sorted(Comparator.comparing(t -> t.getMarkup().getName()))
					.toList();

			// document all markups, just a big long table...
			if (Strings.isNotBlank(markupName)) {
				String finalMarkupName = markupName;
				Optional<DefaultMarkupType> markupType = markupTypes
						.stream()
						.filter(d -> d.getMarkup().getName().equals(finalMarkupName))
						.findFirst();
				if (markupType.isEmpty()) {
					result.append("Markup ").append(markupName).append(" not found!");
				}
				else {
					generateSingleMarkupDocumentation(markupType.get(), result);
				}
			}
			else {
				result.append("\n%%table-filter");
				result.append("\n%%sortable");
				result.append("\n");

				ArrayList<HtmlElement> children = new ArrayList<>();
				children.add(new HtmlElement("tr").children(
						new HtmlElement("th").content("Markup Name"),
						new HtmlElement("th").content("Description"),
						new HtmlElement("th").content("Annotations")
				));

				for (DefaultMarkupType markupType : markupTypes) {
					children.add(toTableRow(markupType));
					children.add(new HtmlNode("\n"));
				}
				HtmlElement table = new HtmlElement("table").clazz("wikitable markup-documentation-table")
						.children(children.toArray(HtmlProvider[]::new));

				result.append(table);
				result.append("\n/%");
				result.append("\n/%");
				result.append("\n");
			}
		}

		private HtmlElement toTableRow(DefaultMarkupType markupType) {
			HtmlElement tr = new HtmlElement("tr");
			DefaultMarkup markup = markupType.getMarkup();
			HtmlProvider[] brs = getDescriptionContent(markup);
			tr = tr.children(
					new HtmlElement("td").content(markup.getName()),
					new HtmlElement("td").children(brs),
					new HtmlElement("td").children(getAnnotationDescription(markup))
			);
			return tr;
		}

		@NotNull
		private static HtmlProvider[] getDescriptionContent(DefaultMarkup markup) {
			ArrayList<HtmlProvider> children = new ArrayList<>();
			children.add(new Span(markup.getDocumentation()).clazz("markup-description"));
			if (Strings.isNotBlank(markup.getTemplate())) {
				children.add(new HtmlElement("br"));
				children.add(new Span("Template:"));
				children.add(new Span("%%prettify\n{{{\n" + markup.getTemplate() + "}}}\n").clazz("markup-template"));
			}
			return children.toArray(HtmlProvider[]::new);
		}

		private HtmlElement getAnnotationDescription(DefaultMarkup markup) {
			return new HtmlElement("ul").children(Arrays.stream(markup.getAnnotations())
					.map(MarkupDocumentationRenderer::toListItem)
					.toArray(HtmlElement[]::new));
		}

		private static HtmlElement toListItem(DefaultMarkup.Annotation annotation) {
			String documentation = annotation.getDocumentation();
			if (Strings.isBlank(documentation)) {
				return new Li().children(new Span("@" + annotation.getName() + getModifier(annotation)));
			}
			else {
				return new Li().children(new Span(annotation.getName() + getModifier(annotation) + ": "),
						new Span().children(new HtmlNode(annotation.getDocumentation())));
			}
		}

		private void generateSingleMarkupDocumentation(DefaultMarkupType type, RenderResult result) {

			DefaultMarkup markup = type.getMarkup();
			result.append("!!").append(markup.getName()).append("\n\n");

			result.append("! General\n\n");
			result.append(getMarkupDocumentation(markup)).append("\n\n");

			result.append("! Annotations\n\n");

			for (DefaultMarkup.Annotation annotation : markup.getAnnotations()) {
				result.append("* @")
						.append(annotation.getName())
						.append(getModifier(annotation))
						.append(": ")
						.appendHtml(getAnnotationDocumentation(annotation))
						.append("\n");
//				result.append("** Value pattern: ").append(annotation.getPattern()).append("\n");
			}
		}

		private static String getAnnotationDocumentation(DefaultMarkup.Annotation annotation) {
			String documentation = annotation.getDocumentation();
			if (Strings.isBlank(documentation)) {
				documentation = "No documentation available yet.";
			}
			return documentation;
		}

		@NotNull
		private static String getMarkupDocumentation(DefaultMarkup markup) {
			String documentation = markup.getDocumentation();
			if (Strings.isBlank(documentation)) {
				documentation = "No general documentation available yet.";
			}
			if (Strings.isNotBlank(markup.getTemplate())) {
				if (Strings.isNotBlank(documentation)) documentation += "\n\n";
				documentation += "Template:\n\n%%prettify\n{{{\n" + markup.getTemplate() + "\n}}}\n";
			}
			return documentation;
		}

		@NotNull
		private static String getModifier(DefaultMarkup.Annotation annotation) {
			String modifier = "";
			if (annotation.isMandatory()) {
				modifier = " (mandatory";
			}
			if (annotation.isDeprecated()) {
				if (annotation.isMandatory()) {
					modifier += ", deprecated)";
				}
				else {
					modifier = " (deprecated)";
				}
			}
			else if (Strings.isNotBlank(modifier)) {
				modifier += ")";
			}
			return modifier;
		}
	}
}
