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

package de.d3web.we.kdom.semanticAnnotation;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.d3web.we.kdom.DefaultAbstractKnowWEObjectType;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.sectionFinder.SectionFinder;
import de.d3web.we.kdom.sectionFinder.SectionFinderResult;

public class SemanticAnnotation extends DefaultAbstractKnowWEObjectType {

	private static String ANNOTATIONBEGIN = "\\[";
	private static String ANNOTATIONEND = "\\]";

	public SemanticAnnotation() {

	}

	@Override
	public SectionFinder getSectioner() {
		return new AnnotationSectionFinder();
	}

	public static class AnnotationSectionFinder extends SectionFinder {

		private String PATTERN = ANNOTATIONBEGIN + "[\\w\\W]*?" + ANNOTATIONEND;

		@Override
		public List<SectionFinderResult> lookForSections(String text,
				Section father) {
			ArrayList<SectionFinderResult> result = new ArrayList<SectionFinderResult>();
			Pattern p = Pattern.compile(PATTERN);
			Matcher m = p.matcher(text);
			while (m.find()) {
				String found = m.group();
				if (found.contains("::"))
					result.add(new SectionFinderResult(m.start(), m.end()));
			}
			return result;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.d3web.we.dom.AbstractKnowWEObjectType#init()
	 */
	@Override
	protected void init() {
		this.setCustomRenderer(new StandardAnnotationRenderer());
		this.childrenTypes.add(new SemanticAnnotationStartSymbol("["));
		this.childrenTypes.add(new SemanticAnnotationEndSymbol("]"));
		this.childrenTypes.add(new SemanticAnnotationContent());
	}

}
