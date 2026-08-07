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

package de.knowwe.search.index;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.jetbrains.annotations.NotNull;

import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.jspwiki.types.HeaderType;
import de.knowwe.kdom.defaultMarkup.AnnotationContentType;
import de.knowwe.kdom.defaultMarkup.AnnotationNameType;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupType;
import de.knowwe.kdom.defaultMarkup.UnknownAnnotationType;

/**
 * Turns the KDOM of a chunk into readable text plus a bag of markup names.
 * <p>
 * This is what makes the snippets legible: JSPWiki's own provider indexes the raw page source, so its fragments are
 * full of {@code %%}, {@code @annotation} and table pipes. Here the syntax is dropped and only what a reader would read
 * is kept, while the markup vocabulary is preserved separately so {@code %%Question} and {@code @file} stay findable.
 * <p>
 * The rules follow the KDOM as it actually is: prose sits in {@code ParagraphType › WikiTextType} leaves, while a
 * markup's own {@code PlainText} children carry the {@code %%Name} and {@code %} delimiters and must not be indexed as
 * text.
 *
 * @author Albrecht Striffler (denkbares GmbH) + Claude for wiki-search
 */
public class KdomTextExtractor {

	public @NotNull ExtractedText extract(@NotNull Collection<Section<?>> sections) {
		StringBuilder body = new StringBuilder();
		Set<String> markupTokens = new LinkedHashSet<>();
		for (Section<?> section : sections) {
			visit(section, body, markupTokens);
		}
		return new ExtractedText(normalize(body), Set.copyOf(markupTokens));
	}

	private void visit(Section<?> section, StringBuilder body, Set<String> markupTokens) {
		if (section.get() instanceof HeaderType) {
			// the heading is indexed in its own field, and its text still carries the "!!" markers
			return;
		}
		if (section.get() instanceof DefaultMarkupType markupType) {
			visitMarkup(section, markupType, body, markupTokens);
			return;
		}
		List<Section<? extends de.knowwe.core.kdom.Type>> children = section.getChildren();
		if (children.isEmpty()) {
			body.append(section.getText()).append('\n');
			return;
		}
		for (Section<?> child : children) {
			visit(child, body, markupTokens);
		}
	}

	private void visitMarkup(Section<?> section, DefaultMarkupType markupType,
							 StringBuilder body, Set<String> markupTokens) {
		markupTokens.add("%%" + markupType.getName());

		// annotation names, both the declared ones and those the markup does not know
		for (Section<AnnotationNameType> name : Sections.successors(section, AnnotationNameType.class)) {
			markupTokens.add("@" + name.get().getName(name));
		}
		for (Section<UnknownAnnotationType> unknown : Sections.successors(section, UnknownAnnotationType.class)) {
			String name = UnknownAnnotationType.getName(unknown);
			if (name != null) markupTokens.add("@" + name);
		}

		// the payload a reader sees: the markup content plus every annotation value
		String content = DefaultMarkupType.getContent(section);
		if (content != null) body.append(content).append('\n');
		for (Section<? extends AnnotationContentType> annotation :
				DefaultMarkupType.getAllAnnotationContentSections(section)) {
			body.append(annotation.getText()).append('\n');
		}
	}

	/**
	 * Collapses the whitespace that the line by line collection leaves behind, so snippets do not start with blank
	 * lines and term positions stay meaningful.
	 */
	private static String normalize(StringBuilder body) {
		return body.toString().replaceAll("[ \\t]+", " ").replaceAll("\\s*\\n\\s*", "\n").trim();
	}
}
