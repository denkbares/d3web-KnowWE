/*
 * Copyright (C) 2011 denkbares GmbH
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

/**
 * 
 * 
 * @author Albrecht Striffler
 * @created 30.01.2011
 */
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
				null, lastKB).replaceAll("<.+?>", "");
		String currentKBOutput = new KBRenderer().renderHTML(KnowWEEnvironment.DEFAULT_WEB, title,
				null, currentKB).replaceAll("<.+?>", "");

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
					+ "incremental compile below\n+++++++++++++++++++++++\n" + lastKBOutput;
			KnowWEUtils.writeFile(KnowWEUtils.getVersionsSavePath() + fileName, logContent);

			return new CITestResult(TestResultType.FAILED,
					"Detected difference in the knowledgebase after a full reparse. Check "
							+ fileName + " for results.");
		}

		return new CITestResult(TestResultType.SUCCESSFUL,
				"No difference detected in the d3web KnowledgeBase after a full reparse.");
	}

}
