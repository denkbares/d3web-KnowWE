/*
 * Copyright (C) 2012 University Wuerzburg, Computer Science VI
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
package de.knowwe.core.compile.terminology;

import java.util.Objects;

import org.jetbrains.annotations.NotNull;

import com.denkbares.strings.Identifier;
import de.knowwe.core.kdom.parsing.Section;

class TermLogEntry implements Comparable<TermLogEntry> {

	private final Section<?> section;
	private final Class<?> termClass;
	private final Identifier termIdentifier;

	public TermLogEntry(@NotNull Section<?> section, @NotNull Class<?> termClass, @NotNull Identifier termIdentifier) {
		this.section = section;
		this.termClass = termClass;
		this.termIdentifier = termIdentifier;
	}

	public Section<?> getSection() {
		return section;
	}

	public Class<?> getTermClass() {
		return termClass;
	}

	public Identifier getTermIdentifier() {
		return termIdentifier;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		TermLogEntry that = (TermLogEntry) o;
		return section.equals(that.section) && termClass.equals(that.termClass) && termIdentifier.equals(that.termIdentifier);
	}

	@Override
	public int hashCode() {
		return Objects.hash(section, termClass, termIdentifier);
	}

	@Override
	public int compareTo(@NotNull TermLogEntry o) {
		if (this == o) return 0;

		int result = section.compareTo(o.section);
		if (result != 0) return result;
		result = termClass.getName().compareTo(o.termClass.getName());
		if (result != 0) return result;
		return termIdentifier.compareTo(o.termIdentifier);
	}
}
