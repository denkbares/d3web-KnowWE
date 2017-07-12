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

package de.knowwe.kdom.xml;

import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.knowwe.core.kdom.AbstractType;
import de.knowwe.core.kdom.rendering.NothingRenderer;
import de.knowwe.core.kdom.sectionFinder.SectionFinderResult;

public class XMLHead extends AbstractType {

	private static String patternString = "^" + XMLSectionFinder.getXMLTagPattern();
	private static Pattern pattern = Pattern.compile(patternString);

	public XMLHead() {
		this.setRenderer(NothingRenderer.getInstance());
		setSectionFinder((text, father, type) -> {
			Matcher matcher = pattern.matcher(text);
			if (matcher.find()) {
				if (matcher.group(1) == null && matcher.group(4) == null) {
					return Collections.singletonList(new SectionFinderResult(matcher.start(), matcher.end()));
				}
			}
			return null;
		});
	}

}
