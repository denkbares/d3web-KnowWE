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

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.denkbares.utils.Stopwatch;
import de.knowwe.core.action.UserActionContext;
import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Section;

/**
 * Marks the section as imported into another wiki at a specific time
 *
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 01.02.22
 */
public class ImportMarker {

	public static final String REQUEST_FROM = "requestFrom";
	private final Date creationDate;
	private final String source;

	public ImportMarker(@NotNull Date creationDate, @Nullable String source) {
		this.creationDate = creationDate;
		this.source = source;
	}

	/**
	 * The time at which the last import happened
	 */
	@NotNull
	public Date getCreationDate() {
		return creationDate;
	}

	/**
	 * The source to where the section was imported. May be null, if the source is unknown
	 */
	@Nullable
	public String getSource() {
		return source;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ImportMarker that = (ImportMarker) o;
		return Objects.equals(source, that.source);
	}

	@Override
	public int hashCode() {
		return Objects.hash(source);
	}

	public static void markAsImported(Section<? extends Type> section, UserActionContext context) {
		Set<ImportMarker> markers = section.computeIfAbsent(null, ImportMarker.class.getName(),
				(c, s) -> Collections.newSetFromMap(new ConcurrentHashMap<>()));
		ImportMarker marker = new ImportMarker(new Date(), context.getParameter(REQUEST_FROM));
		markers.remove(marker);
		markers.add(marker);
	}

	@NotNull
	public static Set<ImportMarker> getImportMarkers(Section<?> section) {
		Collection<? extends ImportMarker> markers = section.getObject(ImportMarker.class.getName());
		if (markers == null) return Collections.emptySet();
		return new HashSet<>(markers);
	}

	public String getInfoText() {
		return getInfoText("content");
	}

	public String getInfoText(String contentName) {
		String source = getSource();
		if (source == null) {
			source = "another wiki";
		}
		return "%%information This " + contentName + " was imported by " + source + " "
				+ Stopwatch.getDisplay(System.currentTimeMillis() - getCreationDate()
				.getTime()) + " ago /%";
	}
}
