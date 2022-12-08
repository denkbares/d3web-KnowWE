package utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.jetbrains.annotations.NotNull;

import com.denkbares.progress.ConsoleProgressBarListener;
import com.denkbares.strings.Strings;
import com.denkbares.utils.Files;

/**
 * Small util class to generate a cleaned wiki folder from the original wiki git repo
 *
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 14.10.21
 */
@SuppressWarnings("UseOfSystemOutOrSystemErr")
public class WikiContentCleaner {

	private final Set<String> cleanableAttachments = new LinkedHashSet<>();
	private final Set<String> ignoredFiles = new LinkedHashSet<>();
	private final Set<String> knownAttachments = new LinkedHashSet<>();
	private final Set<String> knownFiles = new LinkedHashSet<>();
	private final Map<String, Integer> maxNOfPatterns = new LinkedHashMap<>();
	private final Map<String, Map<String, String>> replaceCommands = new LinkedHashMap<>();

	/**
	 * Replace commands to be applied to all wiki pages that match the key pattern.
	 * The key of the map is a regex of all pages to which the replace command should be applied. The value is a pair of
	 * the search-regex and replacement string.
	 */
	public WikiContentCleaner setReplaceCommands(Map<String, Map<String, String>> replaceCommands) {
		this.replaceCommands.putAll(replaceCommands);
		return this;
	}

	/**
	 * Keys of the map a patterns for page titles, the values are numbers of pages that are allowed to match the pattern
	 * in the cleaned up wiki. If there are more pages in the source wiki matching the pattern, they are skipped.
	 */
	public WikiContentCleaner setMaxNOfPatterns(Map<String, Integer> maxNOfPatterns) {
		this.maxNOfPatterns.putAll(maxNOfPatterns);
		return this;
	}

	/**
	 * Files that match one of the strings as a prefix or a regex pattern will be included
	 */
	public WikiContentCleaner setPrefixWhiteList(Set<String> knownFiles) {
		this.knownFiles.addAll(knownFiles);
		return this;
	}

	/**
	 * Extension to be used/included
	 */
	public WikiContentCleaner setKnownAttachments(Set<String> knownAttachments) {
		this.knownAttachments.addAll(knownAttachments);
		return this;
	}

	/**
	 * These files or directories can be skipped directly (full name, no regex)
	 */
	public WikiContentCleaner setIgnoredFileNames(Set<String> ignoredFiles) {
		this.ignoredFiles.addAll(ignoredFiles);
		return this;
	}

	/**
	 * Attachments with the given extensions can be skipped without messages
	 */
	public WikiContentCleaner setCleanableAttachmentExtensions(Set<String> cleanableExtensions) {
		this.cleanableAttachments.addAll(cleanableExtensions);
		return this;
	}

	public void createCleanedCopy(String sourceFolderPath, String targetFolderPath) throws IOException {
		File sourceFolder = checkedSourceFolder(sourceFolderPath);
		File targetFolder = checkedTargetFolder(targetFolderPath);

		ConsoleProgressBarListener progress = new ConsoleProgressBarListener();
		List<String> knownSkipped = new ArrayList<>();
		List<String> unknownSkipped = new ArrayList<>();
		System.out.print("Copy new cleaned wiki...");
		File[] files = Objects.requireNonNull(sourceFolder.listFiles());
		Arrays.sort(files);
		for (File sourceFile : progress.iterate(files)) {
			String fileName = sourceFile.getName();
			if (ignoredFiles.contains(fileName)) {
				knownSkipped.add(fileName);
				continue;
			}
			if (isAttachmentFolderWithCleanableAttachments(sourceFile)) {
				knownSkipped.add(fileName);
				continue;
			}
			boolean copy = false;
			boolean limit = false;
			boolean matched = false;
			for (Map.Entry<String, Integer> entry : maxNOfPatterns.entrySet()) {
				String regex = entry.getKey();
				int remaining = entry.getValue();
				if (fileName.matches(regex)) {
					matched = true;
					maxNOfPatterns.put(regex, remaining - 1);
					if (remaining > 0) {
						copy = true;
					}
					else {
						limit = true;
					}
					break;
				}
			}
			if (copy || (knownFiles.isEmpty() || knownFiles.stream()
					.anyMatch(regex -> fileName.matches(regex) || fileName.startsWith(regex)))) {
				File target = new File(targetFolder, fileName);
				if (sourceFile.isFile()) {
					String source = Strings.readFile(sourceFile);
					for (Map.Entry<String, Map<String, String>> replaceEntry : replaceCommands.entrySet()) {
						String namePattern = replaceEntry.getKey();
						Map<String, String> replaceCommands = replaceEntry.getValue();
						if (!(fileName.matches(namePattern) || fileName.startsWith(namePattern))) continue;
						for (Map.Entry<String, String> replaceCommand : replaceCommands.entrySet()) {
							source = source.replaceAll(replaceCommand.getKey(), replaceCommand.getValue());
						}
					}
					Strings.writeFile(target, source);
				}
				else if (sourceFile.isDirectory()) {
					for (File attachmentDir : Objects.requireNonNull(sourceFile.listFiles())) {
						if (ignoredFiles.contains(attachmentDir.getName())) continue;
						if (isCleanableAttachmentDirectory(attachmentDir)) continue;
						target.mkdirs();
						Files.recursiveCopy(attachmentDir, new File(target, attachmentDir.getName()));
					}
				}
			}
			else if (!limit) {
				unknownSkipped.add(fileName);
			}
		}

		System.out.println("Done!\nSkipped " + knownSkipped.size() + " known files/directories and " + unknownSkipped.size() + " unknown files");
		System.out.println("The following is a list of all unknown skipped files/directories:\n" + unknownSkipped.stream()
				.map(Strings::quote)
				.collect(Collectors.joining(",\n")));
	}

	private boolean isAttachmentFolderWithCleanableAttachments(File attachmentDir) {
		if (attachmentDir.isDirectory()) {
			for (File file : Objects.requireNonNull(attachmentDir.listFiles())) {
				if (ignoredFiles.contains(file.getName())) continue;
				if (!isCleanableAttachmentDirectory(file)) {
					return false;
				}
			}
			return true;
		}
		return false;
	}

	private boolean isCleanableAttachmentDirectory(File file) {
		int extensionStart = file.getName().lastIndexOf(".");
		int extensionEnd = file.getName().lastIndexOf("-");
		if (extensionStart < 0 || extensionEnd < 0 || extensionStart >= extensionEnd) {
			return false;
		}
		for (String knownAttachment : knownAttachments) {
			if (file.getPath().matches(knownAttachment)) {
				return false;
			}
		}
		String extension = file.getName().toLowerCase().substring(extensionStart + 1, extensionEnd);
		boolean cleanable = cleanableAttachments.contains(extension);
		if (!cleanable && !knownAttachments.isEmpty() && !knownAttachments.contains(extension)) {
			System.out.println("Unknown attachment type, will not clean: " + file.getPath());
		}
		return cleanable;
	}

	@NotNull
	private File checkedSourceFolder(String sourceFolderPath) {
		File sourceFolder = new File(sourceFolderPath);
		if (!sourceFolder.isDirectory()) {
			throw new IllegalArgumentException("Source folder '" + sourceFolderPath + "' is not a directory");
		}
		return sourceFolder;
	}

	@NotNull
	private File checkedTargetFolder(String targetFolderPath) {
		File targetFolder = new File(targetFolderPath);
		if (targetFolder.exists()) {
			if (targetFolder.isDirectory()) {
				System.out.print("Delete last version of cleaned wiki...");
				ConsoleProgressBarListener progress = new ConsoleProgressBarListener();
				for (File file : progress.iterate(Objects.requireNonNull(targetFolder.listFiles()))) {
					Files.recursiveDelete(file);
				}
				targetFolder.mkdirs();
			}
			else {
				throw new IllegalArgumentException("Target folder '" + targetFolderPath + "' exists, but is not a directory");
			}
		}
		return targetFolder;
	}
}
