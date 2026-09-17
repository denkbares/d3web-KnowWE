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

package de.knowwe.search.query;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.NumericDocValuesField;
import org.apache.lucene.document.SortedDocValuesField;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.util.BytesRef;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.denkbares.plugin.test.InitPluginManager;
import connector.DummyConnector;
import de.knowwe.core.Environment;
import de.knowwe.core.kdom.Article;
import de.knowwe.search.index.SearchFields;
import de.knowwe.search.index.SectionDocumentBuilder;
import de.knowwe.search.index.WikiSearchIndex;
import utils.TestUtils;

import static de.knowwe.search.query.SearchRequest.Scope.ATTACHMENTS;
import static de.knowwe.search.query.SearchRequest.Scope.CONTENT;
import static de.knowwe.search.query.SearchRequest.Scope.TITLES;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Where a search looks: page names, page content, attachments -- in any combination.
 * <p>
 * These were two flags that excluded each other, {@code titleOnly} and {@code attachmentsOnly}, and the interface showed
 * them as two checkboxes a reader could tick both of. That combination meant something ("attachments, by name only")
 * that no label ever mentioned, while the useful combinations -- pages <i>and</i> attachments, or the text without the
 * names -- could not be expressed at all.
 *
 * @author Albrecht Striffler (denkbares GmbH) + Claude for wiki-search
 */
public class SearchScopeTest {

	@Rule
	public final TemporaryFolder folder = new TemporaryFolder();

	private final SectionDocumentBuilder documents = new SectionDocumentBuilder();
	private WikiSearchIndex index;
	private WikiSearcher searcher;

	@Before
	public void setUp() throws IOException {
		InitPluginManager.init();
		if (!Environment.isInitialized()) {
			DummyConnector connector = new DummyConnector();
			connector.setKnowWEExtensionPath(TestUtils.createKnowWEExtensionPath());
			Environment.initInstance(connector);
		}
		index = new WikiSearchIndex(folder.getRoot().toPath().resolve("index"));
		searcher = new WikiSearcher(index);

		page("Zwirbelkontakt", "!! Montage\nDen Stecker auf Korrosion pruefen.\n");
		attachment("Zwirbelkontakt", "pruefplan.txt", "Korrosion an der Kontaktflaeche");
	}

	@After
	public void tearDown() throws IOException {
		index.close();
	}

	@Test
	public void bothPartsOfAPageAreSearchedByDefault() throws IOException {
		assertEquals(List.of("Zwirbelkontakt"), titles(search("Zwirbelkontakt", SearchRequest.DEFAULT_SCOPES)));
		assertEquals(List.of("Zwirbelkontakt"), titles(search("Korrosion", SearchRequest.DEFAULT_SCOPES)));
	}

	@Test
	public void namesAloneDoNotMatchWhatIsWrittenOnThePage() throws IOException {
		assertEquals("the name still matches", List.of("Zwirbelkontakt"), titles(search("Zwirbelkontakt", Set.of(TITLES))));
		assertTrue("a word from the text must not", search("Korrosion", Set.of(TITLES)).isEmpty());
	}

	@Test
	public void contentAloneDoesNotMatchTheName() throws IOException {
		assertEquals("a word from the text still matches", List.of("Zwirbelkontakt"),
				titles(search("Korrosion", Set.of(CONTENT))));
		assertTrue("the page name must not", search("Zwirbelkontakt", Set.of(CONTENT)).isEmpty());
	}

	@Test
	public void attachmentsStayOutUnlessTheyAreAskedFor() throws IOException {
		assertEquals("only the page", 1, search("Korrosion", SearchRequest.DEFAULT_SCOPES).size());
		assertEquals("only the attachment", 1, search("Korrosion", Set.of(ATTACHMENTS)).size());
		assertTrue("and it is the attachment", search("Korrosion", Set.of(ATTACHMENTS)).get(0).attachment());
	}

	@Test
	public void pagesAndAttachmentsCanBeSearchedTogether() throws IOException {
		// the combination the two flags could not express at all
		List<SearchHit> hits = search("Korrosion", Set.of(TITLES, CONTENT, ATTACHMENTS));

		assertEquals(2, hits.size());
		assertEquals("one of each kind", 1, hits.stream().filter(SearchHit::attachment).count());
	}

	@Test
	public void anAttachmentIsFoundByItsFileName() throws IOException {
		// the file name is the attachment's heading, so it counts as a name and not as content
		assertEquals(1, search("pruefplan", Set.of(TITLES, ATTACHMENTS)).size());
		assertEquals(1, search("pruefplan", Set.of(ATTACHMENTS)).size());
	}

	@Test
	public void anEmptyChoiceSearchesThePages() {
		assertEquals(SearchRequest.DEFAULT_SCOPES, new SearchRequest("x", false, 0, 10, Set.of()).scopes());
	}

	private List<SearchHit> search(String query, Set<SearchRequest.Scope> scopes) throws IOException {
		return searcher.search(new SearchRequest(query, false, 0, 10, scopes)).hits();
	}

	private static List<String> titles(List<SearchHit> hits) {
		return hits.stream().map(SearchHit::title).toList();
	}

	private void page(String title, String content) throws IOException {
		Environment.getInstance().getArticleManager(Environment.DEFAULT_WEB).registerArticle(title, content);
		Article article = Environment.getInstance().getArticle(Environment.DEFAULT_WEB, title);
		index.replacePage(title, documents.build(article));
		index.refresh();
	}

	/** The fields AttachmentDocumentBuilder writes, without needing a wiki that can hand out attachments. */
	private void attachment(String page, String file, String text) throws IOException {
		Document document = new Document();
		String key = SectionDocumentBuilder.pageKey(page);
		document.add(new StringField(SearchFields.PAGE_KEY, key, Field.Store.NO));
		document.add(new SortedDocValuesField(SearchFields.PAGE_KEY_SORT, new BytesRef(key)));
		document.add(new StringField(SearchFields.TYPE, SearchFields.TYPE_ATTACHMENT, Field.Store.YES));
		document.add(new StoredField(SearchFields.TITLE, page));
		document.add(new TextField(SearchFields.TITLE_TEXT, page, Field.Store.NO));
		document.add(new TextField(SearchFields.HEADING, file, Field.Store.NO));
		document.add(new TextField(SearchFields.BREADCRUMB, page + " › " + file, Field.Store.YES));
		document.add(new Field(SearchFields.BODY, text, SearchFields.BODY_TYPE));
		document.add(new StoredField(SearchFields.SECTION_ID, ""));
		document.add(new StoredField(SearchFields.SECTION_PATH, page + "/" + file));
		document.add(new StoredField(SearchFields.ORDINAL, 0));
		document.add(new NumericDocValuesField(SearchFields.ORDINAL, 0));
		index.replaceAttachment(page + "/" + file, document);
		index.refresh();
	}
}
