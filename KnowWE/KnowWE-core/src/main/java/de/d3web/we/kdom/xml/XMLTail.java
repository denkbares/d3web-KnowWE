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

package de.d3web.we.kdom.xml;

import java.util.ArrayList;
import java.util.List;

import de.d3web.we.kdom.AbstractType;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.SectionIDDeclarant;
import de.d3web.we.kdom.Type;
import de.d3web.we.kdom.rendering.KnowWEDomRenderer;
import de.d3web.we.kdom.rendering.NothingRenderer;
import de.d3web.we.kdom.sectionFinder.SectionFinder;
import de.d3web.we.kdom.sectionFinder.SectionFinderResult;

public class XMLTail extends AbstractType implements SectionIDDeclarant {

	public static final String SEPARATOR = "/";
	public static final String TAIL_SUFFIX = "_tail";

	@Override
	public String createSectionID(Section<? extends Type> father) {
		return getEndOfId(father.getID()) + TAIL_SUFFIX;
	}

	private String getEndOfId(String id) {
		return id.substring(id.lastIndexOf(SEPARATOR) + 1);
	}

	@Override
	protected void init() {
		sectionFinder = new XMLTailSectionFinder();
		this.allowesGlobalTypes = false;
	}

	@Override
	public KnowWEDomRenderer getRenderer() {
		return NothingRenderer.getInstance();
	}

	public class XMLTailSectionFinder implements SectionFinder {

		@Override
		public List<SectionFinderResult> lookForSections(String text2, Section<?> father, Type type) {

			if (father.get() instanceof AbstractXMLType) {
				String text = AbstractXMLType.getAttributeMapFor(father).get(
						AbstractXMLType.TAIL);
				if (text != null) {
					int start = text2.lastIndexOf(text);
					List<SectionFinderResult> result = new ArrayList<SectionFinderResult>();
					result.add(new SectionFinderResult(start, start + text.length()));
					return result;
				}
			}

			return null;
		}

	}

}
