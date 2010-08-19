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

import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.kdom.include.Include;
import de.d3web.we.kdom.include.IncludeSectionFinderResult;
import de.d3web.we.kdom.sectionFinder.SectionFinderResult;
import de.d3web.we.utils.KnowWEUtils;

public class IncludeSectionizerModule implements SectionizerModule {

	@Override
	@SuppressWarnings("unchecked")
	public Section<?> createSection(KnowWEArticle article, KnowWEObjectType ob, Section<?> father, Section<?> thisSection, String secText, SectionFinderResult result) {
		Section s = null;
		if (result instanceof IncludeSectionFinderResult) {
			s = Section.createTypedSection(
					thisSection.getOriginalText().substring(
							result.getStart(),
							result.getEnd()),
					ob,
					father,
					thisSection.getOffSetFromFatherText()
							+ result.getStart(),
					article,
					result.getId(),
					false);

			KnowWEUtils.storeSectionInfo(s.getWeb(), s.getTitle(), s.getID(),
					Include.INCLUDE_ADDRESS_KEY,
					((IncludeSectionFinderResult) result).getIncludeAddress());
			KnowWEEnvironment.getInstance().getIncludeManager(
					s.getWeb()).registerInclude(s);

		}
		return s;
	}

}
