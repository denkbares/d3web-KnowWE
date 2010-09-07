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

package de.d3web.we.testcase;

import java.util.ArrayList;
import java.util.List;

import de.d3web.core.knowledge.terminology.Choice;
import de.d3web.core.knowledge.terminology.Question;
import de.d3web.core.knowledge.terminology.QuestionChoice;
import de.d3web.core.knowledge.terminology.QuestionYN;
import de.d3web.core.knowledge.terminology.Solution;
import de.d3web.we.core.knowledgeService.D3webKnowledgeService;
import de.d3web.we.d3webModule.D3webModule;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.basic.PlainText;
import de.d3web.we.kdom.table.Table;
import de.d3web.we.kdom.table.TableCell;
import de.d3web.we.kdom.table.TableCellContent;
import de.d3web.we.kdom.table.TableHeaderLine;

/**
 * @author Florian Ziegler
 */
public class TestcaseUtils {

	/**
	 * @param s the current Section (Cell)
	 * @return the alternatives for the current Cell
	 */
	public static String[] getKnowledge(Section<?> s) {

		D3webKnowledgeService knowledgeService = D3webModule.getAD3webKnowledgeServiceInTopic(
				s.getWeb(), s.getTitle());
		List<Question> questions = knowledgeService.getBase().getQuestions();
		List<Solution> solutions = knowledgeService.getBase().getSolutions();

		if (s.getObjectType() instanceof TestcaseTableColHeaderCellContent) {
			return null;
		}

		if (s.getObjectType() instanceof TableCellContent) {
			if (s.getFather().getFather().getObjectType() instanceof TableHeaderLine) {
				return getHeaderAlternatives(s, questions, solutions);
			}
			else {
				return getSimpleTableCellAlternatives(s, questions, solutions);
			}
		}

		return null;
	}

	/**
	 * returns possible questions for the LineHeader
	 * 
	 * @param s the current Section (Cell)
	 * @param questions all questions of the knowledgeBase
	 * @return question alternatives
	 */
	public static String[] getHeaderAlternatives(Section<?> s, List<Question> questions, List<Solution> solutions) {

		List<String> temp = new ArrayList<String>();

		for (Question q : questions) {
			temp.add("" + q);
		}

		temp.add("[:;:]");
		for (Solution sol : solutions) {
			if (!("" + sol).equals("P000")) {
				temp.add("" + sol);
			}
		}

		return temp.toArray(new String[temp.size()]);
	}

	/**
	 * returns possible answer for a Cell regarding its LineHeader
	 * 
	 * @param s the current Section (Cell)
	 * @param questions all questions of the knowledgeBase
	 * @return answer alternatives
	 */
	public static String[] getSimpleTableCellAlternatives(Section<?> s,
			List<Question> questions, List<Solution> solutions) {

		if (questions == null) {
			return null;
		}

		List<String> temp = new ArrayList<String>();

		Section<PlainText> columnHeader = getMatchingColumnHeader(s);
		if (columnHeader == null) {
			return null;
		}

		String text = columnHeader.getOriginalText().trim();
		// find the matching question for the LineHeader
		// and add its answers
		for (Question q : questions) {
			if (q.getName().equals(text)) {
				if (q instanceof QuestionYN) {
					return new String[] {
							"Yes", "No", "Unknown" };
				}
				else if (q instanceof QuestionChoice) {

					for (Choice c : ((QuestionChoice) q)
							.getAllAlternatives()) {
						temp.add(c.getName());
					}
					temp.add("Unknown");
				}
				else {
					return null;
				}
			}
		}

		if (temp.isEmpty()) {
			for (Solution sol : solutions) {
				if (sol.getName().equals(text)) {
					return new String[] {
							"established", "suggested", "excluded" };
				}
			}
		}
		return temp.toArray(new String[temp.size()]);
	}

	/**
	 * returns the TableHeaderCell's PlainText section which is on top of the
	 * given Section
	 * 
	 * @created 26.06.2010
	 * @param s, the given section
	 * @return PlainText section
	 */
	@SuppressWarnings("unchecked")
	private static Section<PlainText> getMatchingColumnHeader(Section<?> s) {
		String id = s.getID();
		String cellNumber = id.substring(id.indexOf("/TableLine"));
		cellNumber = cellNumber.substring(cellNumber.indexOf("/TableCell") + 10);
		int number = Integer.valueOf(cellNumber.substring(0, cellNumber.indexOf("/")));

		Section<? extends Table> table = (Section<? extends Table>) s.getFather().getFather().getFather();
		if (table == null) {
			return null;
		}

		Section<TableHeaderLine> tableHeaderLine = table.findChildOfType(
				TableHeaderLine.class);
		if (tableHeaderLine == null || tableHeaderLine.getChildren() == null
				|| tableHeaderLine.getChildren().size() < 1) {
			return null;
		}

		Section<TableCellContent> colHeader = ((Section<TableCell>) tableHeaderLine.getChildren().get(
				number - 1)).findChildOfType(
				TableCellContent.class);
		if (colHeader == null) {
			return null;
		}

		Section<PlainText> headerText = (Section<PlainText>) colHeader.getChildren().get(0);
		if (headerText == null) {
			return null;
		}
		return headerText;
	}
}
