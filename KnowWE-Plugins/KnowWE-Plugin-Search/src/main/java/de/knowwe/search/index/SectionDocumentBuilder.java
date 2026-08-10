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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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

import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.parsing.Section;

/**
 * Turns an article into the Lucene documents of the section index, one per {@link IndexChunk}.
 * <p>
 * All documents of a page share {@link SearchFields#PAGE_KEY}, which makes replacing a page a single
 * {@code updateDocuments(new Term(PAGE_KEY, key), docs)} — delete and re-add in one atomic step, so there is never a
 * moment in which the page is missing from the results.
 *
 * @author Albrecht Striffler (denkbares GmbH) + Claude for wiki-search
 */
public class SectionDocumentBuilder {

	private final ArticleChunker chunker;
	private final KdomTextExtractor extractor;

	public SectionDocumentBuilder() {
		this(new ArticleChunker(), new KdomTextExtractor());
	}

	public SectionDocumentBuilder(@NotNull ArticleChunker chunker, @NotNull KdomTextExtractor extractor) {
		this.chunker = chunker;
		this.extractor = extractor;
	}

	/** The key under which every document of this page is stored, and by which they are all replaced. */
	/**
	 * A page name reduced to what a reader typing it would produce: lower case, and any run of punctuation or spaces as
	 * a single space. So "Cable Nr-24", "cable nr 24" and "Cable  Nr-24" all arrive as the same key, while
	 * "Cable Nr-24 Overview" does not.
	 * <p>
	 * Used for {@link SearchFields#TITLE_EXACT} on both sides -- if the index and the query normalised differently, the
	 * field would simply never match and nobody would notice.
	 */
	public static @NotNull String exactKey(@NotNull String title) {
		return title.toLowerCase(Locale.ROOT).replaceAll("[^\\p{L}\\p{N}]+", " ").trim();
	}

	public static @NotNull String pageKey(@NotNull String title) {
		return title.toLowerCase(Locale.ROOT);
	}

	public @NotNull List<Document> build(@NotNull Article article) {
		return build(article, 0L);
	}

	public @NotNull List<Document> build(@NotNull Article article, long lastModified) {
		String title = article.getTitle();
		List<Document> documents = new ArrayList<>();
		for (IndexChunk chunk : chunker.chunk(article)) {
			ExtractedText text = extractor.extract(chunk.sections());
			// a heading is worth a document for its title alone, anything else needs content
			if (text.isEmpty() && chunk.heading() == null) continue;
			documents.add(build(title, chunk, text, lastModified));
		}
		return documents;
	}

	private Document build(String title, IndexChunk chunk, ExtractedText text, long lastModified) {
		Document document = new Document();
		String key = pageKey(title);

		document.add(new StringField(SearchFields.PAGE_KEY, key, Field.Store.NO));
		document.add(new SortedDocValuesField(SearchFields.PAGE_KEY_SORT, new BytesRef(key)));
		document.add(new StringField(SearchFields.TYPE, SearchFields.TYPE_SECTION, Field.Store.YES));

		document.add(new StoredField(SearchFields.TITLE, title));
		document.add(new TextField(SearchFields.TITLE_TEXT, title, Field.Store.NO));
		document.add(new TextField(SearchFields.TITLE_GRAM, title, Field.Store.NO));
		document.add(new StringField(SearchFields.TITLE_EXACT, exactKey(title), Field.Store.NO));

		if (chunk.heading() != null) {
			document.add(new TextField(SearchFields.HEADING, chunk.heading(), Field.Store.NO));
			document.add(new TextField(SearchFields.HEADING_GRAM, chunk.heading(), Field.Store.NO));
		}
		String breadcrumb = chunk.breadcrumb(title);
		document.add(new TextField(SearchFields.BREADCRUMB, breadcrumb, Field.Store.YES));

		document.add(new Field(SearchFields.BODY, text.body(), SearchFields.BODY_TYPE));
		if (!text.markupTokens().isEmpty()) {
			document.add(new TextField(SearchFields.MARKUP, String.join(" ", text.markupTokens()), Field.Store.NO));
		}

		Section<?> anchor = chunk.anchor();
		document.add(new StoredField(SearchFields.SECTION_ID, anchor.getID()));
		document.add(new StoredField(SearchFields.SECTION_PATH, positionPath(anchor)));

		document.add(new StoredField(SearchFields.ORDINAL, chunk.ordinal()));
		document.add(new NumericDocValuesField(SearchFields.ORDINAL, chunk.ordinal()));

		document.add(new LongPoint(SearchFields.LAST_MODIFIED, lastModified));
		document.add(new NumericDocValuesField(SearchFields.LAST_MODIFIED, lastModified));
		document.add(new StoredField(SearchFields.LAST_MODIFIED, lastModified));

		return document;
	}

	/**
	 * {@code Section.getPositionInKDOM()} as a dotted path. Section ids are not durable across a reparse or a restart,
	 * so this is what lets a hit find its section again; see {@code SectionAnchor}.
	 */
	static @NotNull String positionPath(@NotNull Section<?> section) {
		StringBuilder path = new StringBuilder();
		for (Integer position : section.getPositionInKDOM()) {
			if (!path.isEmpty()) path.append('.');
			path.append(position);
		}
		return path.toString();
	}
}
