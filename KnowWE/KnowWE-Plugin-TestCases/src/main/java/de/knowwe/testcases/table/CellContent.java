/*
 * Copyright (C) 2010 University Wuerzburg, Computer Science VI
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

import java.util.List;

import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.sectionFinder.AllTextSectionFinder;
import de.knowwe.core.kdom.sectionFinder.SectionFinderResult;
import de.knowwe.kdom.AnonymousType;
import de.knowwe.kdom.constraint.ConstraintSectionFinder;
import de.knowwe.kdom.constraint.SectionFinderConstraint;
import de.knowwe.kdom.table.TableCellContent;
import de.knowwe.kdom.table.TableUtils;
import de.knowwe.testcases.TimeStampType;

/**
 * @author Florian Ziegler
 * @created 10.08.2010
 */
public class CellContent extends TableCellContent {

	/**
	 * 
	 * @author Reinhard Hatko
	 * @created 17.03.2011
	 */
	private final class TableColumnConstraint implements SectionFinderConstraint {

		private final int column;

		/**
		 * Constrains the Sectionfinder to a single column.
		 * 
		 * @param column the column in which the sectionfinder is active
		 *        (0-based)
		 */
		public TableColumnConstraint(int column) {
			this.column = column;
		}

		@Override
		public <T extends Type> boolean satisfiesConstraint(List<SectionFinderResult> found, Section<?> father, Class<T> type, String text) {
			int column = TableUtils.getColumn(father);
			return this.column == column;
		}

		@Override
		public <T extends Type> void filterCorrectResults(List<SectionFinderResult> found, Section<?> father, Class<T> type, String text) {
			// clear all results, if outside the column range
			found.clear();
		}
	}

	public CellContent() {
		setRenderer(new TestcaseTableCellContentRenderer());

		TimeStampType timeStampType = new TimeStampType();
		timeStampType.setSectionFinder(new ConstraintSectionFinder(
				timeStampType.getSectionFinder(),
				new TableColumnConstraint(1)));

		AnonymousType nameType = new AnonymousType("name");
		nameType.setSectionFinder(new ConstraintSectionFinder(new AllTextSectionFinder(),
				new TableColumnConstraint(0)));

		ValueType valueType = new ValueType();
		valueType.setSectionFinder(new ConstraintSectionFinder(valueType.getSectionFinder()));

		childrenTypes.add(nameType);
		childrenTypes.add(timeStampType);
		childrenTypes.add(valueType);

	}

}