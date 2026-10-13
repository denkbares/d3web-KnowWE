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
package de.knowwe.include;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Locale;
import java.util.Map;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.denkbares.strings.Strings;

/**
 * Display helpers for the annotations of the InterWikiImport markup, rendered as a label/value list.
 */
final class InterWikiImportAnnotationFormat {

	private static final Map<String, String> LABELS = Map.ofEntries(
			Map.entry("wiki", "Wiki"),
			Map.entry("page", "Page"),
			Map.entry("section", "Section"),
			Map.entry("mode", "Mode"),
			Map.entry("latestChange", "Last change in source"),
			Map.entry("trackingAcceptedAt", "Acknowledged"),
			Map.entry("replacement", "Replacement"),
			Map.entry("regexReplacement", "Regex replacement"),
			Map.entry("interval", "Interval"),
			Map.entry("compile", "Compile"),
			Map.entry("validationMode", "Validation"),
			Map.entry("package", "Package"));

	private InterWikiImportAnnotationFormat() {
	}

	/**
	 * Returns the display label of the annotation, for unknown annotations the camel case name split
	 * into words, e.g. "someAnnotation" becomes "Some annotation".
	 */
	static @NotNull String getLabel(@NotNull String annotationName) {
		String label = LABELS.get(annotationName);
		if (label != null) return label;
		String words = annotationName.replaceAll("([a-z0-9])([A-Z])", "$1 $2").toLowerCase(Locale.ROOT);
		return words.isEmpty() ? annotationName : Character.toUpperCase(words.charAt(0)) + words.substring(1);
	}

	/**
	 * Formats the instant as local date and time, e.g. "Oct 1, 2026, 1:18 PM" or "01.10.2026, 13:18".
	 */
	static @NotNull String formatDateTime(@NotNull Instant instant, @NotNull Locale locale, @NotNull ZoneId zone) {
		return DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.SHORT)
				.withLocale(locale)
				.withZone(zone)
				.format(instant);
	}

	/**
	 * Formats the time between the instant and now in its largest unit, e.g. "less than 1 minute ago", "5 minutes ago",
	 * "3 hours ago" or "2 days ago".
	 */
	static @NotNull String formatRelative(@NotNull Instant instant, @NotNull Instant now) {
		Duration duration = Duration.between(instant, now);
		if (duration.isNegative()) return "in the future";
		long minutes = duration.toMinutes();
		if (minutes < 1) return "less than 1 minute ago";
		if (minutes < 60) return plural(minutes, "minute") + " ago";
		long hours = duration.toHours();
		if (hours < 48) return plural(hours, "hour") + " ago";
		return plural(duration.toDays(), "day") + " ago";
	}

	private static String plural(long count, String unit) {
		return count + " " + unit + (count == 1 ? "" : "s");
	}

	/**
	 * Returns the wiki without protocol and trailing slash, e.g. "wiki.example.com/KnowWE".
	 */
	static @NotNull String formatWiki(@NotNull String wiki) {
		return wiki.trim().replaceAll("^https?://", "").replaceAll("/+$", "");
	}

	/**
	 * Formats the import mode, e.g. "tracking" becomes "Tracking".
	 */
	static @NotNull String formatMode(@NotNull String mode) {
		String trimmed = mode.trim();
		if (trimmed.isEmpty()) return trimmed;
		return Character.toUpperCase(trimmed.charAt(0)) + trimmed.substring(1).toLowerCase(Locale.ROOT);
	}

	/**
	 * Splits a replacement annotation "search->replacement" the same way the replacement is applied
	 * (see AttachmentUpdateMarkup), or returns null if the text is no replacement.
	 */
	static @Nullable String[] splitReplacement(@NotNull String replacement) {
		String[] parsed = Strings.parseConcat("->", replacement);
		if (parsed.length < 2) return null;
		return new String[] { parsed[0], parsed[1] };
	}
}
