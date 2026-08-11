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
 * <p>
 * Ignores without a prefix are interpreted as regular expressions for the message text. To ignore all messages from
 * matching article titles, prefix the regular expression with {@value #ARTICLE_IGNORE_PREFIX}, for example
 * {@code ignore: "article:Solution.*"}.
 *
 * @author Veronika Sehne (denkbares GmbH)
 * @created 22.10.20
 */
public abstract class CompilerHasMessagesTest extends AbstractTest<PackageCompiler> implements ResultRenderer {

	/**
	 * Prefix for ignore entries that should match against article titles instead of message verbalizations.
	 */
	private static final String ARTICLE_IGNORE_PREFIX = "article:";

	private final Message.Type type;

	public CompilerHasMessagesTest(Message.Type type) {
		this.type = type;
		this.addIgnoreParameter("allowed-message-regex", TestParameter.Type.Regex, TestParameter.Mode.Optional,
				"Specify regular expression of messages that are ignored by this test. <br>Prefix with 'article:' to ignore all messages from matching article titles.");
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

		IgnorePatterns ignorePatterns = compileIgnores(specification.getIgnores());

		//noinspection DataFlowIssue
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

	private List<Message> getMessages(PackageCompiler compiler, Collection<Section<?>> sectionsOfPackage, IgnorePatterns ignorePatterns) {
		Map<Section<?>, Collection<Message>> allMessagesMap = sectionsOfPackage.stream().distinct()
				.collect(Collectors.toMap(s -> s, s -> new ConcatenateCollection<>(Messages.getMessagesFromSubtree(compiler, s, type), Messages
						.getMessagesFromSubtree(s, type))));

		return allMessagesMap.entrySet()
				.stream()
				.flatMap(e -> e.getValue().stream()
						.filter(m -> shouldReport(m, e.getKey(), ignorePatterns)))
				.collect(Collectors.toList());
	}

	@Override
	public Class<PackageCompiler> getTestObjectClass() {
		return PackageCompiler.class;
	}

	@Override
	public void renderResultMessage(UserContext context, String testObjectName, de.d3web.testing.Message message, TestResult testResult, RenderResult renderResult) {
		Class<?> testObjectClass = CIRenderer.renderResultMessageHeader(message, testResult, renderResult);
		renderResult.append(message.getText());
		CIRenderer.renderResultMessageFooter(context, testObjectName, testObjectClass, message, renderResult);
	}

	private void appendMessages(PackageCompiler compiler, Map<String, List<Section<?>>> sectionsByTitle, int totalNumberOfMessages, IgnorePatterns ignorePatterns, StringBuilder buffer) {
		ArrayList<String> titles = new ArrayList<>(sectionsByTitle.keySet());
		titles.sort(NumberAwareComparator.CASE_INSENSITIVE);
		for (String title : titles) {
			List<Section<?>> sections = sectionsByTitle.get(title);
			Map<? extends Section<?>, List<Message>> messagesBySection = sections
					.stream()
					.collect(Collectors.toMap(s -> s, s -> new ConcatenateCollection<>(Messages.getMessagesFromSubtree(compiler, s, type),
							Messages.getMessagesFromSubtree(s, type)).stream()
							.filter(m -> shouldReport(m, s, ignorePatterns))
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

	/**
	 * Splits ignore declarations into message-text patterns and article-title patterns. This keeps existing message
	 * ignores backward-compatible while allowing article-wide message suppression through prefixed ignore entries.
	 */
	private static IgnorePatterns compileIgnores(String[][] ignores) {
		List<Pattern> messagePatterns = new ArrayList<>();
		List<Pattern> articlePatterns = new ArrayList<>();
		Stream.of(ignores)
				.flatMap(Stream::of)
				.map(Strings::unquote)
				.forEach(ignore -> addIgnorePattern(ignore, messagePatterns, articlePatterns));
		return new IgnorePatterns(messagePatterns, articlePatterns);
	}

	/**
	 * Adds an ignore declaration to the appropriate pattern list. Prefixed declarations target article titles; all
	 * other declarations keep the historical behavior and target message verbalization.
	 */
	private static void addIgnorePattern(String ignore, List<Pattern> messagePatterns, List<Pattern> articlePatterns) {
		String articleRegex = removeArticlePrefix(ignore);
		if (articleRegex != null && !articleRegex.isBlank()) {
			articlePatterns.add(Pattern.compile(articleRegex));
		}
		else {
			messagePatterns.add(Pattern.compile(ignore));
		}
	}

	/**
	 * Removes the specified prefix case-insensitively and returns {@code null} if the text does not start with it.
	 */
	private static String removeArticlePrefix(String text) {
		if (text.regionMatches(true, 0, CompilerHasMessagesTest.ARTICLE_IGNORE_PREFIX, 0, CompilerHasMessagesTest.ARTICLE_IGNORE_PREFIX.length())) {
			return text.substring(CompilerHasMessagesTest.ARTICLE_IGNORE_PREFIX.length());
		}
		return null;
	}

	/**
	 * Returns whether a compiler message should be shown after excluding test-internal messages and configured ignores.
	 */
	private static boolean shouldReport(Message message, Section<?> section, IgnorePatterns ignorePatterns) {
		if (message.getSource() instanceof Class<?> classSource && Test.class.isAssignableFrom(classSource)) return false;
		return !ignorePatterns.isIgnored(message, section);
	}

	/**
	 * Compiled ignore patterns for the two supported matching targets.
	 */
	private record IgnorePatterns(List<Pattern> messagePatterns, List<Pattern> articlePatterns) {

		/**
		 * Checks article-title ignores before message-text ignores, because an article ignore suppresses all messages
		 * produced below the matched section title.
		 */
		private boolean isIgnored(Message message, Section<?> section) {
			String title = section.getTitle();
			if (title != null && articlePatterns.stream().anyMatch(p -> p.matcher(title).find())) return true;

			String verbalization = message.getVerbalization();
			return verbalization != null && messagePatterns.stream().anyMatch(p -> p.matcher(verbalization).find());
		}
	}
}
