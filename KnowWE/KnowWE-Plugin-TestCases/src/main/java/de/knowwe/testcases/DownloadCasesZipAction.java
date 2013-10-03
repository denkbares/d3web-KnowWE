package de.knowwe.testcases;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import de.d3web.core.session.Session;
import de.d3web.core.utilities.Triple;
import de.d3web.empiricaltesting.SequentialTestCase;
import de.d3web.testcase.model.TestCase;
import de.knowwe.core.Environment;
import de.knowwe.core.action.AbstractAction;
import de.knowwe.core.action.UserActionContext;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;

public class DownloadCasesZipAction extends AbstractAction {

	public static final String PARAM_FILENAME = "filename";

	@Override
	public void execute(UserActionContext context) throws IOException {

		String playerid = context.getParameter("playerid");
		Section<?> section = DownloadCaseAction.getPlayerSection(context, playerid);
		if (section == null) return; // error will already be added

		List<Triple<TestCaseProvider, Section<?>, Article>> providers =
				TestCaseUtils.getTestCaseProviders(Sections.cast(section.getFather(),
						TestCasePlayerType.class));

		if (providers.isEmpty()) {
			context.sendError(HttpServletResponse.SC_NOT_FOUND,
					"No testcase found to download.");
			return;
		}

		Collection<String> skipped = new ArrayList<String>();

		for (Triple<TestCaseProvider, Section<?>, Article> triple : providers) {

			Section<?> testCaseSection = triple.getB();
			TestCaseProvider provider = triple.getA();
			String testCaseName = provider.getName();

			if (!userCanView(context, section) || !userCanView(context, testCaseSection)) {
				skipped.add("'" + testCaseName
						+ "' was skipped, because you are not authorized for it.");
				continue;
			}

			TestCase testCase = provider.getTestCase();
			Session session = provider.getActualSession(context);

			if (session == null) {
				context.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
						"Unable to download TestCase: No valid session found");
				return;
			}

			SequentialTestCase sequentialTestCase = null;
			try {
				sequentialTestCase = TestCaseUtils.transformToSTC(testCase, testCaseName,
						session.getKnowledgeBase());
			}
			catch (Exception e) {
				context.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
						"Exception while creating downloadable xml file: " + e.getMessage());
				return;
			}
		}

		context.setContentType("application/x-bin");

		context.setHeader("Content-Disposition", "attachment;filename=\"" + section.getTitle()
				+ "zip\"");

		OutputStream out = context.getOutputStream();

		// TestPersistence.getInstance().writeCases(out,
		// Arrays.asList(sequentialTestCase), false);

		out.flush();
		out.close();

	}

	private boolean userCanView(UserActionContext context, Section<?> section) {
		return Environment.getInstance().getWikiConnector().userCanViewArticle(
				section.getTitle(),
				context.getRequest());
	}

}