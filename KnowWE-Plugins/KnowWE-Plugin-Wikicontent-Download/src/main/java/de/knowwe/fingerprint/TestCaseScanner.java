package de.knowwe.fingerprint;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.d3web.core.knowledge.KnowledgeBase;
import de.d3web.core.session.Session;
import de.d3web.core.session.SessionFactory;
import de.d3web.testcase.TestCaseUtils;
import de.d3web.testcase.model.Check;
import de.d3web.testcase.model.TestCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.denkbares.utils.Pair;
import de.d3web.we.utils.D3webUtils;
import de.knowwe.core.compile.packaging.PackageCompileType;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.testcases.ProviderTriple;
import de.knowwe.testcases.TestCasePlayerType;

public class TestCaseScanner implements Scanner {
	private static final Logger LOGGER = LoggerFactory.getLogger(TestCaseScanner.class);

	@Override
	public void scan(Article article, File target) throws IOException {
		// checks if the article contains test cases
		// if yes, execute them an check them all
		List<Section<TestCasePlayerType>> players = Sections.
				successors(article.getRootSection(), TestCasePlayerType.class);
		if (players.isEmpty()) return;
		LOGGER.info("Scanning cases on " + article.getTitle());
		try (PrintStream out = new PrintStream(target)) {
			for (Section<TestCasePlayerType> player : players) {
				List<ProviderTriple> providers = de.knowwe.testcases.TestCaseUtils.getTestCaseProviders(player);
				for (ProviderTriple triple : providers) {
					TestCase testCase = triple.getA().getTestCase();
					Section<? extends PackageCompileType> kbSection = triple.getC();
					// test case with original knowledge base
					KnowledgeBase base = D3webUtils.getKnowledgeBase(kbSection);
					out.printf("Results for test case '%s'\n", triple.getA().getName());
					execute(base, testCase, out);
					out.print("\n");
				}
			}
		}
	}

	private final Map<Pair<KnowledgeBase, TestCase>, String> cache = new HashMap<>();

	private void execute(KnowledgeBase base, TestCase testCase, PrintStream out) {
		Pair<KnowledgeBase, TestCase> key = new Pair<>(base, testCase);
		String result = cache.get(key);
		if (result == null) {
			StringBuilder builder = new StringBuilder();
			Session session = SessionFactory.createSession(base, testCase.getStartDate());
			for (Date date : testCase.chronology()) {
				builder.append("- ").append((date.getTime() < 1000)
						? ("line " + date.getTime()) : ("time " + date)).append(":\n");
//				out.printf("- %s:\n", (date.getTime() < 1000)
//						? ("line " + date.getTime()) : ("time " + date));

				TestCaseUtils.applyFindings(session, testCase, date);
				for (Check check : testCase.getChecks(date, session.getKnowledgeBase())) {
					builder.append("  check '").append(check.getCondition()
							.trim()).append("': ").append(check.check(session) ? "ok" : "failed").append("\n");
//					out.printf("  check '%s': %s\n", check.getCondition().trim(),
//							check.check(session) ? "ok" : "failed");
				}
			}
			result = builder.toString();
			cache.put(key, result);
		}
		out.print(result);
	}

	@Override
	public String getExtension() {
		return ".cases";
	}

	@Override
	public String getItemName() {
		return "Test Cases";
	}

	@Override
	public Diff compare(File file1, File file2) throws IOException {
		return Fingerprint.compareTextFiles(file1, file2);
	}

}
