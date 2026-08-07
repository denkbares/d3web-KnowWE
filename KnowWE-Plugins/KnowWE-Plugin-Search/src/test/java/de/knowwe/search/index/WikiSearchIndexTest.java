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
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

/**
 * @author Albrecht Striffler (denkbares GmbH) + Claude for wiki-search
 */
public class WikiSearchIndexTest {

	@Rule
	public final TemporaryFolder folder = new TemporaryFolder();

	@Test
	public void writtenDocumentsAreVisibleWithoutACommit() throws IOException {
		try (WikiSearchIndex index = open()) {
			index.replacePage("Seite", List.of(document("Seite", "Steckverbinder")));
			index.refresh();

			assertEquals(1, count(index, SearchFields.BODY, "steckverbinder"));
		}
	}

	@Test
	public void replacingAPageRemovesItsFormerDocuments() throws IOException {
		try (WikiSearchIndex index = open()) {
			index.replacePage("Seite", List.of(document("Seite", "alt"), document("Seite", "auch alt")));
			index.refresh();
			assertEquals(2, index.documentCount());

			index.replacePage("Seite", List.of(document("Seite", "neu")));
			index.refresh();

			assertEquals(1, index.documentCount());
			assertEquals(0, count(index, SearchFields.BODY, "alt"));
			assertEquals(1, count(index, SearchFields.BODY, "neu"));
		}
	}

	@Test
	public void theKeyIsCaseInsensitiveLikeTheArticleManager() throws IOException {
		try (WikiSearchIndex index = open()) {
			index.replacePage("Seite", List.of(document("Seite", "alt")));
			index.replacePage("SEITE", List.of(document("SEITE", "neu")));
			index.refresh();

			assertEquals("the same page under different spelling must not be indexed twice", 1, index.documentCount());
		}
	}

	@Test
	public void removingAPageRemovesAllOfItsDocuments() throws IOException {
		try (WikiSearchIndex index = open()) {
			index.replacePage("A", List.of(document("A", "eins"), document("A", "zwei")));
			index.replacePage("B", List.of(document("B", "drei")));
			index.refresh();

			index.removePage("A");
			index.refresh();

			assertEquals(1, index.documentCount());
			assertEquals(1, count(index, SearchFields.BODY, "drei"));
		}
	}

	@Test
	public void anIndexOfTheSameSchemaIsReused() throws IOException {
		Path path = folder.getRoot().toPath().resolve("index");
		try (WikiSearchIndex index = new WikiSearchIndex(path)) {
			index.replacePage("Seite", List.of(document("Seite", "bleibt")));
			index.commit();
		}
		try (WikiSearchIndex reopened = new WikiSearchIndex(path)) {
			assertEquals("a compatible index must survive a restart", 1, reopened.documentCount());
		}
	}

	@Test
	public void anIndexOfAForeignSchemaIsDiscarded() throws IOException {
		Path path = folder.getRoot().toPath().resolve("index");
		try (WikiSearchIndex index = new WikiSearchIndex(path)) {
			index.replacePage("Seite", List.of(document("Seite", "veraltet")));
			index.commit();
		}
		// simulate what a changed analyzer or field layout leaves behind
		stampSchemaVersion(path, "someOtherVersion");

		try (WikiSearchIndex reopened = new WikiSearchIndex(path)) {
			assertEquals("an index of an unknown schema must be rebuilt, not silently reused",
					0, reopened.documentCount());
		}
	}

	@Test
	public void aCorruptDirectoryIsRebuiltInsteadOfFailingToStart() throws IOException {
		Path path = folder.getRoot().toPath().resolve("index");
		Files.createDirectories(path);
		Files.writeString(path.resolve("segments_1"), "this is not a lucene index", StandardCharsets.UTF_8);

		try (WikiSearchIndex index = new WikiSearchIndex(path)) {
			index.replacePage("Seite", List.of(document("Seite", "geht wieder")));
			index.refresh();
			assertEquals(1, count(index, SearchFields.BODY, "geht"));
		}
	}

	private WikiSearchIndex open() throws IOException {
		return new WikiSearchIndex(folder.getRoot().toPath().resolve("index"));
	}

	private static Document document(String title, String body) {
		Document document = new Document();
		document.add(new StringField(SearchFields.PAGE_KEY, SectionDocumentBuilder.pageKey(title), Field.Store.NO));
		document.add(new TextField(SearchFields.BODY, body, Field.Store.YES));
		return document;
	}

	private static int count(WikiSearchIndex index, String field, String term) throws IOException {
		return index.search(searcher -> searcher.count(new TermQuery(new Term(field, term))));
	}

	/**
	 * Stamps a foreign schema version into the commit user data, which is exactly what a changed analyzer or field
	 * layout amounts to from the next start's point of view.
	 */
	private static void stampSchemaVersion(Path path, String version) throws IOException {
		assertNotEquals("the test would prove nothing with the current version", version, SearchFields.SCHEMA_VERSION);
		try (Directory directory = FSDirectory.open(path);
			 IndexWriter writer = new IndexWriter(directory, new IndexWriterConfig())) {
			writer.setLiveCommitData(Map.of("schemaVersion", version).entrySet());
			writer.commit();
		}
	}
}
