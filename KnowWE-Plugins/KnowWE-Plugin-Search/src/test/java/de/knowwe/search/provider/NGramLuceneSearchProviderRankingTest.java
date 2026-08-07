/*
 * Copyright (C) 2026 denkbares GmbH. All rights reserved.
 */

package de.knowwe.search.provider;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.wiki.api.core.Context;
import org.apache.wiki.api.core.Engine;
import org.apache.wiki.api.core.Page;
import org.apache.wiki.api.core.Session;
import org.apache.wiki.api.providers.PageProvider;
import org.apache.wiki.api.search.SearchResult;
import org.apache.wiki.attachment.AttachmentManager;
import org.apache.wiki.auth.AuthorizationManager;
import org.apache.wiki.pages.PageManager;
import org.junit.Assert;
import org.junit.Test;

public class NGramLuceneSearchProviderRankingTest {

	@Test
	public void exactTextOutranksFuzzyPageName() throws Exception {
		List<DocData> corpus = Arrays.asList(
				new DocData("Motro Handbook", "only generic notes"),
				new DocData("Service Guide", "How to replace a motor safely")
		);

		List<String> order = searchResultPageNames(corpus, "motor", 20);
		Assert.assertEquals("Service Guide", order.get(0));
	}

	@Test
	public void exactPageNameOutranksFuzzyText() throws Exception {
		List<DocData> corpus = Arrays.asList(
				new DocData("Motor Calibration", "only generic notes"),
				new DocData("General Notes", "motro procedure and values")
		);

		List<String> order = searchResultPageNames(corpus, "motor", 20);
		Assert.assertEquals("Motor Calibration", order.get(0));
	}

	@Test
	public void exactRescoringDoesNotReorderWhenNothingExactMatches() throws Exception {
		List<DocData> corpus = Arrays.asList(
				new DocData("Motro Handbook", "motro details"),
				new DocData("General Notes", "motro procedure")
		);

		List<String> order = searchResultPageNames(corpus, "motor", 20);
		Assert.assertEquals(2, order.size());
	}

	@Test
	public void exactContentCanGetLostInLargeFuzzyNameCorpus() throws Exception {
		List<DocData> corpus = new ArrayList<>();
		corpus.add(new DocData(
				"General diagnostics handbook",
				createLongText("This chapter contains the exact sequence motor controller synchronization. ", 80)
		));

		for (int i = 0; i < 1300; i++) {
			corpus.add(new DocData(
					"Motro Controllre Troubleshooting Guide " + i,
					"generic maintenance notes"
			));
		}

		List<String> order = searchResultPageNames(corpus, "motor controller synchronization", 1400);
		int targetPos = order.indexOf("General diagnostics handbook");
		Assert.assertTrue(
				"Expected exact content match to survive large fuzzy-name corpus; position was " + targetPos,
				targetPos >= 0 && targetPos < 50
		);
	}

	@Test
	public void exactContentInLongTextShouldStillRankHigh() throws Exception {
		List<DocData> corpus = Arrays.asList(
				new DocData("Motro guide", createLongText("random maintenance paragraph ", 300)),
				new DocData("Motro handbook", createLongText("random troubleshooting paragraph ", 320)),
				new DocData("Service notes", createLongText(
						"preface and index entries ",
						200
				) + "exact phrase: hydraulic pressure compensation valve reset procedure"),
				new DocData("Motro calibration", createLongText("random assembly paragraph ", 280))
		);

		List<String> order = searchResultPageNames(corpus, "hydraulic pressure compensation valve reset procedure", 20);
		Assert.assertEquals("Service notes", order.get(0));
	}

	@Test
	public void exactContentStaysVisibleBeyondRescoreLimit() throws Exception {
		List<DocData> corpus = new ArrayList<>();
		corpus.add(new DocData(
				"Service Procedures",
				createLongText("motor controller synchronization checklist ", 70)
		));
		for (int i = 0; i < 2500; i++) {
			corpus.add(new DocData(
					"Motro Controllre Service Notes " + i,
					createLongText("generic maintenance text ", 20)
			));
		}

		List<String> order = searchResultPageNames(corpus, "motor controller synchronization", 2500);
		int targetPos = order.indexOf("Service Procedures");
		Assert.assertTrue(
				"Expected exact content match to remain in competitive range even with >1000 fuzzy results; position was " + targetPos,
				targetPos >= 0 && targetPos < 150
		);
	}

	@Test
	public void adaptSingleQueryPrefersExactContentOverAdapterLikeTitles() throws Exception {
		List<DocData> corpus = new ArrayList<>();
		for (String title : adapterLikeTitles()) {
			corpus.add(new DocData(title, createLongText(loremSentence(), 120)));
		}
		for (String title : exactAdaptSingleContentTitles()) {
			corpus.add(new DocData(title, createLongText(loremSentence(), 80) + " adapt_single " + createLongText(loremSentence(), 40)));
		}

		List<String> order = searchResultPageNames(corpus, "adapt_single", 100);
		Set<String> expectedContentHits = new HashSet<>(exactAdaptSingleContentTitles());
		Assert.assertTrue("Expected first result to have exact adapt_single in content but was: " + order.get(0),
				expectedContentHits.contains(order.get(0)));
	}

	@Test
	public void contentsFieldQueryForAdaptSingleReturnsRealContentHits() throws Exception {
		List<DocData> corpus = new ArrayList<>();
		for (String title : adapterLikeTitles()) {
			corpus.add(new DocData(title, createLongText(loremSentence(), 120)));
		}
		for (String title : exactAdaptSingleContentTitles()) {
			corpus.add(new DocData(title, createLongText(loremSentence(), 80) + " adapt_single " + createLongText(loremSentence(), 40)));
		}

		List<String> order = searchResultPageNames(corpus, "contents:adapt_single", 100);
		Set<String> expectedContentHits = new HashSet<>(exactAdaptSingleContentTitles());
		Assert.assertTrue("Expected first result for contents:adapt_single to be an exact content hit but was: " + order.get(0),
				expectedContentHits.contains(order.get(0)));
	}

	private static List<String> searchResultPageNames(List<DocData> corpus, String queryString, int maxItems) throws Exception {
		Map<String, Page> pageByName = new HashMap<>();
		for (DocData doc : corpus) {
			pageByName.put(doc.pageName, createPage(doc.pageName));
		}

		HarnessProvider provider = new HarnessProvider();
		Path indexDir = Files.createTempDirectory("ngram-findpages-test");
		try {
			Engine engine = createEngine(pageByName);
			provider.configure(engine, indexDir);
			provider.indexCorpus(corpus, pageByName);
			Context context = createContext(engine, maxItems);
			Collection<SearchResult> results = provider.findPages(queryString, 0, context);
			return results.stream()
					.filter(r -> r != null && r.getPage() != null)
					.map(r -> r.getPage().getName())
					.collect(Collectors.toList());
		}
		finally {
			deleteRecursively(indexDir);
		}
	}

	private static String createLongText(String sentence, int repetitions) {
		StringBuilder sb = new StringBuilder(sentence.length() * repetitions);
		sb.append(sentence.repeat(Math.max(0, repetitions)));
		return sb.toString();
	}

	private static String loremSentence() {
		return "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. ";
	}

	private static List<String> adapterLikeTitles() {
		return Arrays.asList(
				"Solution U01.sicherungskasten_u01_Steuerelektronik_AD6X1-1",
				"Solution U01.sicherungskasten_u01_Steuerelektronik_AD6@X1-3",
				"Solution U01.sicherungskasten_u01_Steuerelektronik_AD6X1-A3",
				"Solution U01.sicherungskasten_u01_Steuerelektronik_AD6X1-4 6",
				"Solution U01.sicherungskasten_u01_Steuerelektronik_AD6@X1-2",
				"Solution U01.sicherungskasten_u01_Steuerelektronik_AD6@X1-A4",
				"Adapter B328",
				"Messadapter alle",
				"STTE Adapter-Fernabfeuerung",
				"Unit adapterplatte_druckverbinder",
				"Adapters",
				"Group Messadapter-Leopard-2-A7, -QAT",
				"Adapter B314",
				"Adaptionsstellen A7V UMD1",
				"STTE Adapter-FBI-X16",
				"Adapter B010",
				"Adapter B016",
				"Adapter B046",
				"Adapter B092",
				"Adapter B331"
		);
	}

	private static List<String> exactAdaptSingleContentTitles() {
		return Arrays.asList(
				"APU CAN HEY W4124",
				"LEO TSW1940",
				"LEO TSW1944",
				"LEO TSW1946",
				"Test Step W1941",
				"Test Step W1945",
				"Test Step W1947",
				"Test Step W2170",
				"Test Step W2171",
				"Test Step W3120",
				"Test Step W3121",
				"Test Step W3126"
		);
	}

	private static Engine createEngine(Map<String, Page> pageByName) {
		AttachmentManager attachmentManager = proxy(AttachmentManager.class, (proxy, method, args) -> {
			if ("listAttachments".equals(method.getName())) return List.of();
			return defaultValue(method.getReturnType());
		});

		AuthorizationManager authorizationManager = proxy(AuthorizationManager.class, (proxy, method, args) -> {
			if ("checkPermission".equals(method.getName())) return true;
			return defaultValue(method.getReturnType());
		});

		PageManager pageManager = proxy(PageManager.class, (proxy, method, args) -> {
			if ("getPage".equals(method.getName()) && args != null && args.length >= 1) {
				return pageByName.get((String) args[0]);
			}
			return defaultValue(method.getReturnType());
		});

		return proxy(Engine.class, (proxy, method, args) -> {
			if ("getManager".equals(method.getName()) && args != null && args.length == 1) {
				Class<?> managerClass = (Class<?>) args[0];
				if (AttachmentManager.class.equals(managerClass)) return attachmentManager;
				if (AuthorizationManager.class.equals(managerClass)) return authorizationManager;
				if (PageManager.class.equals(managerClass)) return pageManager;
			}
			return defaultValue(method.getReturnType());
		});
	}

	private static Context createContext(Engine engine, int maxItems) {
		Object request = proxy(javax.servlet.http.HttpServletRequest.class, (proxy, method, args) -> {
			if ("getParameter".equals(method.getName()) && args != null && args.length == 1) {
				String key = (String) args[0];
				if ("start".equals(key)) return "0";
				if ("maxitems".equals(key)) return String.valueOf(maxItems);
				return null;
			}
			return defaultValue(method.getReturnType());
		});
		Session session = proxy(Session.class, (proxy, method, args) -> defaultValue(method.getReturnType()));
		return proxy(Context.class, (proxy, method, args) -> {
			switch (method.getName()) {
				case "getEngine":
					return engine;
				case "getHttpRequest":
					return request;
				case "getWikiSession":
					return session;
				default:
					return defaultValue(method.getReturnType());
			}
		});
	}

	private static Page createPage(String pageName) {
		return proxy(Page.class, (proxy, method, args) -> {
			switch (method.getName()) {
				case "getName":
				case "toString":
					return pageName;
				case "getVersion":
					return PageProvider.LATEST_VERSION;
				case "compareTo":
					Page other = (Page) args[0];
					return pageName.compareTo(other == null ? "" : other.getName());
				default:
					return defaultValue(method.getReturnType());
			}
		});
	}

	private static void deleteRecursively(Path root) throws IOException {
		if (root == null || !Files.exists(root)) return;
		try (var walk = Files.walk(root)) {
			walk.sorted((a, b) -> b.compareTo(a)).forEach(path -> {
				try {
					Files.deleteIfExists(path);
				}
				catch (IOException ignored) {
				}
			});
		}
	}

	@SuppressWarnings("unchecked")
	private static <T> T proxy(Class<T> type, InvocationHandler handler) {
		return (T) Proxy.newProxyInstance(type.getClassLoader(), new Class[]{type}, handler);
	}

	private static Object defaultValue(Class<?> returnType) {
		if (returnType == null || !returnType.isPrimitive()) return null;
		if (boolean.class.equals(returnType)) return false;
		if (byte.class.equals(returnType)) return (byte) 0;
		if (short.class.equals(returnType)) return (short) 0;
		if (int.class.equals(returnType)) return 0;
		if (long.class.equals(returnType)) return 0L;
		if (float.class.equals(returnType)) return 0f;
		if (double.class.equals(returnType)) return 0d;
		if (char.class.equals(returnType)) return '\0';
		return null;
	}

	private static class HarnessProvider extends NGramLuceneSearchProvider {
		void configure(Engine engine, Path directory) {
			this.m_engine = engine;
			this.m_luceneDirectory = directory.toString();
		}

		void indexCorpus(List<DocData> corpus, Map<String, Page> pageByName) throws Exception {
			try (var analyzer = getLuceneAnalyzer();
				 Directory directory = FSDirectory.open(Path.of(m_luceneDirectory));
				 IndexWriter writer = new IndexWriter(directory, new IndexWriterConfig(analyzer))) {
				for (DocData doc : corpus) {
					luceneIndexPage(pageByName.get(doc.pageName), doc.pageText, writer);
				}
			}
		}
	}

	private static final class DocData {
		private final String pageName;
		private final String pageText;

		private DocData(String pageName, String pageText) {
			this.pageName = pageName;
			this.pageText = pageText;
		}
	}
}
