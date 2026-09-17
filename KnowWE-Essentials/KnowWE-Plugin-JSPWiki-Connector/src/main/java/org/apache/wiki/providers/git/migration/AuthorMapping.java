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
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Maps the author strings the file provider stored to the git identities the commits are attributed to.
 * <p>
 * The wiki keeps only one opaque string per version, whatever {@code page.getAuthor()} returned at save time, which is
 * a login name, a mail address or the literal "unknown". The git provider instead resolves a full name and a mail
 * address at save time, from the user database or from what the identity provider stated at sign in. Neither source is
 * reachable from a stand alone tool, and with single sign on the identity data is not persisted anywhere at all, so the
 * mapping has to be supplied by hand.
 * <p>
 * The file is written once by a scanning run, edited by a human and read back by the real run. Several wiki authors may
 * map to the same identity, which is how outdated mail addresses of one person are merged.
 */
public final class AuthorMapping {

	private static final DateTimeFormatter DAY = DateTimeFormatter.ofPattern("yyyy-MM-dd").withZone(ZoneId.systemDefault());

	/**
	 * A git commit identity. An empty mail address is legal and is what git records when none is known.
	 */
	public record Identity(String name, String email) {

		public Identity {
			name = sanitize(name);
			email = sanitize(email);
		}

		/**
		 * Strips what would break the {@code Name <mail>} form of a git identity line.
		 */
		private static String sanitize(String value) {
			if (value == null) {
				return "";
			}
			return value.replace("<", "").replace(">", "").replace("\n", " ").replace("\r", " ").trim();
		}

		@Override
		public String toString() {
			return name + " <" + email + ">";
		}
	}

	/**
	 * How often and when an author string occurs, used to order the template so the few authors that matter come first.
	 */
	public static final class Stats {
		int count;
		long firstMillis = Long.MAX_VALUE;
		long lastMillis = Long.MIN_VALUE;

		void add(long timeMillis) {
			count++;
			firstMillis = Math.min(firstMillis, timeMillis);
			lastMillis = Math.max(lastMillis, timeMillis);
		}
	}

	private final Map<String, Identity> identities;
	private final Set<String> unmapped = new LinkedHashSet<>();

	private AuthorMapping(Map<String, Identity> identities) {
		this.identities = identities;
	}

	/**
	 * An empty mapping, every author falls back to {@link #fallback}.
	 */
	public static AuthorMapping empty() {
		return new AuthorMapping(new LinkedHashMap<>());
	}

	/**
	 * Fixes the identity of one author, which the migration uses for the commits it writes itself.
	 */
	public void define(String author, Identity identity) {
		identities.put(author, identity);
	}

	/**
	 * Reads a mapping file of {@code <wiki author> = <Full Name> <mail>} lines, ignoring blank lines and lines starting
	 * with a hash.
	 */
	public static AuthorMapping load(File file) throws IOException {
		Map<String, Identity> identities = new LinkedHashMap<>();
		List<String> lines = Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);
		for (int i = 0; i < lines.size(); i++) {
			String line = lines.get(i).trim();
			if (line.isEmpty() || line.startsWith("#")) {
				continue;
			}
			int separator = line.indexOf('=');
			if (separator < 0) {
				throw new IOException("Malformed author mapping in " + file + " line " + (i + 1) + ", expected '<author> = <Name> <mail>'");
			}
			String author = line.substring(0, separator).trim();
			identities.put(author, parseIdentity(line.substring(separator + 1).trim()));
		}
		return new AuthorMapping(identities);
	}

	private static Identity parseIdentity(String value) {
		int open = value.lastIndexOf('<');
		int close = value.lastIndexOf('>');
		if (open >= 0 && close > open) {
			return new Identity(value.substring(0, open).trim(), value.substring(open + 1, close).trim());
		}
		return new Identity(value, "");
	}

	/**
	 * The identity to attribute a commit of the given wiki author to. Unmapped authors fall back and are remembered,
	 * so a run can report them.
	 */
	public Identity resolve(String author) {
		Identity identity = identities.get(author);
		if (identity != null) {
			return identity;
		}
		unmapped.add(author);
		return fallback(author);
	}

	/**
	 * What the git provider itself would record for an author it cannot resolve, the author string as the name plus,
	 * where the author is a mail address, that address as the mail address.
	 */
	public static Identity fallback(String author) {
		String value = author == null ? "unknown" : author;
		return new Identity(value, value.contains("@") ? value : "");
	}

	/**
	 * The author strings {@link #resolve} did not find, in first use order.
	 */
	public Set<String> unmapped() {
		return unmapped;
	}

	/**
	 * Writes the template a human edits, one line per author string, prefilled with the fallback identity and
	 * annotated with how often and in which period that author saved.
	 */
	public static void writeTemplate(File file, Map<String, Stats> statsByAuthor) throws IOException {
		List<Map.Entry<String, Stats>> entries = new ArrayList<>(new TreeMap<>(statsByAuthor).entrySet());
		entries.sort((left, right) -> Integer.compare(right.getValue().count, left.getValue().count));
		try (PrintWriter out = new PrintWriter(Files.newBufferedWriter(file.toPath(), StandardCharsets.UTF_8))) {
			out.println("# Wiki author to git identity mapping, one line per author string found in the wiki.");
			out.println("# Format is '<wiki author> = <Full Name> <mail address>'.");
			out.println("#");
			out.println("# Every line is prefilled with the fallback the git provider itself would use. Correct the ones");
			out.println("# that matter, ideally so they match what the running wiki records for the same person today,");
			out.println("# which 'git shortlog -se' of an already migrated wiki reports. Several authors may be mapped to");
			out.println("# the same identity, which merges outdated mail addresses of one person into one contributor.");
			out.println("#");
			out.println("# Authors are ordered by how many versions they saved.");
			out.println();
			for (Map.Entry<String, Stats> entry : entries) {
				Stats stats = entry.getValue();
				out.println("# " + stats.count + " version(s), " + DAY.format(Instant.ofEpochMilli(stats.firstMillis))
						+ " until " + DAY.format(Instant.ofEpochMilli(stats.lastMillis)));
				out.println(entry.getKey() + " = " + fallback(entry.getKey()));
				out.println();
			}
		}
	}
}
