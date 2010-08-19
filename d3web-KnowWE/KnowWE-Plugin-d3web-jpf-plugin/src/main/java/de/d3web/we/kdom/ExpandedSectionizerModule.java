/*
 * Copyright (C) 2010 Chair of Artificial Intelligence and Applied Informatics
 * Computer Science VI, University of Wuerzburg
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

package de.d3web.we.kdom;

import de.d3web.we.kdom.sectionFinder.SectionFinderResult;

public class ExpandedSectionizerModule implements SectionizerModule {

	@Override
	public Section<?> createSection(KnowWEArticle article, KnowWEObjectType ob, Section<?> father, Section<?> thisSection, String secText, SectionFinderResult result) {
		if (result instanceof ExpandedSectionFinderResult) {
			return createExpandedSection(
					(ExpandedSectionFinderResult) result, father);

		}
		return null;
	}

	private Section<?> createExpandedSection(ExpandedSectionFinderResult result, Section<?> father) {
		Section<?> s = Section.createTypedSection(result.getText(),
				result.getObjectType(), father,
				result.getStart(), father.getArticle(), null, true);
		if (s.getOffSetFromFatherText() < 0
				|| s.getOffSetFromFatherText() > father.getOriginalText().length()
				|| !father.getOriginalText().substring(s.getOffSetFromFatherText()).startsWith(
						s.getOriginalText())) {
			s.setOffSetFromFatherText(father.getOriginalText().indexOf(
					s.getOriginalText()));
		}

		for (ExpandedSectionFinderResult childResult : result.getChildren()) {
			createExpandedSection(childResult, s);
		}
		return s;
	}

}
