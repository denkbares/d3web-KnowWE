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

package org.apache.wiki.providers.git.migration;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.DateTimeException;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.HashSet;
import java.util.NavigableMap;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.jetbrains.annotations.Nullable;

/**
 * Reads a page directory written by the versioning file provider and the basic attachment provider, and turns its
 * version history into the ordered list of commits a git replay has to produce.
 * <p>
 * Pages keep their latest version as the top level {@code <page>.txt} and all earlier ones as {@code OLD/<page>/<n>.txt},
 * with author, change note and date per version in {@code OLD/<page>/page.properties}. Attachments keep every version
 * including the latest in {@code <page>-att/<file>-dir/<n>.<ext>}, with the same metadata in {@code attachment.properties}.
 * The git side numbers versions by the position of a commit in the file's history, so replaying these versions oldest
 * first reproduces the original version numbers exactly.
 * <p>
 * The scanner never writes and never fails on damaged data, everything it cannot map is recorded in the
 * {@link ScanResult} so a dry run can show it before anything is migrated.
 */
public final class LegacyScanner {

	static final String PAGE_EXT = ".txt";
	static final String OLD_DIR = "OLD";
	static final String PROPERTIES_EXT = ".properties";
	static final String PAGE_PROPERTIES = "page" + PROPERTIES_EXT;
	static final String ATTACHMENT_PROPERTIES = "attachment" + PROPERTIES_EXT;
	static final String ATTACHMENT_DIR_EXT = "-att";
	static final String VERSION_DIR_EXT = "-dir";

	private static final Pattern VERSION_FILE = Pattern.compile("(\\d+)(\\..*)?");
	/**
	 * The marker a soft delete leaves behind, the whole page replaced by one line naming who deleted it and when.
	 */
	private static final Pattern DELETION_MARKER = Pattern.compile("Deleted by .+ at .+");
	private static final long MAX_MARKER_LENGTH = 1024;
	private static final Pattern VERSION_KEY = Pattern.compile("(\\d+)\\.(author|date|changenote)");

	private final File pageDir;
	private final File oldDir;
	private final boolean placeholders;
	private final boolean applySoftDeletes;
	private final ScanResult result = new ScanResult();
	private final List<MigrationEvent> pendingEvents = new ArrayList<>();

	/**
	 * @param pageDir          the wiki page directory, the working tree of the repository to be created
	 * @param placeholders     whether missing page versions are replayed as placeholder commits to keep version numbers
	 * @param applySoftDeletes whether a version that is only a deletion marker is replayed as a deletion
	 */
	public LegacyScanner(File pageDir, boolean placeholders, boolean applySoftDeletes) {
		this.pageDir = pageDir;
		this.oldDir = new File(pageDir, OLD_DIR);
		this.placeholders = placeholders;
		this.applySoftDeletes = applySoftDeletes;
	}

	public ScanResult scan() throws IOException {
		scanPages();
		scanOrphanedPages();
		scanAttachments();
		collectLegacyArtifacts();
		collectLeftovers();
		result.events.addAll(chronological(pendingEvents));
		pendingEvents.clear();
		return result;
	}

	// --- pages ---------------------------------------------------------------

	private void scanPages() throws IOException {
		for (File pageFile : listSorted(pageDir, file -> file.isFile() && file.getName().endsWith(PAGE_EXT))) {
			String stem = stripSuffix(pageFile.getName(), PAGE_EXT);
			scanPage(stem, pageFile);
		}
	}

	/**
	 * A page that still exists. Its latest version is the top level file, everything below it comes from the version
	 * directory.
	 */
	private void scanPage(String stem, File pageFile) throws IOException {
		File versionDir = new File(oldDir, stem);
		Properties props = loadProperties(new File(versionDir, PAGE_PROPERTIES));
		Properties heritage = loadProperties(new File(pageDir, stem + PROPERTIES_EXT));
		NavigableMap<Integer, File> files = versionFiles(versionDir, stem);
		int latest = Math.max(Math.max(highestVersionKey(props), files.isEmpty() ? 0 : files.lastKey() + 1), 1);
		if (files.containsKey(latest)) {
			result.anomalies.add(stem + " has both a top level file and " + OLD_DIR + "/" + stem + "/" + latest
					+ PAGE_EXT + " for version " + latest + ", the top level file wins");
		}

		File[] contents = new File[latest + 1];
		for (int version = 1; version <= latest; version++) {
			contents[version] = version == latest ? pageFile : files.get(version);
		}
		emitVersions(stem + PAGE_EXT, stem, contents, props, heritage, true);
		result.pages++;
	}

	/**
	 * A version directory whose page file is gone, the remains of a delete that did not finish or of a manual removal.
	 * Its versions are replayed and the page is then deleted again, so the history stays readable in git.
	 */
	private void scanOrphanedPages() throws IOException {
		for (File versionDir : listSorted(oldDir, File::isDirectory)) {
			String stem = versionDir.getName();
			if (new File(pageDir, stem + PAGE_EXT).exists()) {
				continue;
			}
			Properties props = loadProperties(new File(versionDir, PAGE_PROPERTIES));
			NavigableMap<Integer, File> files = versionFiles(versionDir, stem);
			if (files.isEmpty()) {
				result.anomalies.add("version directory " + OLD_DIR + "/" + stem + " holds no version file, skipped");
				continue;
			}
			int highestKey = highestVersionKey(props);
			int latest = files.lastKey();
			if (highestKey > latest) {
				result.lostVersions.add(stem + " version " + highestKey
						+ " was the deleted page file and exists nowhere, the page ends at version " + latest);
			}
			File[] contents = new File[latest + 1];
			for (int version = 1; version <= latest; version++) {
				contents[version] = files.get(version);
			}
			long lastTime = emitVersions(stem + PAGE_EXT, stem, contents, props, new Properties(), true);
			pendingEvents.add(new MigrationEvent(stem + PAGE_EXT, latest + 1, Math.max(lastTime, versionDir.lastModified()),
					"unknown", "Page deleted before the git migration", null, MigrationEvent.Kind.DELETE));
			result.orphans.add(stem + " (" + latest + " version(s), deleted again after the replay)");
			result.pages++;
		}
	}

	// --- attachments ---------------------------------------------------------

	private void scanAttachments() throws IOException {
		for (File attachmentDir : listSorted(pageDir, file -> file.isDirectory() && file.getName().endsWith(ATTACHMENT_DIR_EXT))) {
			for (File versionDir : listSorted(attachmentDir, File::isDirectory)) {
				scanAttachment(attachmentDir, versionDir);
			}
		}
	}

	/**
	 * One attachment of one page. The target path drops the {@code -dir} suffix of the version directory, which is how
	 * the legacy name relates to the flat name the git provider uses.
	 */
	private void scanAttachment(File attachmentDir, File versionDir) throws IOException {
		String dirName = versionDir.getName();
		if (!dirName.endsWith(VERSION_DIR_EXT)) {
			result.anomalies.add("unexpected directory " + relative(versionDir) + ", not an attachment version directory");
			return;
		}
		String legacyName = stripSuffix(dirName, VERSION_DIR_EXT);
		String fileName = WikiNames.normalize(legacyName);
		if (!fileName.equals(legacyName)) {
			result.anomalies.add(relative(versionDir) + " is named the way an older provider encoded it, the"
					+ " attachment is migrated to " + attachmentDir.getName() + "/" + fileName + " instead");
		}
		String path = attachmentDir.getName() + "/" + fileName;
		Properties props = loadProperties(new File(versionDir, ATTACHMENT_PROPERTIES));
		NavigableMap<Integer, File> files = versionFiles(versionDir, relative(versionDir));
		if (files.isEmpty()) {
			result.anomalies.add(relative(versionDir) + " holds no version file, skipped");
			return;
		}
		int latest = Math.max(highestVersionKey(props), files.lastKey());
		File flat = new File(attachmentDir, fileName);
		if (flat.isFile()) {
			result.anomalies.add(path + " exists in both layouts, the flat file is replayed as the newest version");
			latest++;
		}
		File[] contents = new File[latest + 1];
		for (int version = 1; version <= latest; version++) {
			contents[version] = files.get(version);
		}
		if (flat.isFile()) {
			contents[latest] = flat;
		}
		emitVersions(path, relative(versionDir), contents, props, new Properties(), false);
		result.attachments++;
	}

	// --- shared version handling ---------------------------------------------

	/**
	 * Turns the per version content files of one page or attachment into events, filling gaps and collecting metadata.
	 * Returns the time of the newest emitted event.
	 */
	private long emitVersions(String path, String label, File[] contents, Properties props, Properties heritage, boolean page) {
		int latest = contents.length - 1;
		long[] times = times(props, contents, label);
		long lastTime = 0;
		for (int version = 1; version <= latest; version++) {
			File content = contents[version];
			MigrationEvent.Kind kind = MigrationEvent.Kind.VERSION;
			if (content == null) {
				String message = label + " version " + version + " is missing";
				if (!page || !placeholders) {
					result.gaps.add(message + ", later versions shift down by one");
					continue;
				}
				content = nearestContent(contents, version);
				if (content == null) {
					result.gaps.add(message + " and no neighbouring version exists, skipped");
					continue;
				}
				kind = MigrationEvent.Kind.PLACEHOLDER;
				result.gaps.add(message + ", replayed as a placeholder based on " + content.getName());
			}
			String author = author(props, heritage, version);
			lastTime = times[version];
			String marker = page && applySoftDeletes ? deletionMarker(content) : null;
			if (marker == null) {
				pendingEvents.add(new MigrationEvent(path, version, lastTime, author,
						props.getProperty(version + ".changenote", ""), content, kind));
			}
			else {
				// the wiki saved the marker as an ordinary version, so it keeps that version number, but in git the
				// page is gone from there on and comes back only if a later version restores it
				pendingEvents.add(new MigrationEvent(path, version, lastTime, author, marker, null,
						MigrationEvent.Kind.DELETE));
				result.softDeletes.add(path + " version " + version + ", " + marker);
			}
			result.countAuthor(author, lastTime);
			if (page) {
				result.pageVersions++;
			}
			else {
				result.attachmentVersions++;
			}
		}
		return lastTime;
	}

	/**
	 * The time of every version, the stored date where the wiki persisted one and the file timestamp otherwise.
	 * <p>
	 * A version must never be dated after the version that follows it, or the replay would renumber both. Where that
	 * happens the date that yields is the weaker one, a file timestamp, which a copy of the wiki data resets, or the
	 * date of version 1, which the wiki backfills from exactly such a timestamp. A conflict deeper in the history is
	 * corrected the other way instead, so one damaged date can never drag a whole history back with it. Every
	 * correction of a stored date is reported.
	 */
	private long[] times(Properties props, File[] contents, String label) {
		int latest = contents.length - 1;
		long[] times = new long[latest + 1];
		boolean[] guessed = new boolean[latest + 1];
		for (int version = 1; version <= latest; version++) {
			String stored = props.getProperty(version + ".date");
			if (stored != null) {
				try {
					times[version] = ZonedDateTime.from(DateTimeFormatter.ISO_DATE_TIME.parse(stored)).toInstant().toEpochMilli();
					continue;
				}
				catch (DateTimeException ignored) {
					// a hand edited or truncated date is not worth failing over, fall back to the file timestamp
				}
			}
			guessed[version] = true;
			times[version] = contents[version] == null ? 0 : contents[version].lastModified();
		}
		for (int version = latest - 1; version >= 1; version--) {
			if ((guessed[version] || version == 1) && times[version] > times[version + 1]) {
				report(label, version, times[version], times[version + 1], guessed[version]);
				times[version] = times[version + 1];
			}
		}
		for (int version = 2; version <= latest; version++) {
			if (times[version] < times[version - 1]) {
				report(label, version, times[version], times[version - 1], guessed[version]);
				times[version] = times[version - 1];
			}
		}
		return times;
	}

	private void report(String label, int version, long from, long to, boolean guessed) {
		if (guessed) {
			return;
		}
		result.correctedDates.add(label + " version " + version + " was dated " + Instant.ofEpochMilli(from)
				+ ", which contradicts its neighbour, and is replayed as " + Instant.ofEpochMilli(to));
	}

	/**
	 * The text of the deletion marker if this version is one, else null. Only short files are read, a page of real
	 * content can never be a marker.
	 */
	@Nullable
	private static String deletionMarker(File content) {
		if (content.length() == 0 || content.length() > MAX_MARKER_LENGTH) {
			return null;
		}
		try {
			String text = Files.readString(content.toPath(), StandardCharsets.UTF_8).trim();
			return DELETION_MARKER.matcher(text).matches() ? text : null;
		}
		catch (IOException e) {
			// not readable as text, which a marker always is
			return null;
		}
	}

	/**
	 * The content that stands in for a missing version, the closest earlier one, else the closest later one.
	 */
	@Nullable
	private static File nearestContent(File[] contents, int version) {
		for (int lower = version - 1; lower >= 1; lower--) {
			if (contents[lower] != null) {
				return contents[lower];
			}
		}
		for (int higher = version + 1; higher < contents.length; higher++) {
			if (contents[higher] != null) {
				return contents[higher];
			}
		}
		return null;
	}

	/**
	 * The author of a version. A version without its own author key falls back to the author of the properties file the
	 * plain file system provider left behind, which is what the versioning provider does when it reads such a page.
	 */
	private static String author(Properties props, Properties heritage, int version) {
		String author = props.getProperty(version + ".author");
		if (author == null || author.isEmpty()) {
			author = heritage.getProperty("author");
		}
		return author == null || author.isEmpty() ? "unknown" : author;
	}

	/**
	 * The numbered version files of a directory, keyed by version number.
	 */
	private NavigableMap<Integer, File> versionFiles(File directory, String label) {
		NavigableMap<Integer, File> files = new TreeMap<>();
		File[] candidates = directory.listFiles();
		if (candidates == null) {
			return files;
		}
		for (File candidate : candidates) {
			String name = candidate.getName();
			if (!candidate.isFile() || name.equals(PAGE_PROPERTIES) || name.equals(ATTACHMENT_PROPERTIES)) {
				continue;
			}
			Matcher matcher = VERSION_FILE.matcher(name);
			if (!matcher.matches()) {
				result.anomalies.add("ignored " + label + "/" + name + ", not a version file");
				continue;
			}
			File previous = files.put(Integer.parseInt(matcher.group(1)), candidate);
			if (previous != null) {
				result.anomalies.add(label + " has two files for version " + matcher.group(1) + ", using " + name);
			}
		}
		return files;
	}

	private static int highestVersionKey(Properties props) {
		int highest = 0;
		for (String key : props.stringPropertyNames()) {
			Matcher matcher = VERSION_KEY.matcher(key);
			if (matcher.matches()) {
				highest = Math.max(highest, Integer.parseInt(matcher.group(1)));
			}
		}
		return highest;
	}

	// --- ordering ------------------------------------------------------------

	/**
	 * Orders all events by time so the replay follows the wiki's actual chronology, after making sure no version of a
	 * file is dated before the version it follows. Without that correction a single wrong stored date would reorder
	 * two versions of one page and thereby renumber them. Versions that share a time keep their order through the tie
	 * break below, so a correction never moves a date further than it has to.
	 */
	static List<MigrationEvent> chronological(List<MigrationEvent> events) {
		Map<String, Long> latestPerPath = new HashMap<>();
		List<MigrationEvent> corrected = new ArrayList<>(events.size());
		for (MigrationEvent event : sortedByPathAndVersion(events)) {
			Long previous = latestPerPath.get(event.path());
			long time = previous == null ? event.timeMillis() : Math.max(event.timeMillis(), previous);
			latestPerPath.put(event.path(), time);
			corrected.add(event.withTime(time));
		}
		corrected.sort(Comparator.comparingLong(MigrationEvent::timeMillis)
				.thenComparing(MigrationEvent::path)
				.thenComparingInt(MigrationEvent::version));
		return corrected;
	}

	private static List<MigrationEvent> sortedByPathAndVersion(List<MigrationEvent> events) {
		List<MigrationEvent> sorted = new ArrayList<>(events);
		sorted.sort(Comparator.comparing(MigrationEvent::path).thenComparingInt(MigrationEvent::version));
		return sorted;
	}

	// --- working tree bookkeeping --------------------------------------------

	/**
	 * The paths the migration moves out of the working tree, the version store of the pages, the properties files of
	 * the plain file system provider and the version directories of the attachments.
	 */
	private void collectLegacyArtifacts() {
		if (oldDir.isDirectory()) {
			result.legacyArtifacts.add(OLD_DIR);
		}
		for (File file : listSorted(pageDir, file -> file.isFile() && file.getName().endsWith(PROPERTIES_EXT))) {
			result.legacyArtifacts.add(file.getName());
		}
		for (File attachmentDir : listSorted(pageDir, file -> file.isDirectory() && file.getName().endsWith(ATTACHMENT_DIR_EXT))) {
			for (File versionDir : listSorted(attachmentDir, file -> file.isDirectory() && file.getName().endsWith(VERSION_DIR_EXT))) {
				result.legacyArtifacts.add(relative(versionDir));
			}
		}
	}

	/**
	 * Everything that is neither migrated nor moved away, which is what git would report as untracked after the
	 * migration. These are the files a startup sweep of the git provider would otherwise commit.
	 */
	private void collectLeftovers() throws IOException {
		Set<String> migrated = new HashSet<>();
		for (MigrationEvent event : pendingEvents) {
			migrated.add(event.path());
		}
		Set<String> legacy = new HashSet<>(result.legacyArtifacts);
		try (Stream<Path> walk = Files.walk(pageDir.toPath())) {
			walk.filter(Files::isRegularFile).forEach(path -> {
				String relative = pageDir.toPath().relativize(path).toString().replace(File.separatorChar, '/');
				if (migrated.contains(relative) || relative.startsWith(".git/")) {
					return;
				}
				for (String artifact : legacy) {
					if (relative.equals(artifact) || relative.startsWith(artifact + "/")) {
						return;
					}
				}
				result.leftovers.add(relative);
			});
		}
		result.leftovers.sort(Comparator.naturalOrder());
	}

	// --- small helpers -------------------------------------------------------

	private String relative(File file) {
		return pageDir.toPath().relativize(file.toPath()).toString().replace(File.separatorChar, '/');
	}

	private static List<File> listSorted(File directory, FileFilter filter) {
		File[] files = directory.listFiles(filter);
		if (files == null) {
			return List.of();
		}
		Arrays.sort(files, Comparator.comparing(File::getName));
		return Arrays.asList(files);
	}

	private static String stripSuffix(String value, String suffix) {
		return value.substring(0, value.length() - suffix.length());
	}

	private static Properties loadProperties(File file) throws IOException {
		Properties props = new Properties();
		if (file.isFile()) {
			try (InputStream in = Files.newInputStream(file.toPath())) {
				props.load(in);
			}
		}
		return props;
	}
}
