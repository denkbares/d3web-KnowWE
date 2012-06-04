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

import de.d3web.testing.Message;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.report.Messages;

/**
 * This tests checks, if
 * 
 * @author Marc-Oliver Ochlast
 * @created 29.05.2010
 */
public class ArticleHasErrorsTest extends AbstractTest<Article> {

	@Override
	public Message execute(Article moni, String[] args2) {

		boolean hasError = false;
		StringBuffer buffy = new StringBuffer();

		String monitoredArticleTitle = moni.getTitle();

		Collection<de.knowwe.core.report.Message> messages = new LinkedList<de.knowwe.core.report.Message>();
		for (Section<?> sec : moni.getReviseIterator().getAllSections()) {
			messages.addAll(Messages.getErrors(Messages.getMessages(moni, sec)));
		}

		buffy.append("<a href=\"Wiki.jsp?page=" + monitoredArticleTitle + "\"> "
				+ monitoredArticleTitle + "</a>:\n");
		buffy.append("<ul>");
		for (de.knowwe.core.report.Message message : messages) {
			// This finds only messages, that are explicitly stored
			// as Message.ERROR, because the Type Message.UNKNOWN_ERROR
			// is not public!
			hasError = true;
			// buffy.append("Error on monitored article: ");
			buffy.append("<li> " + message.getVerbalization() + "</li>");
		}
		buffy.append("</ul>");
		if (hasError) {
			return new Message(
					Message.Type.FAILURE, buffy.toString());
		}
		else {
			return new Message(
					Message.Type.SUCCESS, null);
		}
	}

	@Override
	public Class<Article> getTestObjectClass() {
		return Article.class;
	}

	@Override
	public int numberOfArguments() {
		return 0;
	}
}
