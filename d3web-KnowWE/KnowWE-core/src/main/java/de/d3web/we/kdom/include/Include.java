/*
 * Copyright (C) 2009 Chair of Artificial Intelligence and Applied Informatics
 *                    Computer Science VI, University of Wuerzburg
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

package de.d3web.we.kdom.include;

import java.util.Map;

import de.d3web.we.kdom.SectionID;
import de.d3web.we.kdom.sectionFinder.SectionFinder;
import de.d3web.we.kdom.sectionFinder.SectionFinderResult;
import de.d3web.we.kdom.xml.AbstractXMLObjectType;
import de.d3web.we.kdom.xml.XMLSectionFinder;


public class Include extends AbstractXMLObjectType {

	private static Include instance;
	
	public static Include getInstance() {
		if (instance == null) {
			instance = new Include();
		}
		return instance;
	}
	
	private Include() {
		super("include");
		this.customRenderer = new EditIncludeSectionRenderer(IncludeSectionRenderer.getInstance());
	}
	
	@Override
	public SectionFinder getSectioner() {
		return new IncludeSectionFinder(this.getXMLTagName());
	}
	
	private class IncludeSectionFinder extends XMLSectionFinder {
		
		public IncludeSectionFinder(String tagName) {
			super(tagName);
		}
		
		@Override
		protected SectionFinderResult makeSectionFinderResult(int start, 
				int end, SectionID sectionID, Map<String, String> parameterMap) {

			String src = parameterMap.get("src");
			if (src != null) {
				IncludeAddress a = new IncludeAddress(parameterMap.get("src"));
				return new IncludeSectionFinderResult(start, end, sectionID, a);
			}
			return new IncludeSectionFinderResult(start, end, sectionID, null);
		}
		
	}

}

