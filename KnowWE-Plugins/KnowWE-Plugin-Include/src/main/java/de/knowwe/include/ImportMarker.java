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

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.denkbares.utils.Stopwatch;
import de.knowwe.core.action.UserActionContext;
import de.knowwe.core.kdom.RootType;
import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.utils.KnowWEUtils;

/**
 * Marks the section as imported into another wiki at a specific time
 *
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 01.02.22
 */
public class ImportMarker {

	public static final String REQUEST_FROM = "requestFrom";
	public static final String REQUEST_LINK = "requestLink";
	public static final String REFERENCE = "reference";
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
		Set<ImportMarker> markers = section.computeIfAbsent(null, ImportMarker.class.getName(),
				(c, s) -> Collections.newSetFromMap(new ConcurrentHashMap<>()));
		ImportMarker marker = new ImportMarker(new Date(), context.getParameter(REFERENCE), context.getParameter(REQUEST_FROM), context.getParameter(REQUEST_LINK));

		markers.remove(marker);
		markers.add(marker);

		// also safe statically, so it can be displayed after the page changed
		Set<ImportMarker> staticMarkersOfPage = IMPORT_MARKERS.computeIfAbsent(section.getTitle(), k -> Collections.newSetFromMap(new ConcurrentHashMap<>()));
		staticMarkersOfPage.add(marker);
		Date twoHoursAgo = Date.from(Instant.now().minus(2, ChronoUnit.HOURS));
		staticMarkersOfPage.removeIf(m -> m.getCreationDate().before(twoHoursAgo)); // cleanup
	}

	@NotNull
	public static List<ImportMarker> getImportMarkers(Section<?> section) {
		Collection<? extends ImportMarker> markers = section.getObject(ImportMarker.class.getName());
		if (markers == null) markers = Set.of();
		List<ImportMarker> markerList = new ArrayList<>(markers);
		if (section.get() instanceof RootType) {
			Set<ImportMarker> staticMarkers = IMPORT_MARKERS.getOrDefault(section.getTitle(), new HashSet<>());
			staticMarkers.removeAll(markers); // don't duplicate
			markerList.addAll(staticMarkers);
		}
		markerList.sort(Comparator.comparing(ImportMarker::getSourceLabel).thenComparing(ImportMarker::getSourceLink));
		return List.copyOf(markerList);
	}

	public String getInfoText() {
		return getInfoText("content");
	}

	public String getInfoText(String contentName) {
		String source;
		String sourceLabel = getSourceLabel();
		if (sourceLabel == null) {
			sourceLabel = "another wiki";
		}
		String sourceLink = getSourceLink();
		if (sourceLink == null) {
			source = sourceLabel;
		}
		else {
			source = KnowWEUtils.getWikiLink(sourceLabel, sourceLink);
		}

		return "%%information This " + contentName + " was imported by " + source + " "
				+ Stopwatch.getDisplay(System.currentTimeMillis() - getCreationDate()
				.getTime()) + " ago /%";
	}
}
