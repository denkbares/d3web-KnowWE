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
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.denkbares.collections.CountingSet;
import com.denkbares.strings.Strings;
import de.d3web.testing.AbstractTest;
import de.d3web.testing.Message;
import de.d3web.testing.MessageObject;
import de.d3web.testing.TestParameter;
import de.d3web.testing.TestResult;
import de.d3web.testing.TestingUtils;
import de.d3web.we.ci4ke.build.CIRenderer;
import de.knowwe.core.compile.Compiler;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.report.Message.Type;
import de.knowwe.core.report.Messages;
import de.knowwe.core.utils.KnowWEUtils;

/**
 * Abstract test to check of Messages in Articles.
 *
 * @author Marc-Oliver Ochlast, Albrecht Striffler (denkbares GmbH)
 */
public abstract class ArticleHasMessagesTest extends AbstractTest<Article> implements ResultRenderer {

	private final Type type;

	public ArticleHasMessagesTest(Type type) {
		this.type = type;
		this.addIgnoreParameter("allowed-message-regex", TestParameter.Type.Regex, TestParameter.Mode.Optional,
				"Specify regular expression of messages that are ignored by this test");
	}

	@Override
	public Message execute(Article moni, String[] args2, String[]... ignores) throws InterruptedException {

		boolean hasError = false;
		boolean hasWarning = false;
		StringBuilder buffer = new StringBuilder();

		Map<Compiler, Collection<de.knowwe.core.report.Message>> allMessagesMap =
				Messages.getMessagesMapFromSubtree(moni.getRootSection(), type);

		List<Pattern> ignorePatterns = Stream.of(ignores)
				.flatMap(Stream::of)
				.map(Strings::unquote)
				.map(Pattern::compile)
				.collect(Collectors.toList());

		List<de.knowwe.core.report.Message> messages = allMessagesMap.values()
				.stream()
				.flatMap(Collection::stream)
				.filter(s -> ignorePatterns.stream().noneMatch(p -> p.matcher(s.getVerbalization()).find()))
				.collect(Collectors.toList());

		TestingUtils.checkInterrupt();

		buffer.append(" ")
				.append(type.toString().toLowerCase())
				.append("s found in article '[")
				.append(fixTitle(moni.getTitle()))
				.append("]'");
		if (!messages.isEmpty()) {
			for (de.knowwe.core.report.Message message : messages) {
				if (message.getType() == Type.ERROR) {
					hasError = true;
					break;
				}
				if (message.getType() == Type.WARNING) {
					hasWarning = true;
				}
			}
			CountingSet<de.knowwe.core.report.Message> msgSet =
					new CountingSet<>();
			msgSet.addAll(messages);
			for (de.knowwe.core.report.Message message : msgSet) {
				String verbalization = message.getVerbalization();
				if (message.getDisplay() == de.knowwe.core.report.Message.Display.PLAIN) {
					verbalization = KnowWEUtils.maskJSPWikiMarkup(verbalization);
				}
				buffer.append("\n* ").append(verbalization);
				int count = msgSet.getCount(message);
				if (count > 1) buffer.append(" (").append(count).append("&times;)");
			}
		}
		if (hasError) {
			return new Message(
					Message.Type.FAILURE, buffer.toString(),
					new MessageObject(moni.getTitle(), Article.class));
		}
		if (hasWarning) {
			return new Message(
					Message.Type.WARNING, buffer.toString(),
					new MessageObject(moni.getTitle(), Article.class));
		}
		else {
			return new Message(Message.Type.SUCCESS, null);
		}
	}

	private String fixTitle(String title) {
		if (title.contains("@")) { // somehow JSPWiki links cannot handle @
			return title + "|" + KnowWEUtils.getAsAbsoluteLink("Wiki.jsp?page=" + Strings.encodeURL(title));
		}
		else {
			return title;
		}
	}

	@Override
	public Class<Article> getTestObjectClass() {
		return Article.class;
	}

	@Override
	public void renderResultMessage(String web, String testObjectName, Message message, TestResult testResult, RenderResult renderResult) {
		Class<?> testObjectClass = CIRenderer.renderResultMessageHeader(web, message, testResult, renderResult);
		renderResult.append(message.getText());
		CIRenderer.renderResultMessageFooter(web, testObjectName, testObjectClass, message, renderResult);
	}
}
