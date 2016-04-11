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

import de.d3web.core.knowledge.terminology.Question;
import de.d3web.strings.Identifier;
import de.d3web.strings.Strings;
import de.d3web.we.object.QuestionReference;
import de.d3web.we.reviseHandler.D3webHandler;
import de.knowwe.core.compile.terminology.RenamableTerm;
import de.knowwe.core.compile.terminology.TerminologyManager;
import de.knowwe.core.kdom.AbstractType;
import de.knowwe.core.kdom.sectionFinder.AllTextFinderTrimmed;
import de.knowwe.core.kdom.sectionFinder.SectionFinderResult;
import de.knowwe.core.report.Messages;
import de.knowwe.kdom.table.TableUtils;

/**
 * @author Reinhard Hatko
 * @created 21.01.2011
 */
public class HeaderCellContent extends AbstractType implements RenamableTerm {

	public HeaderCellContent() {
		QuestionReference questionReference = new QuestionReference();
		questionReference.setSectionFinder((text, father, type) -> {
			int column = TableUtils.getColumn(father);
			String cellText = Strings.trimQuotes(father.getText());
			if ((column == 0 && cellText.equalsIgnoreCase("Name"))
					|| ((column == 0 || column == 1) && cellText.equalsIgnoreCase("Time"))
					|| cellText.equalsIgnoreCase("Checks")) {
				return null;
			}
			else {
				return SectionFinderResult.singleItemList(new SectionFinderResult(0, text.length()));
			}
		});
		addChildType(questionReference);

		setSectionFinder(new AllTextFinderTrimmed());

		this.addCompileScript((D3webHandler<HeaderCellContent>) (compiler, section) -> {

			int column = TableUtils.getColumn(section);
			String cellText = Strings.trimQuotes(section.getText());
			if ((column == 0 && cellText.equalsIgnoreCase("Name"))
					|| ((column == 0 || column == 1) && cellText.equalsIgnoreCase("Time"))
					|| cellText.equalsIgnoreCase("Checks")) {
				return Messages.noMessage();
			}

			TerminologyManager tHandler = compiler.getTerminologyManager();
			Identifier termIdentifier = new Identifier(cellText);
			tHandler.registerTermReference(compiler, section, Question.class, termIdentifier);
			if (!tHandler.isDefinedTerm(termIdentifier)) {
				return Messages.asList(Messages.noSuchObjectError(Question.class.getSimpleName(), cellText));
			}
			return Messages.noMessage();
		});

	}

}
