package de.knowwe.fingerprint;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Date;
import java.util.List;

import de.d3web.core.knowledge.KnowledgeBase;
import de.d3web.core.session.Session;
import de.d3web.core.session.SessionFactory;
import de.d3web.testcase.TestCaseUtils;
import de.d3web.testcase.model.Check;
import de.d3web.testcase.model.TestCase;
import de.d3web.utils.Triple;
import de.d3web.we.utils.D3webUtils;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.testcases.TestCasePlayerType;
import de.knowwe.testcases.TestCaseProvider;

public class TestCaseScanner implements Scanner {

	@Override
	public void scan(Article article, File target) throws IOException {
		// checks if the article contains test cases
		// if yes, execute them an check them all
		List<Section<TestCasePlayerType>> players = Sections.
				findSuccessorsOfType(article.getRootSection(), TestCasePlayerType.class);
		if (players.isEmpty()) return;

		PrintStream out = new PrintStream(target);
		try {
			for (Section<TestCasePlayerType> player : players) {
				List<Triple<TestCaseProvider, Section<?>, Article>> providers = de.knowwe.testcases.TestCaseUtils.getTestCaseProviders(player);
				for (Triple<TestCaseProvider, Section<?>, Article> triple : providers) {
					TestCase testCase = triple.getA().getTestCase();
					Article master = triple.getC();
					// test case with original knowledge base
					KnowledgeBase base = D3webUtils.getKnowledgeBase(master);
					out.printf("Results for test case '%s'\n", triple.getA().getName());
					execute(base, testCase, out);
					out.print("\n");
				}
			}
		}
		finally {
			out.close();
		}
	}

	private void execute(KnowledgeBase base, TestCase testCase, PrintStream out) {
		Session session = SessionFactory.createSession(base, testCase.getStartDate());
		for (Date date : testCase.chronology()) {
			out.printf("- %s:\n", (date.getTime() < 1000)
					? ("line " + date.getTime()) : ("time " + date));

			TestCaseUtils.applyFindings(session, testCase, date);
			for (Check check : testCase.getChecks(date, session.getKnowledgeBase())) {
				out.printf("  check '%s': %s\n", check.getCondition().trim(),
						check.check(session) ? "ok" : "failed");
			}
		}
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
