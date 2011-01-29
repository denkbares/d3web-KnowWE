package de.d3web.we.ci4ke.testmodules;

import java.util.logging.Level;
import java.util.logging.Logger;

import de.d3web.core.knowledge.KnowledgeBase;
import de.d3web.we.basic.D3webKnowledgeHandler;
import de.d3web.we.basic.D3webModule;
import de.d3web.we.ci4ke.testing.AbstractCITest;
import de.d3web.we.ci4ke.testing.CITestResult;
import de.d3web.we.ci4ke.testing.CITestResult.TestResultType;
import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.knowledgebase.KBRenderer;
import de.d3web.we.utils.KnowWEUtils;


public class ReparseTest extends AbstractCITest {

	@Override
	public CITestResult call() throws Exception {

		String title = getParameter(0);
		if (title.isEmpty()) {
			return new CITestResult(TestResultType.FAILED, "Parameter 0 was invalid!");
		}

		KnowWEArticle article = KnowWEEnvironment.getInstance().getArticle(
				KnowWEEnvironment.DEFAULT_WEB, title);

		if (!article.isReParse()) {
			return new CITestResult(TestResultType.SUCCESSFUL,
					"Last build was no reparse.");
		}

		D3webKnowledgeHandler d3webKH = D3webModule.getKnowledgeRepresentationHandler(
				KnowWEEnvironment.DEFAULT_WEB);

		KnowledgeBase lastKB = d3webKH.getLastKB(title);
		KnowledgeBase currentKB = d3webKH.getKBM(title).getKnowledgeBase();

		String lastKBOutput = new KBRenderer().renderHTML(KnowWEEnvironment.DEFAULT_WEB, title,
				null, lastKB);
		String currentKBOutput = new KBRenderer().renderHTML(KnowWEEnvironment.DEFAULT_WEB, title,
				null, currentKB);

		if (!lastKBOutput.equals(currentKBOutput)) {
			Logger.getLogger(this.getClass().getName()).log(
					Level.WARNING,
					"Detected difference in the knowledgebase after a full reparse.");
			int version = KnowWEEnvironment.getInstance().getWikiConnector().getVersion(
					title);
			String fileName = title + " " + version + " KB-diff.txt";
			String logEntry = title + ", " + version
					+ ", full reparse with difference in knowledgebase ,"
					+ " logfile: " + fileName + "\n";

			KnowWEUtils.appendToFile(KnowWEUtils.getPageChangeLogPath(), logEntry);

			String logContent = currentKBOutput + "\n+++++++++++++++++++++++\nfull compile above\n"
					+
					"incremental compile below\n+++++++++++++++++++++++\n" + lastKBOutput;
			KnowWEUtils.writeFile(KnowWEUtils.getVersionsSavePath() + fileName, logContent);

			return new CITestResult(TestResultType.FAILED,
					"Detected difference in the knowledgebase after a full reparse. Check "
							+ fileName + " for results.");
		}

		return new CITestResult(TestResultType.SUCCESSFUL,
				"No difference detected in the d3web KnowledgeBase after a full reparse.");
	}

}
