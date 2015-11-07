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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import de.d3web.strings.Strings;
import de.d3web.testcase.model.CheckTemplate;
import de.d3web.testcase.model.DefaultFindingTemplate;
import de.d3web.testcase.model.FindingTemplate;
import de.d3web.we.kdom.condition.CompositeCondition;
import de.d3web.we.kdom.condition.Conjunct;
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
 * Creates all the Finding- and CheckTemplates for the current line (independent of any knowledge bases or
 * D3webCompilers).
 *
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 03.11.15
 */
public class TestCaseTableLineGlobalCompileScript extends DefaultGlobalScript<TableLine> {

	private static final String LAST_TIME_STAMP_KEY = "lastTimeStamp";
	private static final String CHECKS_KEY = "checks";
	private static final String FINDINGS_KEY = "findings";
	private static final String DESCRIPTION_KEY = "description";
	private static final String DATE_KEY = "date";

	@Override
	public void compile(DefaultGlobalCompiler compiler, Section<TableLine> section) throws CompilerMessage {

		Section<Table> tableSection = Sections.ancestor(section, Table.class);
		if (tableSection == null) throw CompilerMessage.error("Parsing exception, no table found");

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

		section.storeObject(DATE_KEY, date);

		Section<NameType> nameSection = $(section).successor(NameType.class).getFirst();
		if (nameSection != null) {
			section.storeObject(DESCRIPTION_KEY, nameSection.get().getName(nameSection));
		}

		// Findings
		List<Section<CellValueType>> values = $(section).successor(CellValueType.class).asList();
		List<FindingTemplate> findingTemplates = new ArrayList<>();
		for (Section<CellValueType> valueSec : values) {

			String valueString = Strings.trimQuotes(valueSec.getText());
			if (valueString.isEmpty()) continue;
			if (valueString.equals("-")) continue;

			Section<?> questionReference = TableUtils.getColumnHeader(valueSec, QuestionReference.class);
			if (questionReference == null) continue;

			String termName = KnowWEUtils.getTermName(questionReference);

			findingTemplates.add(new DefaultFindingTemplate(termName, valueString));
		}

		section.storeObject(FINDINGS_KEY, findingTemplates);

		// Checks
		Section<CompositeCondition> topLevelConditionSection = $(section).successor(CompositeCondition.class)
				.getFirst();
		ArrayList<CheckTemplate> checkTemplates = new ArrayList<>();
		if (topLevelConditionSection == null) return;

		// break down top level CondAnds into individual Checks...
		if (isCondAnd(topLevelConditionSection)) {
			List<Section<CompositeCondition>> andComposits
					= $(topLevelConditionSection).successor(2, CompositeCondition.class)
					.filter(successor -> successor != topLevelConditionSection).asList();
			for (Section<CompositeCondition> compositeSection : andComposits) {
				checkTemplates.add(new KnowWEConditionCheckTemplate(compositeSection));
			}
		}
		else {
			checkTemplates.add(new KnowWEConditionCheckTemplate(topLevelConditionSection));
		}
		section.storeObject(CHECKS_KEY, checkTemplates);

	}

	public static String getDescription(Section<TableLine> tableLine) {
		return (String) tableLine.getObject(DESCRIPTION_KEY);
	}

	public static Date getDate(Section<TableLine> tableLine) {
		return (Date) tableLine.getObject(DATE_KEY);
	}

	public static List<CheckTemplate> getCheckTemplates(Section<TableLine> tableLine) {
		//noinspection unchecked
		List<CheckTemplate> checkTemplates = (List<CheckTemplate>) tableLine.getObject(CHECKS_KEY);
		return checkTemplates == null ? Collections.emptyList() : checkTemplates;
	}

	public static void setFindingTemplates(Section<TableLine> tableLine, List<DefaultFindingTemplate> findingTemplates) {
		tableLine.storeObject(FINDINGS_KEY, findingTemplates);
	}

	public static List<DefaultFindingTemplate> getFindingTemplates(Section<TableLine> tableLine) {
		//noinspection unchecked
		List<DefaultFindingTemplate> findingTemplates = (List<DefaultFindingTemplate>) tableLine.getObject(FINDINGS_KEY);
		return findingTemplates == null ? Collections.emptyList() : findingTemplates;
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