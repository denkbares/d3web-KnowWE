/*
 * Copyright (C) 2026 denkbares GmbH, Germany
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

package de.d3web.we.ci4ke.build;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;

import com.denkbares.utils.Pair;
import com.denkbares.utils.Streams;
import de.d3web.testing.BuildResult;
import de.d3web.testing.Message;
import de.d3web.testing.TestResult;
import de.d3web.we.ci4ke.dashboard.CIDashboard;
import de.d3web.we.ci4ke.dashboard.action.CIFreezeFailedTestsAction;
import de.knowwe.core.Environment;
import de.knowwe.core.wikiConnector.WikiAttachment;

import static de.d3web.we.ci4ke.dashboard.action.CIFreezeFailedTestsAction.*;

/**
 * Marks test as Frozen according to attachments
 *
 * @author Philipp Sehne (denkbares GmbH)
 * @created 08.05.2026
 */
class CIBuildFrozenTestAdjuster {

	static void adjustFrozenTests(BuildResult buildResult, CIDashboard dashboard) throws IOException {
		if (buildResult == null) return;
		List<TestResult> testResults = buildResult.getResults();
		List<TestResult> newResults = new ArrayList<>();
		List<TestResult> removeResults = new ArrayList<>();
		for (TestResult testResult : testResults) {
			if (isFrozenTest(testResult, dashboard)) {
				Map<String, Message> unexpectedMessagesNormal = Collections.synchronizedMap(new TreeMap<>());
				Map<String, Message> unexpectedMessagesFrozen = Collections.synchronizedMap(new TreeMap<>());
				Map<String, Message> expectedMessages = Collections.synchronizedMap(new TreeMap<>());
				boolean isFullyFrozen = true;

				Optional<WikiAttachment> attachment = Environment.getInstance().getWikiConnector().getAttachments(dashboard.getDashboardArticle()).stream().filter(a -> a.getFileName().contains(CIFreezeFailedTestsAction.getFileName(dashboard, testResult))).findFirst();
				if (attachment.isEmpty()) continue;
				String fileText = Streams.getTextAndClose(attachment.get().getInputStream());

				for (String testObject : testResult.getTestObjectsWithUnexpectedOutcome()) {
					Message originalMessage = testResult.getMessageForTestObject(testObject);
					Pair<Message, Message> messagePair = splitText(originalMessage, fileText, testObject);
					String normalText = messagePair.getA().getText();
					String frozenText = messagePair.getB().getText();
					if (!normalText.isEmpty()) {
						unexpectedMessagesNormal.put(testObject, messagePair.getA());
						isFullyFrozen = false;
					}
					if (!frozenText.isEmpty()) {
						unexpectedMessagesFrozen.put(testObject, messagePair.getB());
					}
				}

				for (String testObject : testResult.getTestObjectsWithExpectedOutcome()) {
					expectedMessages.put(testObject, testResult.getMessageForTestObject(testObject));
				}
				TestResult normalTest;
				TestResult frozenTest;
				if (isFullyFrozen) {
					normalTest = TestResult.createTestResult(testResult.getTestName(), testResult.getConfiguration(),  Collections.synchronizedMap(new TreeMap<>()), Collections.synchronizedMap(new TreeMap<>()), new Message(Message.Type.SUCCESS));
				} else {
					normalTest = TestResult.createTestResult(testResult.getTestName(), testResult.getConfiguration(),  unexpectedMessagesNormal, expectedMessages, testResult.getSummary());
				}
				newResults.add(normalTest);
				if (!unexpectedMessagesFrozen.isEmpty()) {
					frozenTest = TestResult.createTestResult(testResult.getTestName(), testResult.getConfiguration(),  unexpectedMessagesFrozen, expectedMessages, testResult.getSummary());
					frozenTest.setFrozenTest(true);
					newResults.add(frozenTest);
				}
				removeResults.add(testResult);
			}
		}
		for (TestResult removeResult : removeResults) {
			buildResult.removeResult(removeResult);
		}
		for (TestResult newResult : newResults) {
			buildResult.addTestResult(newResult);
		}
		//Duplicate tests should not happen, but just in case in left the method here
		//mergeDuplicates(buildResult);
	}

	private static boolean isFrozenTest(TestResult testResult, CIDashboard dashboard) throws IOException {
		if (testResult.isSoftTest()) return false;
		Collection<WikiAttachment> attachments = Environment.getInstance().getWikiConnector().getAttachments(dashboard.getDashboardArticle()).stream().filter(a -> a.getFileName().contains(CIFreezeFailedTestsAction.getFileName(dashboard, testResult))).toList();
		Optional<WikiAttachment> attachment = attachments.stream().findFirst();
		if (attachment.isEmpty()) return false;
		for (String testObject : testResult.getTestObjectsWithUnexpectedOutcome()) {
			String[] lines = testResult.getMessageForTestObject(testObject).getText().split("\\R");
			if (containsLine(lines, Streams.getTextAndClose(attachment.get().getInputStream()))) return true;
		}
		return false;
	}

	private static boolean containsLine(String[] lines, String fileText) {
		List<String> fileLines = List.of(fileText.split("\\R"));
		Set<String> fileLineSet = new HashSet<>();
		fileLines.stream().map(CIFreezeFailedTestsAction::normalizeLink).forEach(fileLineSet::add);

		for (String line : lines) {
			if (fileLineSet.contains(normalizeLink(line))) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Splits a rendered test-object message into the still active part and the frozen part.
	 * Headers are matched by a normalized key because rendered report headers contain changing
	 * counts and singular/plural wording. The original header text is kept for output.
	 * Wiki list depth is significant inside a matched header block: parent list items are context
	 * for nested findings. Such context is emitted to the normal or frozen side only when that side
	 * still contains a matching child finding.
	 */
	static Pair<Message, Message> splitText(Message message, String fileText, String testObject) {

		List<String> messageLines = List.of(message.getText().split("\\R"));
		messageLines = messageLines.stream()
				.filter(s -> s != null && !s.isBlank())
				.toList();

		StringBuilder normalTest = new StringBuilder();
		StringBuilder frozenTest = new StringBuilder();
		boolean hasNormalContent = false;
		boolean hasFrozenContent = false;

		Map<String, List<String>> frozenContent = extractMatchingFileSection(fileText, testObject);

		String currentHeader = null;
		String currentHeaderKey = null;
		List<String> currentContent = new ArrayList<>();

		// A non-list line starts a new report block. List lines are split only within that block.
		for (String messageLine : messageLines) {
			if (!messageLine.startsWith("*")) {
				if (currentHeader != null) {
					BlockSplit blockSplit = splitBlock(currentContent, frozenContent.get(currentHeaderKey));
					hasNormalContent |= flushBlock(normalTest, blockSplit.normalContent(), currentHeader);
					hasFrozenContent |= flushBlock(frozenTest, blockSplit.frozenContent(), currentHeader);
				}
				currentContent.clear();
				currentHeader = messageLine;
				currentHeaderKey = normalizeHeader(messageLine);
				continue;
			}
			currentContent.add(messageLine);
		}

		if (currentHeader != null) {
			BlockSplit blockSplit = splitBlock(currentContent, frozenContent.get(currentHeaderKey));
			hasNormalContent |= flushBlock(normalTest, blockSplit.normalContent(), currentHeader);
			hasFrozenContent |= flushBlock(frozenTest, blockSplit.frozenContent(), currentHeader);
		}

		return new Pair<>(
				new Message(message.getType(), hasNormalContent ? adjustHeaderCounts(normalTest.toString()).trim() : ""),
				new Message(message.getType(), hasFrozenContent ? adjustHeaderCounts(frozenTest.toString()).trim() : "")
		);
	}

	/**
	 * Appends a header block only if this side of the split has content. Empty blocks are skipped
	 * so fully frozen/fully normal sections do not leave orphan headers behind.
	 *
	 * @return true if a block was appended
	 */
	private static boolean flushBlock(StringBuilder builder, List<String> newContent, String header) {
		if  (newContent.isEmpty()) {
			return false;
		}
		builder.append(System.lineSeparator()).append(header).append(System.lineSeparator());
		for (String line : newContent) {
			builder.append(line).append(System.lineSeparator());
		}
		return true;
	}

	/**
	 * Splits all list content below one report header into normal and frozen lines. The frozen
	 * content comes from the freeze attachment and is normalized before matching against the
	 * current rendered message.
	 */
	private static BlockSplit splitBlock(List<String> content, List<String> frozenContent) {
		Set<String> frozenLines = new HashSet<>();
		if (frozenContent != null) {
			// Generated wiki-link anchors can change; compare the stable link text instead.
			frozenContent.stream().map(CIFreezeFailedTestsAction::normalizeLink).forEach(frozenLines::add);
		}

		BlockSplit result = new BlockSplit();
		for (MessageLine root : parseMessageLines(content)) {
			SplitMessageLine split = splitMessageLine(root, frozenLines);
			result.normalContent().addAll(split.normalLines());
			result.frozenContent().addAll(split.frozenLines());
		}
		return result;
	}

	/**
	 * Converts wiki list lines into a tree by their leading-star depth. This lets the adjuster keep
	 * parent list items as context for nested findings instead of treating every rendered line as an
	 * independent finding.
	 */
	private static List<MessageLine> parseMessageLines(List<String> lines) {
		List<MessageLine> roots = new ArrayList<>();
		Deque<MessageLine> stack = new ArrayDeque<>();
		for (String line : lines) {
			MessageLine current = new MessageLine(line, getListDepth(line));
			while (!stack.isEmpty() && stack.peek().depth() >= current.depth()) {
				stack.pop();
			}
			if (stack.isEmpty()) {
				roots.add(current);
			}
			else {
				stack.peek().children().add(current);
			}
			stack.push(current);
		}
		return roots;
	}

	/**
	 * Splits one wiki-list subtree. Leaf lines are the findings that can be frozen independently.
	 * Branch lines are context and follow their normal/frozen children. If a previously frozen
	 * child disappeared, the branch is not emitted to the frozen result by itself.
	 */
	private static SplitMessageLine splitMessageLine(MessageLine line, Set<String> frozenLines) {
		SplitMessageLine result = new SplitMessageLine();
		for (MessageLine child : line.children()) {
			SplitMessageLine childSplit = splitMessageLine(child, frozenLines);
			result.normalChildren().addAll(childSplit.normalLines());
			result.frozenChildren().addAll(childSplit.frozenLines());
		}

		boolean lineFrozen = frozenLines.contains(normalizeLink(line.line()));
		if (!line.children().isEmpty()) {
			if (!result.normalChildren().isEmpty()) {
				result.normalLines().add(line.line());
				result.normalLines().addAll(result.normalChildren());
			}
			else if (!lineFrozen) {
				result.normalLines().add(line.line());
			}
			if (!result.frozenChildren().isEmpty()) {
				result.frozenLines().add(line.line());
				result.frozenLines().addAll(result.frozenChildren());
			}
			return result;
		}

		if (!lineFrozen || !result.normalChildren().isEmpty()) {
			result.normalLines().add(line.line());
			result.normalLines().addAll(result.normalChildren());
		}
		if (lineFrozen || !result.frozenChildren().isEmpty()) {
			result.frozenLines().add(line.line());
			result.frozenLines().addAll(result.frozenChildren());
		}
		return result;
	}

	/**
	 * Returns the wiki-list nesting level encoded by leading stars. Examples: "* item" has depth 1,
	 * "** detail" has depth 2, and a non-list line has depth 0.
	 */
	private static int getListDepth(String line) {
		int depth = 0;
		while (depth < line.length() && line.charAt(depth) == '*') {
			depth++;
		}
		return depth;
	}

	private static Map<String, List<String>> extractMatchingFileSection(String fileText, String testObject) {
		List<String> fileLines = List.of(fileText.split("\\R"));
		fileLines = fileLines.stream()
				.filter(s -> s != null && !s.isBlank())
				.toList();

		boolean inMatchingSection = false;
		Map<String, List<String>> frozenContent = new HashMap<>();

		String currentHeader = null;
		List<String> currentContent = new ArrayList<>();

		boolean firstLineMustBeSectionHeader = true;

		//find SectionHeader that contains testObject, put content under that Section into the map
		for (String fileLine : fileLines) {
			boolean isHeader = !fileLine.startsWith("*");
			boolean isSectionHeader = isHeader && fileLines.indexOf(fileLine) < fileLines.size() - 1 && !fileLines.get(fileLines.indexOf(fileLine) + 1).startsWith("*");
			if (firstLineMustBeSectionHeader) {
				isSectionHeader = true;
				firstLineMustBeSectionHeader = false;
			}

			if (isSectionHeader) {
				if (inMatchingSection) {
					inMatchingSection = false;
				}
				if (fileLine.contains(testObject)) {
					inMatchingSection = true;
				}
			}
			if (inMatchingSection) {
				//if (isSectionHeader) continue;
				if (isHeader) {
					if (currentHeader != null) {
						if (frozenContent.containsKey(currentHeader)) {
							frozenContent.get(currentHeader).addAll(currentContent);
						} else {
							frozenContent.put(currentHeader, new ArrayList<>(currentContent));
						}
					}
					currentContent.clear();
					currentHeader = normalizeHeader(fileLine);
				} else {
					currentContent.add(normalizeLink(fileLine));
				}
			}
		}
		if (frozenContent.containsKey(currentHeader)) {
			frozenContent.get(currentHeader).addAll(currentContent);
		} else {
			frozenContent.put(currentHeader, new ArrayList<>(currentContent));
		}

		return frozenContent;
	}

	private static String adjustHeaderCounts(String text) {

		String[] lines = text.split("\\R");

		List<String> result = new ArrayList<>();

		String currentHeader = null;
		List<String> currentContent = new ArrayList<>();

		boolean isFirstHeader = true;
		int totalCount = 0;

		for (String line : lines) {

			if (!line.startsWith("*")) {

				// flush previous block
				if (currentHeader != null) {

					int count = countMessages(currentContent);
					totalCount += count;

					if (!isFirstHeader) {
						currentHeader = replaceLastNumber(currentHeader, count);
					}

					result.add(currentHeader);
					result.addAll(currentContent);

					isFirstHeader = false;
				}

				currentHeader = line;
				currentContent.clear();

			} else {
				currentContent.add(line);
			}
		}

		// flush last block
		if (currentHeader != null) {

			int count = countMessages(currentContent);
			totalCount += count;

			if (!isFirstHeader) {
				currentHeader = replaceLastNumber(currentHeader, count);
			}

			result.add(currentHeader);
			result.addAll(currentContent);
		}

		if (!result.isEmpty()) {
			String firstHeader = result.get(0);
			firstHeader = replaceFirstNumber(firstHeader, totalCount);
			result.set(0, firstHeader);
		}

		return String.join(System.lineSeparator(), result);
	}

	/**
	 * Counts report findings inside a header block. Context parent lines are not counted as
	 * separate findings when they only group nested list entries.
	 */
	private static int countMessages(List<String> content) {
		return parseMessageLines(content).stream().mapToInt(CIBuildFrozenTestAdjuster::countLeaves).sum();
	}

	/**
	 * Header counts should describe findings, not context lines. For grouped wiki lists this means
	 * counting the deepest lines; for flat lists each root line is one finding.
	 */
	private static int countLeaves(MessageLine line) {
		if (line.children().isEmpty()) return 1;
		return line.children().stream().mapToInt(CIBuildFrozenTestAdjuster::countLeaves).sum();
	}

	private record BlockSplit(List<String> normalContent, List<String> frozenContent) {

		private BlockSplit() {
			this(new ArrayList<>(), new ArrayList<>());
		}
	}

	private record MessageLine(String line, int depth, List<MessageLine> children) {

		private MessageLine(String line, int depth) {
			this(line, depth, new ArrayList<>());
		}
	}

	private record SplitMessageLine(List<String> normalLines, List<String> frozenLines, List<String> normalChildren, List<String> frozenChildren) {

		private SplitMessageLine() {
			this(new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
		}
	}

	private static void mergeDuplicates(BuildResult buildResult) {
		List<String> mergedResultsName = new ArrayList<>();
		List<TestResult> duplicates = new ArrayList<>();
		List<TestResult> mergedResults = new ArrayList<>();

		for (TestResult result : buildResult.getResults()) {

			if (mergedResultsName.contains(result.getTestName())) continue;
			List<TestResult> tempDuplicates = getDuplicates(result, buildResult);

			if (!tempDuplicates.isEmpty()) {
				mergedResultsName.add(result.getTestName());
				TestResult mergedResult = mergeEqualTests(result, tempDuplicates);
				duplicates.addAll(tempDuplicates);
				duplicates.add(result);
				mergedResults.add(mergedResult);
			}
		}
		for (TestResult duplicate : duplicates) {
			buildResult.removeResult(duplicate);
		}
		for (TestResult mergedResult : mergedResults) {
			buildResult.addTestResult(mergedResult);
		}
	}

	private static List<TestResult> getDuplicates(TestResult testResult, BuildResult buildResult) {
		List<TestResult> results = buildResult.getResults();
		List<TestResult> duplicates = new ArrayList<>();
		Set<String> testObjects2 = new HashSet<>(testResult.getTestObjectsWithUnexpectedOutcome());
		testObjects2.addAll(testResult.getTestObjectsWithExpectedOutcome());
		for (TestResult result : results) {
			if (result.getTestName().equals(testResult.getTestName()) && result.isFrozenTest() == testResult.isFrozenTest() && !result.equals(testResult) && result.getConfiguration() == testResult.getConfiguration()) {
				//Expected and Unexpected together to get total test Objects
				Set<String> testObjects1 = new HashSet<>(result.getTestObjectsWithUnexpectedOutcome());
				testObjects1.addAll(result.getTestObjectsWithExpectedOutcome());
				if (testObjects1.equals(testObjects2)) {
					duplicates.add(result);
				}
			}
		}
		return duplicates;
	}

	private static TestResult mergeEqualTests(TestResult testResult, List<TestResult> duplicates) {

		if (duplicates.isEmpty()) return testResult;
		duplicates.add(testResult);

		Map<String, Message> unexpectedMessages = Collections.synchronizedMap(new TreeMap<>());
		Map<String, Message> expectedMessages = Collections.synchronizedMap(new TreeMap<>());
		Set<String> unexpectedTestObjects = new HashSet<>();
		duplicates.stream().map(TestResult::getTestObjectsWithUnexpectedOutcome).forEach(unexpectedTestObjects::addAll);

		for (String testObject : unexpectedTestObjects) {
			List<Message> list = duplicates.stream()
					.filter(r -> r.getTestObjectsWithUnexpectedOutcome().contains(testObject))
					.map(r -> r.getMessageForTestObject(testObject))
					.toList();
			String newText = mergeTexts(list.stream().map(Message::getText).toList());
			Message.Type type = list.stream().findFirst().get().getType(); //should always be present
			unexpectedMessages.put(testObject, new Message(type, newText));
		}

		Set<String> expectedTestObjects = new HashSet<>();
		duplicates.stream().map(TestResult::getTestObjectsWithExpectedOutcome).forEach(expectedTestObjects::addAll);
		for (String testObject : expectedTestObjects) {
			StringBuilder stringBuilder = new StringBuilder();
			for (TestResult mergeResult : duplicates) {
				if (!mergeResult.getTestObjectsWithExpectedOutcome().contains(testObject)) continue;
				stringBuilder.append(mergeResult.getMessageForTestObject(testObject).getText()).append("\n");
			}
			expectedMessages.put(testObject, new Message(Message.Type.SUCCESS, stringBuilder.toString()));
		}
		TestResult newTest = TestResult.createTestResult(testResult.getTestName(), testResult.getConfiguration(), unexpectedMessages, expectedMessages, testResult.getSummary());
		newTest.setFrozenTest(testResult.isFrozenTest());
		return newTest;
	}

	private static String mergeTexts(List<String> texts) {

		Map<String, LinkedHashSet<String>> map = new LinkedHashMap<>();

		for (String text : texts) {
			processText(text, map);
		}

		StringBuilder result = new StringBuilder();

		for (Map.Entry<String, LinkedHashSet<String>> entry : map.entrySet()) {
			boolean header = true;

			Set<String> content = entry.getValue();

			for (String line : content) {
				if (header) {
					result.append(System.lineSeparator());
					header = false;
				}
				result.append(line).append(System.lineSeparator());
			}
		}

		return adjustHeaderCounts(result.toString().trim());
	}

	private static void processText(String text, Map<String, LinkedHashSet<String>> map) {

		String[] lines = text.split("\\R");

		String currentHeader = null;

		for (String line : lines) {

			if (!line.startsWith("*")) {
				currentHeader = normalizeHeader(line);
				map.putIfAbsent(currentHeader, new LinkedHashSet<>());
				if (map.get(currentHeader).isEmpty()) { //always add not normalized header as the first line
					map.get(currentHeader).add(line);
				}
			} else if (currentHeader != null) {
				map.get(currentHeader).add(line);
			}
		}
	}

}
