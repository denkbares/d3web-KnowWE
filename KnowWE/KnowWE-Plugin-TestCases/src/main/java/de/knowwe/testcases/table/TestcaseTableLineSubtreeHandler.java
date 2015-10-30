package de.knowwe.testcases.table;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import de.d3web.core.knowledge.KnowledgeBase;
import de.d3web.core.knowledge.terminology.Question;
import de.d3web.core.knowledge.terminology.QuestionChoice;
import de.d3web.core.knowledge.terminology.QuestionMC;
import de.d3web.core.manage.KnowledgeBaseUtils;
import de.d3web.core.session.QuestionValue;
import de.d3web.core.session.Value;
import de.d3web.core.session.ValueUtils;
import de.d3web.empiricaltesting.Finding;
import de.d3web.empiricaltesting.RatedTestCase;
import de.d3web.strings.Strings;
import de.d3web.we.knowledgebase.D3webCompiler;
import de.d3web.we.object.QuestionReference;
import de.d3web.we.reviseHandler.D3webHandler;
import de.d3web.we.utils.D3webUtils;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.report.Message;
import de.knowwe.core.utils.KnowWEUtils;
import de.knowwe.kdom.table.TableLine;
import de.knowwe.kdom.table.TableUtils;
import de.knowwe.testcases.NameType;
import de.knowwe.core.kdom.basicType.TimeStampType;

/**
 * @author Reinhard Hatko
 * @created 16.03.2011
 */
public class TestcaseTableLineSubtreeHandler implements D3webHandler<TableLine> {

	public static final String TESTCASE_KEY = "TestCaseKey";

	@Override
	public Collection<Message> create(D3webCompiler article, Section<TableLine> s) {

		KnowledgeBase kb = D3webUtils.getKnowledgeBase(article);

		Section<TimeStampType> timeStamp = Sections.successor(s, TimeStampType.class);

		RatedTestCase ratedTestCase = new RatedTestCase();
		if (timeStamp != null) {
			try {
				long time = TimeStampType.getTimeInMillis(timeStamp);
				ratedTestCase.setTimeStamp(new Date(time));
			}
			catch (NumberFormatException e) {
				// illegal timestamp... just continue, error message will be
				// handled in TimeStampType
			}
		}

		Section<NameType> nameSection = Sections.successor(s, NameType.class);
		if (nameSection != null) {
			String rtcName = nameSection.get().getRTCName(nameSection);
			ratedTestCase.setName(rtcName);
		}

		List<Section<CellValueType>> values = Sections.successors(s, CellValueType.class);
		for (Section<CellValueType> valueSec : values) {
			// if value is unchanged, ignore it
			String valueString = Strings.trimQuotes(valueSec.getText());
			if (valueString.isEmpty()) continue;
			if (valueString.equals("-")) continue;

			Section<?> qRef = TableUtils.getColumnHeader(valueSec, QuestionReference.class);

			if (qRef == null) continue;

			String qName = Strings.trimQuotes(qRef.getText().trim());
			Question question = kb.getManager().searchQuestion(qName);
			if (question == null) continue;

			Value value = KnowledgeBaseUtils.findValue(question, valueString, false);
			if (value != null) {
				Finding existingFinding = null;
				if (question instanceof QuestionMC) {
					List<Finding> findings = ratedTestCase.getFindings();
					for (Finding finding : findings) {
						if (finding.getQuestion() == question) {
							existingFinding = finding;
							break;
						}
					}
					if (existingFinding != null) {
						Value newValue = ValueUtils.createQuestionChoiceValue((QuestionChoice) question, valueString);
						existingFinding.setValue((QuestionValue) newValue);
					}
				}
				if (existingFinding == null) ratedTestCase.add(new Finding(question, (QuestionValue) value));

			}
		}

		KnowWEUtils.storeObject(article, s, TestcaseTableLineSubtreeHandler.TESTCASE_KEY, ratedTestCase);
		return Collections.emptyList();
	}
}