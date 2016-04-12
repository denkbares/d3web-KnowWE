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
package de.d3web.we.ci4ke.test;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;

import de.d3web.collections.CountingSet;
import de.d3web.testing.AbstractTest;
import de.d3web.testing.Message;
import de.d3web.testing.MessageObject;
import de.d3web.testing.TestingUtils;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.report.Message.Type;
import de.knowwe.core.report.Messages;
import de.knowwe.core.compile.Compiler;

/**
 * Abstract test to check of Messages in Articles.
 *
 * @author Marc-Oliver Ochlast, Albrecht Striffler (denkbares GmbH)
 */
public abstract class ArticleHasMessagesTest extends AbstractTest<Article> {

	private final Type type;

	public ArticleHasMessagesTest(Type type) {
		this.type = type;
	}

	@Override
	public Message execute(Article moni, String[] args2, String[]... ignores) throws InterruptedException {

		boolean hasError = false;
		StringBuilder buffer = new StringBuilder();

		Collection<de.knowwe.core.report.Message> messages = new LinkedList<de.knowwe.core.report.Message>();
		Map<Compiler, Collection<de.knowwe.core.report.Message>> allMessagesMap =
				Messages.getMessagesMapFromSubtree(moni.getRootSection(), type);

		for (de.knowwe.core.compile.Compiler s : allMessagesMap.keySet()) {
			messages.addAll(allMessagesMap.get(s));
		}

		TestingUtils.checkInterrupt();

		buffer.append(" " + type.toString().toLowerCase() + "s found in article '").append(moni.getTitle()).append("'");
		if (!messages.isEmpty()) {
			// This finds only messages, that are explicitly stored
			// as Message.ERROR, because the Type Message.UNKNOWN_ERROR
			// is not public!
			hasError = true;
			CountingSet<de.knowwe.core.report.Message> msgSet =
					new CountingSet<de.knowwe.core.report.Message>();
			for (de.knowwe.core.report.Message message : messages) {
				msgSet.add(message);
			}
			for (de.knowwe.core.report.Message message : msgSet) {
				buffer.append("\n* ").append(message.getVerbalization());
				int count = msgSet.getCount(message);
				if (count > 1) buffer.append(" (").append(count).append("&times;)");
			}
		}
		if (hasError) {
			return new Message(
					Message.Type.FAILURE, buffer.toString(),
					new MessageObject(moni.getTitle(), Article.class));
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
}