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

import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexOptions;

/**
 * The field names of the section index, in one place, versioned together with {@link #SCHEMA_VERSION}.
 *
 * @author Albrecht Striffler (denkbares GmbH) + Claude for wiki-search
 */
public final class SearchFields {

	/**
	 * Bump whenever fields, their types or the analyzers change. The index directory carries this, and it is written
	 * into the commit user data, so a mismatch forces a rebuild instead of returning quietly wrong results.
	 */
	public static final String SCHEMA_VERSION = "v2";

	/**
	 * Lower cased page title. This is the <b>update and delete key</b>: replacing a page means replacing all of its
	 * chunk documents in one {@code updateDocuments} call. Lower cased because KnowWE's article manager treats titles
	 * case insensitively.
	 */
	public static final String PAGE_KEY = "pageKey";

	/** {@code section} or {@code attachment}. */
	public static final String TYPE = "type";

	/**
	 * What a write replaces, as opposed to what a document belongs to.
	 * <p>
	 * A section's is its page: re-indexing a page replaces its sections. An attachment's is its own path, because
	 * re-indexing the page it hangs on must not delete it -- it shares the page key for permissions and grouping, and
	 * deleting by that key would take the attachments with it.
	 */
	public static final String REPLACE_KEY = "replaceKey";
	public static final String TYPE_SECTION = "section";
	public static final String TYPE_ATTACHMENT = "attachment";

	/** Page title in its display form. Stored only. */
	public static final String TITLE = "title";
	/** Page title, analyzed for scoring. */
	public static final String TITLE_TEXT = "titleText";
	/** Page title, edge grammed for as-you-type. */
	public static final String TITLE_GRAM = "titleGram";

	/** The heading of this chunk alone. */
	public static final String HEADING = "heading";
	/** The heading, edge grammed for as-you-type. */
	public static final String HEADING_GRAM = "headingGram";
	/** {@code Page › H1 › H2}, both displayed and searchable. */
	public static final String BREADCRUMB = "breadcrumb";

	/** The readable text of the chunk. Stored with offsets, because the snippets are highlighted from it. */
	public static final String BODY = "body";

	/** Markup and annotation names such as {@code %%Question} and {@code @file}, matched at low weight. */
	public static final String MARKUP = "markup";

	public static final String AUTHOR = "author";
	public static final String ATTACHMENT_NAMES = "attachmentNames";

	/** Primary anchor for resolving the live section again when rendering a preview. */
	public static final String SECTION_ID = "sectionId";
	/**
	 * {@code Section.getPositionInKDOM()} joined by dots. The fallback anchor, because section ids are not durable
	 * across a reparse or a restart.
	 */
	public static final String SECTION_PATH = "sectionPath";
	/** Position of the chunk within its page; stable tie breaker. */
	public static final String ORDINAL = "ordinal";

	public static final String LAST_MODIFIED = "lastModified";

	/** Only set when the page carries an ACL at all, which lets the common case be filtered cheaply. */
	public static final String HAS_ACL = "hasAcl";
	/** One value per principal allowed to view, encoded as {@code Kind:name}. */
	public static final String ACL_VIEW = "aclView";

	/** Doc values twin of {@link #PAGE_KEY}, so hits can later be grouped by page in Lucene rather than in Java. */
	public static final String PAGE_KEY_SORT = "pageKeySort";

	/**
	 * Stored, with postings offsets so {@code UnifiedHighlighter} can build snippets without re-analysing the whole
	 * text per hit. Deliberately not term vectors: they would roughly double the index for no gain here.
	 */
	public static final FieldType BODY_TYPE = new FieldType(TextField.TYPE_STORED);

	static {
		BODY_TYPE.setIndexOptions(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS_AND_OFFSETS);
		BODY_TYPE.freeze();
	}

	private SearchFields() {
	}
}
