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
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.StoredFields;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.store.ByteBuffersDirectory;
import org.apache.lucene.store.Directory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.denkbares.plugin.test.InitPluginManager;
import connector.DummyConnector;
import de.knowwe.core.Environment;
import de.knowwe.core.kdom.Article;
import de.knowwe.search.analysis.WikiAnalyzers;
import utils.TestUtils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Indexes real articles into an in memory index and searches them, so the fields, the analyzers and the chunking are
 * proven to work together rather than only in isolation.
 *
 * @author Albrecht Striffler (denkbares GmbH) + Claude for wiki-search
 */
public class SectionDocumentBuilderTest {

	private final SectionDocumentBuilder builder = new SectionDocumentBuilder();
	private Directory directory;
	private IndexWriter writer;

	@Before
	public void setUp() throws IOException {
		InitPluginManager.init();
		if (!Environment.isInitialized()) {
			DummyConnector connector = new DummyConnector();
			connector.setKnowWEExtensionPath(TestUtils.createKnowWEExtensionPath());
			Environment.initInstance(connector);
		}
		directory = new ByteBuffersDirectory();
		writer = new IndexWriter(directory, new IndexWriterConfig(WikiAnalyzers.forIndexing()));
	}

	@After
	public void tearDown() throws IOException {
		writer.close();
		directory.close();
	}

	@Test
	public void aBodyTermFindsExactlyItsOwnSection() throws IOException {
		index("Kabelbaum X-200", """
				!! Montage
				Den Steckverbinder auf Korrosion pruefen.
				!! Lagerung
				Trocken und staubfrei lagern.
				""");

		assertEquals(List.of("Kabelbaum X-200 › Montage"), breadcrumbs(term(SearchFields.BODY, "korrosion")));
		assertEquals(List.of("Kabelbaum X-200 › Lagerung"), breadcrumbs(term(SearchFields.BODY, "staubfrei")));
	}

	@Test
	public void germanInflectionAndUmlautsAreBridged() throws IOException {
		index("Pruefungen", """
				!! Sichtpruefung
				Die Leitungen werden geprüft.
				""");

		assertEquals("plural must find the singular form",
				List.of("Pruefungen › Sichtpruefung"), breadcrumbs(typed(SearchFields.BODY, "Leitung")));
		assertEquals("a query typed without umlauts must reach the indexed word",
				List.of("Pruefungen › Sichtpruefung"), breadcrumbs(typed(SearchFields.BODY, "geprueft")));
	}

	@Test
	public void markupIsFoundBothWaysAndPointsAtItsOwnBlock() throws IOException {
		index("MarkupPage", """
				!! Wissensbasis
				etwas Prosa

				%%Package
				demo
				%
				""");

		assertEquals("with sigil", List.of("MarkupPage › Wissensbasis"), breadcrumbs(term(SearchFields.MARKUP, "%%package")));
		assertEquals("without sigil", List.of("MarkupPage › Wissensbasis"), breadcrumbs(term(SearchFields.MARKUP, "package")));

		// and it is the markup block itself, not the prose chunk before it
		Document hit = first(term(SearchFields.MARKUP, "package"));
		assertTrue("the markup block must carry its own content",
				hit.get(SearchFields.BODY).contains("demo"));
	}

	@Test
	public void theTitleIsSearchableOnEveryChunkOfThePage() throws IOException {
		index("Steckverbinder Handbuch", """
				!! Eins
				a
				!! Zwei
				b
				""");

		assertEquals(2, term(SearchFields.TITLE_TEXT, "handbuch").size());
	}

	@Test
	public void typingAPrefixOfTheTitleAlreadyFinds() throws IOException {
		index("Steckverbinder Handbuch", "!! Eins\na\n");

		assertEquals(1, term(SearchFields.TITLE_GRAM, "steckver").size());
	}

	@Test
	public void replacingAPageReplacesAllOfItsChunksAtOnce() throws IOException {
		index("Wandelbar", """
				!! Alt
				alter Inhalt
				!! Auch alt
				noch mehr
				""");
		assertEquals(2, term(SearchFields.TITLE_TEXT, "wandelbar").size());

		Article changed = register("Wandelbar", "!! Neu\nneuer Inhalt\n");
		writer.updateDocuments(new Term(SearchFields.PAGE_KEY, SectionDocumentBuilder.pageKey("Wandelbar")),
				builder.build(changed));

		assertEquals("the old chunks must be gone", List.of(), breadcrumbs(term(SearchFields.BODY, "alter")));
		assertEquals(List.of("Wandelbar › Neu"), breadcrumbs(term(SearchFields.BODY, "neuer")));
	}

	@Test
	public void everyChunkKeepsBothAnchorsForThePreview() throws IOException {
		index("Anker", "!! Kapitel\nInhalt\n");

		Document hit = first(term(SearchFields.BODY, "inhalt"));
		assertTrue("section id must be stored", !hit.get(SearchFields.SECTION_ID).isBlank());
		assertTrue("position in kdom must be stored as fallback anchor",
				hit.get(SearchFields.SECTION_PATH).matches("\\d+(\\.\\d+)*"));
	}

	private void index(String title, String content) throws IOException {
		writer.addDocuments(builder.build(register(title, content)));
	}

	private static Article register(String title, String content) {
		Environment.getInstance().getArticleManager(Environment.DEFAULT_WEB).registerArticle(title, content);
		return Environment.getInstance().getArticle(Environment.DEFAULT_WEB, title);
	}

	/** Looks up a term verbatim, to assert what is actually in the index. */
	private List<Document> term(String field, String text) throws IOException {
		return search(new TermQuery(new Term(field, text)));
	}

	/** What a user typing into the search box produces: the text run through the query analyzer. */
	private List<Document> typed(String field, String text) throws IOException {
		BooleanQuery.Builder query = new BooleanQuery.Builder();
		try (TokenStream stream = WikiAnalyzers.forQuerying().tokenStream(field, new StringReader(text))) {
			CharTermAttribute attribute = stream.addAttribute(CharTermAttribute.class);
			stream.reset();
			while (stream.incrementToken()) {
				query.add(new TermQuery(new Term(field, attribute.toString())), BooleanClause.Occur.SHOULD);
			}
			stream.end();
		}
		return search(query.build());
	}

	private List<Document> search(Query query) throws IOException {
		writer.commit();
		try (DirectoryReader reader = DirectoryReader.open(directory)) {
			IndexSearcher searcher = new IndexSearcher(reader);
			StoredFields stored = searcher.storedFields();
			List<Document> hits = new ArrayList<>();
			for (ScoreDoc hit : searcher.search(query, 20).scoreDocs) {
				hits.add(stored.document(hit.doc));
			}
			return hits;
		}
	}

	private static List<String> breadcrumbs(List<Document> hits) {
		return hits.stream().map(hit -> hit.get(SearchFields.BREADCRUMB)).toList();
	}

	private static Document first(List<Document> hits) {
		assertTrue("expected at least one hit", !hits.isEmpty());
		return hits.get(0);
	}
}
