/*
 * Copyright (C) 2010 University Wuerzburg, Computer Science VI
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package de.d3web.we.kdom.table.attributes;

import java.util.ArrayList;
import java.util.List;

import de.d3web.we.kdom.DefaultAbstractKnowWEObjectType;
import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.sectionFinder.ISectionFinder;
import de.d3web.we.kdom.sectionFinder.SectionFinderResult;
import de.d3web.we.kdom.table.TableCellContent;
import de.d3web.we.kdom.table.TableUtils;

/**
 * Temp Type which will be overridden by the AttributeTable's SubtreeHandler.
 * This SubtreeHandler will give these types the correct
 * 
 * @author Sebastian Furth
 * @created 28/10/2010
 */
public class AttributeTableTempType extends DefaultAbstractKnowWEObjectType {

	public AttributeTableTempType() {
		sectionFinder = new AttributeTableTempTypeSectionFinder();
	}

	public class AttributeTableTempTypeSectionFinder implements ISectionFinder {

		@SuppressWarnings("unchecked")
		@Override
		public List<SectionFinderResult> lookForSections(String text, Section<?> father, KnowWEObjectType type) {

			List<SectionFinderResult> result = new ArrayList<SectionFinderResult>();

			if (TableUtils.getColumn((Section<? extends TableCellContent>) father) == 0) {
				String trimmed = text.trim();
				int start = text.indexOf(trimmed);
				int end = start + trimmed.length();
				result.add(new SectionFinderResult(start, end));
			}

			return result;
		}
	}

}
