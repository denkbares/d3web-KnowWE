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
package de.knowwe.testcases;

import java.util.LinkedList;
import java.util.List;

import de.d3web.core.knowledge.KnowledgeBase;
import de.d3web.core.session.Session;
import de.d3web.testcase.model.TestCase;
import de.d3web.we.basic.SessionProvider;
import de.d3web.we.utils.D3webUtils;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.report.Message;
import de.knowwe.core.report.Messages;
import de.knowwe.core.user.UserContext;
import de.knowwe.core.utils.KnowWEUtils;
import de.knowwe.core.wikiConnector.ConnectorAttachment;

/**
 * Abstract class providing all methods to create a {@link TestCaseProvider}
 * based on an Attachment
 * 
 * @author Markus Friedrich (denkbares GmbH)
 * @created 27.01.2012
 */
public abstract class AttachmentTestCaseProvider implements TestCaseProvider {

	protected TestCase testCase;
	protected ConnectorAttachment attachment;
	protected List<Message> messages = new LinkedList<Message>();
	private List<Message> testCaseMessages = new LinkedList<Message>();

	protected final Article article;

	public AttachmentTestCaseProvider(Article article, ConnectorAttachment attachment) {
		super();
		this.article = article;
		this.attachment = attachment;
		parse();
	}

	@Override
	public TestCase getTestCase() {
		ConnectorAttachment actualAttachment = KnowWEUtils.getAttachment(
				attachment.getParentName(),
				attachment.getFileName());
		if (actualAttachment == null) {
			messages.clear();
			messages.add(Messages.error("File " + attachment.getFileName()
					+ " cannot be found attached to this article.\n"));
			return null;
		}
		if (attachment == null || !attachment.getDate().equals(actualAttachment.getDate())) {
			attachment = actualAttachment;
			messages.clear();
			parse();
		}
		return testCase;
	}

	public abstract void parse();

	@Override
	public List<Message> getMessages() {
		List<Message> result = new LinkedList<Message>();
		result.addAll(messages);
		result.addAll(testCaseMessages);
		return result;
	}

	@Override
	public Session getActualSession(UserContext user) {
		KnowledgeBase kb = D3webUtils.getKnowledgeBase(user.getWeb(), article.getTitle());
		Session session = SessionProvider.getSession(user, kb);
		return session;
	}

	@Override
	public void storeSession(Session session, UserContext user) {
		SessionProvider provider = SessionProvider.getSessionProvider(user);
		provider.setSession(session);
		getDebugStatus(user).setSession(session);
	}

	@Override
	public SessionDebugStatus getDebugStatus(UserContext user) {
		String key = "SessionDebugStatus_" + article.getTitle() + "/" + getName();
		SessionDebugStatus status = (SessionDebugStatus) user.getSession().getAttribute(key);
		if (status == null) {
			status = new SessionDebugStatus(getActualSession(user));
			user.getSession().setAttribute(key, status);
		}
		return status;
	}

	@Override
	public String getName() {
		return attachment.getPath();
	}

	protected void updateTestCaseMessages(KnowledgeBase kb) {
		testCaseMessages = new LinkedList<Message>();
		if (testCase != null) {
			for (String s : testCase.check(kb)) {
				testCaseMessages.add(Messages.error(attachment.getPath() + ": " + s));
			}
		}
	}

}