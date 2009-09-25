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

package copies;
import java.util.ArrayList;
import java.util.List;

import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.sectionFinder.SectionFinder;
import de.d3web.we.kdom.sectionFinder.SectionFinderResult;

/**
 * Copied mainly from the Original RegexSectioner.
 * 
 * @author Johannes Dienst
 *
 */
public class TestStringSectionFinder extends SectionFinder {
	
	private String string;
	private boolean last = false;
	
	public TestStringSectionFinder(String s) {
		this.string = s;
	}
	
	public TestStringSectionFinder(String s, boolean last) {
		this.string = s;
		this.last = last;
	}
	
	@Override
	public List<SectionFinderResult> lookForSections(String text, Section father) {
		int index = text.indexOf(string); 
		if(last) index = text.lastIndexOf(string);
		
		if(index == -1) return null;
		List<SectionFinderResult> result = new ArrayList<SectionFinderResult>();
		//return result;
		result.add(new SectionFinderResult(index, index + string.length()));		
		return result;
	}

}

