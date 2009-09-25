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

package de.d3web.we.kdom.Annotation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.d3web.we.kdom.DefaultAbstractKnowWEObjectType;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.sectionFinder.SectionFinder;
import de.d3web.we.kdom.sectionFinder.SectionFinderResult;

public class FindingComparator extends DefaultAbstractKnowWEObjectType {
	private HashMap<String, String> operatorstore;

	public FindingComparator() {
		operatorstore = new HashMap<String, String>();
	}

	public static final String[] operators = { "<=", ">=", "=", "<", ">" };


//	@Override
//	public KnowWEDomRenderer getRenderer() {
//		return new XCLComparatorEditorRenderer();
//	}

	public class AnnotationKnowledgeSliceObjectComparatorSectionFinder extends
			SectionFinder {

		@Override
		public List<SectionFinderResult> lookForSections(String text, Section father) {
			int index = -1;
			String foundOperator = "";
			for (String operator : operators) {
				index = text.lastIndexOf(operator);
				if (index != -1) {
					foundOperator = operator;
					break;
				}
			}

			List<SectionFinderResult> result = new ArrayList<SectionFinderResult>();
			if (index != -1) {
				operatorstore.put(
						text.substring(index, index + foundOperator.length()),
						foundOperator);
				result.add(new SectionFinderResult(
						index, index + foundOperator.length()));

			}
			return result;
		}
	}

	public String getComparator(Section section) {
		return section.getOriginalText().trim();
	}

	@Override
	protected void init() {
		this.sectionFinder = 
			new AnnotationKnowledgeSliceObjectComparatorSectionFinder();		
	}

}
