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
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import de.d3web.core.io.progress.ProgressListener;
import de.d3web.core.session.Session;
import de.d3web.core.utilities.Triple;
import de.d3web.empiricaltesting.SequentialTestCase;
import de.d3web.empiricaltesting.TestPersistence;
import de.d3web.testcase.model.TestCase;
import de.knowwe.core.Environment;
import de.knowwe.core.action.UserActionContext;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.utils.progress.AttachmentOperation;
import de.knowwe.testcases.TestCasePlayerType;
import de.knowwe.testcases.TestCaseProvider;
import de.knowwe.testcases.TestCaseUtils;

/**
 * 
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 08.09.2013
 */
public class CasesZipOperation extends AttachmentOperation {

	public CasesZipOperation(Article article, String attachmentFileName) {
		super(article, attachmentFileName);
	}

	private boolean userCanView(UserActionContext context, Section<?> section) {
		// return true;
		return Environment.getInstance().getWikiConnector().userCanViewArticle(
				section.getTitle(), context.getRequest());
	}

	@Override
	public void execute(UserActionContext context, File resultFile, ProgressListener listener) throws IOException, InterruptedException {
		// get cases
		Section<?> section = CheckDownloadCaseAction.getPlayerSection(context);
		if (section == null) return; // error will already be added

		List<Triple<TestCaseProvider, Section<?>, Article>> providers =
				TestCaseUtils.getTestCaseProviders(Sections.cast(section.getFather(),
						TestCasePlayerType.class));

		// check cases and write zip file and entries
		ZipOutputStream out = new ZipOutputStream(new FileOutputStream(resultFile));

		StringBuilder skipped = new StringBuilder();

		boolean atLeastOne = false;
		int i = 0;
		for (Triple<TestCaseProvider, Section<?>, Article> triple : providers) {

			Section<?> testCaseSection = triple.getB();
			TestCaseProvider provider = triple.getA();
			String testCaseName = provider.getName();

			listener.updateProgress(i++ / providers.size(), "Transforming and zipping case '"
					+ testCaseName + "'.");

			if (!userCanView(context, section) || !userCanView(context, testCaseSection)) {
				skipped.append("'" + testCaseName
						+ "' was skipped because you are not authorized to see it.\n");
				continue;
			}

			TestCase testCase = provider.getTestCase();
			Session session = provider.getActualSession(context);

			if (session == null) {
				skipped.append("'" + testCaseName
						+ "' was skipped because of an internal error (no session found).\n");
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
						+ e.getMessage() + "\n");
				continue;
			}

			ZipEntry e = new ZipEntry(CheckDownloadCaseAction.toXMLFileName(testCaseName));
			out.putNextEntry(e);
			TestPersistence.getInstance().writeCases(out,
					Arrays.asList(sequentialTestCase), false);

			out.closeEntry();
			atLeastOne = true;
			// System.out.println("Wrote " + testCaseName);
		}
		listener.updateProgress(1f, "Transformed all cases.");

		out.flush();
		out.close();

		// write response
		List<String> json = new ArrayList<String>();

		String skippedString = skipped.toString();
		if (!skippedString.isEmpty()) {
			json.add("skipped");
			json.add(skippedString);
		}
		if (!atLeastOne) {
			json.add("error");
			json.add("There are not test cases to download.");
		}

		CheckDownloadCaseAction.sendJSON(context, json.toArray(new String[json.size()]));

	}

}
