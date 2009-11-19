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
package de.d3web.we.kdom.dashTree;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.d3web.we.kdom.DefaultAbstractKnowWEObjectType;
import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.sectionFinder.SectionFinder;
import de.d3web.we.kdom.sectionFinder.SectionFinderResult;

public class SubTree extends DefaultAbstractKnowWEObjectType{
	
	@Override
	protected void init() {
		this.sectionFinder = new SubTreeFinder();
		this.childrenTypes.add(new Root());
		this.childrenTypes.add(this);
	}
	
	public static int getLevel(Section s) {
		Section root = s.findChildOfType(Root.class);
		if(root == null) return 0;
		return Root.getLevel(root)+1;
	}
	
	
	class SubTreeFinder extends SectionFinder {

		@Override
		public List<SectionFinderResult> lookForSections(String text,
				Section father) {
			
			int level = 0;
			
			KnowWEObjectType fatherType = father.getObjectType();
			if(fatherType instanceof SubTree) {
				level = getLevel(father);
			}
			
			String dashesPrefix = "";
			for(int i=0; i < level; i++) {
				dashesPrefix += "-";
			}
			
			ArrayList<SectionFinderResult> result = new ArrayList<SectionFinderResult>();
			Matcher m = Pattern.compile("^"+dashesPrefix+"[^-]+", Pattern.MULTILINE).matcher(text);
			int lastStart = -1;
			while (m.find()) {
				String finding = m.group();
				if(lastStart > -1) {
					String found = text.substring(lastStart,m.start()+1);
					result.add(new SectionFinderResult(lastStart,m.start()));
				}
				lastStart = m.start();
				
				
			}
			if(lastStart > -1 ) {
				result.add(new SectionFinderResult(lastStart,text.length()));
			}
			return result;
			
			
		}
		
	}

}
