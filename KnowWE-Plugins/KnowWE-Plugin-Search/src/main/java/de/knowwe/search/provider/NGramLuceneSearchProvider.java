/*
 * Copyright (C) 2023 denkbares GmbH. All rights reserved.
 */

package de.knowwe.search.provider;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexOptions;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.StoredFields;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.BoostQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.QueryRescorer;
import org.apache.lucene.search.Rescorer;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.InvalidTokenOffsetsException;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.SimpleHTMLEncoder;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.wiki.api.core.Attachment;
import org.apache.wiki.api.core.Context;
import org.apache.wiki.api.core.Page;
import org.apache.wiki.api.exceptions.ProviderException;
import org.apache.wiki.api.providers.PageProvider;
import org.apache.wiki.api.search.SearchResult;
import org.apache.wiki.api.spi.Wiki;
import org.apache.wiki.attachment.AttachmentManager;
import org.apache.wiki.auth.AuthorizationManager;
import org.apache.wiki.auth.permissions.PagePermission;
import org.apache.wiki.pages.PageManager;
import org.apache.wiki.search.LuceneSearchProvider;
import org.apache.wiki.util.TextUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.denkbares.strings.Strings;
import com.denkbares.util.lucene.LuceneUtils;
import com.denkbares.utils.Stopwatch;

/**
 * Based on the jspwiki LuceneSearchProvider, but with various improvements: separate fields for page and attachment
 * content, exact-match twin fields next to the n-gram fields, per-field boosts and a second scoring pass that rescores
 * the n-gram hits with the exact query.
 * <p>
 * Moved here unchanged from KnowWE-Plugin-CBX so that every wiki can use it, not just the CBX ones;
 * {@code com.denkbares.knowwe.jspwiki.NGramLuceneSearchProvider} remains as a thin subclass so existing
 * {@code jspwiki.searchProvider} settings keep working. Superseded step by step by the section-level index of this
 * module, see documentation/Wiki-Search-Plan.md.
 *
 * @author Albrecht Striffler (denkbares GmbH) + Claude for wiki-search
 */
public class NGramLuceneSearchProvider extends LuceneSearchProvider {

	protected static final Logger LOG = LoggerFactory.getLogger(NGramLuceneSearchProvider.class);

	public static final String LUCENE_ID = LuceneSearchProvider.LUCENE_ID;
	public static final String LUCENE_PAGE_CONTENTS = LuceneSearchProvider.LUCENE_PAGE_CONTENTS;
	public static final String LUCENE_AUTHOR = LuceneSearchProvider.LUCENE_AUTHOR;
	public static final String LUCENE_ATTACHMENTS = LuceneSearchProvider.LUCENE_ATTACHMENTS;
	public static final String LUCENE_PAGE_NAME = LuceneSearchProvider.LUCENE_PAGE_NAME;
	public static final String LUCENE_PAGE_KEYWORDS = LuceneSearchProvider.LUCENE_PAGE_KEYWORDS;

	protected static final String LUCENE_FIELD_ATTACHMENT_PREFIX = "attachment-";
	protected static final String LUCENE_ATTACHMENT_NAME = LUCENE_FIELD_ATTACHMENT_PREFIX + "name";
	protected static final String LUCENE_ATTACHMENT_CONTENTS = LUCENE_FIELD_ATTACHMENT_PREFIX + "contents";
	protected static final String LUCENE_ATTACHMENT_AUTHOR = LUCENE_FIELD_ATTACHMENT_PREFIX + "author";

	protected static final String LUCENE_PAGE_NAME_EXACT = "name-exact";
	protected static final String LUCENE_PAGE_CONTENTS_EXACT = "contents-exact";
	protected static final String LUCENE_ATTACHMENT_NAME_EXACT = "attachment-name-exact";
	protected static final String LUCENE_ATTACHMENT_CONTENTS_EXACT = "attachment-contents-exact";

	protected static final String[] QUERY_FIELDS = {
			LUCENE_PAGE_CONTENTS,
			LUCENE_PAGE_NAME,
			LUCENE_AUTHOR,
			LUCENE_ATTACHMENTS,
			LUCENE_PAGE_KEYWORDS,
			LUCENE_ATTACHMENT_NAME,
			LUCENE_ATTACHMENT_CONTENTS,
			LUCENE_ATTACHMENT_AUTHOR
	};

	protected static final String[] EXACT_QUERY_FIELDS = {
			LUCENE_PAGE_NAME_EXACT,
			LUCENE_PAGE_CONTENTS_EXACT,
			LUCENE_ATTACHMENT_NAME_EXACT,
			LUCENE_ATTACHMENT_CONTENTS_EXACT
	};

	private static final int EXACT_MATCH_RERANK_LIMIT = 1000;
	private static final float EXACT_RESCORE_MULTIPLIER = 12f;
	private static final float FIRST_PASS_EXACT_BOOST = 4f;

	private static final FieldType FIELD_TYPE;

	static {
		FIELD_TYPE = new FieldType(TextField.TYPE_NOT_STORED);
		FIELD_TYPE.setIndexOptions(IndexOptions.DOCS);
		FIELD_TYPE.setOmitNorms(true);
		FIELD_TYPE.freeze();
	}

	protected static final Map<String, Float> BOOSTS = new HashMap<>();
	protected static final Map<String, Float> EXACT_BOOSTS = new HashMap<>();

	static {
		for (String queryField : QUERY_FIELDS) {
			float boost;
			if (queryField.equals(LUCENE_PAGE_NAME)) {
				boost = 2;
			}
			else if (queryField.equals(LUCENE_ATTACHMENTS)) {
				boost = 0.5f;
			}
			else {
				boost = 1;
			}
			if (queryField.contains(LUCENE_FIELD_ATTACHMENT_PREFIX)) {
				boost /= 2;
			}
			BOOSTS.put(queryField, boost);
		}

		EXACT_BOOSTS.put(LUCENE_PAGE_NAME_EXACT, 8f);
		EXACT_BOOSTS.put(LUCENE_PAGE_CONTENTS_EXACT, 4f);
		EXACT_BOOSTS.put(LUCENE_ATTACHMENT_NAME_EXACT, 3f);
		EXACT_BOOSTS.put(LUCENE_ATTACHMENT_CONTENTS_EXACT, 1.5f);
	}

	private static final int MAX_FRAGMENTS = 5;

	@Override
	protected Analyzer getLuceneAnalyzer() {
		Map<String, Analyzer> perFieldAnalyzers = new HashMap<>();
		Analyzer exactAnalyzer = new StandardAnalyzer();
		perFieldAnalyzers.put(LUCENE_PAGE_NAME_EXACT, exactAnalyzer);
		perFieldAnalyzers.put(LUCENE_PAGE_CONTENTS_EXACT, exactAnalyzer);
		perFieldAnalyzers.put(LUCENE_ATTACHMENT_NAME_EXACT, exactAnalyzer);
		perFieldAnalyzers.put(LUCENE_ATTACHMENT_CONTENTS_EXACT, exactAnalyzer);
		return new PerFieldAnalyzerWrapper(LuceneUtils.TermAnalyzer.ngram_faster.getAnalyzer(), perFieldAnalyzers);
	}

	@Override
	protected String getIndexId() {
		// bumped ngram3 -> ngram4 for the Lucene 6.6.6 -> 10 upgrade: Lucene 10 cannot open the legacy v6
		// on-disk ngram index (IndexFormatTooOldException). The new id routes to a fresh directory so the
		// index is rebuilt from the wiki pages; the old ngram3 directory is left unused.
		return "ngram4";
	}

	@Override
	public Collection<SearchResult> findPages(final String query, final int flags, final Context wikiContext) throws ProviderException {
		Stopwatch stopwatch = new Stopwatch();
		ArrayList<SearchResult> list = null;
		Highlighter highlighter = null;
		String cleanedQuery = Strings.isNotBlank(query) && query.trim()
				.endsWith(":") ? query.replaceAll(":\\s*$", "") : query;

		try (final Analyzer analyzer = getLuceneAnalyzer();
			 final Directory luceneDir = FSDirectory.open(new File(m_luceneDirectory).toPath());
			 final IndexReader reader = DirectoryReader.open(luceneDir)) {

			final Query luceneQuery = createBaseQuery(cleanedQuery, analyzer);
			final Query exactQuery = createExactQuery(cleanedQuery, analyzer);
			final Query firstPassQuery = createFirstPassQuery(luceneQuery, exactQuery);
			final IndexSearcher searcher = new IndexSearcher(reader);

			String details = wikiContext.getHttpRequest().getParameter("details");
			String startString = wikiContext.getHttpRequest().getParameter("start");
			String maxitemsString = wikiContext.getHttpRequest().getParameter("maxitems");
			int start = startString == null ? 0 : Integer.parseInt(startString);
			int maxItems = maxitemsString == null ? 100 : Integer.parseInt(maxitemsString);
			if ("on".equals(details)) {
				highlighter = new Highlighter(new SimpleHTMLFormatter("<span class=\"searchmatch\">", "</span>"),
						new SimpleHTMLEncoder(),
						new QueryScorer(luceneQuery));
			}

			TopDocs hitsTopDocs = searcher.search(firstPassQuery, MAX_SEARCH_HITS);
			hitsTopDocs = rescoreWithExactQuery(searcher, hitsTopDocs, exactQuery);
			ScoreDoc[] hits = hitsTopDocs.scoreDocs;
			final AuthorizationManager mgr = m_engine.getManager(AuthorizationManager.class);

			List<SearchResult> ordered = new ArrayList<>(hits.length);
			// lucene-10: IndexSearcher.doc(int) removed -> obtain a StoredFields accessor once and reuse
			final StoredFields storedFields = searcher.storedFields();
			for (ScoreDoc hit : hits) {
				final int docID = hit.doc;
				final Document doc = storedFields.document(docID);
				final String pageName = doc.get(LUCENE_ID);
				final Page page = m_engine.getManager(PageManager.class).getPage(pageName, PageProvider.LATEST_VERSION);

				if (page != null) {
					final PagePermission pp = new PagePermission(page, PagePermission.VIEW_ACTION);
					if (mgr.checkPermission(wikiContext.getWikiSession(), pp)) {
						final int score = (int) (hit.score * 100);
						final String text = doc.get(LUCENE_PAGE_CONTENTS);
						String[] fragments = new String[0];
						if (text != null && highlighter != null) {
							final TokenStream tokenStream = analyzer.tokenStream(LUCENE_PAGE_CONTENTS, new StringReader(text));
							fragments = highlighter.getBestFragments(tokenStream, text, MAX_FRAGMENTS);
						}
						ordered.add(new SearchResultImpl(page, score, fragments));
					}
				}
				else {
					LOG.error("Lucene found a result page '{}' that could not be loaded, removing from Lucene cache", pageName);
					pageRemoved(Wiki.contents().page(m_engine, pageName));
				}
			}

			list = new ArrayList<>(ordered.size());
			for (int i = 0; i < start; i++) {
				list.add(null);
			}
			for (int i = start; i < ordered.size() && i < start + maxItems; i++) {
				list.add(ordered.get(i));
			}
			for (int i = start + maxItems; i < ordered.size(); i++) {
				list.add(null);
			}
		}
		catch (final IOException e) {
			LOG.error("Failed during lucene search", e);
		}
		catch (final ParseException e) {
			LOG.info("Broken query; cannot parse query: {}", query, e);
			throw new ProviderException("You have entered a query Lucene cannot process [" + query + "]: " + e.getMessage());
		}
		catch (final InvalidTokenOffsetsException e) {
			LOG.error("Tokens are incompatible with provided text ", e);
		}

		stopwatch.log(LOG, "Executed search query: " + query + ", got " + (list == null ? 0 : list.size()) + " results");
		return list;
	}

	Query createBaseQuery(String query, Analyzer analyzer) throws ParseException {
		QueryParser parser = new MultiFieldQueryParser(QUERY_FIELDS, analyzer, BOOSTS);
		parser.setAllowLeadingWildcard(true);
		return parser.parse(query);
	}

	Query createExactQuery(String query, Analyzer analyzer) throws ParseException {
		QueryParser parser = new MultiFieldQueryParser(EXACT_QUERY_FIELDS, analyzer, EXACT_BOOSTS);
		parser.setAllowLeadingWildcard(true);
		parser.setDefaultOperator(QueryParser.Operator.AND);
		return parser.parse(query);
	}

	Query createFirstPassQuery(Query baseQuery, Query exactQuery) {
		BooleanQuery.Builder builder = new BooleanQuery.Builder();
		builder.add(baseQuery, BooleanClause.Occur.SHOULD);
		builder.add(new BoostQuery(exactQuery, FIRST_PASS_EXACT_BOOST), BooleanClause.Occur.SHOULD);
		return builder.build();
	}

	TopDocs rescoreWithExactQuery(IndexSearcher searcher, TopDocs baseHits, Query exactQuery) throws IOException {
		int topN = Math.min(EXACT_MATCH_RERANK_LIMIT, baseHits.scoreDocs.length);
		if (topN <= 0) return baseHits;
		Rescorer rescorer = new QueryRescorer(exactQuery) {
			@Override
			protected float combine(float firstPassScore, boolean secondPassMatches, float secondPassScore) {
				return firstPassScore + (secondPassMatches ? secondPassScore * EXACT_RESCORE_MULTIPLIER : 0f);
			}
		};
		return rescorer.rescore(searcher, baseHits, topN);
	}

	@Override
	protected Document luceneIndexPage(final Page page, final String text, final IndexWriter writer) throws IOException {
		if (LOG.isDebugEnabled()) {
			LOG.debug("Indexing {}...", page.getName());
		}

		// make a new, empty document
		final Document doc = new Document();
		if (text == null) {
			return doc;
		}

		// Raw name is the keyword we'll use to refer to this document for updates.
		doc.add(new Field(LUCENE_ID, page.getName(), StringField.TYPE_STORED));

		final String indexedText = text.replace("__", " ");
		boolean isAttachment = page instanceof Attachment;
		// Body text.  It is stored in the doc for search contexts.
		doc.add(new Field(isAttachment ? LUCENE_ATTACHMENT_CONTENTS : LUCENE_PAGE_CONTENTS, indexedText, FIELD_TYPE));
		doc.add(new Field(isAttachment ? LUCENE_ATTACHMENT_CONTENTS_EXACT : LUCENE_PAGE_CONTENTS_EXACT, indexedText, FIELD_TYPE));

		// Allow searching by page name. Both beautified and raw
		final String unTokenizedTitle = StringUtils.replaceChars(page.getName(), TextUtil.PUNCTUATION_CHARS_ALLOWED, PUNCTUATION_TO_SPACES);
		String searchableTitle = TextUtil.beautifyString(page.getName()) + " " + unTokenizedTitle;
		doc.add(new Field(isAttachment ? LUCENE_ATTACHMENT_NAME : LUCENE_PAGE_NAME, searchableTitle, FIELD_TYPE));
		doc.add(new Field(isAttachment ? LUCENE_ATTACHMENT_NAME_EXACT : LUCENE_PAGE_NAME_EXACT, searchableTitle, FIELD_TYPE));

		// Allow searching by author name
		if (page.getAuthor() != null) {
			doc.add(new Field(isAttachment ? LUCENE_ATTACHMENT_AUTHOR : LUCENE_AUTHOR, page.getAuthor(), FIELD_TYPE));
		}

		// Now add the names of the attachments of this page
		if (!(page instanceof Attachment)) {
			try {
				final List<Attachment> attachments = m_engine.getManager(AttachmentManager.class).listAttachments(page);
				final StringBuilder attachmentNames = new StringBuilder();

				for (final Attachment att : attachments) {
					attachmentNames.append(att.getName()).append(";");
				}
				doc.add(new Field(LUCENE_ATTACHMENTS, attachmentNames.toString(), FIELD_TYPE));
			}
			catch (final ProviderException e) {
				// Unable to read attachments
				LOG.error("Failed to get attachments for page", e);
			}
			// also index page keywords, if available
			if (page.getAttribute("keywords") != null) {
				doc.add(new Field(LUCENE_PAGE_KEYWORDS, page.getAttribute("keywords").toString(), FIELD_TYPE));
			}
		}

		//noinspection SynchronizationOnLocalVariableOrMethodParameter
		synchronized (writer) {
			writer.addDocument(doc);
		}

		return doc;
	}
}
