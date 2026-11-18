/*
 * Copyright (C) 2022 denkbares GmbH, Germany
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import de.knowwe.core.action.UserActionContext;
import de.knowwe.core.kdom.RootType;
import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Section;

/**
 * Marks the section as imported into another wiki at a specific time. The markers are only kept in memory and are
 * refreshed with every import request of the importing wiki (its poller asks every 10 minutes). A marker that was not
 * refreshed for {@link #TIME_TO_LIVE} is no longer shown, so removed imports and wikis that stopped asking disappear.
 *
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 01.02.22
 */
public class ImportMarker {

	public static final String REQUEST_FROM = "requestFrom";
	public static final String REQUEST_LINK = "requestLink";
	public static final String REFERENCE = "reference";

	/**
	 * How long a marker is shown after the last import request. Several times the poll interval of the importing wikis
	 * (10 minutes), so a few failed polls do not make the marker flicker.
	 */
	public static final Duration TIME_TO_LIVE = Duration.ofHours(3);

	private static final String DEFAULT_WIKI_LABEL = "another wiki";

	private final Date creationDate;
	private final String reference;
	private final String sourceLabel;
	private final String sourceLink;
	private static final Map<String, Set<ImportMarker>> IMPORT_MARKERS = new ConcurrentHashMap<>();

	public ImportMarker(@NotNull Date creationDate, @NotNull String reference, @Nullable String sourceLabel, @Nullable String sourceLink) {
		this.creationDate = creationDate;
		this.reference = reference;
		this.sourceLabel = sourceLabel;
		this.sourceLink = sourceLink;
	}

	/**
	 * The time at which the last import happened
	 */
	@NotNull
	public Date getCreationDate() {
		return creationDate;
	}

	/**
	 * The reference the importing wiki asked for, e.g. the page or the page and header of the imported section
	 */
	@NotNull
	public String getReference() {
		return reference;
	}

	/**
	 * The source label to where the section was imported. May be null, if the source is unknown
	 */
	@Nullable
	public String getSourceLabel() {
		return sourceLabel;
	}

	/**
	 * The absolute link  to where the section was imported. May be null, if the source is unknown
	 */
	@Nullable
	public String getSourceLink() {
		return sourceLink;
	}

	/**
	 * Whether the last import request is longer ago than {@link #TIME_TO_LIVE}.
	 */
	public boolean isExpired(@NotNull Instant now) {
		return creationDate.toInstant().plus(TIME_TO_LIVE).isBefore(now);
	}

	/**
	 * The name of the importing wiki, the part of the label before the first ": " (the importing wiki sends
	 * "&lt;wiki&gt;: &lt;page&gt;"), or "another wiki" if it is not known.
	 */
	@NotNull
	public String getWikiLabel() {
		if (sourceLabel == null) return DEFAULT_WIKI_LABEL;
		int separator = sourceLabel.indexOf(": ");
		if (separator <= 0) return DEFAULT_WIKI_LABEL;
		return sourceLabel.substring(0, separator);
	}

	/**
	 * The title of the importing page, the part of the label after the first ": ", or the whole label if it has no
	 * wiki part. Null if the label is not known.
	 */
	@Nullable
	public String getPageLabel() {
		if (sourceLabel == null) return null;
		int separator = sourceLabel.indexOf(": ");
		return separator < 0 ? sourceLabel : sourceLabel.substring(separator + 2);
	}

	/**
	 * The base URL of the importing wiki, the link up to "Wiki.jsp", or null if there is no such link.
	 */
	@Nullable
	public String getWikiUrl() {
		if (sourceLink == null) return null;
		int index = sourceLink.indexOf("Wiki.jsp");
		return index < 0 ? null : sourceLink.substring(0, index);
	}

	/**
	 * The link to the importing page without the anchor of the importing section, or null if there is no link.
	 */
	@Nullable
	public String getPageLink() {
		if (sourceLink == null) return null;
		int anchor = sourceLink.indexOf('#');
		return anchor < 0 ? sourceLink : sourceLink.substring(0, anchor);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ImportMarker that = (ImportMarker) o;
		return Objects.equals(sourceLabel, that.sourceLabel) && Objects.equals(sourceLink, that.sourceLink) && Objects.equals(reference, that.reference);
	}

	@Override
	public int hashCode() {
		return Objects.hash(sourceLabel, sourceLink, reference);
	}

	public static void markAsImported(Section<? extends Type> section, UserActionContext context) {
		markAsImported(section, context.getParameter(REFERENCE), context.getParameter(REQUEST_FROM),
				context.getParameter(REQUEST_LINK));
	}

	/**
	 * Records that the given source section was imported by the wiki identified by {@code sourceLabel} /
	 * {@code sourceLink}. Used both by the direct {@link GetWikiSectionTextAction} (params from the request)
	 * and by the bulk {@link GetWikiChangesSinceAction} (values carried per import in the request body), so
	 * the "imported by" marker shows up regardless of which path resolved the content.
	 */
	public static void markAsImported(Section<? extends Type> section, @NotNull String reference,
									  @Nullable String sourceLabel, @Nullable String sourceLink) {
		markAsImported(section, new ImportMarker(new Date(), reference, sourceLabel, sourceLink));
	}

	static void markAsImported(Section<? extends Type> section, ImportMarker marker) {
		Set<ImportMarker> markers = section.computeIfAbsent(null, ImportMarker.class.getName(),
				(c, s) -> Collections.newSetFromMap(new ConcurrentHashMap<>()));

		// remove+add so the creation date (the "... ago" shown in the marker) refreshes on every import
		markers.remove(marker);
		markers.add(marker);

		// also safe statically, so it can be displayed after the page changed
		Set<ImportMarker> staticMarkersOfPage = IMPORT_MARKERS.computeIfAbsent(section.getTitle(), k -> Collections.newSetFromMap(new ConcurrentHashMap<>()));
		staticMarkersOfPage.remove(marker);
		staticMarkersOfPage.add(marker);
	}

	/**
	 * Returns the markers of the section that are not expired. For the root section of a page, the markers of all
	 * sections of the page are returned, also the ones of sections that no longer exist since the page was changed.
	 */
	@NotNull
	public static List<ImportMarker> getImportMarkers(Section<?> section) {
		Instant now = Instant.now();
		Collection<ImportMarker> markers = section.getObject(ImportMarker.class.getName());
		if (markers == null) markers = Set.of();
		else markers.removeIf(marker -> marker.isExpired(now));
		List<ImportMarker> markerList = new ArrayList<>(markers);
		if (section.get() instanceof RootType) {
			Set<ImportMarker> staticMarkersOfPage = IMPORT_MARKERS.get(section.getTitle());
			if (staticMarkersOfPage != null) {
				staticMarkersOfPage.removeIf(marker -> marker.isExpired(now));
				Set<ImportMarker> staticMarkers = new HashSet<>(staticMarkersOfPage);
				staticMarkers.removeAll(markers); // don't duplicate
				markerList.addAll(staticMarkers);
			}
		}
		markerList.sort(Comparator.comparing((ImportMarker m) -> m.getSourceLabel() == null ? "" : m.getSourceLabel())
				.thenComparing(m -> m.getSourceLink() == null ? "" : m.getSourceLink())
				.thenComparing(ImportMarker::getCreationDate));
		return List.copyOf(markerList);
	}

	/**
	 * The imports of one importing page, i.e. of all its import markups asking for the same source page or section.
	 *
	 * @param label   the title of the importing page, null if not known
	 * @param link    the link to the importing page, null if not known
	 * @param markers the markers of the imports, at least one
	 */
	public record PageImports(@Nullable String label, @Nullable String link, @NotNull List<ImportMarker> markers) {

		/** The time of the most recent import of this page */
		@NotNull
		public Instant latestImport() {
			return latest(markers);
		}
	}

	/**
	 * The imports of one importing wiki, grouped by importing page.
	 *
	 * @param label the name of the importing wiki
	 * @param url   the base URL of the importing wiki, null if not known
	 * @param pages the importing pages, at least one
	 */
	public record WikiImports(@NotNull String label, @Nullable String url, @NotNull List<PageImports> pages) {

		/** The time of the most recent import of this wiki */
		@NotNull
		public Instant latestImport() {
			return latest(pages.stream().flatMap(page -> page.markers().stream()).toList());
		}

		/** The number of imports (import markups and source wikis) of this wiki */
		public int importCount() {
			return pages.stream().mapToInt(page -> page.markers().size()).sum();
		}
	}

	private static Instant latest(List<ImportMarker> markers) {
		return markers.stream()
				.map(marker -> marker.getCreationDate().toInstant())
				.max(Comparator.naturalOrder())
				.orElse(Instant.EPOCH);
	}

	/**
	 * Groups the markers by importing wiki and, within each wiki, by importing page, so several imports of the same
	 * wiki or page can be shown as one entry. Wikis and pages keep the order of their first marker.
	 */
	@NotNull
	public static List<WikiImports> groupByWiki(@NotNull List<ImportMarker> markers) {
		Map<List<String>, Map<List<String>, List<ImportMarker>>> wikis = new LinkedHashMap<>();
		for (ImportMarker marker : markers) {
			List<String> wikiKey = Arrays.asList(marker.getWikiLabel(), marker.getWikiUrl());
			List<String> pageKey = Arrays.asList(marker.getPageLabel(), marker.getPageLink());
			wikis.computeIfAbsent(wikiKey, k -> new LinkedHashMap<>())
					.computeIfAbsent(pageKey, k -> new ArrayList<>())
					.add(marker);
		}
		List<WikiImports> result = new ArrayList<>();
		wikis.forEach((wikiKey, pages) -> {
			List<PageImports> pageImports = new ArrayList<>();
			pages.forEach((pageKey, pageMarkers) ->
					pageImports.add(new PageImports(pageKey.get(0), pageKey.get(1), List.copyOf(pageMarkers))));
			result.add(new WikiImports(wikiKey.get(0), wikiKey.get(1), List.copyOf(pageImports)));
		});
		return List.copyOf(result);
	}

	/** Removes all markers kept for the pages, only for tests. */
	static void clearStaticMarkers() {
		IMPORT_MARKERS.clear();
	}
}
