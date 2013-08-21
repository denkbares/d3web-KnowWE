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

import de.knowwe.core.kdom.InvalidKDOMSchemaModificationOperation;
import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.sectionFinder.RegexSectionFinder;
import de.knowwe.core.kdom.sectionFinder.SectionFinderResult;
import de.knowwe.core.report.MessageRenderer;
import de.knowwe.kdom.constraint.ConstraintSectionFinder;
import de.knowwe.kdom.constraint.SectionFinderConstraint;
import de.knowwe.kdom.table.TableCell;
import de.knowwe.kdom.table.TableLine;

/**
 * The header line of the testcase table.
 * 
 * @author Reinhard Hatko
 * @created 21.01.2011
 */
public class HeaderLine extends TableLine {

	public HeaderLine() {
		try {
			replaceChildType(new HeaderCell(), TableCell.class);
		}
		catch (InvalidKDOMSchemaModificationOperation e) {
			e.printStackTrace();
		}

		setSectionFinder(new ConstraintSectionFinder(new RegexSectionFinder(TableLine.LINEPATTERN),
				new SectionFinderConstraint() {

					@Override
					public <T extends Type> boolean satisfiesConstraint(List<SectionFinderResult> found, Section<?> father, Class<T> type, String text) {

						// header line
						return found.size() == 1;
					}

					@Override
					public <T extends Type> void filterCorrectResults(List<SectionFinderResult> found, Section<?> father, Class<T> type, String text) {
						if (found.size() < 2) {
							return;
						}
						SectionFinderResult result = found.get(0);
						found.clear();
						found.add(result);
					}

				}));
	}

	// deactivate message renderer to avoid <span>...</span> rendered
	// in between table lines
	@Override
	public MessageRenderer getNoticeRenderer() {
		return null;
	}

	// deactivate message renderer to avoid <span>...</span> rendered
	// in between table lines
	@Override
	public MessageRenderer getWarningRenderer() {
		return null;
	}

	// deactivate message renderer to avoid <span>...</span> rendered
	// in between table lines
	@Override
	public MessageRenderer getErrorRenderer() {
		return null;
	}

}
