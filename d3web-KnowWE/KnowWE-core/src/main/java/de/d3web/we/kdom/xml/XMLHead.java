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

import de.d3web.we.kdom.DefaultAbstractKnowWEObjectType;
import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.SectionIDDeclarant;
import de.d3web.we.kdom.renderer.NothingRenderer;
import de.d3web.we.kdom.rendering.KnowWEDomRenderer;
import de.d3web.we.kdom.sectionFinder.SectionFinder;
import de.d3web.we.kdom.sectionFinder.SectionFinderResult;

public class XMLHead extends DefaultAbstractKnowWEObjectType implements SectionIDDeclarant {

	public static final String SEPARATOR = "/";
	public static final String HEAD_SUFFIX = "_head";

	@Override
	public String createSectionID(Section<? extends KnowWEObjectType> father) {
		return getEndOfId(father.getID()) + HEAD_SUFFIX;
	}

	private String getEndOfId(String id) {
		return id.substring(id.lastIndexOf(SEPARATOR) + 1);
	}

	@Override
	protected void init() {
		sectionFinder = new XMLHeadSectionFinder();
		this.allowesGlobalTypes = false;
	}

	@Override
	public KnowWEDomRenderer getRenderer() {
		return NothingRenderer.getInstance();
	}

	public class XMLHeadSectionFinder extends SectionFinder {

		@Override
		public List<SectionFinderResult> lookForSections(String text2, Section father, KnowWEObjectType type) {

			if (father.getObjectType() instanceof AbstractXMLObjectType) {
				String text = AbstractXMLObjectType.getAttributeMapFor(father).get(
						AbstractXMLObjectType.HEAD);
				if (text != null) {
					List<SectionFinderResult> result = new ArrayList<SectionFinderResult>();
					result.add(new SectionFinderResult(0, text.length()));
					return result;
				}
			}

			return null;
		}

	}
}
