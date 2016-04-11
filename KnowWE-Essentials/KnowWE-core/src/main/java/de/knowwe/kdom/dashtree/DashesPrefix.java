/*
 * Copyright (C) 2009 Chair of Artificial Intelligence and Applied Informatics
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
package de.knowwe.kdom.dashtree;

import java.util.ArrayList;
import java.util.List;

import de.knowwe.core.kdom.AbstractType;
import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.rendering.DefaultTextRenderer;
import de.knowwe.core.kdom.sectionFinder.SectionFinder;
import de.knowwe.core.kdom.sectionFinder.SectionFinderResult;

public class DashesPrefix extends AbstractType {

	public DashesPrefix(final char key) {
		this.setSectionFinder(new DashesPrefixFinder(key));
		this.setRenderer((sec, user, string) -> {
			// to suppress horizontal line in case of more than three dashes
			if (sec.getText().trim().startsWith("" + key) && key == '-') {
				string.append('~');
			}
			DefaultTextRenderer.getInstance().render(sec, user, string);
		});
	}

	class DashesPrefixFinder implements SectionFinder {

		private final char key;

		public DashesPrefixFinder(char key) {
			this.key = key;
		}

		@Override
		public List<SectionFinderResult> lookForSections(String text,
				Section<?> father, Type type) {

			int leadingSpaces = text.indexOf(text.trim());

			int index = leadingSpaces;
			while (text.charAt(index) == key) {
				index++;
			}
			ArrayList<SectionFinderResult> result = new ArrayList<SectionFinderResult>();
			result.add(new SectionFinderResult(0, index));

			return result;
		}

	}

}
