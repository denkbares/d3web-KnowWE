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
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import de.d3web.core.knowledge.KnowledgeBase;
import de.d3web.core.knowledge.terminology.Question;
import de.d3web.core.manage.KnowledgeBaseUtils;
import de.d3web.core.session.QuestionValue;
import de.d3web.empiricaltesting.Finding;
import de.d3web.empiricaltesting.RatedTestCase;
import de.d3web.we.object.QuestionReference;
import de.d3web.we.utils.D3webUtils;
import de.knowwe.core.kdom.InvalidKDOMSchemaModificationOperation;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.kdom.subtreeHandler.SubtreeHandler;
import de.knowwe.core.report.Message;
import de.knowwe.core.report.Messages;
import de.knowwe.core.utils.KnowWEUtils;
import de.knowwe.kdom.table.TableCell;
import de.knowwe.kdom.table.TableLine;
import de.knowwe.testcases.TimeStampType;

/**
 * 
 * @author Sebastian Furth
 * @created 20/10/2010
 */
public class TestcaseTableLine extends TableLine {

	public static final String TESTCASE_KEY = "TESTCASE";

	public TestcaseTableLine() {
		setRenderer(new TestcaseTableLineRenderer());
		try {
			replaceChildType(new Cell(), TableCell.class);
		}
		catch (InvalidKDOMSchemaModificationOperation e) {
			e.printStackTrace();
		}

		addSubtreeHandler(new TestcaseTableLineSubtreeHandler());
	}

	/**
	 * 
	 * @author Reinhard Hatko
	 * @created 16.03.2011
	 */
	private final class TestcaseTableLineSubtreeHandler extends SubtreeHandler<TestcaseTableLine> {

		@Override
		public Collection<Message> create(Article article, Section<TestcaseTableLine> s) {

			KnowledgeBase kb = D3webUtils.getKnowledgeBase(article.getWeb(), article.getTitle());

			Section<TimeStampType> timeStamp = Sections.findSuccessor(s, TimeStampType.class);

			// returns 0 for illegal time stamp
			// we could also return here, but then the Values are not checked
			long time = TimeStampType.getTimeInMillis(timeStamp);

			RatedTestCase testCase = new RatedTestCase();
			testCase.setTimeStamp(new Date(time));

			List<Section<ValueType>> values = new LinkedList<Section<ValueType>>();
			Sections.findSuccessorsOfType(s, ValueType.class, values);

			for (Section<ValueType> valueSec : values) {

				// if value is unchanged, ignore it
				if (Sections.findSuccessor(valueSec, UnchangedType.class) != null) continue;

				Section<? extends HeaderCell> headerCell = TestcaseTable.findHeaderCell(valueSec);

				if (headerCell == null) {
					Messages.storeMessage(article, valueSec, getClass(),
							Messages.noSuchObjectError("No header found for answer '"
									+ valueSec.getText() + "'."));
					continue;
				}

				Section<QuestionReference> qRef = Sections.findSuccessor(headerCell,
						QuestionReference.class);
				String qName = qRef.getText();
				Question question = kb.getManager().searchQuestion(qName);

				if (question == null) {
					continue;
				}

				String valueString = valueSec.getText();
				// TODO unknown value
				QuestionValue value;
				try {
					value = KnowledgeBaseUtils.findValue(question, valueString);

				}// sectionizing finds a choiceValue, if illegal number is
					// entered
				catch (NumberFormatException e) {
					// on sectionizing an invalid AnswerRef was found.
					// replace message...
					Messages.clearMessages(article,
							Sections.findSuccessor(valueSec, CellAnswerRef.class));

					Messages.storeMessage(article, valueSec, getClass(),
							Messages.invalidNumberError(valueString));
					continue;
				}

				if (value != null) {
					Finding finding = new Finding(question, value);
					testCase.add(finding);
				}
				else {
					Messages.storeMessage(article, valueSec, getClass(),
							Messages.noSuchObjectError(valueString));
				}

			}

			KnowWEUtils.storeObject(article, s, TESTCASE_KEY, testCase);

			return Collections.emptyList();
		}

	}

}
