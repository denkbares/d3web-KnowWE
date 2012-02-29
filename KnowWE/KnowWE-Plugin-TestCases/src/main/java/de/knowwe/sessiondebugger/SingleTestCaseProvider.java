/*
 * Copyright (C) 2012 denkbares GmbH
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
package de.knowwe.sessiondebugger;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.d3web.core.session.Session;
import de.d3web.testcase.model.TestCase;
import de.d3web.we.utils.D3webUtils;
import de.knowwe.core.KnowWEEnvironment;
import de.knowwe.core.kdom.KnowWEArticle;
import de.knowwe.core.report.Message;

/**
 * Capsules a {@link TestCase}
 * 
 * @author Markus Friedrich (denkbares GmbH)
 * @created 27.02.2012
 */
public class SingleTestCaseProvider implements TestCaseProvider {

	private TestCase testCase;
	private final KnowWEArticle article;
	private final String name;
	private final Map<String, SessionDebugStatus> statusPerUser = new HashMap<String, SessionDebugStatus>();

	public SingleTestCaseProvider(TestCase testCase, KnowWEArticle article, String name) {
		super();
		this.testCase = testCase;
		this.article = article;
		this.name = name;
	}

	@Override
	public TestCase getTestCase() {
		return testCase;
	}

	@Override
	public Session getActualSession(String user) {
		return D3webUtils.getSession(article.getTitle(), user, article.getWeb());
	}

	@Override
	public SessionDebugStatus getDebugStatus(String user) {
		SessionDebugStatus status = statusPerUser.get(user);
		if (status == null) {
			status = new SessionDebugStatus(getActualSession(user));
			statusPerUser.put(user, status);
		}
		return status;
	}

	@Override
	public void storeSession(Session session, String user) {
		String sessionId = KnowWEEnvironment.generateDefaultID(article.getTitle());
		D3webUtils.getBroker(user, article.getWeb()).addSession(sessionId, session);
		getDebugStatus(user).setSession(session);
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public List<Message> getMessages() {
		return Collections.emptyList();
	}

}
