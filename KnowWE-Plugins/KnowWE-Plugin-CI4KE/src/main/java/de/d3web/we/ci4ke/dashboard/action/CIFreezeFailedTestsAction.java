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

package de.d3web.we.ci4ke.dashboard.action;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.denkbares.utils.Streams;
import de.d3web.testing.BuildResult;
import de.d3web.testing.TestResult;
import de.d3web.we.ci4ke.dashboard.CIDashboard;
import de.d3web.we.ci4ke.dashboard.CIDashboardManager;
import de.d3web.we.ci4ke.dashboard.type.CIDashboardType;
import de.knowwe.core.Environment;
import de.knowwe.core.action.AbstractAction;
import de.knowwe.core.action.UserActionContext;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.wikiConnector.WikiAttachment;
import de.knowwe.core.wikiConnector.WikiConnector;

/**
 * Freezes failed tests by storing them inside an attachment
 *
 * @author Philipp Sehne (denkbares GmbH)
 * @created 08.05.2026
 */
public class CIFreezeFailedTestsAction extends AbstractAction {

	Path renamedPath = null;

	@Override
	public void execute(UserActionContext context) throws IOException {
		Section<?> section = getSection(context);
		CIDashboard dashboard = CIDashboardManager.getDashboard(
				Sections.cast(section, CIDashboardType.class));

		if (dashboard == null) return;

		BuildResult build = dashboard.getLatestBuild();
		if (build == null) return;

		List<TestResult> results = build.getResults();

		WikiConnector wikiConnector = Environment.getInstance().getWikiConnector();

		//remove all previous files
		List<WikiAttachment> attachments = wikiConnector
				.getAttachments(dashboard.getDashboardArticle())
				.stream()
				.filter(a -> a.getFileName().contains("CIFreeze_" + dashboard.getDashboardName()))
				.toList();

		for (WikiAttachment attachment : attachments) {
			wikiConnector.deleteAttachment(context.getArticle().getTitle(), attachment.getFileName(), "CIFreeze_" + dashboard.getDashboardName());
		}

		for (TestResult result : results) {
			if (result.isSuccessful()) continue;

			String fileName = getFileName(dashboard, result);

			Path tempPath = Files.createTempFile(fileName + "_", ".txt");
			File file = tempPath.toFile();

			try {
				Optional<WikiAttachment> existingAttachment = wikiConnector
						.getAttachments(dashboard.getDashboardArticle())
								.stream()
								.filter(a -> a.getFileName().contains(fileName))
								.findFirst();

				String AttachmentString = "";
				if (existingAttachment.isPresent()) {
					AttachmentString = Streams.getTextAndClose(existingAttachment.get().getInputStream());
				}

				if (result.getTestObjectsWithUnexpectedOutcome().isEmpty()) continue;

				for (String testObject : result.getTestObjectsWithUnexpectedOutcome()) {
					addIfNotExists(file, AttachmentString, result.getMessageForTestObject(testObject).getText());
				}

				renamedPath = tempPath.resolveSibling(fileName);
				Files.move(tempPath, renamedPath, StandardCopyOption.REPLACE_EXISTING);

				File finalFile = renamedPath.toFile();

				wikiConnector.storeAttachment(
						dashboard.getDashboardArticle(),
						"CI-FreezeFailedTests",
						finalFile
				);

			} finally {
				Files.deleteIfExists(tempPath);
				if (renamedPath != null) {
					Files.deleteIfExists(renamedPath);
				}
			}
		}
	}

	public static String getFileName(CIDashboard dashboard, TestResult testResult) {
		String config = Arrays.toString(testResult.getConfiguration())
				.replaceAll("[^a-zA-Z0-9\\s]", "")
				.trim()
				.replaceAll("\\s+", "_");
		return "CIFreeze_" + dashboard.getDashboardName() + "_" + testResult.getTestName() + "_" + config +".txt";
	}

	/**
	 * Adds new frozen tests into a file without adding duplicates
	 *
	 * @param file the file to store the frozen test into
	 * @param fileText the text already present in the file
	 * @param newTests the new test to add into the file
	 * @created 08.05.2026
	 */
	private static void addIfNotExists(File file, String fileText, String newTests) throws IOException {

		if (!file.exists()) {
			file.createNewFile();
		}

		List<String> fileLines = Arrays.asList(fileText.split("\\R"));
		fileLines = fileLines.stream()
				.filter(s -> s != null && !s.isBlank())
				.toList();
		List<String> newLines = Arrays.asList(newTests.split("\\R"));
		newLines = newLines.stream()
				.filter(s -> s != null && !s.isBlank())
				.collect(Collectors.toList());
		if (newLines.isEmpty())  {
			Files.writeString(file.toPath(), fileText);
			return;
		}

		String newSectionHeader = newLines.get(0);
		String normalizedNewSection = normalizeHeader(newSectionHeader);

		StringBuilder result = new StringBuilder();
		boolean inMatchingSection = false;
		boolean sectionHandled = false;

		String currentHeader = null;
		List<String> currentContent = new ArrayList<>();

		boolean isFirstHeaderInSection = true;

		for (String line : fileLines) {
			boolean isHeader = !line.startsWith("*");
			boolean isSectionHeader = isHeader && fileLines.indexOf(line) < fileLines.size() - 1 && !fileLines.get(fileLines.indexOf(line) + 1).startsWith("*");
			if (isSectionHeader) {
				String normalized = normalizeHeader(line);
				if (sectionHandled) {
					flushBlock(newLines, result, currentHeader, currentContent);
					appendMissingNewContent(newLines, result, true);
					inMatchingSection = false;
				}
				if (normalized.equals(normalizedNewSection)) {
					inMatchingSection = true;
					sectionHandled = true;
				}
			}
			if (inMatchingSection) {
				if (isHeader) {
					if (currentHeader != null) {
						flushBlock(newLines, result, currentHeader, currentContent);
					}
					currentContent.clear();
					currentHeader = line;
					if (isFirstHeaderInSection) {
						newLines.remove(0); //remove Section Header since it already exists
						isFirstHeaderInSection = false;
					}
				} else {
					currentContent.add(line);
				}
			} else {
				appendLine(line, result, isHeader, isSectionHeader);
			}
		}

		// flush last block
		if (currentHeader != null && inMatchingSection) {
			flushBlock(newLines, result, currentHeader, currentContent);
			appendMissingNewContent(newLines, result,true);
		}

		// --- If section not found → append whole new section ---
		if (!sectionHandled) {
			appendMissingNewContent(newLines, result, false);
		}

		Files.writeString(file.toPath(), result.toString().trim());
	}

	private static void appendMissingNewContent(List<String> newLines, StringBuilder result, boolean firstHeaderRemoved) {
		boolean isFirstHeader = !firstHeaderRemoved;
		for (String line : newLines) {
			boolean isHeader = !line.startsWith("*");
			if (isHeader){
				appendLine(line, result, true, isFirstHeader);
				isFirstHeader = false;
			} else {
				appendLine(line, result, false, false);
			}
		}
		newLines.clear(); //clear to remove getting duplicates when calling again
	}

	private static void flushBlock(List<String> newLines, StringBuilder result, String currentHeader, List<String> currentContent) {
		List<String> newContent = extractContentForHeader(newLines, currentHeader);

		for (String l : newContent) {
			boolean existsInSameHeader = currentContent.contains(l);
			if (!existsInSameHeader) {
				currentContent.add(l);
			}
		}

		int count = currentContent.size();
		String updatedHeader = replaceLastNumber(currentHeader, count);

		appendLine(updatedHeader, result, true, false);
		for (String l : currentContent) {
			appendLine(l, result, false, false);
		}
	}

	private static List<String> extractContentForHeader(List<String> lines, String targetHeader) {
		String normalizedTarget = normalizeHeader(targetHeader);

		List<String> extractedContent = new ArrayList<>();

		String currentHeader = null;
		List<String> copyList = new ArrayList<>(lines);

		List<Integer> indicesToRemove = new ArrayList<>();

		for (int i = 0; i < copyList.size(); i++) {
			String line = copyList.get(i);
			if (!line.startsWith("*")) {
				currentHeader = line;
				if (normalizeHeader(currentHeader).equals(normalizedTarget)) indicesToRemove.add(i);
			} else if (currentHeader != null &&
					normalizeHeader(currentHeader).equals(normalizedTarget)) {
				extractedContent.add(line);
				indicesToRemove.add(i);
			}
		}

		indicesToRemove.sort(Comparator.reverseOrder());
		for (int idx : indicesToRemove) {
			lines.remove(idx);
		}

		return extractedContent;
	}

	private static void appendLine(String line, StringBuilder result, boolean isHeader, boolean isSectionHeader) {
		if (isSectionHeader) result.append(System.lineSeparator());
		if (isHeader) result.append(System.lineSeparator());
		result.append(line).append(System.lineSeparator());
	}

	public static String replaceFirstNumber(String text, int newNumber) {
		// Replace FIRST standalone number only
		return text.replaceFirst("\\b\\d+\\b", String.valueOf(newNumber));
	}

	public static String replaceLastNumber(String text, int newNumber) {
		// Replace LAST standalone number only
		return text.replaceAll("\\b(\\d+)\\b(?!.*\\b\\d+\\b)", String.valueOf(newNumber));
	}

	public static String normalizeHeader(String header) {
		header = normalizeLink(header);
		return header.replaceAll("\\b\\d+\\b(?!.*\\b\\d+\\b)", "REMOVE")
				.replaceAll("REMOVE\\s+\\S+", "")
				.replaceAll("\\s{2,}", " ")
				.trim();
	}

	//normalize Header without plural fix
//	public static String normalizeHeader(String header) {
//		header = normalizeLink(header);
//		return header.replaceAll("\b\d+\b(?!.*\b\d+\b)", "")
//				.trim();
//	}

	public static String normalizeLink(String link) {
		return link.replaceAll("#[a-f0-9]{1,8}]", "]");
	}

}
