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

package de.d3web.we.kdom.basic;

import java.util.ArrayList;
import java.util.List;

import de.d3web.we.kdom.AbstractType;
import de.d3web.we.kdom.Type;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.Sectionizable;
import de.d3web.we.kdom.sectionFinder.ISectionFinder;
import de.d3web.we.kdom.sectionFinder.SectionFinderResult;

public class EmbracedType extends AbstractType {

	boolean steal = false;

	public boolean isSteal() {
		return steal;
	}

	public void setSteal(boolean steal) {
		this.steal = steal;
	}

	public EmbracedType(Type bodyType, String start, String end) {
		this.childrenTypes.add(new EmbraceStart(start));
		this.childrenTypes.add(new EmbraceEnd(end));
		this.childrenTypes.add(bodyType);
		this.sectionFinder = new EmbracementFinder(bodyType, start, end);
	}

	class EmbracementFinder implements ISectionFinder {

		private final String start;
		private final String end;
		private final Type bodyType;

		public EmbracementFinder(Type body, String start, String end) {
			this.start = start;
			this.end = end;
			this.bodyType = body;
		}

		@Override
		public List<SectionFinderResult> lookForSections(String text,
				Section<?> father, Type type) {
			String trimmed = text.trim();
			List<SectionFinderResult> result = new ArrayList<SectionFinderResult>();
			if (bodyType instanceof Sectionizable) {
				Sectionizable sBodyType = (Sectionizable) bodyType;
				if (steal) {
					if (trimmed.contains(start) && trimmed.contains(end)) {
						String body = trimmed.substring(trimmed.indexOf(start)
								+ start.length(), trimmed.indexOf(end) + 1
								- end.length());
						List<SectionFinderResult> lookAheadSections = sBodyType
								.getSectioFinder().lookForSections(body, father, type);
						if (lookAheadSections != null
								&& lookAheadSections.size() > 0) {
							result.add(new SectionFinderResult(text.indexOf(start),
									text.indexOf(end) + end.length()));
						}
					}

				}
				else {

					if (trimmed.startsWith(start) && trimmed.endsWith(end)) {
						String body = trimmed.substring(start.length(), trimmed
								.length()
								- end.length());
						List<SectionFinderResult> lookAheadSections = sBodyType.getSectioFinder().lookForSections(
								body, father, type);
						if (lookAheadSections != null && lookAheadSections.size() > 0) {
							result.add(new SectionFinderResult(text.indexOf(trimmed),
									text.indexOf(trimmed) + trimmed.length()));
						}
					}
				}
			}
			return result;
		}

	}
}
