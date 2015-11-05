/*
 * Copyright (C) 2015 denkbares GmbH, Germany
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

import java.util.Date;
import java.util.List;

import de.d3web.core.knowledge.KnowledgeBase;
import de.d3web.strings.Strings;
import de.d3web.testcase.model.DefaultFindingTemplate;
import de.d3web.testcase.model.DefaultTestCase;
import de.d3web.we.kdom.condition.CompositeCondition;
import de.d3web.we.kdom.condition.Conjunct;
import de.d3web.we.knowledgebase.D3webCompiler;
import de.d3web.we.object.QuestionReference;
import de.knowwe.core.compile.DefaultGlobalCompiler;
import de.knowwe.core.compile.DefaultGlobalCompiler.DefaultGlobalScript;
import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.basicType.PlainText;
import de.knowwe.core.kdom.basicType.TimeStampType;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.report.CompilerMessage;
import de.knowwe.core.utils.KnowWEUtils;
import de.knowwe.kdom.table.Table;
import de.knowwe.kdom.table.TableLine;
import de.knowwe.kdom.table.TableUtils;
import de.knowwe.testcases.NameType;
import de.knowwe.testcases.download.KnowWEConditionCheckTemplate;

import static de.knowwe.core.kdom.parsing.Sections.$;

/**
 * This script does all the stuff of the Finding and Check creation, that is independent of the actual {@link
 * D3webCompiler}s and {@link KnowledgeBase}s.
 *
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 03.11.15
 */
public class TestCaseTableLineGlobalCompileScript extends DefaultGlobalScript<TableLine> {

	private static final String LAST_TIME_STAMP_KEY = "lastTimeStamp";

	@Override
	public void compile(DefaultGlobalCompiler compiler, Section<TableLine> section) throws CompilerMessage {

		Section<Table> tableSection = Sections.ancestor(section, Table.class);
		if (tableSection == null) throw CompilerMessage.error("Parsing exception, no table found");

		DefaultTestCase testCase = TestCaseTableGlobalCompileScript.getTestCase(tableSection);

		Date date;

		Section<TimeStampType> timeStampSection = $(section).successor(TimeStampType.class).getFirst();
		long timeStamp;
		if (timeStampSection == null) {
			Object lastTimeStampObject = tableSection.getObject(LAST_TIME_STAMP_KEY);
			if (lastTimeStampObject == null) {
				timeStamp = 0;
			}
			else {
				timeStamp = ((long) lastTimeStampObject) + 1;
			}
			tableSection.storeObject(LAST_TIME_STAMP_KEY, timeStamp);
		}
		else {
			try {
				timeStamp = TimeStampType.getTimeInMillis(timeStampSection);
			}
			catch (NumberFormatException ignore) {
				// illegal timestamp...  error message will be
				// handled in TimeStampType
				return;
			}
		}
		date = new Date(timeStamp);

		// make sure the date/entry appears in chronology, even if it is empty otherwise
		testCase.addFinding(date);

		Section<NameType> nameSection = $(section).successor(NameType.class).getFirst();
		if (nameSection != null) {
			testCase.addDescription(date, nameSection.get().getName(nameSection));
		}

		// Findings
		List<Section<CellValueType>> values = $(section).successor(CellValueType.class).asList();
		for (Section<CellValueType> valueSec : values) {

			String valueString = Strings.trimQuotes(valueSec.getText());
			if (valueString.isEmpty()) continue;
			if (valueString.equals("-")) continue;

			Section<?> questionReference = TableUtils.getColumnHeader(valueSec, QuestionReference.class);
			if (questionReference == null) continue;

			String termName = KnowWEUtils.getTermName(questionReference);

			testCase.addFinding(date, new DefaultFindingTemplate(termName, valueString));

		}

		// Checks
		Section<CompositeCondition> topLevelConditionSection = $(section).successor(CompositeCondition.class)
				.getFirst();

		if (topLevelConditionSection == null) return;

		// break down top level CondAnds into individual Checks...
		if (isCondAnd(topLevelConditionSection)) {
			List<Section<CompositeCondition>> andComposits
					= $(topLevelConditionSection).successor(2, CompositeCondition.class)
					.filter(successor -> successor != topLevelConditionSection).asList();
			for (Section<CompositeCondition> compositeSection : andComposits) {
				testCase.addCheck(date, new KnowWEConditionCheckTemplate(compositeSection));
			}
		}
		else {
			testCase.addCheck(date, new KnowWEConditionCheckTemplate(topLevelConditionSection));
		}

	}

	private boolean isCondAnd(Section<CompositeCondition> conditionSection) {
		for (Section<? extends Type> child : conditionSection.getChildren()) {
			if (!(child.get() instanceof Conjunct
					|| (child.get() instanceof PlainText
					&& Strings.trim(child.getText()).toLowerCase().equals("and")))) {
				return false;
			}
		}
		return true;
	}

}