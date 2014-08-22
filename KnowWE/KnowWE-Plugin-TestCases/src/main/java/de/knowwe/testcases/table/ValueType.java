/*
 * Copyright (C) 2011 University Wuerzburg, Computer Science VI
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
package de.knowwe.testcases.table;

import java.util.LinkedList;
import java.util.List;

import de.knowwe.core.kdom.AbstractType;
import de.knowwe.core.kdom.basicType.EmptyType;
import de.knowwe.core.kdom.basicType.KeywordType;
import de.knowwe.core.kdom.basicType.Number;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.kdom.rendering.DefaultTextRenderer;
import de.knowwe.core.kdom.sectionFinder.AllTextFinderTrimmed;
import de.knowwe.core.kdom.sectionFinder.SectionFinderResult;
import de.knowwe.kdom.constraint.ConstraintSectionFinder;
import de.knowwe.kdom.constraint.SingleChildConstraint;
import de.knowwe.kdom.table.TableLine;

/**
 * 
 * @author Reinhard Hatko
 * @created 21.01.2011
 */
public class ValueType extends AbstractType {

	public ValueType() {
		setSectionFinder(new AllTextFinderTrimmed());
		addChildType(new EmptyType());
		addChildType(new KeywordType("UNKNOWN"));
		addChildType(new KeywordType("-?-"));
		Number number = new Number();
		number.setRenderer(DefaultTextRenderer.getInstance());
		number.setSectionFinder(new ConstraintSectionFinder(number.getSectionFinder(),
				SingleChildConstraint.getInstance()));
		addChildType(number);
		CellAnswerRef aRef = new CellAnswerRef();
		aRef.setRenderer(DefaultTextRenderer.getInstance());
		addChildType(aRef);

		aRef.setSectionFinder((text, father, type) -> {
			Section<TestcaseTable> table = Sections.ancestor(father, TestcaseTable.class);
			List<Section<TableLine>> lines = new LinkedList<>();
			Sections.successors(table, TableLine.class, lines);
			if (lines.size() > 1) {
				if (text.trim().length() > 0) {
					return SectionFinderResult.singleItemList(new SectionFinderResult(0,
							text.length()));
				}// no text to match
				else {
					return null;
				}
			}// in the first line of the table, there are no values
			else {
				return null;
			}

		});
	}
}
