/*
 * Copyright (C) 2020 denkbares GmbH, Germany
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
 *
 */

package de.d3web.we.ci4ke.test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jetbrains.annotations.NotNull;

import com.denkbares.collections.ConcatenateCollection;
import com.denkbares.strings.NumberAwareComparator;
import com.denkbares.strings.Strings;
import de.d3web.testing.AbstractTest;
import de.d3web.testing.MessageObject;
import de.d3web.testing.Test;
import de.d3web.testing.TestParameter;
import de.d3web.testing.TestResult;
import de.d3web.testing.TestSpecification;
import de.d3web.testing.TestingUtils;
import de.d3web.we.ci4ke.build.CIRenderer;
import de.knowwe.core.Environment;
import de.knowwe.core.compile.Compiler;
import de.knowwe.core.compile.PackageCompiler;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.report.Message;
import de.knowwe.core.report.Messages;
import de.knowwe.core.user.UserContext;
import de.knowwe.core.utils.KnowWEUtils;

/**
 * Abstract test to check of Messages in Compilers.
 *
 * @author Veronika Sehne (denkbares GmbH)
 * @created 22.10.20
 */
public abstract class CompilerHasMessagesTest extends AbstractTest<PackageCompiler> implements ResultRenderer {

	private final Message.Type type;

	public CompilerHasMessagesTest(Message.Type type) {
		this.type = type;
		this.addIgnoreParameter("allowed-message-regex", TestParameter.Type.Regex, TestParameter.Mode.Optional,
				"Specify regular expression of messages that are ignored by this test");
	}

	@Override
	public de.d3web.testing.Message execute(TestSpecification<PackageCompiler> specification, PackageCompiler compiler) throws InterruptedException {
		boolean hasError = false;
		boolean hasWarning = false;
		StringBuilder buffer = new StringBuilder();

		String[] packagesToCompile = compiler.getCompileSection()
				.get()
				.getPackagesToCompile(compiler.getCompileSection());
		Collection<Section<?>> sectionsOfPackage = compiler.getPackageManager().getSectionsOfPackage(packagesToCompile);

		List<Pattern> ignorePatterns = Stream.of(specification.getIgnores())
				.flatMap(Stream::of)
				.map(Strings::unquote)
				.map(Pattern::compile)
				.collect(Collectors.toList());

		Map<String, List<Section<?>>> sectionsByTitle = sectionsOfPackage.stream()
				.distinct()
				.collect(Collectors.groupingBy(Section::getTitle));

		List<de.knowwe.core.report.Message> messages = getMessages(compiler, sectionsOfPackage, ignorePatterns);

		TestingUtils.checkInterrupt();

		int totalNumberOfMessages = messages.size();
		buffer.append(" ")
				.append(Strings.pluralOf(totalNumberOfMessages, type.toString().toLowerCase()))
				.append(" found in compiler [")
				.append(compiler.getName())
				.append("|")
				.append(getWikiLink(compiler.getCompileSection()));
		if (!messages.isEmpty()) {
			buffer.append("]\n\n");
			for (de.knowwe.core.report.Message message : messages) {
				if (message.getType() == Message.Type.ERROR) {
					hasError = true;
					break;
				}
				if (message.getType() == Message.Type.WARNING) {
					hasWarning = true;
				}
			}
			appendMessages(compiler, sectionsByTitle, totalNumberOfMessages, ignorePatterns, buffer);
		}
		if (hasError) {
			return new de.d3web.testing.Message(
					de.d3web.testing.Message.Type.FAILURE, buffer.toString(),
					new MessageObject(compiler.toString(), Compiler.class));
		}
		if (hasWarning) {
			return new de.d3web.testing.Message(
					de.d3web.testing.Message.Type.WARNING, buffer.toString(),
					new MessageObject(compiler.toString(), Compiler.class));
		}
		else {
			return new de.d3web.testing.Message(de.d3web.testing.Message.Type.SUCCESS, null);
		}
	}

	private List<Message> getMessages(PackageCompiler compiler, Collection<Section<?>> sectionsOfPackage, List<Pattern> ignorePatterns) {
		Map<Section<?>, Collection<Message>> allMessagesMap = sectionsOfPackage.stream().distinct()
				.collect(Collectors.toMap(s -> s, s -> new ConcatenateCollection<>(Messages.getMessagesFromSubtree(compiler, s, type), Messages
						.getMessagesFromSubtree(s, type))));

		return allMessagesMap.values()
				.stream()
				.flatMap(Collection::stream)
				.filter(m -> !(m.getSource() instanceof Class<?> classSource) || !Test.class.isAssignableFrom(classSource))
				.filter(m -> ignorePatterns.stream().noneMatch(p -> p.matcher(m.getVerbalization()).find()))
				.collect(Collectors.toList());
	}

	@Override
	public Class<PackageCompiler> getTestObjectClass() {
		return PackageCompiler.class;
	}

	@Override
	public void renderResultMessage(UserContext context, String testObjectName, de.d3web.testing.Message message, TestResult testResult, RenderResult renderResult) {
		Class<?> testObjectClass = CIRenderer.renderResultMessageHeader(context, message, testResult, renderResult);
		renderResult.append(message.getText());
		CIRenderer.renderResultMessageFooter(context, testObjectName, testObjectClass, message, renderResult);
	}

	private void appendMessages(PackageCompiler compiler, Map<String, List<Section<?>>> sectionsByTitle, int totalNumberOfMessages, List<Pattern> ignorePatterns, StringBuilder buffer) {
		ArrayList<String> titles = new ArrayList<>(sectionsByTitle.keySet());
		titles.sort(NumberAwareComparator.CASE_INSENSITIVE);
		for (String title : titles) {
			List<Section<?>> sections = sectionsByTitle.get(title);
			Map<? extends Section<?>, List<Message>> messagesBySection = sections
					.stream()
					.collect(Collectors.toMap(s -> s, s -> new ConcatenateCollection<>(Messages.getMessagesFromSubtree(compiler, s, type),
							Messages.getMessagesFromSubtree(s, type)).stream().filter(m -> ignorePatterns.stream()
									.noneMatch(p -> p.matcher(m.getVerbalization()).find()))
							.collect(Collectors.toList())));

			List<Map.Entry<? extends Section<?>, List<Message>>> messagesBySectionSorted = sortMessagesBySection(messagesBySection);
			int sum = messagesBySection.values().stream().mapToInt(Collection::size).sum();
			if (sum > 0) {
				Section<?> section = sections.get(0);
				if (totalNumberOfMessages > 1000) {
					buffer.append("\n* [")
							.append(title)
							.append("|")
							.append(getWikiLink(section))
							.append("] (")
							.append(Strings.pluralOf(sum, type.name().toLowerCase()))
							.append(")");
				}
				else {
					buffer.append("\n\n__[")
							.append(title)
							.append("|")
							.append(getWikiLink(section))
							.append("]__ has ")
							.append(Strings.pluralOf(sum, type.name().toLowerCase()))
							.append(":");
					for (Map.Entry<? extends Section<?>, List<Message>> listEntry : messagesBySectionSorted) {

						List<Message> sortedMessages = sortMessages(listEntry);
						for (Message message : sortedMessages) {
							String verbalization = message.getVerbalization();
							if (message.getDisplay() == de.knowwe.core.report.Message.Display.PLAIN) {
								verbalization = KnowWEUtils.maskJSPWikiMarkup(verbalization.replaceAll("[\\[\\]|]", ""));
								buffer.append("\n* ")
										.append("[")
										.append(verbalization)
										.append("|")
										.append(getWikiLink(listEntry.getKey()))
										.append("]");
							}
							else {
								buffer.append("\n* ").append(message.getVerbalization());
							}
						}
					}
				}
			}
		}
	}

	private String getWikiLink(Section<?> section) {
		String wikiLink;
		String title = section.getTitle();
		if (title != null && title.contains("/") && !KnowWEUtils.isAttachmentArticle(section.getArticle())) {
			wikiLink = Environment.getInstance().getWikiConnector().getBaseUrl() + KnowWEUtils.getURLLink(title);
		}
		else {
			wikiLink = KnowWEUtils.getWikiLinkPart(section);
		}
		return wikiLink;
	}

	private List<Map.Entry<? extends Section<?>, List<Message>>> sortMessagesBySection(Map<? extends Section<?>, List<Message>> messagesBySection) {
		Comparator<Map.Entry<? extends Section<?>, List<Message>>> complexComparator = (entry1, entry2) -> {
			int sectionCompare = entry1.getKey().compareTo(entry2.getKey());
			if (sectionCompare != 0) {
				return sectionCompare;
			}
			List<Message> list1 = entry1.getValue();
			List<Message> list2 = entry2.getValue();

			int minLength = Math.min(list1.size(), list2.size());
			for (int i = 0; i < minLength; i++) {
				int messageCompare = createMessageString(list1.get(i), entry1.getKey()).compareTo(createMessageString(list2.get(i), entry2.getKey()));
				if (messageCompare != 0) {
					return messageCompare;
				}
			}
			return Integer.compare(list1.size(), list2.size());
		};
		return messagesBySection.entrySet().stream()
				.sorted(complexComparator)
				.collect(Collectors.toList());
	}

	@NotNull
	private List<Message> sortMessages(Map.Entry<? extends Section<?>, List<Message>> listEntry) {
		Section<?> sectionKey = listEntry.getKey();
		Comparator<Message> fullStringComparator = (message1, message2) -> {
			String fullString1 = createMessageString(message1, sectionKey);
			String fullString2 = createMessageString(message2, sectionKey);
			return fullString1.compareTo(fullString2);
		};
		return listEntry.getValue()
				.stream()
				.sorted(fullStringComparator)
				.toList();
	}

	private String createMessageString(Message message, Section<?> section) {
		String verbalization = message.getVerbalization();
		if (message.getDisplay() == de.knowwe.core.report.Message.Display.PLAIN) {
			verbalization = KnowWEUtils.maskJSPWikiMarkup(verbalization.replaceAll("[\\[\\]|]", ""));
			return "\n* " + "[" + verbalization + "|" + getWikiLink(section) + "]";
		}
		else {
			return verbalization;
		}
	}
}
