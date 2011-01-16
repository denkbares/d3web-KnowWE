/*
 * Copyright (C) 2010 University Wuerzburg, Computer Science VI
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

import java.util.Collection;
import java.util.LinkedList;

import de.d3web.we.ci4ke.testing.AbstractCITest;
import de.d3web.we.ci4ke.testing.CITestResult;
import de.d3web.we.ci4ke.testing.CITestResult.TestResultType;
import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.report.KDOMError;
import de.d3web.we.utils.KnowWEUtils;

/**
 * 
 * @author Marc-Oliver Ochlast
 * @created 29.05.2010
 */
public class ArticleHasErrorsTest extends AbstractCITest {

	@Override
	public CITestResult call() throws Exception {
		boolean hasError = false;
		StringBuffer buffy = new StringBuffer();

		String monitoredArticleTitle = getParameter(0);
		if (monitoredArticleTitle.isEmpty()) {
			return new CITestResult(TestResultType.FAILED, "Parameter 0 was invalid!");
		}

		KnowWEArticle moni = KnowWEEnvironment.getInstance().getArticle(
				KnowWEEnvironment.DEFAULT_WEB, monitoredArticleTitle);
		if (moni == null) {
			return new CITestResult(TestResultType.FAILED, "MonitoredArticle not found or invalid!");
		}

		Collection<KDOMError> messages = new LinkedList<KDOMError>();
		for (Section<?> sec : moni.getReviseIterator().getAllSections()) {
			messages.addAll(KnowWEUtils.getMessages(moni, sec, KDOMError.class));
		}

		for (KDOMError message : messages) {
			// This finds only messages, that are explicitly stored
			// as Message.ERROR, because the Type Message.UNKNOWN_ERROR
			// is not public!
			hasError = true;
			buffy.append("Error on monitored article: ");
			buffy.append(message.getVerbalization());
			buffy.append("<br/><br/>");
		}
		if (hasError) {
			return new CITestResult(TestResultType.FAILED, buffy.toString());
		}
		else {
			return new CITestResult(TestResultType.SUCCESSFUL,
					"No error on monitored Article!");
		}
	}
}
