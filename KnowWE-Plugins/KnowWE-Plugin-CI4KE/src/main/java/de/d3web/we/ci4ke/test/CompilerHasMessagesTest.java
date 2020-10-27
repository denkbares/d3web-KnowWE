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

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.denkbares.collections.ConcatenateCollection;
import com.denkbares.strings.Strings;
import de.d3web.testing.AbstractTest;
import de.d3web.testing.MessageObject;
import de.d3web.testing.TestParameter;
import de.d3web.testing.TestResult;
import de.d3web.testing.TestSpecification;
import de.d3web.testing.TestingUtils;
import de.d3web.we.ci4ke.build.CIRenderer;
import de.knowwe.core.compile.Compiler;
import de.knowwe.core.compile.NamedCompiler;
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

		String name;
		if (compiler instanceof NamedCompiler) {
			name = ((NamedCompiler) compiler).getName();
		}
		else {
			name = compiler.getCompileSection().getTitle();
		}

		buffer.append(" ")
				.append(type.toString().toLowerCase())
				.append("s found in compiler '")
				.append(name)
				.append("'");
		if (!messages.isEmpty()) {
			for (de.knowwe.core.report.Message message : messages) {
				if (message.getType() == Message.Type.ERROR) {
					hasError = true;
					break;
				}
				if (message.getType() == Message.Type.WARNING) {
					hasWarning = true;
				}
			}
			appendMessages(compiler, sectionsByTitle, ignorePatterns, buffer);
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

	private void appendMessages(PackageCompiler compiler, Map<String, List<Section<?>>> sectionsByTitle, List<Pattern> ignorePatterns, StringBuilder buffer) {
		for (Map.Entry<String, List<Section<?>>> entry : sectionsByTitle.entrySet()) {
			String title = entry.getKey();
			Map<? extends Section<?>, List<Message>> messagesBySection = entry.getValue()
					.stream()
					.collect(Collectors.toMap(s -> s, s -> new ConcatenateCollection<>(Messages.getMessagesFromSubtree(compiler, s, type), Messages
							.getMessagesFromSubtree(s, type)).stream()
							.filter(m -> ignorePatterns.stream()
									.noneMatch(p -> p.matcher(m.getVerbalization()).find()))
							.collect(Collectors.toList())));
			int sum = messagesBySection.values().stream().mapToInt(Collection::size).sum();
			if (sum > 0) {
				buffer.append("\n\n__[")
						.append(title)
						.append("]__ has ")
						.append(Strings.pluralOf(sum, type.name().toLowerCase()))
						.append(":");
				for (Map.Entry<? extends Section<?>, List<Message>> listEntry : messagesBySection.entrySet()) {
					for (Message message : listEntry.getValue()) {
						String verbalization = message.getVerbalization();
						if (message.getDisplay() == de.knowwe.core.report.Message.Display.PLAIN) {
							verbalization = KnowWEUtils.maskJSPWikiMarkup(verbalization);
						}
						buffer.append("\n* ")
								.append("[")
								.append(verbalization)
								.append("|")
								.append(KnowWEUtils.getWikiLink(listEntry.getKey()))
								.append("]");
					}
				}
			}
		}
	}
}
