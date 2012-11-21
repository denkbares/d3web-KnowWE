/*
 * Copyright (C) 2011 denkbares GmbH
 * 
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
package de.knowwe.jspwiki.types;

import java.util.ArrayList;
import java.util.List;

import de.knowwe.core.kdom.AbstractType;
import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.sectionFinder.SectionFinder;
import de.knowwe.core.kdom.sectionFinder.SectionFinderResult;

/**
 * 
 * @author Lukas Brehl
 * @created 25.05.2012
 */

public class SectionHeaderType extends AbstractType {

	/*
	 * The SectionHeaderType takes the first line of each SectionType.
	 */
	public SectionHeaderType() {
		this.setSectionFinder(new SectionHeaderSectionFinder());
	}

	public class SectionHeaderSectionFinder implements SectionFinder {

		@Override
		public List<SectionFinderResult> lookForSections(String text,
				Section<?> father, Type type) {
			List<SectionFinderResult> result = new ArrayList<SectionFinderResult>();
			String[] rows = text.split("(\n)");
			int end = rows[0].length() + 1;
			SectionFinderResult s = new SectionFinderResult(0, end);
			result.add(s);
			return result;
		}
	}
}
