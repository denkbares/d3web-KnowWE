package de.knowwe.testcases;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import de.d3web.core.session.Session;
import de.d3web.core.utilities.Triple;
import de.d3web.empiricaltesting.SequentialTestCase;
import de.d3web.empiricaltesting.TestPersistence;
import de.d3web.testcase.model.TestCase;
import de.knowwe.core.Environment;
import de.knowwe.core.action.AbstractAction;
import de.knowwe.core.action.UserActionContext;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;

public class DownloadCaseAction extends AbstractAction {

	public static final String PARAM_FILENAME = "filename";

	@Override
	public void execute(UserActionContext context) throws IOException {
		String playerid = context.getParameter("playerid");
		Section<?> section = Sections.getSection(playerid);
		if (section == null || !(section.getFather().get() instanceof TestCasePlayerType)) {
			context.sendError(HttpServletResponse.SC_CONFLICT,
					"Unable to find TestCasePlayer with id '"
							+ playerid + "' , possibly because somebody else"
							+ " has edited the page.");
			return;
		}

		Triple<TestCaseProvider, Section<?>, Article> selectedTestCaseTriple = getSelectedTestCaseTriple(
				section, context);
		Section<?> testCaseSection = selectedTestCaseTriple.getB();

		if (!userCanView(context, section) || !userCanView(context, testCaseSection)) {
			context.sendError(HttpServletResponse.SC_FORBIDDEN,
					"You are not allowed to download this TestCase");
			return;
		}

		TestCaseProvider provider = selectedTestCaseTriple.getA();
		TestCase testCase = provider.getTestCase();
		Session session = provider.getActualSession(context);

		if (session == null) {
			context.sendError(HttpServletResponse.SC_CONFLICT,
					"Unable to download TestCase: No valid session found");
			return;
		}

		context.setContentType("application/x-bin");

		String fileName = provider.getName();
		if (!fileName.toLowerCase().endsWith(".xml")) fileName += ".xml";

		context.setHeader("Content-Disposition", "attachment;filename=\"" + fileName
				+ "\"");

		SequentialTestCase sequentialTestCase = TestCaseUtils.transformToSTC(testCase,
				session.getKnowledgeBase());

		OutputStream out = context.getOutputStream();

		TestPersistence.getInstance().writeCases(out, Arrays.asList(sequentialTestCase), false);

		out.flush();
		out.close();

	}

	private boolean userCanView(UserActionContext context, Section<?> section) {
		return Environment.getInstance().getWikiConnector().userCanViewArticle(
				section.getTitle(),
				context.getRequest());
	}

	private Triple<TestCaseProvider, Section<?>, Article> getSelectedTestCaseTriple(Section<?> section, UserActionContext context) {
		Section<TestCasePlayerType> playerSection =
				Sections.cast(section.getFather(), TestCasePlayerType.class);
		List<Triple<TestCaseProvider, Section<?>, Article>> providers =
				de.knowwe.testcases.TestCaseUtils.getTestCaseProviders(playerSection);

		String selectedTestCaseId = TestCasePlayerRenderer.getSelectedTestCaseId(section,
				context);
		for (Triple<TestCaseProvider, Section<?>, Article> triple : providers) {
			if (triple.getA().getTestCase() != null) {
				String id = triple.getC().getTitle() + "/" + triple.getA().getName();
				if (id.equals(selectedTestCaseId)) {
					return triple;
				}
			}
		}
		return null;
	}

}