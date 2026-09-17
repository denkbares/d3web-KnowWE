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

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Set;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.LongPoint;
import org.apache.lucene.document.NumericDocValuesField;
import org.apache.lucene.document.SortedDocValuesField;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.util.BytesRef;
import org.jetbrains.annotations.NotNull;

import de.knowwe.core.wikiConnector.WikiAttachment;

/**
 * Turns one attachment into one document.
 * <p>
 * An attachment is filed under its page: the page key is the parent's, so a page and its attachments are replaced
 * together and the read permission of the page covers them -- JSPWiki decides an attachment's permission by its parent
 * anyway. In the result list an attachment therefore appears inside its page's block, with the file name where a
 * section's heading would be.
 * <p>
 * The text of a file is only read where reading it is a matter of decoding, see {@link #TEXTUAL}. Everything else is
 * findable by name; extracting from PDF or Office would mean pulling Tika into the distribution, which is a decision of
 * its own and not one to smuggle in here.
 *
 * @author Albrecht Striffler (denkbares GmbH) + Claude for wiki-search
 */
public class AttachmentDocumentBuilder {

	/** Extensions whose content is text once decoded. */
	private static final Set<String> TEXTUAL = Set.of(
			"txt", "xml", "properties", "json", "csv", "tsv", "md", "html", "htm", "svg", "yml", "yaml", "log", "d3web");

	/**
	 * Read no further than this. A single attachment must not be able to dominate the index, and the beginning of a file
	 * is what a search needs to find it -- the rest is on the page itself.
	 */
	static final int MAX_CONTENT_CHARS = 200_000;

	public @NotNull Document build(@NotNull WikiAttachment attachment) throws IOException {
		String page = attachment.getParentName();
		String file = attachment.getFileName();
		String key = SectionDocumentBuilder.pageKey(page);

		Document document = new Document();
		document.add(new StringField(SearchFields.PAGE_KEY, key, Field.Store.NO));
		document.add(new SortedDocValuesField(SearchFields.PAGE_KEY_SORT, new BytesRef(key)));
		document.add(new StringField(SearchFields.TYPE, SearchFields.TYPE_ATTACHMENT, Field.Store.YES));

		document.add(new StoredField(SearchFields.TITLE, page));
		document.add(new TextField(SearchFields.TITLE_TEXT, page, Field.Store.NO));
		document.add(new TextField(SearchFields.TITLE_GRAM, page, Field.Store.NO));
		document.add(new StringField(SearchFields.TITLE_EXACT, SectionDocumentBuilder.exactKey(page), Field.Store.NO));

		// the file name takes the place of the heading, so a hit reads as "page > file" like any other
		document.add(new TextField(SearchFields.HEADING, file, Field.Store.NO));
		document.add(new TextField(SearchFields.HEADING_GRAM, file, Field.Store.NO));
		document.add(new TextField(SearchFields.ATTACHMENT_NAMES, file, Field.Store.NO));
		document.add(new TextField(SearchFields.BREADCRUMB, page + " › " + file, Field.Store.YES));

		document.add(new Field(SearchFields.BODY, content(attachment), SearchFields.BODY_TYPE));

		// no section to go back to; the hit points at the file and renders no preview
		document.add(new StoredField(SearchFields.SECTION_ID, ""));
		document.add(new StoredField(SearchFields.SECTION_PATH, attachment.getPath()));
		document.add(new StoredField(SearchFields.ORDINAL, 0));
		document.add(new NumericDocValuesField(SearchFields.ORDINAL, 0));

		long modified = attachment.getDate() == null ? 0 : attachment.getDate().getTime();
		document.add(new LongPoint(SearchFields.LAST_MODIFIED, modified));
		document.add(new NumericDocValuesField(SearchFields.LAST_MODIFIED, modified));
		document.add(new StoredField(SearchFields.LAST_MODIFIED, modified));

		return document;
	}

	/**
	 * @return the file's text, or the empty string for anything we cannot read as text
	 */
	static @NotNull String content(@NotNull WikiAttachment attachment) throws IOException {
		if (!isTextual(attachment.getFileName())) return "";
		StringBuilder text = new StringBuilder();
		try (InputStream stream = attachment.getInputStream()) {
			byte[] buffer = new byte[8192];
			int read;
			while ((read = stream.read(buffer)) > 0 && text.length() < MAX_CONTENT_CHARS) {
				text.append(new String(buffer, 0, read, StandardCharsets.UTF_8));
			}
		}
		return text.length() > MAX_CONTENT_CHARS ? text.substring(0, MAX_CONTENT_CHARS) : text.toString();
	}

	/** An attachment replaces only itself, never the page it hangs on. */
	public static @NotNull String replaceKey(@NotNull String path) {
		return "attachment:" + path.toLowerCase(Locale.ROOT);
	}

	static boolean isTextual(@NotNull String fileName) {
		int dot = fileName.lastIndexOf('.');
		if (dot < 0 || dot == fileName.length() - 1) return false;
		return TEXTUAL.contains(fileName.substring(dot + 1).toLowerCase(Locale.ROOT));
	}
}
