/*
 * Copyright (C) 2013 University Wuerzburg, Computer Science VI
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
package de.knowwe.testcases.download;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import de.d3web.core.io.progress.ParallelProgress;
import de.d3web.core.io.progress.ProgressListener;
import de.d3web.core.knowledge.KnowledgeBase;
import de.d3web.core.session.Session;
import de.d3web.empiricaltesting.SequentialTestCase;
import de.d3web.empiricaltesting.TestPersistence;
import de.d3web.testcase.model.TestCase;
import de.d3web.utils.Triple;
import de.knowwe.core.Environment;
import de.knowwe.core.action.UserActionContext;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.utils.progress.AjaxProgressListener;
import de.knowwe.core.utils.progress.FileDownloadOperation;
import de.knowwe.core.utils.progress.LongOperationUtils;
import de.knowwe.testcases.ProviderTriple;
import de.knowwe.testcases.TestCasePlayerType;
import de.knowwe.testcases.TestCaseProvider;
import de.knowwe.testcases.TestCaseUtils;
import de.knowwe.util.Icon;

/**
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 08.09.2013
 */
public class CasesZipOperation extends FileDownloadOperation {

	private StringBuilder skipped = null;
	private StringBuilder errors = null;
	private List<Triple<String, KnowledgeBase, TestCase>> casesToWrite = null;

	public CasesZipOperation(Article article, String attachmentFileName) {
		super(article, attachmentFileName);
	}

	public void before(UserActionContext user, AjaxProgressListener listener) throws IOException {
		Section<?> section = CheckDownloadCaseAction.getPlayerSection(user);
		List<ProviderTriple> providers =
				TestCaseUtils.getTestCaseProviders(Sections.cast(section.getParent(),
						TestCasePlayerType.class));
		skipped = new StringBuilder();
		errors = new StringBuilder();
		casesToWrite = new ArrayList<Triple<String, KnowledgeBase, TestCase>>();

		check(user, section, providers, casesToWrite);
	}

	private void check(UserActionContext context, Section<?> section, List<ProviderTriple> providers, List<Triple<String, KnowledgeBase, TestCase>> casesToWrite) {

		if (!userCanView(context, section)) {
			throw new Error("You are not allowed to download here.");
		}

		for (ProviderTriple triple : providers) {

			Section<?> testCaseSection = triple.getB();
			TestCaseProvider provider = triple.getA();
			String testCaseName = provider.getName();

			if (!userCanView(context, section) || !userCanView(context, testCaseSection)) {
				skipped.append(testCaseName
						+ ": You are not authorized to see this case.<br/>");
				continue;
			}

			TestCase testCase = provider.getTestCase();
			Session session = provider.getActualSession(context);

			if (session == null) {
				skipped.append(testCaseName
						+ ": Internal error (no session found).<br/>");
				continue;
			}
			casesToWrite.add(new Triple<String, KnowledgeBase, TestCase>(testCaseName,
					session.getKnowledgeBase(), testCase));
		}
	}

	@Override
	public void execute(UserActionContext context, File resultFile, AjaxProgressListener listener) throws IOException, InterruptedException {
		before(context, listener);
		ParallelProgress parallel = new ParallelProgress(listener, 50f, 50f);
		ProgressListener executeListener = parallel.getSubTaskProgressListener(0);
		ProgressListener zipListener = parallel.getSubTaskProgressListener(1);
		List<SequentialTestCase> stcs = transform(executeListener);
		zipSTCs(resultFile, stcs, zipListener);

	}

	private List<SequentialTestCase> transform(ProgressListener listener) throws InterruptedException {
		List<SequentialTestCase> stcs = new ArrayList<SequentialTestCase>();
		int i = 0;
		for (Triple<String, KnowledgeBase, TestCase> triple : casesToWrite) {
			LongOperationUtils.checkCancel();
			String testCaseName = triple.getA();
			KnowledgeBase kb = triple.getB();
			TestCase testCase = triple.getC();

			listener.updateProgress((float) i++ / (float) casesToWrite.size(),
					"Transforming test case '" + testCaseName + "'");

			SequentialTestCase sequentialTestCase = null;
			try {
				sequentialTestCase = TestCaseUtils.transformToSTC(testCase, testCaseName, kb);
			}
			catch (Exception e) {
				skipped.append(""
						+ testCaseName
						+ ": Internal error while creating the xml file: "
						+ e.getMessage() + "<br/>");
				continue;
			}

			stcs.add(sequentialTestCase);
		}
		return stcs;
	}

	private void zipSTCs(File resultFile, List<SequentialTestCase> stcs, ProgressListener listener) throws IOException, InterruptedException {

		if (!stcs.isEmpty()) {

			ZipOutputStream out = new ZipOutputStream(new FileOutputStream(resultFile));
			try {
				int i = 0;
				Set<String> usedEntryNames = new HashSet<String>();
				for (SequentialTestCase sequentialTestCase : stcs) {
					LongOperationUtils.checkCancel();
					String testCaseName = getNewEntryName(usedEntryNames, sequentialTestCase);
					listener.updateProgress((float) i++ / (float) casesToWrite.size(),
							"Zipping test case '" + testCaseName + ".xml'");
					String fileName = CheckDownloadCaseAction.toXMLFileName(testCaseName);
					ZipEntry e = new ZipEntry(fileName);
					out.putNextEntry(e);
					TestPersistence.getInstance().writeCases(out,
							Arrays.asList(sequentialTestCase), false);

					out.closeEntry();
				}
			}
			finally {
				out.flush();
				out.close();
			}
		}
		else {
			errors.append("There are not test cases to download.<br/>");
			throw new IOException("There are not test cases to download.");
		}
	}

	private String getNewEntryName(Set<String> usedEntryNames, SequentialTestCase sequentialTestCase) {
		String testCaseName = sequentialTestCase.getName();
		int i = 2;
		while (usedEntryNames.contains(testCaseName)) {
			testCaseName = sequentialTestCase.getName() + i++;
		}
		usedEntryNames.add(testCaseName);
		return testCaseName;
	}

	private boolean userCanView(UserActionContext context, Section<?> section) {
		return Environment.getInstance().getWikiConnector().userCanViewArticle(
				section.getTitle(),
				context.getRequest());
	}

	@Override
	public String getReport(UserActionContext context) {
		String report = super.getReport(context);
		if (report == null) report = "";

		String skipped = this.skipped.toString();
		if (!skipped.isEmpty()) {
			report += "<p>Skipped:<br/>" + skipped + "</p>";
		}
		String errors = this.errors.toString();
		if (!errors.isEmpty()) {
			report += "<p>Error:<br/>" + errors + "</p>";
		}
		return report;
	}

	@Override
	public Icon getFileIcon() {
		return Icon.FILE_ZIP;
	}

}
