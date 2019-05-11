/*
 * Copyright (C) 2014 denkbares GmbH, Germany
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

import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;

/**
* @author Albrecht Striffler (denkbares GmbH)
* @created 19.12.14
*/
public class SectionIDSource implements ArticleStatementSource {

	private final String sectionID;

	public SectionIDSource(Section<?> section) {
		this.sectionID = section.getID();
	}

	@Override
	public Article getArticle() {
		return Sections.get(sectionID).getArticle();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		SectionIDSource that = (SectionIDSource) o;

		if (!sectionID.equals(that.sectionID)) return false;

		return true;
	}

	@Override
	public int hashCode() {
		return sectionID.hashCode();
	}
}
