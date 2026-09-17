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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Compares the imported history against the commits that were planned, reading the whole repository log once.
 * <p>
 * Every version is checked by the object name of its content, which the import computed while writing, so a wrong
 * order, a lost version, a wrong author, a wrong date or wrong content all show up here without reading a single
 * source file again. The check that matters most is that version numbers line up, and a version number is nothing but
 * the position of a commit in the history of its path.
 */
public final class MigrationVerifier {

	private static final char RECORD_SEPARATOR = 1;
	private static final char FIELD_SEPARATOR = 2;
	private static final char HEADER_END = 3;

	/**
	 * The log format this verifier parses. Control characters separate the fields because a change note may contain
	 * anything a person can type, including line breaks.
	 */
	public static final String LOG_FORMAT = "%x01%at%x02%an%x02%ae%x02%B%x03";

	/** One commit as git reports it for one path. */
	private record Actual(String blob, long epochSeconds, String name, String email, String message) {
	}

	private MigrationVerifier() {
	}

	/**
	 * Returns one message per difference found, empty when the imported history matches the plan exactly.
	 *
	 * @param logOutput  output of {@code git log} in {@link #LOG_FORMAT}, oldest commit first and with raw diffs
	 * @param plan       what the import was asked to write
	 * @param ignorePath a path to leave out of the comparison, the repository setup file
	 */
	public static List<String> verify(String logOutput, ImportPlan plan, String ignorePath) {
		Map<String, List<Actual>> actual = parse(logOutput, ignorePath);
		Map<String, List<ImportPlan.PlannedCommit>> expected = new LinkedHashMap<>();
		for (ImportPlan.PlannedCommit commit : plan.commits()) {
			if (commit.event().path().equals(ignorePath)) {
				continue;
			}
			expected.computeIfAbsent(commit.event().path(), path -> new ArrayList<>()).add(commit);
		}

		List<String> problems = new ArrayList<>();
		for (Map.Entry<String, List<ImportPlan.PlannedCommit>> entry : expected.entrySet()) {
			String path = entry.getKey();
			List<ImportPlan.PlannedCommit> planned = entry.getValue();
			List<Actual> found = actual.getOrDefault(path, List.of());
			if (planned.size() != found.size()) {
				problems.add(path + " has " + found.size() + " commit(s) in git but " + planned.size() + " were planned");
				continue;
			}
			for (int index = 0; index < planned.size(); index++) {
				problems.addAll(compare(path, index + 1, planned.get(index), found.get(index)));
			}
		}
		for (String path : actual.keySet()) {
			if (!expected.containsKey(path)) {
				problems.add(path + " is in git but was never planned");
			}
		}
		return problems;
	}

	private static List<String> compare(String path, int version, ImportPlan.PlannedCommit planned, Actual actual) {
		List<String> problems = new ArrayList<>();
		String at = path + " v" + version + " ";
		if (planned.blob() == null ? !isDeletion(actual.blob()) : !planned.blob().equals(actual.blob())) {
			problems.add(at + "has content " + actual.blob() + " but " + planned.blob() + " was written");
		}
		if (planned.event().timeMillis() / 1000L != actual.epochSeconds()) {
			problems.add(at + "is dated " + actual.epochSeconds() + " but "
					+ planned.event().timeMillis() / 1000L + " was written");
		}
		if (!planned.identity().name().equals(actual.name()) || !planned.identity().email().equals(actual.email())) {
			problems.add(at + "is authored by " + actual.name() + " <" + actual.email() + "> but "
					+ planned.identity() + " was written");
		}
		if (!trimEnd(planned.event().message()).equals(trimEnd(actual.message()))) {
			problems.add(at + "has the change note '" + trimEnd(actual.message()) + "' but '"
					+ trimEnd(planned.event().message()) + "' was written");
		}
		return problems;
	}

	private static boolean isDeletion(String blob) {
		return blob == null || blob.chars().allMatch(character -> character == '0');
	}

	private static Map<String, List<Actual>> parse(String logOutput, String ignorePath) {
		Map<String, List<Actual>> byPath = new LinkedHashMap<>();
		for (String commit : logOutput.split(Pattern.quote(String.valueOf(RECORD_SEPARATOR)))) {
			if (commit.isBlank()) {
				continue;
			}
			int headerEnd = commit.indexOf(HEADER_END);
			String[] fields = commit.substring(0, headerEnd).split(Pattern.quote(String.valueOf(FIELD_SEPARATOR)), -1);
			long epochSeconds = Long.parseLong(fields[0]);
			for (String line : commit.substring(headerEnd + 1).split("\n")) {
				if (!line.startsWith(":")) {
					continue;
				}
				int tab = line.indexOf('\t');
				String[] columns = line.substring(1, tab).trim().split("\\s+");
				String path = line.substring(tab + 1);
				if (path.equals(ignorePath)) {
					continue;
				}
				byPath.computeIfAbsent(path, key -> new ArrayList<>())
						.add(new Actual(columns[3], epochSeconds, fields[1], fields[2], fields[3]));
			}
		}
		return byPath;
	}

	private static String trimEnd(String value) {
		int end = value.length();
		while (end > 0 && (value.charAt(end - 1) == '\n' || value.charAt(end - 1) == '\r')) {
			end--;
		}
		return value.substring(0, end);
	}
}
