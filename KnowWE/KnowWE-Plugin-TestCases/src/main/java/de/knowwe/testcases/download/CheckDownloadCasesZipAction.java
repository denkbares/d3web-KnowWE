package de.knowwe.testcases.download;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

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
import de.knowwe.testcases.TestCasePlayerType;
import de.knowwe.testcases.TestCaseProvider;
import de.knowwe.testcases.TestCaseUtils;

public class CheckDownloadCasesZipAction extends AbstractAction {

	public static final String PARAM_FILENAME = "filename";

	@Override
	public void execute(UserActionContext context) throws IOException {

		Section<?> section = CheckDownloadCaseAction.getPlayerSection(context);
		if (section == null) return; // error will already be added

		List<Triple<TestCaseProvider, Section<?>, Article>> providers =
				TestCaseUtils.getTestCaseProviders(Sections.cast(section.getFather(),
						TestCasePlayerType.class));

		StringBuilder skipped = new StringBuilder();
		List<SequentialTestCase> casesToWrite = new ArrayList<SequentialTestCase>();
		checkAndTransform(context, section, providers, skipped, casesToWrite);

		writeResponse(context, section, skipped, casesToWrite);

	}

	private void writeResponse(UserActionContext context, Section<?> section, StringBuilder skipped, List<SequentialTestCase> casesToWrite) throws IOException, FileNotFoundException {
		List<String> json = new ArrayList<String>();
		if (!casesToWrite.isEmpty()) {

			File caseZip = File.createTempFile("TestCasesZip", null);
			caseZip.deleteOnExit();
			ZipOutputStream out = new ZipOutputStream(new FileOutputStream(caseZip));

			for (SequentialTestCase sequentialTestCase : casesToWrite) {

				String fileName = CheckDownloadCaseAction.toXMLFileName(sequentialTestCase.getName());
				ZipEntry e = new ZipEntry(fileName);
				out.putNextEntry(e);
				TestPersistence.getInstance().writeCases(out,
						Arrays.asList(sequentialTestCase), false);

				out.closeEntry();
			}

			out.flush();
			out.close();
			json.add("path");
			json.add(caseZip.getAbsolutePath());
			json.add("file");
			json.add(section.getTitle() + "Cases.zip");
		}
		else {
			json.add("error");
			json.add("There are not test cases to download.");
		}
		String skippedString = skipped.toString();
		if (!skippedString.isEmpty()) {
			json.add("skipped");
			json.add(skippedString);
		}

		CheckDownloadCaseAction.sendJSON(context, json.toArray(new String[json.size()]));
	}

	private void checkAndTransform(UserActionContext context, Section<?> section, List<Triple<TestCaseProvider, Section<?>, Article>> providers, StringBuilder skipped, List<SequentialTestCase> casesToWrite) {
		for (Triple<TestCaseProvider, Section<?>, Article> triple : providers) {

			Section<?> testCaseSection = triple.getB();
			TestCaseProvider provider = triple.getA();
			String testCaseName = provider.getName();

			if (!userCanView(context, section) || !userCanView(context, testCaseSection)) {
				skipped.append("'" + testCaseName
						+ "' was skipped because you are not authorized to see it.<br/>");
				continue;
			}

			TestCase testCase = provider.getTestCase();
			Session session = provider.getActualSession(context);

			if (session == null) {
				skipped.append("'" + testCaseName
						+ "' was skipped because of an internal error (no session found).<br/>");
				continue;
			}

			SequentialTestCase sequentialTestCase = null;
			try {
				sequentialTestCase = TestCaseUtils.transformToSTC(testCase, testCaseName,
						session.getKnowledgeBase());
			}
			catch (Exception e) {
				skipped.append("'"
						+ testCaseName
						+ "' was skipped because of an internal error while creating the xml file: "
						+ e.getMessage() + "<br/>");
				continue;
			}

			casesToWrite.add(sequentialTestCase);
		}
	}

	private boolean userCanView(UserActionContext context, Section<?> section) {
		return Environment.getInstance().getWikiConnector().userCanViewArticle(
				section.getTitle(),
				context.getRequest());
	}

}