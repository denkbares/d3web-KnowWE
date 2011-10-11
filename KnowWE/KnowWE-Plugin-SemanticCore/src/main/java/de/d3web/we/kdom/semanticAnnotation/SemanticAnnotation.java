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

package de.d3web.we.kdom.semanticAnnotation;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.knowwe.core.kdom.AbstractType;
import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.sectionFinder.SectionFinder;
import de.knowwe.core.kdom.sectionFinder.SectionFinderResult;

public class SemanticAnnotation extends AbstractType {

	private static String ANNOTATIONBEGIN = "\\[";
	private static String ANNOTATIONEND = "\\]";

	public SemanticAnnotation() {
		super(new AnnotationSectionFinder());
	}

	public static class AnnotationSectionFinder implements SectionFinder {

		private final String PATTERN = ANNOTATIONBEGIN + "[\\w\\W]*?" + ANNOTATIONEND;

		@Override
		public List<SectionFinderResult> lookForSections(String text,
				Section<?> father, Type type) {
			ArrayList<SectionFinderResult> result = new ArrayList<SectionFinderResult>();
			Pattern p = Pattern.compile(PATTERN);
			Matcher m = p.matcher(text);
			while (m.find()) {
				String found = m.group();
				if (found.contains("::")) result.add(new SectionFinderResult(m.start(), m.end()));
			}
			return result;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.d3web.we.dom.AbstractType#init()
	 */
	@Override
	protected void init() {
		this.setCustomRenderer(new StandardAnnotationRenderer());
		this.childrenTypes.add(new SemanticAnnotationStartSymbol("["));
		this.childrenTypes.add(new SemanticAnnotationEndSymbol("]"));
		this.childrenTypes.add(new SemanticAnnotationContent());
	}

}
