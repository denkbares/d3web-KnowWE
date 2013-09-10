package de.knowwe.testcases.table;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import de.d3web.core.knowledge.KnowledgeBase;
import de.d3web.core.knowledge.terminology.Question;
import de.d3web.core.manage.KnowledgeBaseUtils;
import de.d3web.core.session.QuestionValue;
import de.d3web.core.session.values.Unknown;
import de.d3web.empiricaltesting.Finding;
import de.d3web.empiricaltesting.RatedTestCase;
import de.d3web.strings.Strings;
import de.d3web.we.object.QuestionReference;
import de.d3web.we.utils.D3webUtils;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.kdom.subtreeHandler.SubtreeHandler;
import de.knowwe.core.report.Message;
import de.knowwe.core.report.Messages;
import de.knowwe.core.utils.KnowWEUtils;
import de.knowwe.testcases.NameType;
import de.knowwe.testcases.TimeStampType;

/**
 * 
 * @author Reinhard Hatko
 * @created 16.03.2011
 */
final class TestcaseTableLineSubtreeHandler extends SubtreeHandler<TestcaseTableLine> {

	@Override
	public Collection<Message> create(Article article, Section<TestcaseTableLine> s) {

		KnowledgeBase kb = D3webUtils.getKnowledgeBase(article.getWeb(), article.getTitle());

		Section<TimeStampType> timeStamp = Sections.findSuccessor(s, TimeStampType.class);

		RatedTestCase testCase = new RatedTestCase();
		if (timeStamp != null) {
			// returns 0 for illegal time stamp
			// we could also return here, but then the Values are not
			// checked
			long time = TimeStampType.getTimeInMillis(timeStamp);
			testCase.setTimeStamp(new Date(time));
		}

		Section<NameType> nameSection = Sections.findSuccessor(s, NameType.class);
		if (nameSection != null) {
			String rtcName = nameSection.get().getRTCName(nameSection);
			testCase.setName(rtcName);
		}

		List<Section<ValueType>> values = Sections.findSuccessorsOfType(s, ValueType.class);
		for (Section<ValueType> valueSec : values) {
			// if value is unchanged, ignore it
			String valueString = Strings.trimQuotes(valueSec.getText());
			if (valueString.isEmpty()) continue;
			if (valueString.equals("-")) continue;

			Section<? extends HeaderCell> headerCell = TestcaseTable.findHeaderCell(valueSec);

			if (headerCell == null) {
				Messages.storeMessage(article, valueSec, getClass(),
						Messages.noSuchObjectError("No header found for answer '"
								+ valueSec.getText() + "'."));
				continue;
			}

			Section<QuestionReference> qRef =
					Sections.findSuccessor(headerCell, QuestionReference.class);
			if (qRef == null) continue;

			String qName = Strings.trimQuotes(qRef.getText().trim());
			Question question = kb.getManager().searchQuestion(qName);
			if (question == null) continue;

			try {
				QuestionValue value;
				if (valueString.equals("-?-") || valueString.equalsIgnoreCase("UNKNOWN")) {
					value = Unknown.getInstance();
				}
				else {
					value = KnowledgeBaseUtils.findValue(question, valueString);
				}
				if (value != null) {
					Finding finding = new Finding(question, value);
					testCase.add(finding);
				}
				else {
					// sectionizing finds a choiceValue
					Messages.storeMessage(article, valueSec, getClass(),
							Messages.noSuchObjectError(valueString));
				}
			}
			catch (NumberFormatException e) {
				// sectionizing find an illegal number
				// replace message...
				Messages.clearMessages(article,
						Sections.findSuccessor(valueSec, CellAnswerRef.class));
				Messages.storeMessage(article, valueSec, getClass(),
						Messages.invalidNumberError(valueString));
			}
		}

		KnowWEUtils.storeObject(article, s, TestcaseTableLine.TESTCASE_KEY, testCase);
		return Collections.emptyList();
	}
}