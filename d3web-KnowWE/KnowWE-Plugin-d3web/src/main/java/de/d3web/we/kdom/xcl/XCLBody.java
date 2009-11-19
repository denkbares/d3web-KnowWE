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

package de.d3web.we.kdom.xcl;

import java.util.ArrayList;
import java.util.List;

import de.d3web.we.kdom.DefaultAbstractKnowWEObjectType;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.basic.CommentLineType;
import de.d3web.we.kdom.basic.LineBreak;
import de.d3web.we.kdom.sectionFinder.SectionFinder;
import de.d3web.we.kdom.sectionFinder.SectionFinderResult;

public class XCLBody extends DefaultAbstractKnowWEObjectType{
	
	@Override
	protected void init() {
		
		this.childrenTypes.add(new XCListBodyEndSymbol());
		this.childrenTypes.add(new XCListBodyStartSymbol());
		this.childrenTypes.add(new CommentLineType());
		this.childrenTypes.add(new XCLRelation());
		this.sectionFinder = new XCLBodySectionFinder();
	}

	public class XCLBodySectionFinder extends SectionFinder{

		@Override
		public List<SectionFinderResult> lookForSections(String text, Section father) {
			List<SectionFinderResult> matches = new ArrayList<SectionFinderResult>(1);

			if (text.indexOf('{') >= 0) {
				matches.add(
						new SectionFinderResult(
								text.indexOf('{'), text.lastIndexOf('}') + 1));
			}

			return matches;
		}
	}

}
