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

package de.d3web.we.kdom.owlextension;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.sectionFinder.SectionFinder;
import de.d3web.we.kdom.sectionFinder.SectionFinderResult;

public class ExtensionSectionFinder extends SectionFinder {

	private static final String REGEXP_extension = "<extension[\\w\\W]*?</extension>";
	private static final String REGEXP_extensionCONTENT = "<extension[\\w\\W]*?>([\\w\\W]*?)</extension>";
	private static Pattern extensionregex = Pattern.compile(REGEXP_extension);
	private static Pattern extensioncontentregex = Pattern
			.compile(REGEXP_extensionCONTENT);
	private Extension father;

	public ExtensionSectionFinder(KnowWEObjectType type) {
		if (type instanceof Extension) {
			father = (Extension) type;
		}

	}

	@Override
	public List<SectionFinderResult> lookForSections(String text, Section father) {
		ArrayList<SectionFinderResult> sectionlist = new ArrayList<SectionFinderResult>();
		int i = 0;
		Matcher m = extensionregex.matcher(text);
		while (m.find(i)) {
			String s = m.group();
			i += m.start() + s.length() - 1;
			int indexOfKopicStart = m.start();
			SectionFinderResult newsection = new SectionFinderResult(indexOfKopicStart, i);
			if (newsection != null) {
				sectionlist.add(newsection);
				Matcher cmatcher = extensioncontentregex.matcher(s);
				if (cmatcher.find() && (father != null)) {
					String erg = cmatcher.group(1).trim();
					this.father.addExtensionSource(
							text.substring(indexOfKopicStart, indexOfKopicStart), erg);
					ExtensionObject eo = new ExtensionObject(erg, father);
					this.father.setExtensionObject(
							text.substring(indexOfKopicStart, indexOfKopicStart), eo);
				}
			}
			if (i >= text.length()) break;
		}
		return sectionlist;
	}
}
