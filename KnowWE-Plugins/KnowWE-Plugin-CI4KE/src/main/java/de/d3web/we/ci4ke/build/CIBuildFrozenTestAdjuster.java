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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
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
					Pair<Message, Message> messagePair = splitText(testResult.getMessageForTestObject(testObject), fileText, testObject);
					unexpectedMessagesNormal.put(testObject, messagePair.getA());
					unexpectedMessagesFrozen.put(testObject, messagePair.getB());
					if (!messagePair.getA().getText().isEmpty()) isFullyFrozen = false;
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
				frozenTest = TestResult.createTestResult(testResult.getTestName(), testResult.getConfiguration(),  unexpectedMessagesFrozen, expectedMessages, testResult.getSummary());
				frozenTest.setFrozenTest(true);
				newResults.add(normalTest);
				newResults.add(frozenTest);
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
		Set<String> fileLineSet = new HashSet<>(fileLines);

		for (String line : lines) {
			if (fileLineSet.contains(line)) {
				return true;
			}
		}
		return false;
	}

	private static Pair<Message, Message> splitText(Message message, String fileText, String testObject) {

		List<String> messageLines = List.of(message.getText().split("\\R"));
		messageLines = messageLines.stream()
				.filter(s -> s != null && !s.isBlank())
				.toList();

		StringBuilder normalTest = new StringBuilder();
		StringBuilder frozenTest = new StringBuilder();
		boolean fullyFrozen = true;
		boolean fullyNormal = true;

		String currentHeader = null;
		List<String> currentNormalContent = new ArrayList<>();
		List<String> currentFrozenContent = new ArrayList<>();

		Map<String, List<String>> frozenContent = extractMatchingFileSection(fileText, testObject);

		List<String> currentList = null;

		//match headers as keys from frozenContent, split weather frozenContent contains line
		for (String messageLine : messageLines) {
			boolean isHeader = !messageLine.startsWith("*");
			boolean isSectionHeader = isHeader && messageLines.indexOf(messageLine) < messageLines.size() - 1 && !messageLines.get(messageLines.indexOf(messageLine) + 1).startsWith("*");
			if (isHeader) {
				if (isSectionHeader) {
					normalTest.append(System.lineSeparator());//.append(System.lineSeparator()).append(messageLine).append(System.lineSeparator());
					frozenTest.append(System.lineSeparator());//.append(System.lineSeparator()).append(messageLine).append(System.lineSeparator());
					//continue;
				}
				if (currentHeader != null) {
					flushBlock(normalTest, currentNormalContent, currentHeader);
					flushBlock(frozenTest, currentFrozenContent, currentHeader);
				}
				currentNormalContent.clear();
				currentFrozenContent.clear();
				currentList = frozenContent.get(normalizeHeader(messageLine));
				currentHeader = messageLine;
			} else {
				if (currentList != null) {
					if (currentList.contains(normalizeLink(messageLine))) {
						currentFrozenContent.add(messageLine);
						fullyNormal = false;
					} else {
						currentNormalContent.add(messageLine);
						fullyFrozen = false;
					}
				} else {
					currentNormalContent.add(messageLine);
					fullyFrozen = false;
				}
			}
		}

		if (currentHeader != null) {
			flushBlock(normalTest, currentNormalContent, currentHeader);
			flushBlock(frozenTest, currentFrozenContent, currentHeader);
		}

		if (fullyNormal) {
			frozenTest = new StringBuilder();
		}
		if (fullyFrozen) {
			normalTest = new StringBuilder();
		}

		return new Pair<>(
				new Message(message.getType(), adjustHeaderCounts(normalTest.toString()).trim()),
				new Message(message.getType(), adjustHeaderCounts(frozenTest.toString()).trim())
		);
	}

	private static void flushBlock(StringBuilder builder, List<String> newContent, String header) {
		if  (newContent.isEmpty()) {
			return;
		}
		builder.append(System.lineSeparator()).append(header).append(System.lineSeparator());
		for (String line : newContent) {
			builder.append(line).append(System.lineSeparator());
		}
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
							frozenContent.put(currentHeader, new ArrayList<String>(currentContent));
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
			frozenContent.put(currentHeader, new ArrayList<String>(currentContent));
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

					int count = currentContent.size();
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

			int count = currentContent.size();
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
