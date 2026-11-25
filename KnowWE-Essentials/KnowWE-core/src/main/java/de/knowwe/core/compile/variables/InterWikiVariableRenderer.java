/*
 * Copyright (C) 2026 denkbares GmbH, Germany
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package de.knowwe.core.compile.variables;

import java.util.List;

import de.knowwe.core.compile.packaging.PackageManager;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.kdom.rendering.elements.A;
import de.knowwe.core.kdom.rendering.elements.Div;
import de.knowwe.core.kdom.rendering.elements.HtmlElement;
import de.knowwe.core.kdom.rendering.elements.Span;
import de.knowwe.core.user.UserContext;
import de.knowwe.kdom.defaultMarkup.AnnotationType;
import de.knowwe.kdom.defaultMarkup.DefaultMarkup;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupRenderer;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupType;

import static de.knowwe.core.kdom.parsing.Sections.$;

/**
 * Renders the definition of a variable as labeled rows instead of its annotations: the name of the variable, which is
 * a term with the usual tools, the wiki it stands for as a link, and the display name if there is one. The packages
 * are rendered below as usual. An incomplete definition is rendered with all its annotations.
 *
 * @author Albrecht Striffler (denkbares GmbH) + Claude
 * @created 25.11.2026
 */
public class InterWikiVariableRenderer extends DefaultMarkupRenderer {

	@Override
	protected void renderAnnotations(Section<? extends DefaultMarkupType> markupSection, List<Section<AnnotationType>> annotations, UserContext user, RenderResult result) {
		Section<InterWikiVariableMarkup> markup = $(markupSection).closest(InterWikiVariableMarkup.class).getFirst();
		Section<?> name = DefaultMarkupType.getAnnotationContentSection(markupSection, InterWikiVariableMarkup.NAME_ANNOTATION);
		String value = markup == null ? null : InterWikiVariableMarkup.getVariableValue(markup);
		if (markup == null || name == null || value == null) {
			super.renderAnnotations(markupSection, annotations, user, result);
			return;
		}

		String displayName = InterWikiVariableMarkup.getDisplayName(markup);
		String link = value.contains("://") ? value : "https://" + value;
		HtmlElement rows = new Div().style("display:grid;grid-template-columns:max-content 1fr;column-gap:1em;"
				+ "row-gap:2px;white-space:normal");
		// the name as the term it is, with its tools
		rows.children(label(markup, InterWikiVariableMarkup.NAME_ANNOTATION, "Name"), new Span().children(content -> content.append(name, user)));
		rows.children(label(markup, InterWikiVariableMarkup.VALUE_ANNOTATION, "Value"), new Span().children(new A().attributes("href", link).plainText(value)));
		if (displayName != null) {
			rows.children(label(markup, InterWikiVariableMarkup.DISPLAY_NAME_ANNOTATION, "Display name"), new Span().plainText(displayName));
		}
		result.append(rows);

		// packages and the variants compiling this definition, as for any markup
		super.renderAnnotations(markupSection, $(annotations)
				.filter(a -> PackageManager.PACKAGE_ATTRIBUTE_NAME.equalsIgnoreCase(a.get().getName())).asList(), user, result);
	}

	/**
	 * The label of an annotation, with the documentation of the annotation as its tooltip, which is the text also
	 * shown for the annotation by the auto-completion.
	 */
	private static HtmlElement label(Section<InterWikiVariableMarkup> markup, String annotationName, String text) {
		HtmlElement label = new Span().style("color:grey").plainText(text);
		DefaultMarkup.Annotation annotation = markup.get().getMarkup().getAnnotation(annotationName);
		String documentation = annotation == null ? null : annotation.getDocumentation();
		if (documentation != null && !documentation.isBlank()) {
			// the documentation may contain html, a title must not
			label.clazz("tooltipster").title(documentation.replaceAll("<[^>]+>", "").replaceAll("\\s+", " ").trim());
		}
		return label;
	}
}
