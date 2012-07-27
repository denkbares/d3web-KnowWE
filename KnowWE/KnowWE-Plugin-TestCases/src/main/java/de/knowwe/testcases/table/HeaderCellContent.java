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

import java.util.Collection;
import java.util.List;

import de.d3web.we.object.QuestionReference;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.sectionFinder.SectionFinder;
import de.knowwe.core.kdom.sectionFinder.SectionFinderResult;
import de.knowwe.core.kdom.subtreeHandler.SubtreeHandler;
import de.knowwe.core.report.Message;
import de.knowwe.core.report.Messages;
import de.knowwe.core.utils.Strings;
import de.knowwe.kdom.table.TableCellContent;
import de.knowwe.kdom.table.TableCellContentRenderer;
import de.knowwe.kdom.table.TableUtils;

/**
 * 
 * 
 * @author Reinhard Hatko
 * @created 21.01.2011
 */
public class HeaderCellContent extends TableCellContent {

	public HeaderCellContent() {
		setRenderer(new TableCellContentRenderer(false));

		// TODO: check to use AllTextTrimmedFinder instead
		setSectionFinder(new SectionFinder() {

			@Override
			public List<SectionFinderResult> lookForSections(String text, Section<?> father, Type type) {

				String trim = text.trim();
				int start = text.indexOf(trim);
				if (trim.length() > 0) {
					return SectionFinderResult.createSingleItemList(new SectionFinderResult(
							start,
							start + trim.length()));
				}
				else {
					return null;
				}

			}
		});

		this.addSubtreeHandler(new SubtreeHandler<HeaderCellContent>() {

			@Override
			public Collection<Message> create(Article article, Section<HeaderCellContent> s) {

				int column = TableUtils.getColumn(s);
				String questionName = Strings.trimQuotes(s.getText());
				if ((column == 0 && questionName.equalsIgnoreCase("Name"))
						|| ((column == 0 || column == 1) && questionName.equalsIgnoreCase("Time"))
						|| questionName.equalsIgnoreCase("Checks")) {
					return Messages.noMessage();
				}
				// otherwise it is a QuestionReference
				s.setType(new HeaderQuestionReference(), article);

				return Messages.noMessage();
			}

		});

	}

	private static class HeaderQuestionReference extends QuestionReference {

		private HeaderQuestionReference() {
			setRenderer(new TableCellContentRenderer(false));
		}
	}

}
