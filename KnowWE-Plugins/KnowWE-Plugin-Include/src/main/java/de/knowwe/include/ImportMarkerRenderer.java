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

package de.knowwe.include;

import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import org.jetbrains.annotations.NotNull;

import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.kdom.rendering.elements.A;
import de.knowwe.core.kdom.rendering.elements.Div;
import de.knowwe.core.kdom.rendering.elements.HtmlElement;
import de.knowwe.core.kdom.rendering.elements.HtmlProvider;
import de.knowwe.core.kdom.rendering.elements.Li;
import de.knowwe.core.kdom.rendering.elements.PlainTextNode;
import de.knowwe.core.kdom.rendering.elements.Span;
import de.knowwe.core.kdom.rendering.elements.Ul;
import de.knowwe.core.user.UserContext;

/**
 * Renders the "imported by" note of a source page or section: one entry per importing wiki, listing its importing
 * pages, each page once, however many of its imports ask for the content.
 *
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 18.11.26
 */
public final class ImportMarkerRenderer {

	/** Up to this many importing pages of a wiki are listed in one line, more are collapsed */
	static final int MAX_INLINE_PAGES = 3;

	private ImportMarkerRenderer() {
	}

	/**
	 * Renders the note for the whole page, as an information box.
	 */
	public static void renderPageNote(@NotNull List<ImportMarker> markers, @NotNull UserContext user, @NotNull RenderResult result) {
		if (markers.isEmpty()) return;
		result.append(createNote(markers, "page", Instant.now(), getLocale(user), ZoneId.systemDefault())
				.clazz("information iwi-imported-by"));
	}

	/**
	 * Renders the note for an imported section, as a small line below its header.
	 */
	public static void renderSectionNote(@NotNull List<ImportMarker> markers, @NotNull UserContext user, @NotNull RenderResult result) {
		if (markers.isEmpty()) return;
		result.append(createNote(markers, "section", Instant.now(), getLocale(user), ZoneId.systemDefault())
				.clazz("iwi-imported-by iwi-imported-by-section"));
	}

	static HtmlElement createNote(List<ImportMarker> markers, String contentName, Instant now, Locale locale, ZoneId zone) {
		List<ImportMarker.WikiImports> wikis = ImportMarker.groupByWiki(markers);
		String intro = "This " + contentName + " is imported by";
		HtmlElement note = new Div().title("Shown until " + ImportMarker.TIME_TO_LIVE.toHours()
				+ " hours after the last import request of the importing wiki.");
		if (wikis.size() == 1) {
			note.children(new Span().clazz("iwi-imported-by-intro").plainText(intro + " "),
					createWikiEntry(wikis.get(0), now, locale, zone));
		}
		else {
			List<HtmlProvider> items = new ArrayList<>();
			for (ImportMarker.WikiImports wiki : wikis) {
				items.add(new Li().children(createWikiEntry(wiki, now, locale, zone)));
			}
			note.children(new Span().clazz("iwi-imported-by-intro").plainText(intro + " " + wikis.size() + " wikis:"),
					new Ul().clazz("iwi-imported-by-wikis").children(items.toArray(HtmlProvider[]::new)));
		}
		return note;
	}

	/**
	 * One wiki: its name, its importing pages and the time of its latest import. If the wiki has more than
	 * {@link #MAX_INLINE_PAGES} importing pages, they are collapsed.
	 */
	private static HtmlElement createWikiEntry(ImportMarker.WikiImports wiki, Instant now, Locale locale, ZoneId zone) {
		// not linked: a link in the summary of a collapsed wiki would navigate instead of expanding, the pages are linked
		HtmlElement wikiName = new HtmlElement("strong").clazz("iwi-imported-by-wiki-name").plainText(wiki.label());
		if (wiki.url() != null) wikiName.title(wiki.url());
		Instant latest = wiki.latestImport();
		HtmlElement time = new Span().clazz("iwi-imported-by-time")
				.title("Last import request: " + InterWikiImportAnnotationFormat.formatDateTime(latest, locale, zone))
				.plainText(InterWikiImportAnnotationFormat.formatRelative(latest, now));

		List<HtmlProvider> pages = new ArrayList<>();
		for (ImportMarker.PageImports page : wiki.pages()) {
			pages.add(createPageEntry(page, locale, zone));
		}
		if (wiki.pages().size() <= MAX_INLINE_PAGES) {
			List<HtmlProvider> children = new ArrayList<>();
			children.add(wikiName);
			String separator = ": ";
			for (HtmlProvider page : pages) {
				children.add(new PlainTextNode(separator));
				children.add(page);
				separator = ", ";
			}
			children.add(new Span().clazz("iwi-imported-by-separator").plainText(" · "));
			children.add(time);
			return new Span().clazz("iwi-imported-by-wiki").children(children.toArray(HtmlProvider[]::new));
		}
		List<HtmlProvider> items = new ArrayList<>();
		for (HtmlProvider page : pages) {
			items.add(new Li().children(page));
		}
		return new HtmlElement("details").clazz("iwi-imported-by-wiki").children(
				new HtmlElement("summary").children(wikiName,
						new PlainTextNode(" on " + wiki.pages().size() + " pages"),
						new Span().clazz("iwi-imported-by-separator").plainText(" · "),
						time),
				new Ul().clazz("iwi-imported-by-pages").children(items.toArray(HtmlProvider[]::new)));
	}

	/**
	 * One importing page, linked if possible, with the number of its imports if there are several. The tooltip lists
	 * what was imported and when.
	 */
	private static HtmlElement createPageEntry(ImportMarker.PageImports page, Locale locale, ZoneId zone) {
		String label = page.label() == null ? "unknown page" : page.label();
		String tooltip = page.markers().stream()
				.map(marker -> marker.getReference() + " (" + InterWikiImportAnnotationFormat.formatDateTime(
						marker.getCreationDate().toInstant(), locale, zone) + ")")
				.collect(Collectors.joining("\n"));
		HtmlElement name = page.link() == null
				? new Span().plainText(label)
				: new A().attributes("href", page.link()).plainText(label);
		name.title(tooltip);
		int count = page.markers().size();
		if (count == 1) return new Span().clazz("iwi-imported-by-page").children(name);
		return new Span().clazz("iwi-imported-by-page").children(name,
				new Span().clazz("iwi-imported-by-count").plainText(" (" + count + " imports)"));
	}

	private static Locale getLocale(UserContext user) {
		try {
			return user.getLocale();
		}
		catch (RuntimeException e) {
			// no request available, e.g. for asynchronous rendering
			return Locale.ENGLISH;
		}
	}
}
