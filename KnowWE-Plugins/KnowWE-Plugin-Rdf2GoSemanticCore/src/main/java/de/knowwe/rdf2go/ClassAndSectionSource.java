/*
 * Copyright (C) 2025 denkbares GmbH, Germany
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

package de.knowwe.rdf2go;

import java.util.Objects;

import org.jetbrains.annotations.NotNull;

import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.parsing.Section;

/**
 * StatementSource with additional class, to have more control over which statements to clear.
 *
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 22.01.2025
 */
public class ClassAndSectionSource implements ArticleStatementSource {

	private final Section<?> section;
	private final Class<?> clazz;

	public ClassAndSectionSource(@NotNull Section<?> section, @NotNull Class<?> clazz) {
		this.section = section;
		this.clazz = clazz;
	}

	@Override
	public Article getArticle() {
		return section.getArticle();
	}

	@Override
	public boolean equals(Object o) {
		if (o == null || getClass() != o.getClass()) return false;
		ClassAndSectionSource that = (ClassAndSectionSource) o;
		return Objects.equals(section, that.section) && Objects.equals(clazz, that.clazz);
	}

	@Override
	public int hashCode() {
		return Objects.hash(section, clazz);
	}
}
