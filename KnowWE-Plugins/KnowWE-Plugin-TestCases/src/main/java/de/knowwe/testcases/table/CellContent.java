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

import org.apache.commons.lang.ArrayUtils;

import de.d3web.we.kdom.condition.CompositeCondition;
import de.d3web.we.kdom.rules.RuleType;
import de.knowwe.core.kdom.AbstractType;
import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.basicType.TimeStampType;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.sectionFinder.AllTextFinder;
import de.knowwe.core.kdom.sectionFinder.AllTextFinderTrimmed;
import de.knowwe.core.kdom.sectionFinder.SectionFinderResult;
import de.knowwe.kdom.constraint.ConstraintSectionFinder;
import de.knowwe.kdom.constraint.NoBlankSectionsConstraint;
import de.knowwe.kdom.constraint.SectionFinderConstraint;
import de.knowwe.kdom.table.TableUtils;
import de.knowwe.testcases.NameType;

/**
 * @author Florian Ziegler
 * @created 10.08.2010
 */
public class CellContent extends AbstractType {

	public CellContent() {

		setSectionFinder(new AllTextFinderTrimmed());

		TimeStampType timeStampType = new TimeStampType();
		timeStampType.setSectionFinder(new ConstraintSectionFinder(
				timeStampType.getSectionFinder(),
				new TableNameConstraint("Time", 0, 1)));

		NameType nameType = new NameType();
		nameType.setSectionFinder(new ConstraintSectionFinder(AllTextFinder.getInstance(),
				new TableNameConstraint("Name", 0)));

		CompositeCondition checkType = new CompositeCondition();
		checkType.setAllowedTerminalConditions(RuleType.getTerminalConditions());
		checkType.setSectionFinder(new ConstraintSectionFinder(AllTextFinder.getInstance(),
				new TableNameConstraint("Checks", null),
				NoBlankSectionsConstraint.getInstance()));

		CellValueType valueType = new CellValueType();
		valueType.setSectionFinder(new ConstraintSectionFinder(valueType.getSectionFinder(),
				NoBlankSectionsConstraint.getInstance()));

		this.addChildType(nameType);
		this.addChildType(timeStampType);
		this.addChildType(checkType);
		this.addChildType(valueType);
	}

	public static final class TableNameConstraint implements SectionFinderConstraint {

		private final String name;
		private final int[] columns;

		public TableNameConstraint(String name, int... columns) {
			this.name = name;
			this.columns = columns;
		}

		private <T extends Type> boolean satisfiesConstraint(List<SectionFinderResult> found, Section<?> father, Class<T> type, String text) {
			int column = TableUtils.getColumn(father);
			Section<HeaderCellContent> headerCell = TableUtils.getColumnHeader(father, HeaderCellContent.class);
			if (headerCell == null) {
				return false;
			}
			String headerText = headerCell.getText().trim();
			if (headerText.startsWith("||")) {
				headerText = headerText.substring(2);
			}

			boolean columnOk = columns == null || ArrayUtils.contains(columns, column);
			return headerText.trim().equalsIgnoreCase(name) && columnOk;
		}

		@Override
		public <T extends Type> void filterCorrectResults(List<SectionFinderResult> found, Section<?> father, Class<T> type, String text) {
			if (satisfiesConstraint(found, father, type, text)) return;
			found.clear();
		}

	}

}